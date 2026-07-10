package com.rx.admin.modules.audio.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.config.AppConfig;
import com.rx.admin.common.constant.TranscriptionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.audio.convert.AudioConvert;
import com.rx.admin.modules.audio.dto.AudioTranscriptionQueryDTO;
import com.rx.admin.modules.audio.entity.AudioSegment;
import com.rx.admin.modules.audio.entity.AudioTranscription;
import com.rx.admin.modules.audio.mapper.AudioSegmentMapper;
import com.rx.admin.modules.audio.mapper.AudioTranscriptionMapper;
import com.rx.admin.modules.audio.vo.AudioTranscriptionVO;
import com.rx.admin.modules.transcription.WhisperEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AudioTranscriptionService implements IAudioTranscriptionService {

    private final AudioTranscriptionMapper transcriptionMapper;
    private final AudioSegmentMapper segmentMapper;
    private final AudioConvert audioConvert;
    private final AppConfig appConfig;
    private final WhisperEngine whisperEngine;

    @Override
    public Path getTempDir() {
        return whisperEngine.resolveTempDir(TranscriptionConstants.Audio.TEMP_PREFIX);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AudioTranscriptionVO transcribe(File audioFile, String originalName, String language) {
        String effectiveLanguage = resolveLanguage(language);
        String modelName = resolveModelName(null);

        AudioTranscription transcription = new AudioTranscription();
        transcription.setFileName(originalName != null ? originalName : audioFile.getName());
        transcription.setLanguage(effectiveLanguage);
        transcription.setModelName(modelName);
        transcription.setStatus(TranscriptionConstants.Status.SUCCESS);

        try {
            WhisperEngine.WhisperResult result = whisperEngine.transcribeAudio(audioFile, effectiveLanguage, modelName);

            String fullText = result.getFullText();
            if ("zh".equalsIgnoreCase(effectiveLanguage) && fullText != null) {
                fullText = whisperEngine.toSimplified(fullText);
            }
            transcription.setFullText(fullText);
            transcription.setModelName(result.isUsedFallback() ? modelName + "-fallback" : modelName);
            transcriptionMapper.insert(transcription);

            List<AudioSegment> segments = buildSegments(result, transcription.getId(), effectiveLanguage);
            for (AudioSegment segment : segments) {
                segmentMapper.insert(segment);
            }

            return buildVO(transcription, segments);

        } catch (Exception e) {
            log.error("音频转写失败", e);
            transcription.setErrorMessage(e.getMessage());
            transcription.setStatus(TranscriptionConstants.Status.FAIL);
            transcriptionMapper.insert(transcription);
            throw new RuntimeException("音频转写失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AudioTranscriptionVO uploadOnly(File audioFile, String originalName, String language) {
        String effectiveLanguage = resolveLanguage(language);
        Path storageDir = getStorageDir();
        ensureDirExists(storageDir);

        String safeFileName = System.currentTimeMillis() + "_" + whisperEngine.sanitizeFileName(originalName, "audio");
        Path storedPath = storageDir.resolve(safeFileName);
        try {
            Files.copy(audioFile.toPath(), storedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("保存音频文件失败: " + e.getMessage(), e);
        }

        AudioTranscription transcription = new AudioTranscription();
        transcription.setFileName(originalName != null ? originalName : audioFile.getName());
        transcription.setFilePath(storedPath.toString());
        transcription.setLanguage(effectiveLanguage);
        transcriptionMapper.insert(transcription);

        return audioConvert.toVO(transcription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AudioTranscriptionVO transcribeById(Long id, String model) {
        AudioTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription == null) {
            throw new RuntimeException("转写记录不存在");
        }

        String filePath = transcription.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new RuntimeException("音频文件路径不存在");
        }

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            throw new RuntimeException("音频文件不存在: " + filePath);
        }

        String modelName = resolveModelName(model);

        try {
            WhisperEngine.WhisperResult result = whisperEngine.transcribeAudio(audioFile, transcription.getLanguage(), modelName);

            String fullText = result.getFullText();
            if ("zh".equalsIgnoreCase(transcription.getLanguage()) && fullText != null) {
                fullText = whisperEngine.toSimplified(fullText);
            }
            double duration = result.getSegments().stream()
                .mapToDouble(s -> s.getEndTime())
                .max().orElse(whisperEngine.getMediaDuration(audioFile));
            transcription.setDuration(duration);
            transcription.setFullText(fullText);
            transcription.setModelName(result.isUsedFallback() ? modelName + "-fallback" : modelName);
            transcription.setStatus(TranscriptionConstants.Status.SUCCESS);
            transcriptionMapper.updateById(transcription);

            segmentMapper.delete(new LambdaQueryWrapper<AudioSegment>()
                .eq(AudioSegment::getTranscriptionId, id));

            List<AudioSegment> segments = buildSegments(result, id, transcription.getLanguage());
            for (AudioSegment segment : segments) {
                segmentMapper.insert(segment);
            }

            return buildVO(transcription, segments);

        } catch (Exception e) {
            log.error("音频转写失败", e);
            transcription.setErrorMessage(e.getMessage());
            transcription.setStatus(TranscriptionConstants.Status.FAIL);
            transcriptionMapper.updateById(transcription);
            return null;
        }
    }

    @Override
    public AudioTranscriptionVO getById(Long id) {
        AudioTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription == null) {
            return null;
        }
        List<AudioSegment> segments = segmentMapper.selectByTranscriptionId(id);
        return buildVO(transcription, segments);
    }

    @Override
    public String generateSrtContent(Long id) {
        AudioTranscriptionVO vo = getById(id);
        if (vo == null || vo.getSegments() == null || vo.getSegments().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (var segment : vo.getSegments()) {
            sb.append(index++).append("\n");
            sb.append(formatTime(segment.getStartTime())).append(" --> ").append(formatTime(segment.getEndTime())).append("\n");
            sb.append(segment.getText()).append("\n\n");
        }

        return sb.toString();
    }

    @Override
    public String generateAssContent(Long id) {
        AudioTranscriptionVO vo = getById(id);
        if (vo == null || vo.getSegments() == null || vo.getSegments().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[Script Info]\n");
        sb.append("Title: Generated Subtitle\n");
        sb.append("ScriptType: v4.00+\n");
        sb.append("Collisions: Normal\n");
        sb.append("PlayResX: 1920\n");
        sb.append("PlayResY: 1080\n");
        sb.append("\n");
        sb.append("[Styles]\n");
        sb.append("Format: Name, Fontname, Fontsize, PrimaryColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n");
        sb.append("Style: Default,Microsoft YaHei,40,&HFFFFFF,&H000000FF,0,0,0,100,100,0,0,1,2,1,2,10,10,50,1\n");
        sb.append("\n");
        sb.append("[Events]\n");
        sb.append("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n");

        for (var segment : vo.getSegments()) {
            sb.append(String.format("Dialogue: 0,%s,%s,Default,,10,10,50,,%s\n",
                formatAssTime(segment.getStartTime()),
                formatAssTime(segment.getEndTime()),
                segment.getText()));
        }

        return sb.toString();
    }

    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds % 1) * 1000);
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }

    private String formatAssTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int centis = (int) ((seconds % 1) * 100);
        return String.format("%d:%02d:%02d.%02d", hours, minutes, secs, centis);
    }

    private LambdaQueryWrapper<AudioTranscription> buildQueryWrapper(AudioTranscriptionQueryDTO query) {
        LambdaQueryWrapper<AudioTranscription> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(AudioTranscription::getFileName, query.getKeyword())
                   .or()
                   .like(AudioTranscription::getFullText, query.getKeyword());
        }
        if (query.getLanguage() != null && !query.getLanguage().isEmpty()) {
            wrapper.eq(AudioTranscription::getLanguage, query.getLanguage());
        }
        if (query.getStatus() != null) {
            wrapper.eq(AudioTranscription::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(AudioTranscription::getCreateTime);
        return wrapper;
    }

    @Override
    public PageResult<AudioTranscription> pageQuery(AudioTranscriptionQueryDTO query) {
        LambdaQueryWrapper<AudioTranscription> wrapper = buildQueryWrapper(query);
        IPage<AudioTranscription> page = new Page<>(query.getPage(), query.getSize());
        page = transcriptionMapper.selectPage(page, wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<AudioTranscriptionVO> pageQueryVO(AudioTranscriptionQueryDTO query) {
        transcriptionMapper.backfillNullDuration();

        LambdaQueryWrapper<AudioTranscription> wrapper = buildQueryWrapper(query);

        IPage<AudioTranscription> page = new Page<>(query.getPage(), query.getSize());
        page = transcriptionMapper.selectPage(page, wrapper);

        return PageResult.of(page).map(audioConvert::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        segmentMapper.delete(new LambdaQueryWrapper<AudioSegment>()
            .eq(AudioSegment::getTranscriptionId, id));
        transcriptionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        segmentMapper.delete(new LambdaQueryWrapper<AudioSegment>()
            .in(AudioSegment::getTranscriptionId, ids));
        transcriptionMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AudioTranscriptionVO updateFileName(Long id, String fileName) {
        AudioTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription == null) {
            throw new RuntimeException("转写记录不存在");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        transcription.setFileName(fileName.trim());
        transcriptionMapper.updateById(transcription);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AudioTranscriptionVO convertScript(Long id, String target) {
        AudioTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription == null) {
            throw new RuntimeException("转写记录不存在");
        }
        if (target == null || (!"simplified".equals(target) && !"traditional".equals(target))) {
            throw new RuntimeException("目标类型必须是 simplified 或 traditional");
        }

        String fullText = transcription.getFullText();
        if (fullText != null && !fullText.isEmpty()) {
            fullText = "simplified".equals(target)
                ? whisperEngine.toSimplified(fullText)
                : whisperEngine.toTraditional(fullText);
            transcription.setFullText(fullText);
        }

        List<AudioSegment> segments = segmentMapper.selectList(
            new LambdaQueryWrapper<AudioSegment>().eq(AudioSegment::getTranscriptionId, id));
        for (AudioSegment segment : segments) {
            String text = segment.getText();
            if (text != null && !text.isEmpty()) {
                segment.setText("simplified".equals(target)
                    ? whisperEngine.toSimplified(text)
                    : whisperEngine.toTraditional(text));
                segmentMapper.updateById(segment);
            }
        }

        transcriptionMapper.updateById(transcription);
        return getById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        AudioTranscription t = new AudioTranscription();
        t.setId(id);
        t.setStatus(status);
        transcriptionMapper.updateById(t);
    }

    private Path getStorageDir() {
        return whisperEngine.resolveStorageDir(TranscriptionConstants.Audio.TEMP_PREFIX);
    }

    private String resolveLanguage(String language) {
        return (language != null && !language.isEmpty())
            ? language
            : (appConfig.getAudio() != null ? appConfig.getAudio().getDefaultLanguage() : "zh");
    }

    private String resolveModelName(String model) {
        return (model != null && !model.isEmpty())
            ? model
            : (appConfig.getAudio() != null ? appConfig.getAudio().getDefaultModel() : "demo");
    }

    private void ensureDirExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("创建存储目录失败: " + e.getMessage(), e);
        }
    }

    private List<AudioSegment> buildSegments(WhisperEngine.WhisperResult result, Long transcriptionId, String language) {
        List<AudioSegment> segments = new ArrayList<>();
        for (WhisperEngine.WhisperSegment src : result.getSegments()) {
            AudioSegment seg = new AudioSegment();
            seg.setTranscriptionId(transcriptionId);
            seg.setStartTime(src.getStartTime());
            seg.setEndTime(src.getEndTime());
            String text = src.getText();
            if ("zh".equalsIgnoreCase(language) && text != null) {
                text = whisperEngine.toSimplified(text);
            }
            seg.setText(text);
            segments.add(seg);
        }
        return segments;
    }

    private AudioTranscriptionVO buildVO(AudioTranscription result, List<AudioSegment> segments) {
        AudioTranscriptionVO vo = audioConvert.toVO(result);
        vo.setSegments(audioConvert.toSegmentVOList(segments));
        return vo;
    }
}

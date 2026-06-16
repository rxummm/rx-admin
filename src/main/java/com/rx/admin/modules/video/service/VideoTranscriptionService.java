package com.rx.admin.modules.video.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.config.AppConfig;
import com.rx.admin.common.constant.TranscriptionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.transcription.WhisperEngine;
import com.rx.admin.modules.video.convert.VideoConvert;
import com.rx.admin.modules.video.dto.VideoTranscriptionQueryDTO;
import com.rx.admin.modules.video.entity.VideoSegment;
import com.rx.admin.modules.video.entity.VideoTranscription;
import com.rx.admin.modules.video.mapper.VideoSegmentMapper;
import com.rx.admin.modules.video.mapper.VideoTranscriptionMapper;
import com.rx.admin.modules.video.vo.VideoSegmentVO;
import com.rx.admin.modules.video.vo.VideoTranscriptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VideoTranscriptionService implements IVideoTranscriptionService {

    private final VideoTranscriptionMapper transcriptionMapper;
    private final VideoSegmentMapper segmentMapper;
    private final VideoConvert videoConvert;
    private final AppConfig appConfig;
    private final WhisperEngine whisperEngine;

    private static final String[] SPEAKER_LABELS = TranscriptionConstants.Video.SPEAKER_LABELS;
    private static final String[] SPEAKER_NAMES = TranscriptionConstants.Video.SPEAKER_NAMES;

    @Override
    public Path getTempDir() {
        return whisperEngine.resolveTempDir(TranscriptionConstants.Video.TEMP_PREFIX);
    }

    @Override
    @Transactional
    public VideoTranscriptionVO transcribe(File videoFile, String originalName, String language) {
        String effectiveLanguage = resolveLanguage(language);
        String modelName = resolveModelName(null);

        VideoTranscription transcription = new VideoTranscription();
        transcription.setFileName(originalName != null ? originalName : videoFile.getName());
        transcription.setLanguage(effectiveLanguage);
        transcription.setModelName(modelName);
        transcription.setStatus(TranscriptionConstants.Status.SUCCESS);

        try {
            WhisperEngine.WhisperResult result;
            boolean usedFallback = false;

            if (appConfig.getAudio().isWhisperxEnabled() && whisperEngine.isWhisperXAvailable()) {
                try {
                    File wavFile = whisperEngine.extractAudioFromVideo(videoFile);
                    result = whisperEngine.transcribeWithWhisperX(wavFile, effectiveLanguage);
                    Files.deleteIfExists(wavFile.toPath());
                } catch (Exception e) {
                    log.warn("WhisperX 转写失败，回退到 whisper.cpp: {}", e.getMessage());
                    File wavFile = whisperEngine.extractAudioFromVideo(videoFile);
                    result = whisperEngine.transcribeWav(wavFile, effectiveLanguage, modelName);
                    Files.deleteIfExists(wavFile.toPath());
                }
            } else if (whisperEngine.isToolAvailable()) {
                try {
                    File wavFile = whisperEngine.extractAudioFromVideo(videoFile);
                    result = whisperEngine.transcribeWav(wavFile, effectiveLanguage, modelName);
                    Files.deleteIfExists(wavFile.toPath());
                } catch (Exception e) {
                    log.warn("视频转写失败，回退到演示模式: {}", e.getMessage());
                    result = whisperEngine.fallbackResult(effectiveLanguage);
                    usedFallback = true;
                }
            } else {
                log.info("Whisper 未配置，使用演示模式");
                result = whisperEngine.fallbackResult(effectiveLanguage);
                usedFallback = true;
            }

            String fullText = result.getFullText();
            if ("zh".equalsIgnoreCase(effectiveLanguage) && fullText != null) {
                fullText = whisperEngine.toSimplified(fullText);
            }
            transcription.setFullText(fullText);
            transcription.setModelName(usedFallback ? modelName + "-fallback" : modelName);
            transcriptionMapper.insert(transcription);

            List<VideoSegment> segments = buildSegments(result, transcription.getId(), effectiveLanguage);
            int speakerCount = assignSpeakers(segments);
            transcription.setSpeakerCount(speakerCount);
            transcriptionMapper.updateById(transcription);

            for (VideoSegment segment : segments) {
                segmentMapper.insert(segment);
            }

            String srtContent = generateSrtContent(transcription.getId());
            String srtPath = saveSrtFile(transcription.getId(), srtContent);
            transcription.setSrtPath(srtPath);
            transcriptionMapper.updateById(transcription);

            return buildVO(transcription, segments);

        } catch (Exception e) {
            log.error("视频转写失败", e);
            transcription.setErrorMessage(e.getMessage());
            transcription.setStatus(TranscriptionConstants.Status.FAIL);
            transcriptionMapper.insert(transcription);
            throw new RuntimeException("视频转写失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public VideoTranscriptionVO uploadOnly(File videoFile, String originalName, String language) {
        String effectiveLanguage = resolveLanguage(language);
        Path storageDir = getStorageDir();
        ensureDir(storageDir);

        String safeFileName = System.currentTimeMillis() + "_" + whisperEngine.sanitizeFileName(originalName, "video");
        Path storedPath = storageDir.resolve(safeFileName);
        try {
            Files.copy(videoFile.toPath(), storedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("保存视频文件失败: " + e.getMessage(), e);
        }

        VideoTranscription transcription = new VideoTranscription();
        transcription.setFileName(originalName != null ? originalName : videoFile.getName());
        transcription.setFilePath(storedPath.toString());
        transcription.setLanguage(effectiveLanguage);
        transcriptionMapper.insert(transcription);

        return videoConvert.toVO(transcription);
    }

    @Override
    @Transactional
    public VideoTranscriptionVO transcribeById(Long id, String model) {
        VideoTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription == null) {
            throw new RuntimeException("转写记录不存在");
        }

        String filePath = transcription.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new RuntimeException("视频文件路径不存在");
        }

        File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            throw new RuntimeException("视频文件不存在: " + filePath);
        }

        String modelName = resolveModelName(model);

        try {
            WhisperEngine.WhisperResult result;
            boolean usedFallback = false;

            if (appConfig.getAudio().isWhisperxEnabled() && whisperEngine.isWhisperXAvailable()) {
                try {
                    File wavFile = whisperEngine.extractAudioFromVideo(videoFile);
                    result = whisperEngine.transcribeWithWhisperX(wavFile, transcription.getLanguage());
                    Files.deleteIfExists(wavFile.toPath());
                    log.info("WhisperX 转写成功，说话人已通过声纹分离分配");
                } catch (Exception e) {
                    log.warn("WhisperX 转写失败，回退到 whisper.cpp: {}", e.getMessage());
                    File wavFile = whisperEngine.extractAudioFromVideo(videoFile);
                    result = whisperEngine.transcribeWav(wavFile, transcription.getLanguage(), modelName);
                    Files.deleteIfExists(wavFile.toPath());
                }
            } else if (whisperEngine.isToolAvailable()) {
                File wavFile = whisperEngine.extractAudioFromVideo(videoFile);
                result = whisperEngine.transcribeWav(wavFile, transcription.getLanguage(), modelName);
                Files.deleteIfExists(wavFile.toPath());
            } else {
                log.info("Whisper 未配置，使用演示模式");
                result = whisperEngine.fallbackResult(transcription.getLanguage());
                usedFallback = true;
            }

            String fullText = result.getFullText();
            if ("zh".equalsIgnoreCase(transcription.getLanguage()) && fullText != null) {
                fullText = whisperEngine.toSimplified(fullText);
            }
            double duration = result.getSegments().stream()
                .mapToDouble(s -> s.getEndTime())
                .max().orElse(whisperEngine.getMediaDuration(videoFile));
            transcription.setDuration(duration);
            transcription.setFullText(fullText);
            transcription.setModelName(usedFallback ? modelName + "-fallback" : modelName);
            transcription.setStatus(TranscriptionConstants.Status.SUCCESS);

            segmentMapper.delete(new LambdaQueryWrapper<VideoSegment>()
                .eq(VideoSegment::getTranscriptionId, id));

            List<VideoSegment> segments = buildSegments(result, id, transcription.getLanguage());
            int speakerCount = assignSpeakers(segments);
            transcription.setSpeakerCount(speakerCount);

            for (VideoSegment segment : segments) {
                segmentMapper.insert(segment);
            }

            String srtContent = generateSrtContent(id);
            String srtPath = saveSrtFile(id, srtContent);
            transcription.setSrtPath(srtPath);

            transcriptionMapper.updateById(transcription);

            return buildVO(transcription, segments);

        } catch (Exception e) {
            log.error("视频转写失败", e);
            transcription.setErrorMessage(e.getMessage());
            transcription.setStatus(TranscriptionConstants.Status.FAIL);
            transcriptionMapper.updateById(transcription);
            return null;
        }
    }

    @Override
    public VideoTranscriptionVO getById(Long id) {
        VideoTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription == null) {
            return null;
        }
        List<VideoSegment> segments = segmentMapper.selectByTranscriptionId(id);
        return buildVO(transcription, segments);
    }

    private LambdaQueryWrapper<VideoTranscription> buildQueryWrapper(VideoTranscriptionQueryDTO query) {
        LambdaQueryWrapper<VideoTranscription> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(VideoTranscription::getFileName, query.getKeyword())
                   .or()
                   .like(VideoTranscription::getFullText, query.getKeyword());
        }
        if (query.getLanguage() != null && !query.getLanguage().isEmpty()) {
            wrapper.eq(VideoTranscription::getLanguage, query.getLanguage());
        }
        if (query.getStatus() != null) {
            wrapper.eq(VideoTranscription::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(VideoTranscription::getCreateTime);
        return wrapper;
    }

    @Override
    public PageResult<VideoTranscription> pageQuery(VideoTranscriptionQueryDTO query) {
        LambdaQueryWrapper<VideoTranscription> wrapper = buildQueryWrapper(query);
        IPage<VideoTranscription> page = new Page<>(query.getPage(), query.getSize());
        page = transcriptionMapper.selectPage(page, wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<VideoTranscriptionVO> pageQueryVO(VideoTranscriptionQueryDTO query) {
        transcriptionMapper.backfillNullDuration();
        LambdaQueryWrapper<VideoTranscription> wrapper = buildQueryWrapper(query);
        IPage<VideoTranscription> page = new Page<>(query.getPage(), query.getSize());
        page = transcriptionMapper.selectPage(page, wrapper);
        List<VideoTranscriptionVO> voList = page.getRecords().stream()
            .map(videoConvert::toVO)
            .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        VideoTranscription transcription = transcriptionMapper.selectById(id);
        if (transcription != null) {
            deleteFile(transcription.getSrtPath());
            deleteFile(transcription.getFilePath());
        }
        segmentMapper.delete(new LambdaQueryWrapper<VideoSegment>()
            .eq(VideoSegment::getTranscriptionId, id));
        transcriptionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            VideoTranscription transcription = transcriptionMapper.selectById(id);
            if (transcription != null) {
                deleteFile(transcription.getSrtPath());
                deleteFile(transcription.getFilePath());
            }
        }
        segmentMapper.delete(new LambdaQueryWrapper<VideoSegment>()
            .in(VideoSegment::getTranscriptionId, ids));
        transcriptionMapper.delete(new LambdaQueryWrapper<VideoTranscription>()
            .in(VideoTranscription::getId, ids));
    }

    @Override
    @Transactional
    public VideoTranscriptionVO updateFileName(Long id, String fileName) {
        VideoTranscription transcription = transcriptionMapper.selectById(id);
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
    @Transactional
    public VideoTranscriptionVO updateSpeakerName(Long id, String speakerLabel, String speakerName) {
        if (speakerLabel == null || speakerLabel.trim().isEmpty()) {
            throw new RuntimeException("说话人标签不能为空");
        }
        if (speakerName == null || speakerName.trim().isEmpty()) {
            throw new RuntimeException("说话人名称不能为空");
        }

        List<VideoSegment> segments = segmentMapper.selectByTranscriptionId(id);
        for (VideoSegment segment : segments) {
            if (speakerLabel.trim().equals(segment.getSpeakerLabel())) {
                segment.setSpeakerName(speakerName.trim());
                segmentMapper.updateById(segment);
            }
        }

        return getById(id);
    }

    @Override
    public String generateSrtContent(Long id) {
        VideoTranscriptionVO vo = getById(id);
        if (vo == null || vo.getSegments() == null || vo.getSegments().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (VideoSegmentVO segment : vo.getSegments()) {
            sb.append(index++).append("\n");
            sb.append(formatTime(segment.getStartTime())).append(" --> ").append(formatTime(segment.getEndTime())).append("\n");

            String speakerPrefix = "";
            if (segment.getSpeakerName() != null && !segment.getSpeakerName().isEmpty()) {
                speakerPrefix = segment.getSpeakerName() + "：";
            }
            sb.append(speakerPrefix).append(segment.getText()).append("\n\n");
        }

        return sb.toString();
    }

    @Override
    public String generateAssContent(Long id) {
        VideoTranscriptionVO vo = getById(id);
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

        for (VideoSegmentVO segment : vo.getSegments()) {
            String speakerName = segment.getSpeakerName() != null ? segment.getSpeakerName() : "";
            String text = segment.getText();
            if (speakerName != null && !speakerName.isEmpty()) {
                text = speakerName + "：" + text;
            }
            sb.append(String.format("Dialogue: 0,%s,%s,Default,%s,10,10,50,,%s\n",
                formatAssTime(segment.getStartTime()),
                formatAssTime(segment.getEndTime()),
                speakerName,
                text));
        }

        return sb.toString();
    }

    @Override
    public String generateDialogueByRole(Long id) {
        VideoTranscriptionVO vo = getById(id);
        if (vo == null || vo.getSegments() == null || vo.getSegments().isEmpty()) {
            return "";
        }

        java.util.Map<String, List<VideoSegmentVO>> bySpeaker = new java.util.LinkedHashMap<>();
        for (VideoSegmentVO seg : vo.getSegments()) {
            String key = seg.getSpeakerName() != null && !seg.getSpeakerName().isEmpty()
                ? seg.getSpeakerName() : (seg.getSpeakerLabel() != null ? seg.getSpeakerLabel() : "未知角色");
            bySpeaker.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(seg);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("按角色导出台词\n");
        sb.append("=".repeat(40)).append("\n\n");

        for (var entry : bySpeaker.entrySet()) {
            sb.append("【").append(entry.getKey()).append("】\n");
            sb.append("-".repeat(30)).append("\n");
            for (VideoSegmentVO seg : entry.getValue()) {
                String start = formatTime(seg.getStartTime());
                String end = formatTime(seg.getEndTime());
                sb.append("  [").append(start).append(" --> ").append(end).append("]\n");
                sb.append("  ").append(seg.getText()).append("\n\n");
            }
        }

        return sb.toString();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        VideoTranscription t = new VideoTranscription();
        t.setId(id);
        t.setStatus(status);
        transcriptionMapper.updateById(t);
    }

    private Path getStorageDir() {
        return whisperEngine.resolveStorageDir(TranscriptionConstants.Video.TEMP_PREFIX);
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

    private void ensureDir(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("创建目录失败: " + e.getMessage(), e);
        }
    }

    private void deleteFile(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (IOException e) {
                log.warn("删除文件失败: {}", path, e);
            }
        }
    }

    private List<VideoSegment> buildSegments(WhisperEngine.WhisperResult result, Long transcriptionId, String language) {
        List<VideoSegment> segments = new ArrayList<>();
        for (WhisperEngine.WhisperSegment src : result.getSegments()) {
            VideoSegment seg = new VideoSegment();
            seg.setTranscriptionId(transcriptionId);
            seg.setStartTime(src.getStartTime());
            seg.setEndTime(src.getEndTime());
            String text = src.getText();
            if ("zh".equalsIgnoreCase(language) && text != null) {
                text = whisperEngine.toSimplified(text);
            }
            seg.setText(text);
            // 如果 WhisperX 返回了 speaker，直接使用真实声纹分离结果
            if (src.getSpeaker() != null && !src.getSpeaker().isEmpty()) {
                seg.setSpeakerLabel(src.getSpeaker());
            }
            segments.add(seg);
        }
        return segments;
    }

    private int assignSpeakers(List<VideoSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }
        // 检查是否已有真实的 speaker 数据（来自 WhisperX 声纹分离）
        boolean hasRealSpeakers = segments.stream()
            .anyMatch(s -> s.getSpeakerLabel() != null && !s.getSpeakerLabel().isEmpty());

        if (hasRealSpeakers) {
            // WhisperX 已分配说话人，直接统计数量并为缺失的分配默认名称
            Set<String> speakers = new LinkedHashSet<>();
            for (VideoSegment segment : segments) {
                speakers.add(segment.getSpeakerLabel());
                if (segment.getSpeakerName() == null || segment.getSpeakerName().isEmpty()) {
                    segment.setSpeakerName("角色" + speakers.size());
                }
            }
            return speakers.size();
        }

        // 回退：轮询分配（whisper.cpp 模式）
        Set<String> speakers = new HashSet<>();
        int speakerIndex = 0;
        for (VideoSegment segment : segments) {
            segment.setSpeakerLabel(SPEAKER_LABELS[speakerIndex % SPEAKER_LABELS.length]);
            segment.setSpeakerName(SPEAKER_NAMES[speakerIndex % SPEAKER_NAMES.length]);
            speakers.add(SPEAKER_LABELS[speakerIndex % SPEAKER_LABELS.length]);
            speakerIndex++;
        }
        return speakers.size();
    }

    private String saveSrtFile(Long id, String content) throws IOException {
        Path tempDir = getTempDir();
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        String srtPath = tempDir.resolve(id + "_subtitle.srt").toString();
        Files.writeString(Paths.get(srtPath), content, java.nio.charset.StandardCharsets.UTF_8);
        return srtPath;
    }

    private VideoTranscriptionVO buildVO(VideoTranscription result, List<VideoSegment> segments) {
        VideoTranscriptionVO vo = videoConvert.toVO(result);
        vo.setSegments(videoConvert.toSegmentVOList(segments));
        return vo;
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
}
package com.rx.admin.modules.audio.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.constant.TranscriptionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.common.util.FileUploadUtils;
import com.rx.admin.modules.audio.dto.AudioTranscriptionQueryDTO;
import com.rx.admin.modules.audio.service.IAudioTranscriptionService;
import com.rx.admin.modules.audio.vo.AudioTranscriptionVO;
import com.rx.admin.modules.transcription.TranscriptionDispatcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Tag(name = "音频转写管理")
@RestController
@ApiVersion(1)
@RequestMapping("/audio/transcription")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AudioTranscriptionController {

    private final IAudioTranscriptionService audioTranscriptionService;
    private final TranscriptionDispatcher transcriptionDispatcher;

    private static final List<String> AUDIO_EXTENSIONS = TranscriptionConstants.Audio.EXTENSIONS;

    @Operation(summary = "上传音频文件（不转写）")
    @PostMapping("/upload-only")
    @SaCheckPermission(PermissionConstants.AudioTranscription.UPLOAD)
    @OperateLog(module = "音频转写", operation = "上传音频")
    public Result<AudioTranscriptionVO> uploadOnly(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language) throws IOException {

        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            originalName = "audio";
        }

        if (!FileUploadUtils.isValidFile(originalName, TranscriptionConstants.Audio.EXTENSIONS)) {
            return Result.fail("只支持上传音频文件(mp3/wav/m4a/flac/aac/ogg/webm)");
        }

        Path tempFile = FileUploadUtils.saveTempFile(file, audioTranscriptionService.getTempDir());
        try {
            AudioTranscriptionVO result = audioTranscriptionService.uploadOnly(
                tempFile.toFile(), originalName, language);
            return Result.ok(result);
        } finally {
            FileUploadUtils.cleanupTempFile(tempFile);
        }
    }

    @Operation(summary = "对已上传记录进行转写（异步）")
    @PostMapping("/{id}/transcribe")
    @SaCheckPermission(PermissionConstants.AudioTranscription.UPLOAD)
    @OperateLog(module = "音频转写", operation = "执行转写")
    public Result<AudioTranscriptionVO> transcribeById(
            @PathVariable Long id,
            @RequestParam(value = "model", required = false) String model) {
        AudioTranscriptionVO record = audioTranscriptionService.getById(id);
        if (record == null) {
            return Result.fail("转写记录不存在");
        }
        audioTranscriptionService.updateStatus(id, TranscriptionConstants.Status.PENDING);
        transcriptionDispatcher.processAudio(id, model);
        record.setStatus(TranscriptionConstants.Status.PENDING);
        return Result.ok(record);
    }

    @Operation(summary = "上传音频文件并转写（异步）")
    @PostMapping("/upload")
    @SaCheckPermission(PermissionConstants.AudioTranscription.UPLOAD)
    @OperateLog(module = "音频转写", operation = "上传转写")
    public Result<AudioTranscriptionVO> uploadAndTranscribe(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language) throws IOException {

        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            originalName = "audio";
        }

        if (!FileUploadUtils.isValidFile(originalName, AUDIO_EXTENSIONS)) {
            return Result.fail("只支持上传音频文件(mp3/wav/m4a/flac/aac/ogg/webm)");
        }

        Path tempFile = FileUploadUtils.saveTempFile(file, audioTranscriptionService.getTempDir());
        try {
            AudioTranscriptionVO result = audioTranscriptionService.uploadOnly(
                tempFile.toFile(), originalName, language);
            audioTranscriptionService.updateStatus(result.getId(), TranscriptionConstants.Status.PENDING);
            transcriptionDispatcher.processAudio(result.getId(), null);
            result.setStatus(TranscriptionConstants.Status.PENDING);
            return Result.ok(result);
        } finally {
            FileUploadUtils.cleanupTempFile(tempFile);
        }
    }

    @Operation(summary = "分页查询转写记录")
    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.AudioTranscription.LIST)
    public Result<PageResult<AudioTranscriptionVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer status) {

        AudioTranscriptionQueryDTO query = new AudioTranscriptionQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setKeyword(keyword);
        query.setLanguage(language);
        query.setStatus(status);

        return Result.ok(audioTranscriptionService.pageQueryVO(query));
    }

    @Operation(summary = "获取转写详情")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.AudioTranscription.VIEW)
    public Result<AudioTranscriptionVO> getById(@PathVariable Long id) {
        AudioTranscriptionVO result = audioTranscriptionService.getById(id);
        if (result == null) {
            return Result.fail("转写记录不存在");
        }
        return Result.ok(result);
    }

    @Operation(summary = "删除转写记录")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.AudioTranscription.DELETE)
    @OperateLog(module = "音频转写", operation = "删除记录")
    public Result<Void> deleteById(@PathVariable Long id) {
        audioTranscriptionService.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "修改文件名")
    @PutMapping("/{id}/file-name")
    @SaCheckPermission(PermissionConstants.AudioTranscription.UPDATE)
    @OperateLog(module = "音频转写", operation = "修改文件名")
    public Result<AudioTranscriptionVO> updateFileName(
            @PathVariable Long id,
            @RequestParam("fileName") String fileName) {
        AudioTranscriptionVO result = audioTranscriptionService.updateFileName(id, fileName);
        return Result.ok(result);
    }

    @Operation(summary = "下载SRT字幕")
    @GetMapping("/{id}/download-srt")
    @SaCheckPermission(PermissionConstants.AudioTranscription.VIEW)
    public ResponseEntity<byte[]> downloadSrt(@PathVariable Long id) {
        AudioTranscriptionVO vo = audioTranscriptionService.getById(id);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }

        String srtContent = audioTranscriptionService.generateSrtContent(id);
        if (srtContent == null || srtContent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String fileName = vo.getFileName().replaceAll("\\.[^.]+$", "") + ".srt";
        byte[] content = srtContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
            .headers(headers)
            .body(content);
    }

    @Operation(summary = "下载ASS字幕")
    @GetMapping("/{id}/download-ass")
    @SaCheckPermission(PermissionConstants.AudioTranscription.VIEW)
    public ResponseEntity<byte[]> downloadAss(@PathVariable Long id) {
        AudioTranscriptionVO vo = audioTranscriptionService.getById(id);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }

        String assContent = audioTranscriptionService.generateAssContent(id);
        if (assContent == null || assContent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String fileName = vo.getFileName().replaceAll("\\.[^.]+$", "") + ".ass";
        byte[] content = assContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
            .headers(headers)
            .body(content);
    }

    @Operation(summary = "转换简体/繁体")
    @PutMapping("/{id}/convert-script")
    @SaCheckPermission(PermissionConstants.AudioTranscription.VIEW)
    @OperateLog(module = "音频转写", operation = "转换简繁体")
    public Result<AudioTranscriptionVO> convertScript(
            @PathVariable Long id,
            @RequestParam("target") String target) {
        AudioTranscriptionVO result = audioTranscriptionService.convertScript(id, target);
        return Result.ok(result);
    }

    @Operation(summary = "批量删除转写记录")
    @DeleteMapping("/batch/{ids}")
    @SaCheckPermission(PermissionConstants.AudioTranscription.DELETE)
    @OperateLog(module = "音频转写", operation = "批量删除")
    public Result<Void> deleteBatch(@PathVariable String ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的记录");
        }
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .toList();
        if (idList.isEmpty()) {
            return Result.fail("请选择要删除的记录");
        }
        audioTranscriptionService.deleteBatch(idList);
        return Result.ok();
    }
}

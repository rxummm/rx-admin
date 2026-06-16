package com.rx.admin.modules.video.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.constant.TranscriptionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.common.util.FileUploadUtils;
import com.rx.admin.modules.transcription.TranscriptionDispatcher;
import com.rx.admin.modules.video.dto.VideoTranscriptionQueryDTO;
import com.rx.admin.modules.video.service.IVideoTranscriptionService;
import com.rx.admin.modules.video.vo.VideoTranscriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Tag(name = "视频转写管理")
@RestController
@ApiVersion(1)
@RequestMapping("/video/transcription")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VideoTranscriptionController {

    private final IVideoTranscriptionService videoTranscriptionService;
    private final TranscriptionDispatcher transcriptionDispatcher;

    private static final List<String> VIDEO_EXTENSIONS = TranscriptionConstants.Video.EXTENSIONS;

    @Operation(summary = "上传视频文件（不转写）")
    @PostMapping("/upload-only")
    @SaCheckPermission(PermissionConstants.VideoTranscription.UPLOAD)
    @OperateLog(module = "视频转写", operation = "上传视频")
    public Result<VideoTranscriptionVO> uploadOnly(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language) throws IOException {

        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            originalName = "video";
        }

        if (!FileUploadUtils.isValidFile(originalName, TranscriptionConstants.Video.EXTENSIONS)) {
            return Result.fail("只支持上传视频文件(mp4/avi/mkv/flv/mov/webm)");
        }

        Path tempFile = FileUploadUtils.saveTempFile(file, videoTranscriptionService.getTempDir());
        try {
            VideoTranscriptionVO result = videoTranscriptionService.uploadOnly(
                tempFile.toFile(), originalName, language);
            return Result.ok(result);
        } finally {
            FileUploadUtils.cleanupTempFile(tempFile);
        }
    }

    @Operation(summary = "对已上传记录进行转写（异步）")
    @PostMapping("/{id}/transcribe")
    @SaCheckPermission(PermissionConstants.VideoTranscription.UPLOAD)
    @OperateLog(module = "视频转写", operation = "执行转写")
    public Result<VideoTranscriptionVO> transcribeById(
            @PathVariable Long id,
            @RequestParam(value = "model", required = false) String model) {
        VideoTranscriptionVO record = videoTranscriptionService.getById(id);
        if (record == null) {
            return Result.fail("转写记录不存在");
        }
        videoTranscriptionService.updateStatus(id, TranscriptionConstants.Status.PENDING);
        transcriptionDispatcher.processVideo(id, model);
        record.setStatus(TranscriptionConstants.Status.PENDING);
        return Result.ok(record);
    }

    @Operation(summary = "上传视频文件并转写（异步）")
    @PostMapping("/upload")
    @SaCheckPermission(PermissionConstants.VideoTranscription.UPLOAD)
    @OperateLog(module = "视频转写", operation = "上传转写")
    public Result<VideoTranscriptionVO> uploadAndTranscribe(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language) throws IOException {

        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            originalName = "video";
        }

        if (!FileUploadUtils.isValidFile(originalName, VIDEO_EXTENSIONS)) {
            return Result.fail("只支持上传视频文件(mp4/avi/mkv/flv/mov/webm)");
        }

        Path tempFile = FileUploadUtils.saveTempFile(file, videoTranscriptionService.getTempDir());
        try {
            VideoTranscriptionVO result = videoTranscriptionService.uploadOnly(
                tempFile.toFile(), originalName, language);
            videoTranscriptionService.updateStatus(result.getId(), TranscriptionConstants.Status.PENDING);
            transcriptionDispatcher.processVideo(result.getId(), null);
            result.setStatus(TranscriptionConstants.Status.PENDING);
            return Result.ok(result);
        } finally {
            FileUploadUtils.cleanupTempFile(tempFile);
        }
    }

    @Operation(summary = "分页查询转写记录")
    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.VideoTranscription.LIST)
    public Result<PageResult<VideoTranscriptionVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer status) {

        VideoTranscriptionQueryDTO query = new VideoTranscriptionQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setKeyword(keyword);
        query.setLanguage(language);
        query.setStatus(status);

        return Result.ok(videoTranscriptionService.pageQueryVO(query));
    }

    @Operation(summary = "获取转写详情")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.VideoTranscription.VIEW)
    public Result<VideoTranscriptionVO> getById(@PathVariable Long id) {
        VideoTranscriptionVO result = videoTranscriptionService.getById(id);
        if (result == null) {
            return Result.fail("转写记录不存在");
        }
        return Result.ok(result);
    }

    @Operation(summary = "删除转写记录")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.VideoTranscription.DELETE)
    @OperateLog(module = "视频转写", operation = "删除记录")
    public Result<Void> deleteById(@PathVariable Long id) {
        videoTranscriptionService.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "修改文件名")
    @PutMapping("/{id}/file-name")
    @SaCheckPermission(PermissionConstants.VideoTranscription.UPDATE)
    @OperateLog(module = "视频转写", operation = "修改文件名")
    public Result<VideoTranscriptionVO> updateFileName(
            @PathVariable Long id,
            @RequestParam("fileName") @NotBlank @Size(max = 255) String fileName) {
        VideoTranscriptionVO result = videoTranscriptionService.updateFileName(id, fileName);
        return Result.ok(result);
    }

    @Operation(summary = "修改说话人名称")
    @PutMapping("/{id}/speaker-name")
    @SaCheckPermission(PermissionConstants.VideoTranscription.UPDATE)
    @OperateLog(module = "视频转写", operation = "修改说话人名称")
    public Result<VideoTranscriptionVO> updateSpeakerName(
            @PathVariable Long id,
            @RequestParam("speakerLabel") @NotBlank @Size(max = 50) String speakerLabel,
            @RequestParam("speakerName") @NotBlank @Size(max = 100) String speakerName) {
        VideoTranscriptionVO result = videoTranscriptionService.updateSpeakerName(id, speakerLabel, speakerName);
        return Result.ok(result);
    }

    @Operation(summary = "下载SRT字幕")
    @GetMapping("/{id}/download-srt")
    @SaCheckPermission(PermissionConstants.VideoTranscription.VIEW)
    public ResponseEntity<byte[]> downloadSrt(@PathVariable Long id) {
        VideoTranscriptionVO vo = videoTranscriptionService.getById(id);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }

        String srtContent = videoTranscriptionService.generateSrtContent(id);
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

    @Operation(summary = "按角色导出剧本台词")
    @GetMapping("/{id}/download-dialogue")
    @SaCheckPermission(PermissionConstants.VideoTranscription.VIEW)
    public ResponseEntity<byte[]> downloadDialogue(@PathVariable Long id) {
        VideoTranscriptionVO vo = videoTranscriptionService.getById(id);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }

        String dialogueContent = videoTranscriptionService.generateDialogueByRole(id);
        if (dialogueContent == null || dialogueContent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String fileName = vo.getFileName().replaceAll("\\.[^.]+$", "") + "_台词.txt";
        byte[] content = dialogueContent.getBytes(StandardCharsets.UTF_8);

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
    @SaCheckPermission(PermissionConstants.VideoTranscription.VIEW)
    public ResponseEntity<byte[]> downloadAss(@PathVariable Long id) {
        VideoTranscriptionVO vo = videoTranscriptionService.getById(id);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }

        String assContent = videoTranscriptionService.generateAssContent(id);
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

    @Operation(summary = "批量删除转写记录")
    @DeleteMapping("/batch/{ids}")
    @SaCheckPermission(PermissionConstants.VideoTranscription.DELETE)
    @OperateLog(module = "视频转写", operation = "批量删除")
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
        videoTranscriptionService.deleteBatch(idList);
        return Result.ok();
    }
}

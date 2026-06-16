package com.rx.admin.modules.video.player.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.video.player.convert.VideoPlayerConvert;
import com.rx.admin.modules.video.player.entity.VideoFile;
import com.rx.admin.modules.video.player.service.VideoPlayerService;
import com.rx.admin.modules.video.player.vo.VideoFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Tag(name = "视频播放")
@RestController
@ApiVersion(1)
@RequestMapping("/video/player")
@RequiredArgsConstructor
public class VideoPlayerController {

    private final VideoPlayerService videoPlayerService;
    private final VideoPlayerConvert videoConvert;

    @Operation(summary = "扫描视频文件夹")
    @PostMapping("/scan")
    @SaCheckLogin
    @SaCheckPermission("video:player:scan")
    @OperateLog(module = "视频播放", operation = "扫描视频文件夹")
    public Result<List<VideoFileVO>> scan() {
        try {
            List<VideoFile> videos = videoPlayerService.scanVideoFolder();
            return Result.ok("扫描完成，发现 " + videos.size() + " 个视频", videoConvert.toVOList(videos));
        } catch (Exception e) {
            return Result.fail("扫描失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取视频列表")
    @GetMapping("/list")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public Result<List<VideoFileVO>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(videoConvert.toVOList(videoPlayerService.searchVideos(keyword)));
    }

    @Operation(summary = "获取视频详情")
    @GetMapping("/{id}")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public Result<VideoFileVO> detail(@PathVariable Long id) {
        VideoFile video = videoPlayerService.getVideoDetail(id);
        if (video == null) {
            return Result.fail("视频不存在");
        }
        return Result.ok(videoConvert.toVO(video));
    }

    @Operation(summary = "记录播放")
    @PostMapping("/record")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public Result<?> recordPlay(
            @RequestParam Long videoId,
            @RequestParam(defaultValue = "0") Integer playedSeconds) {
        videoPlayerService.recordPlay(videoId, "system", playedSeconds);
        return Result.ok();
    }

    @Operation(summary = "播放统计")
    @GetMapping("/stats")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public Result<Map<String, Object>> stats() {
        return Result.ok(videoPlayerService.getPlayStats());
    }

    @Operation(summary = "最近播放记录")
    @GetMapping("/recent")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public Result<?> recent(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(videoPlayerService.getRecentPlays(limit));
    }

    @Operation(summary = "删除视频记录")
    @DeleteMapping("/{id}")
    @SaCheckLogin
    @SaCheckPermission("video:player:delete")
    @OperateLog(module = "视频播放", operation = "删除视频记录")
    public Result<?> delete(@PathVariable Long id) {
        return Result.ok(videoPlayerService.deleteVideo(id));
    }

    @Operation(summary = "获取视频文件夹路径")
    @GetMapping("/folder")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public Result<Map<String, String>> folder() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("folder", videoPlayerService.getVideoFolder());
        return Result.ok(data);
    }

    @Operation(summary = "流式播放视频文件")
    @GetMapping("/stream/{id}")
    @SaCheckLogin
    @SaCheckPermission("video:player:list")
    public void streamVideo(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        VideoFile video = videoPlayerService.getVideoDetail(id);
        if (video == null) {
            response.setStatus(404);
            return;
        }
        Path videoPath = Path.of(video.getFilePath());
        if (!Files.exists(videoPath)) {
            response.setStatus(404);
            return;
        }

        String contentType = getContentType(video.getVideoType());

        try {
            long fileSize = Files.size(videoPath);
            String rangeHeader = request.getHeader("Range");

            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1;
                long contentLength = end - start + 1;

                response.setStatus(206);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
                response.setContentLengthLong(contentLength);

                try (RandomAccessFile raf = new RandomAccessFile(videoPath.toFile(), "r");
                     OutputStream os = response.getOutputStream()) {
                    raf.seek(start);
                    byte[] buffer = new byte[8192];
                    long remaining = contentLength;
                    int read;
                    while (remaining > 0 && (read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        os.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
            } else {
                response.setContentLengthLong(fileSize);
                Files.copy(videoPath, response.getOutputStream());
                response.getOutputStream().flush();
            }
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    private String getContentType(String videoType) {
        if (videoType == null) return "video/mp4";
        return switch (videoType.toLowerCase()) {
            case "webm" -> "video/webm";
            case "ogg" -> "video/ogg";
            case "mkv" -> "video/x-matroska";
            case "avi" -> "video/x-msvideo";
            case "flv" -> "video/x-flv";
            case "mov" -> "video/quicktime";
            case "m4v" -> "video/x-m4v";
            case "wmv" -> "video/x-ms-wmv";
            case "ts" -> "video/mp2t";
            default -> "video/mp4";
        };
    }
}

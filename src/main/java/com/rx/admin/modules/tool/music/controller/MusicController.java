package com.rx.admin.modules.tool.music.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.music.entity.Song;
import com.rx.admin.modules.tool.music.service.MusicService;
import com.rx.admin.modules.tool.music.convert.SongConvert;
import com.rx.admin.modules.tool.music.convert.PlayRecordConvert;
import com.rx.admin.modules.tool.music.vo.SongVO;
import com.rx.admin.modules.tool.music.vo.PlayRecordVO;
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

@Tag(name = "音乐播放")
@RestController
@ApiVersion(1)
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;
    private final SongConvert songConvert;
    private final PlayRecordConvert playRecordConvert;

    @OperateLog(module = "音乐播放", operation = "扫描音乐文件夹")
    @Operation(summary = "扫描音乐文件夹")
    @PostMapping("/scan")
    @SaCheckLogin
    @SaCheckPermission("tool:music:scan")
    public Result<List<SongVO>> scan() {
        try {
            List<Song> songs = musicService.scanMusicFolder();
            return Result.ok("扫描完成，发现 " + songs.size() + " 首歌曲", songConvert.toVOList(songs));
        } catch (Exception e) {
            return Result.fail("扫描失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取歌曲列表")
    @GetMapping("/songs")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public Result<List<SongVO>> listSongs(@RequestParam(required = false) String keyword) {
        return Result.ok(songConvert.toVOList(musicService.searchSongs(keyword)));
    }

    @Operation(summary = "获取歌曲详情(含歌词)")
    @GetMapping("/song/{id}")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public Result<SongVO> songDetail(@PathVariable Long id) {
        Song song = musicService.getSongDetail(id);
        if (song == null) {
            return Result.fail("歌曲不存在");
        }
        return Result.ok(songConvert.toVO(song));
    }

    @OperateLog(module = "音乐播放", operation = "记录播放记录")
    @Operation(summary = "记录播放")
    @PostMapping("/play/{id}")
    @SaCheckLogin
    @SaCheckPermission("tool:music:play")
    public Result<?> recordPlay(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer playedSeconds) {
        musicService.recordPlay(id, "system", playedSeconds);
        return Result.ok();
    }

    @Operation(summary = "播放统计")
    @GetMapping("/stats")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public Result<Map<String, Object>> stats() {
        return Result.ok(musicService.getPlayStats());
    }

    @Operation(summary = "最近播放记录")
    @GetMapping("/recent")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public Result<List<PlayRecordVO>> recent(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(playRecordConvert.toVOList(musicService.getRecentPlays(limit)));
    }

    @Operation(summary = "热门歌曲排行")
    @GetMapping("/top")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public Result<List<Map<String, Object>>> topSongs(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(musicService.getTopSongs(limit));
    }

    @Operation(summary = "获取音乐文件夹路径")
    @GetMapping("/folder")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public Result<Map<String, String>> folder() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("folder", musicService.getMusicFolder());
        return Result.ok(data);
    }

    @Operation(summary = "流式播放MP3文件")
    @GetMapping("/stream/{id}")
    @SaCheckLogin
    @SaCheckPermission("tool:music:query")
    public void streamMusic(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        Song song = musicService.getSongDetail(id);
        if (song == null) {
            response.setStatus(404);
            return;
        }
        Path mp3Path = Path.of(song.getMp3Path());
        if (!Files.exists(mp3Path)) {
            response.setStatus(404);
            return;
        }
        try {
            long fileSize = Files.size(mp3Path);
            String rangeHeader = request.getHeader("Range");

            response.setContentType("audio/mpeg");
            response.setHeader("Accept-Ranges", "bytes");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1;
                long contentLength = end - start + 1;

                response.setStatus(206);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
                response.setContentLengthLong(contentLength);

                try (RandomAccessFile raf = new RandomAccessFile(mp3Path.toFile(), "r");
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
                Files.copy(mp3Path, response.getOutputStream());
                response.getOutputStream().flush();
            }
        } catch (IOException e) {
            response.setStatus(500);
        }
    }
}

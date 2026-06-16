package com.rx.admin.modules.video.player.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.video.player.entity.VideoFile;
import com.rx.admin.modules.video.player.entity.VideoPlayRecord;
import com.rx.admin.modules.video.player.mapper.VideoFileMapper;
import com.rx.admin.modules.video.player.mapper.VideoPlayRecordMapper;
import com.rx.admin.modules.video.player.service.VideoPlayerService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("null")
public class VideoPlayerServiceImpl extends ServiceImpl<VideoFileMapper, VideoFile> implements VideoPlayerService {

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "mp4", "webm", "ogg", "mkv", "avi", "flv", "mov", "m4v", "wmv", "ts"
    );

    @Value("${video.folder:C:\\Users\\admin\\Downloads\\video}")
    private String videoFolder;

    private final VideoPlayRecordMapper playRecordMapper;

    public VideoPlayerServiceImpl(VideoPlayRecordMapper playRecordMapper) {
        this.playRecordMapper = playRecordMapper;
    }

    @Override
    public List<VideoFile> scanVideoFolder() {
        Path folder = Paths.get(videoFolder);
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            log.warn("视频文件夹不存在: {}", videoFolder);
            return List.of();
        }

        Map<String, Path> videoFiles = new LinkedHashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) continue;
                String name = entry.getFileName().toString();
                String ext = getExtension(name);
                if (SUPPORTED_FORMATS.contains(ext)) {
                    videoFiles.put(name, entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("扫描视频文件夹失败：" + e.getMessage(), e);
        }

        List<VideoFile> existingVideos = list();
        Set<String> existingPaths = existingVideos.stream()
                .map(VideoFile::getFilePath)
                .collect(Collectors.toSet());

        List<VideoFile> result = new ArrayList<>();

        for (Map.Entry<String, Path> entry : videoFiles.entrySet()) {
            String fileName = entry.getKey();
            Path filePath = entry.getValue();
            String filePathStr = filePath.toAbsolutePath().toString();

            try {
                long fileSize = Files.size(filePath);
                String baseName = getBaseName(fileName);
                String ext = getExtension(fileName);
                int duration = getVideoDuration(filePath.toFile());

                if (existingPaths.contains(filePathStr)) {
                    VideoFile exist = existingVideos.stream()
                            .filter(v -> filePathStr.equals(v.getFilePath()))
                            .findFirst().orElse(null);
                    if (exist != null) {
                        exist.setTitle(baseName);
                        exist.setFileName(fileName);
                        exist.setFileSize(fileSize);
                        exist.setVideoType(ext);
                        if (duration > 0) exist.setDuration(duration);
                        updateById(exist);
                        result.add(exist);
                    }
                } else {
                    VideoFile video = new VideoFile();
                    video.setTitle(baseName);
                    video.setFileName(fileName);
                    video.setFilePath(filePathStr);
                    video.setFileSize(fileSize);
                    video.setVideoType(ext);
                    video.setDuration(duration);
                    video.setPlayCount(0);
                    save(video);
                    result.add(video);
                }
            } catch (IOException e) {
                log.warn("跳过无法读取的视频文件: {}, 原因: {}", fileName, e.getMessage());
            }
        }

        result.sort(Comparator.comparing(v -> v.getTitle() != null ? v.getTitle() : ""));
        return result;
    }

    @Override
    public List<VideoFile> searchVideos(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return list(new LambdaQueryWrapper<VideoFile>().orderByDesc(VideoFile::getCreateTime));
        }
        return list(new LambdaQueryWrapper<VideoFile>()
                .like(VideoFile::getTitle, keyword)
                .or()
                .like(VideoFile::getFileName, keyword)
                .orderByDesc(VideoFile::getCreateTime));
    }

    @Override
    public VideoFile getVideoDetail(Long videoId) {
        return getById(videoId);
    }

    @Override
    public void recordPlay(Long videoId, String username, Integer playedSeconds) {
        VideoFile video = getById(videoId);
        if (video != null) {
            video.setPlayCount((video.getPlayCount() == null ? 0 : video.getPlayCount()) + 1);
            video.setLastPlayTime(LocalDateTime.now());
            updateById(video);
        }

        VideoPlayRecord record = new VideoPlayRecord();
        record.setVideoId(videoId);
        record.setVideoTitle(video != null ? video.getTitle() : "");
        record.setUsername(username != null ? username : "anonymous");
        record.setPlayedSeconds(playedSeconds != null ? playedSeconds : 0);
        record.setCreateTime(LocalDateTime.now());
        playRecordMapper.insert(record);
    }

    @Override
    public Map<String, Object> getPlayStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalVideos = count();
        stats.put("totalVideos", totalVideos);

        Long totalPlays = playRecordMapper.selectCount(null);
        stats.put("totalPlays", totalPlays);

        LambdaQueryWrapper<VideoPlayRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(VideoPlayRecord::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        Long todayPlays = playRecordMapper.selectCount(todayWrapper);
        stats.put("todayPlays", todayPlays);

        return stats;
    }

    @Override
    public List<VideoPlayRecord> getRecentPlays(int limit) {
        return playRecordMapper.selectList(
                new LambdaQueryWrapper<VideoPlayRecord>()
                        .orderByDesc(VideoPlayRecord::getCreateTime)
                        .last("LIMIT " + limit));
    }

    @Override
    public boolean deleteVideo(Long videoId) {
        return removeById(videoId);
    }

    @Override
    public String getVideoFolder() {
        return videoFolder;
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    private String getBaseName(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private int getVideoDuration(File videoFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error", "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                int exitCode = process.waitFor();
                if (exitCode == 0 && line != null && !line.isEmpty()) {
                    return (int) Double.parseDouble(line.trim());
                }
            }
        } catch (Exception e) {
            log.debug("ffprobe 获取视频时长失败: {}", e.getMessage());
        }
        return 0;
    }
}

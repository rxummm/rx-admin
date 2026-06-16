package com.rx.admin.modules.video.player.service;

import com.rx.admin.modules.video.player.entity.VideoFile;
import com.rx.admin.modules.video.player.entity.VideoPlayRecord;

import java.util.List;
import java.util.Map;

public interface VideoPlayerService {

    /**
     * 扫描视频文件夹
     */
    List<VideoFile> scanVideoFolder();

    /**
     * 获取视频列表(支持关键词搜索)
     */
    List<VideoFile> searchVideos(String keyword);

    /**
     * 获取视频详情
     */
    VideoFile getVideoDetail(Long videoId);

    /**
     * 记录播放
     */
    void recordPlay(Long videoId, String username, Integer playedSeconds);

    /**
     * 播放统计
     */
    Map<String, Object> getPlayStats();

    /**
     * 最近播放记录
     */
    List<VideoPlayRecord> getRecentPlays(int limit);

    /**
     * 删除视频记录
     */
    boolean deleteVideo(Long videoId);

    /**
     * 获取视频文件夹路径
     */
    String getVideoFolder();
}

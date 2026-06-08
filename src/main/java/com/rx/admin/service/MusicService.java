package com.rx.admin.service;

import com.rx.admin.entity.Song;
import com.rx.admin.entity.PlayRecord;

import java.util.List;
import java.util.Map;

public interface MusicService {

    /**
     * 扫描音乐文件夹，发现MP3及同名的LRC文件
     * @return 发现的歌曲列表
     */
    List<Song> scanMusicFolder();

    /**
     * 获取歌曲详情(含歌词内容)
     */
    Song getSongDetail(Long songId);

    /**
     * 搜索歌曲
     */
    List<Song> searchSongs(String keyword);

    /**
     * 记录播放
     */
    void recordPlay(Long songId, String username, Integer playedSeconds);

    /**
     * 播放统计 - 总播放次数
     */
    Map<String, Object> getPlayStats();

    /**
     * 获取最近播放记录
     */
    List<PlayRecord> getRecentPlays(int limit);

    /**
     * 获取热门歌曲排行
     */
    List<Map<String, Object>> getTopSongs(int limit);

    /**
     * 获取音乐文件夹路径
     */
    String getMusicFolder();
}

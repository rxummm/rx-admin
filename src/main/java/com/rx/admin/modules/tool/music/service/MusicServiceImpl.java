package com.rx.admin.modules.tool.music.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mpatric.mp3agic.*;
import com.rx.admin.modules.tool.music.entity.PlayRecord;
import com.rx.admin.modules.tool.music.entity.Song;
import com.rx.admin.modules.tool.music.mapper.PlayRecordMapper;
import com.rx.admin.modules.tool.music.mapper.SongMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("null")
public class MusicServiceImpl extends ServiceImpl<SongMapper, Song> implements MusicService {

    @Value("${music.folder:C:\\Users\\admin\\Downloads\\music}")
    private String musicFolder;

    private final PlayRecordMapper playRecordMapper;

    public MusicServiceImpl(PlayRecordMapper playRecordMapper) {
        this.playRecordMapper = playRecordMapper;
    }

    @Override
    public List<Song> scanMusicFolder() {
        Path folder = Paths.get(musicFolder);
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            return List.of();
        }

        // 扫描目录下所有 .mp3 文件
        Map<String, Path> mp3Files = new LinkedHashMap<>(); // baseName -> mp3Path
        Map<String, Path> lrcFiles = new LinkedHashMap<>();  // baseName -> lrcPath

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                String baseName = name.contains(".")
                        ? name.substring(0, name.lastIndexOf('.'))
                        : name;
                String ext = name.contains(".")
                        ? name.substring(name.lastIndexOf('.') + 1).toLowerCase()
                        : "";
                if ("mp3".equals(ext)) {
                    mp3Files.put(baseName, entry);
                } else if ("lrc".equals(ext)) {
                    lrcFiles.put(baseName, entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("扫描音乐文件夹失败：" + e.getMessage(), e);
        }

        // 查询已存在的歌曲(通过mp3Path匹配)
        List<Song> existingSongs = list();
        Set<String> existingPaths = existingSongs.stream()
                .map(Song::getMp3Path)
                .collect(Collectors.toSet());

        List<Song> result = new ArrayList<>();

        for (Map.Entry<String, Path> entry : mp3Files.entrySet()) {
            String baseName = entry.getKey();
            Path mp3Path = entry.getValue();
            String mp3PathStr = mp3Path.toAbsolutePath().toString();

            // 查找同名的LRC文件
            Path lrcPath = lrcFiles.get(baseName);
            String lrcPathStr = lrcPath != null ? lrcPath.toAbsolutePath().toString() : null;
            String lrcContent = null;
            if (lrcPath != null) {
                try {
                    lrcContent = Files.readString(lrcPath);
                } catch (IOException e) {
                    log.warn("读取歌词文件失败: {}", e.getMessage());
                }
            }

            try {
                long fileSize = Files.size(mp3Path);

                // 从 MP3 ID3 标签提取元数据
                Mp3Metadata meta = extractMp3Metadata(mp3PathStr, baseName);

                if (existingPaths.contains(mp3PathStr)) {
                    // 已存在，更新信息
                    Song exist = existingSongs.stream()
                            .filter(s -> mp3PathStr.equals(s.getMp3Path()))
                            .findFirst().orElse(null);
                    if (exist != null) {
                        exist.setTitle(meta.title);
                        exist.setArtist(meta.artist);
                        exist.setAlbum(meta.album);
                        exist.setDuration(meta.duration);
                        exist.setLrcPath(lrcPathStr);
                        exist.setLrcContent(lrcContent);
                        exist.setFileSize(fileSize);
                        updateById(exist);
                        result.add(exist);
                    }
                } else {
                    // 新歌曲
                    Song song = new Song();
                    song.setTitle(meta.title);
                    song.setArtist(meta.artist);
                    song.setAlbum(meta.album);
                    song.setMp3Path(mp3PathStr);
                    song.setLrcPath(lrcPathStr);
                    song.setLrcContent(lrcContent);
                    song.setFileSize(fileSize);
                    song.setDuration(meta.duration);
                    song.setPlayCount(0);
                    save(song);
                    result.add(song);
                }
            } catch (IOException e) {
                log.warn("跳过无法读取的歌曲文件: {}", e.getMessage());
            }
        }

        // 标记已删除的文件(路径不存在的歌曲标记为失效)
        for (Song existing : existingSongs) {
            if (!mp3Files.containsKey(
                    existing.getTitle() != null
                            ? existing.getTitle()
                            : getBaseName(existing.getMp3Path()))) {
                // 文件已不存在，可以删除或标记
            }
        }

        // 返回按标题排序的结果
        result.sort(Comparator.comparing(s -> s.getTitle() != null ? s.getTitle() : ""));
        return result;
    }

    @Override
    public Song getSongDetail(Long songId) {
        Song song = getById(songId);
        if (song == null) return null;
        // 确保歌词内容已加载
        if ((song.getLrcContent() == null || song.getLrcContent().isEmpty())
                && song.getLrcPath() != null) {
            try {
                song.setLrcContent(Files.readString(Path.of(song.getLrcPath())));
            } catch (IOException e) {
                log.warn("加载歌词内容失败: {}", e.getMessage());
            }
        }
        return song;
    }

    @Override
    public List<Song> searchSongs(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return list(new LambdaQueryWrapper<Song>().orderByAsc(Song::getTitle));
        }
        return list(new LambdaQueryWrapper<Song>()
                .like(Song::getTitle, keyword)
                .or()
                .like(Song::getArtist, keyword)
                .orderByAsc(Song::getTitle));
    }

    @Override
    public void recordPlay(Long songId, String username, Integer playedSeconds) {
        // 更新歌曲播放次数
        Song song = getById(songId);
        if (song != null) {
            song.setPlayCount((song.getPlayCount() == null ? 0 : song.getPlayCount()) + 1);
            updateById(song);
        }

        // 记录播放历史
        PlayRecord record = new PlayRecord();
        record.setSongId(songId);
        record.setSongTitle(song != null ? song.getTitle() : "");
        record.setUsername(username != null ? username : "anonymous");
        record.setPlayedSeconds(playedSeconds != null ? playedSeconds : 0);
        playRecordMapper.insert(record);
    }

    @Override
    public Map<String, Object> getPlayStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 总歌曲数
        long totalSongs = count();
        stats.put("totalSongs", totalSongs);

        // 总播放次数
        Long totalPlays = playRecordMapper.selectCount(null);
        stats.put("totalPlays", totalPlays);

        // 今日播放次数
        LambdaQueryWrapper<PlayRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(PlayRecord::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        Long todayPlays = playRecordMapper.selectCount(todayWrapper);
        stats.put("todayPlays", todayPlays);

        return stats;
    }

    @Override
    public List<PlayRecord> getRecentPlays(int limit) {
        return playRecordMapper.selectList(
                new LambdaQueryWrapper<PlayRecord>()
                        .orderByDesc(PlayRecord::getCreateTime)
                        .last("LIMIT " + limit));
    }

    @Override
    public List<Map<String, Object>> getTopSongs(int limit) {
        return list(new LambdaQueryWrapper<Song>()
                        .orderByDesc(Song::getPlayCount)
                        .last("LIMIT " + limit))
                .stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("title", s.getTitle());
                    m.put("artist", s.getArtist());
                    m.put("playCount", s.getPlayCount());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getMusicFolder() {
        return musicFolder;
    }

    private String getBaseName(String path) {
        if (path == null) return "";
        String name = Path.of(path).getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    /**
     * 从 MP3 文件的 ID3 标签中提取元数据（艺人、专辑、时长等）
     * 若标签中读不到歌手，则从文件名中解析（格式: 歌名 - 歌手.mp3）
     */
    private Mp3Metadata extractMp3Metadata(String mp3Path, String fallbackTitle) {
        String title = fallbackTitle;
        String artist = "";
        String album = "";
        int duration = 0;

        try {
            Mp3File mp3 = new Mp3File(mp3Path);

            // 时长（秒）
            duration = (int) mp3.getLengthInSeconds();

            // 优先读取 ID3v2 标签
            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                title = notBlank(tag.getTitle()) ? decodeId3Text(tag.getTitle()) : title;
                artist = notBlank(tag.getArtist()) ? decodeId3Text(tag.getArtist()) : "";
                album = notBlank(tag.getAlbum()) ? decodeId3Text(tag.getAlbum()) : "";
            }
            // 兜底读取 ID3v1 标签
            if ((artist.isEmpty() || album.isEmpty()) && mp3.hasId3v1Tag()) {
                ID3v1 tag = mp3.getId3v1Tag();
                if (title.equals(fallbackTitle) && notBlank(tag.getTitle())) {
                    title = decodeId3Text(tag.getTitle());
                }
                if (artist.isEmpty() && notBlank(tag.getArtist())) {
                    artist = decodeId3Text(tag.getArtist());
                }
                if (album.isEmpty() && notBlank(tag.getAlbum())) {
                    album = decodeId3Text(tag.getAlbum());
                }
            }

            log.debug("MP3元数据提取: path={}, title={}, artist={}, album={}, duration={}s",
                    mp3Path, title, artist, album, duration);
        } catch (Exception e) {
            log.warn("MP3元数据提取失败: {}, 原因: {}", mp3Path, e.getMessage());
        }

        // 若从ID3标签读不到歌手，尝试从文件名解析
        if (artist.isEmpty()) {
            artist = parseArtistFromFilename(mp3Path);
        }

        return new Mp3Metadata(title, artist, album, duration);
    }

    /**
     * 从 MP3 文件名中解析歌手名
     * 文件名格式: "歌名 - 歌手名.mp3" 或 "歌名-歌手名.mp3"
     * 取最后一个短横线之后、扩展名之前的部分作为歌手名
     */
    private String parseArtistFromFilename(String mp3Path) {
        if (mp3Path == null) return "";
        String filename = Path.of(mp3Path).getFileName().toString();
        // 去除扩展名
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx > 0) {
            filename = filename.substring(0, dotIdx);
        }
        // 按最后一个 " - " 或 "-" 分割
        String[] separators = {" - ", "-"};
        for (String sep : separators) {
            int idx = filename.lastIndexOf(sep);
            if (idx > 0 && idx + sep.length() < filename.length()) {
                String candidate = filename.substring(idx + sep.length()).trim();
                if (!candidate.isEmpty()) {
                    log.debug("从文件名解析歌手: file={}, artist={}", mp3Path, candidate);
                    return candidate;
                }
            }
        }
        return "";
    }

    /**
     * 处理 ID3 标签中可能的乱码问题
     * mp3agic 返回的字符串可能因编码问题而显示异常
     */
    private String decodeId3Text(String text) {
        if (text == null) return "";
        // 尝试修复常见的编码问题：Latin-1 误读为 UTF-8 等
        // 清理掉控制字符和不可打印字符
        return text.replace('\u0000', ' ').trim();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** MP3 元数据内部 VO */
    private record Mp3Metadata(String title, String artist, String album, int duration) {}
}

package com.rx.admin.modules.tool.music.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SongVO {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private String mp3Path;
    private String lrcPath;
    private String lrcContent;
    private Long fileSize;
    private Integer duration;
    private Integer playCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
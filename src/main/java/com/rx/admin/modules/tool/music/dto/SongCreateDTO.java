package com.rx.admin.modules.tool.music.dto;

import lombok.Data;

@Data
public class SongCreateDTO {
    private String title;
    private String artist;
    private String album;
    private String mp3Path;
    private String lrcPath;
    private String lrcContent;
    private Long fileSize;
    private Integer duration;
}
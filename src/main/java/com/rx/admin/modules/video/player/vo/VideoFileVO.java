package com.rx.admin.modules.video.player.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoFileVO {
    private Long id;
    private String title;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Integer duration;
    private Integer width;
    private Integer height;
    private String videoType;
    private Integer playCount;
    private LocalDateTime lastPlayTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

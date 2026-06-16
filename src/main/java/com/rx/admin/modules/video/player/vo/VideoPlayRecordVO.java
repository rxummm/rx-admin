package com.rx.admin.modules.video.player.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoPlayRecordVO {
    private Long id;
    private Long videoId;
    private String videoTitle;
    private String username;
    private Integer playedSeconds;
    private LocalDateTime createTime;
}

package com.rx.admin.modules.tool.music.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlayRecordVO {
    private Long id;
    private Long songId;
    private String songTitle;
    private String username;
    private Integer playedSeconds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
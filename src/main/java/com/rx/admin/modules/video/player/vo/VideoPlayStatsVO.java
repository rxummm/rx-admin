package com.rx.admin.modules.video.player.vo;

import lombok.Data;

@Data
public class VideoPlayStatsVO {
    private Long totalVideos;
    private Long totalPlays;
    private Long todayPlays;
}

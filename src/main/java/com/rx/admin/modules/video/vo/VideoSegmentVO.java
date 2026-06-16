package com.rx.admin.modules.video.vo;

import lombok.Data;

@Data
public class VideoSegmentVO {
    private Long id;
    private Double startTime;
    private Double endTime;
    private String text;
    private String speakerLabel;
    private String speakerName;
    private Float confidence;
}
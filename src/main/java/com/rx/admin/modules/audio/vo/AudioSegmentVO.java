package com.rx.admin.modules.audio.vo;

import lombok.Data;

@Data
public class AudioSegmentVO {
    private Long id;
    private Double startTime;
    private Double endTime;
    private String text;
    private Float confidence;
}
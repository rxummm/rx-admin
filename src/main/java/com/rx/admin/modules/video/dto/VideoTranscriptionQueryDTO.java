package com.rx.admin.modules.video.dto;

import lombok.Data;

@Data
public class VideoTranscriptionQueryDTO {
    private String keyword;
    private String language;
    private Integer status;
    private int page = 1;
    private int size = 10;
}
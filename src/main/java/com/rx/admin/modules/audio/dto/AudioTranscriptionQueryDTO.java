package com.rx.admin.modules.audio.dto;

import lombok.Data;

@Data
public class AudioTranscriptionQueryDTO {
    private String keyword;
    private String language;
    private Integer status;
    private int page = 1;
    private int size = 10;
}

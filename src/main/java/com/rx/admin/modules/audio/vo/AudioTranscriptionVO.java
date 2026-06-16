package com.rx.admin.modules.audio.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AudioTranscriptionVO {
    private Long id;
    private String fileName;
    private String filePath;
    private String language;
    private String fullText;
    private Double duration;
    private String modelName;
    private Float accuracy;
    private Integer status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private List<AudioSegmentVO> segments;
}

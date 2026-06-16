package com.rx.admin.modules.video.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VideoTranscriptionVO {
    private Long id;
    private String fileName;
    private String filePath;
    private String audioPath;
    private String language;
    private String fullText;
    private Double duration;
    private String modelName;
    private Integer speakerCount;
    private Integer status;
    private String srtPath;
    private String assPath;
    private String errorMessage;
    private LocalDateTime createdAt;
    private List<VideoSegmentVO> segments;
}
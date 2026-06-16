package com.rx.admin.modules.ocr.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OcrRecognitionVO {

    private Long id;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String language;

    private String ocrEngine;

    private String resultText;

    private Integer pageCount;

    private Integer charCount;

    private Float confidence;

    private Integer status;

    private String errorMessage;

    private Long durationMs;

    private LocalDateTime createTime;
}

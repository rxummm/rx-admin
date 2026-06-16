package com.rx.admin.modules.ocr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ocr_recognition")
public class OcrRecognition extends BaseEntity {

    private String fileName;

    private String filePath;

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
}

package com.rx.admin.modules.ocr.dto;

import lombok.Data;

@Data
public class OcrQueryDTO {

    private String keyword;

    private String fileType;

    private Integer status;

    private Integer page = 1;

    private Integer size = 10;
}

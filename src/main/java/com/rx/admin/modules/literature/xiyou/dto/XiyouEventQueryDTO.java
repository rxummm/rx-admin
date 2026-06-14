package com.rx.admin.modules.literature.xiyou.dto;

import lombok.Data;

@Data
public class XiyouEventQueryDTO {
    private String keyword;
    private String eventType;
    private Integer difficultyLevel;
}
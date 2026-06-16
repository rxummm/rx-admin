package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class ContentCategoryCreateDTO {

    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}
package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class GenreUpdateDTO {

    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}
package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class DynastyUpdateDTO {

    private Long id;
    private String name;
    private String code;
    private String regionType;
    private Integer startYear;
    private Integer endYear;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
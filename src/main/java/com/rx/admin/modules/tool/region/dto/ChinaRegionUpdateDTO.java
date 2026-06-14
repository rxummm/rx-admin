package com.rx.admin.modules.tool.region.dto;

import lombok.Data;

@Data
public class ChinaRegionUpdateDTO {
    private Long id;
    private String code;
    private String name;
    private Integer level;
    private String parentCode;
    private String pinyin;
    private String abbreviation;
    private java.math.BigDecimal longitude;
    private java.math.BigDecimal latitude;
    private Integer sort;
    private Integer status;
}
package com.rx.admin.modules.tool.region.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChinaRegionVO {
    private Long id;
    private String code;
    private String name;
    private Integer level;
    private String parentCode;
    private String pinyin;
    private String abbreviation;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
package com.rx.admin.modules.literature.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DynastyVO {

    private Long id;
    private String name;
    private String code;
    private String regionType;
    private Integer startYear;
    private Integer endYear;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
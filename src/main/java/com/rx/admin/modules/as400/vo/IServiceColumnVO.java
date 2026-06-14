package com.rx.admin.modules.as400.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IServiceColumnVO {
    private Long id;
    private Long serviceId;
    private String columnName;
    private String systemColumnName;
    private String dataType;
    private Integer isNullable;
    private String description;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
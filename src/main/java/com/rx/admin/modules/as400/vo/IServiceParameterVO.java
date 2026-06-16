package com.rx.admin.modules.as400.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IServiceParameterVO {
    private Long id;
    private Long serviceId;
    private String paramName;
    private String paramType;
    private String paramDirection;
    private Integer isRequired;
    private String defaultValue;
    private String description;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
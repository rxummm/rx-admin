package com.rx.admin.modules.as400.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("i_service_parameter")
public class IServiceParameter extends BaseEntity {
    private Long serviceId;
    private String paramName;
    private String paramType;
    private String paramDirection;
    private Integer isRequired;
    private String defaultValue;
    private String description;
    private Integer sort;
}

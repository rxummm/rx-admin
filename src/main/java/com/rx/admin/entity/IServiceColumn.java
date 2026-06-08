package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("i_service_column")
public class IServiceColumn extends BaseEntity {
    private Long serviceId;
    private String columnName;
    private String systemColumnName;
    private String dataType;
    private Integer isNullable;
    private String description;
    private Integer sort;
}

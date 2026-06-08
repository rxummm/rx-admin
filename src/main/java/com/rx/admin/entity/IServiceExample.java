package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("i_service_example")
public class IServiceExample extends BaseEntity {
    private Long serviceId;
    private String title;
    private String description;
    private String sqlCode;
    private Integer sort;
}

package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("i_service_authority")
public class IServiceAuthority extends BaseEntity {
    private Long serviceId;
    private String authority;
    private String context;
    private Integer sort;
}

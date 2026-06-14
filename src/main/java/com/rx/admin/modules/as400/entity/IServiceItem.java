package com.rx.admin.modules.as400.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("i_service_item")
public class IServiceItem extends BaseEntity {
    private Long categoryId;
    private String serviceName;
    private String systemObjectName;
    private String serviceType;
    private String briefDescription;
    private String fullDescription;
    private String docUrl;
    private String earliestPossibleRelease;
    private Integer initialDb2GroupLevel;
    private Integer latestDb2GroupLevel;
    private Integer sort;

    // 关联子表
    @TableField(exist = false)
    private List<IServiceParameter> parameters;
    @TableField(exist = false)
    private List<IServiceColumn> columns;
    @TableField(exist = false)
    private List<IServiceExample> examples;
    @TableField(exist = false)
    private List<IServiceAuthority> authorities;
}

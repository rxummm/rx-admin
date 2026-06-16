package com.rx.admin.modules.as400.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("i_service_category")
public class IServiceCategory extends BaseEntity {
    private Long parentId;
    private String name;
    private String code;
    private String description;
    private String docUrl;
    private Integer sort;

    @TableField(exist = false)
    private List<IServiceItem> items;
}

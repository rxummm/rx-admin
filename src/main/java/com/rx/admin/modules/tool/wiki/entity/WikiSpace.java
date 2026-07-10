package com.rx.admin.modules.tool.wiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wiki_space")
public class WikiSpace extends BaseEntity {
    private String name;
    private String description;
    private String icon;
    private String visibility;
    private Long ownerId;
}

package com.rx.admin.modules.tool.kanban.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("kanban_board")
public class KanbanBoard extends BaseEntity {
    private String name;
    private String description;
    private Long ownerId;
    private Integer status;
}

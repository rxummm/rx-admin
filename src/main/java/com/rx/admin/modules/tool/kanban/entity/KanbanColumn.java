package com.rx.admin.modules.tool.kanban.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("kanban_column")
public class KanbanColumn extends BaseEntity {
    private Long boardId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Integer wipLimit;
}

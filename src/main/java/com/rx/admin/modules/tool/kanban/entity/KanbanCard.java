package com.rx.admin.modules.tool.kanban.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("kanban_card")
public class KanbanCard extends BaseEntity {
    private Long boardId;
    private Long columnId;
    private String title;
    private String description;
    private String priority;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate dueDate;
    private String tags;
    private Integer sortOrder;
}

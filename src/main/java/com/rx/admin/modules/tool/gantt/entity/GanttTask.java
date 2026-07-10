package com.rx.admin.modules.tool.gantt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("gantt_task")
public class GanttTask extends BaseEntity {
    private Long projectId;
    private Long parentId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
    private String status;
    private Long assigneeId;
    private String assigneeName;
    private String priority;
    private Integer sortOrder;
}

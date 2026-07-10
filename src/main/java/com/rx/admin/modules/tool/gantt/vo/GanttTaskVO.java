package com.rx.admin.modules.tool.gantt.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GanttTaskVO {
    private Long id;
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
    private LocalDateTime createTime;
}

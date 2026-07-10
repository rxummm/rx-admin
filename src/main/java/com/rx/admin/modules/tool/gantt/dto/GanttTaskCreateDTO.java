package com.rx.admin.modules.tool.gantt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GanttTaskCreateDTO {
    @NotNull(message = "项目ID不能为空")
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
}

package com.rx.admin.modules.tool.kanban.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class KanbanCardCreateDTO {
    @NotNull(message = "看板ID不能为空")
    private Long boardId;
    @NotNull(message = "列ID不能为空")
    private Long columnId;
    private String title;
    private String description;
    private String priority;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate dueDate;
    private String tags;
}

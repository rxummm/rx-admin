package com.rx.admin.modules.tool.kanban.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class KanbanCardVO {
    private Long id;
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
    private LocalDateTime createTime;
}

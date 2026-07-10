package com.rx.admin.modules.tool.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KanbanBoardCreateDTO {
    @NotBlank(message = "看板名称不能为空")
    private String name;
    private String description;
}

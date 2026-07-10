package com.rx.admin.modules.tool.gantt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GanttProjectCreateDTO {
    @NotBlank(message = "项目名称不能为空")
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}

package com.rx.admin.modules.workflow.definition.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WfProcessDefinitionUpdateDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    private String name;
    private String description;
    private String category;
    private String formConfig;
    private String processConfig;
    private Integer status;
}

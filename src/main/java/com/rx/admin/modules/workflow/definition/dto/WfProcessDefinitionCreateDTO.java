package com.rx.admin.modules.workflow.definition.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WfProcessDefinitionCreateDTO {
    @NotBlank(message = "流程名称不能为空")
    private String name;
    @NotBlank(message = "流程编码不能为空")
    private String code;
    private String description;
    private String category;
    private String formConfig;
    private String processConfig;
    private Integer status;
}

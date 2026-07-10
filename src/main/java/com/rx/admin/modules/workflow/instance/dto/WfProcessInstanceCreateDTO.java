package com.rx.admin.modules.workflow.instance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WfProcessInstanceCreateDTO {
    @NotNull(message = "流程定义ID不能为空")
    private Long definitionId;
    private String title;
    private String businessKey;
    private String businessType;
    private String formData;
}

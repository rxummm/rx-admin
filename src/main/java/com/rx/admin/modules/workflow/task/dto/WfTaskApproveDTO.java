package com.rx.admin.modules.workflow.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WfTaskApproveDTO {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    private String comment;
    private String action;
}

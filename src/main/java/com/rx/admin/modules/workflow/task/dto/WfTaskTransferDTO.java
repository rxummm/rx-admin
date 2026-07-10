package com.rx.admin.modules.workflow.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WfTaskTransferDTO {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    @NotNull(message = "转办人ID不能为空")
    private Long targetUserId;
    private String targetUserName;
    private String comment;
}

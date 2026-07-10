package com.rx.admin.modules.workflow.task.dto;

import lombok.Data;

@Data
public class WfTaskQueryDTO {
    private Long assigneeId;
    private Long instanceId;
    private String status;
    private Integer page = 1;
    private Integer size = 10;
}

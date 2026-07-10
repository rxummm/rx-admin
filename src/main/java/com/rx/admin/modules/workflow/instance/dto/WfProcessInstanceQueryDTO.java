package com.rx.admin.modules.workflow.instance.dto;

import lombok.Data;

@Data
public class WfProcessInstanceQueryDTO {
    private String keyword;
    private Long initiatorId;
    private String status;
    private Integer page = 1;
    private Integer size = 10;
}

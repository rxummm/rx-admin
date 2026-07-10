package com.rx.admin.modules.workflow.definition.dto;

import lombok.Data;

@Data
public class WfProcessDefinitionQueryDTO {
    private String keyword;
    private String category;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}

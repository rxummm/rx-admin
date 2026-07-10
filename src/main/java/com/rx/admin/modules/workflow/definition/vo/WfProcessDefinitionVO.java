package com.rx.admin.modules.workflow.definition.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WfProcessDefinitionVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String category;
    private String formConfig;
    private String processConfig;
    private Integer status;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

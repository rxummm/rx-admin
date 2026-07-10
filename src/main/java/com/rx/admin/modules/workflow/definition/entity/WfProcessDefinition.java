package com.rx.admin.modules.workflow.definition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_definition")
public class WfProcessDefinition extends BaseEntity {
    private String name;
    private String code;
    private String description;
    private String category;
    private String formConfig;
    private String processConfig;
    private Integer status;
    private Integer version;
}

package com.rx.admin.modules.workflow.instance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_instance")
public class WfProcessInstance extends BaseEntity {
    private Long definitionId;
    private String title;
    private String businessKey;
    private String businessType;
    private Long initiatorId;
    private String initiatorName;
    private String currentNode;
    private String formData;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

package com.rx.admin.modules.workflow.instance.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WfProcessInstanceVO {
    private Long id;
    private Long definitionId;
    private String definitionName;
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
    private LocalDateTime createTime;
}

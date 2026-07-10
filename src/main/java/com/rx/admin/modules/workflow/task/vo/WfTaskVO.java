package com.rx.admin.modules.workflow.task.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WfTaskVO {
    private Long id;
    private Long instanceId;
    private String instanceTitle;
    private String nodeCode;
    private String nodeName;
    private String taskType;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private String comment;
    private LocalDateTime dueTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
}

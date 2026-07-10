package com.rx.admin.modules.workflow.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task")
public class WfTask extends BaseEntity {
    private Long instanceId;
    private String nodeCode;
    private String nodeName;
    private String taskType;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private String comment;
    private LocalDateTime dueTime;
    private LocalDateTime completeTime;
}

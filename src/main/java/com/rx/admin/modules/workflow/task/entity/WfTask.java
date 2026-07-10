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
    
    // 委托相关字段
    /** 委托人ID */
    private Long delegateId;
    /** 委托人姓名 */
    private String delegateName;
    /** 委托时间 */
    private LocalDateTime delegateTime;
    /** 委托原因 */
    private String delegateReason;
}

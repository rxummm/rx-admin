package com.rx.admin.modules.monitor.job.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 定时任务视图对象 */
@Data
public class JobVO {
    private Long id;
    private String jobName;
    private String beanName;
    private String methodName;
    private String cronExpression;
    private String params;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

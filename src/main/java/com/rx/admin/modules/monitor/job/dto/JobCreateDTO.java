package com.rx.admin.modules.monitor.job.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 定时任务创建请求 */
@Data
public class JobCreateDTO {
    @NotBlank(message = "任务名称不能为空")
    private String jobName;
    @NotBlank(message = "Bean名称不能为空")
    private String beanName;
    @NotBlank(message = "方法名不能为空")
    private String methodName;
    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;
    private String params;
    private Integer status;
    private String remark;
}

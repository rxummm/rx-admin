package com.rx.admin.modules.monitor.job.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job")
public class SysJob extends BaseEntity {
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
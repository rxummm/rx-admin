package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_job_log")
public class SysJobLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private String jobName;
    private String beanName;
    private String methodName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Integer status;
    private String resultMsg;
    private String errorMessage;
    private LocalDateTime createTime;
}

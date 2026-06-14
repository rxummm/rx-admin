package com.rx.admin.modules.monitor.joblog.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysJobLogVO {
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
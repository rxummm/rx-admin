package com.rx.admin.modules.monitor.job.dto;

import lombok.Data;

/** 定时任务查询参数 */
@Data
public class JobQueryDTO {
    private String jobName;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}

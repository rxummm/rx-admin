package com.rx.admin.modules.monitor.joblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.joblog.entity.SysJobLog;

public interface JobLogService extends IService<SysJobLog> {
    PageResult<SysJobLog> pageQuery(int page, int size, Long jobId, Integer status, String startTime, String endTime);
}

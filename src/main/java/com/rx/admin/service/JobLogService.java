package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysJobLog;

public interface JobLogService extends IService<SysJobLog> {
    PageResult<SysJobLog> pageQuery(int page, int size, Long jobId, Integer status, String startTime, String endTime);
}

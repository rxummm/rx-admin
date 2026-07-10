package com.rx.admin.modules.monitor.profiling.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.monitor.profiling.entity.SysProfileRecord;

import java.util.Map;

public interface SysProfileRecordService extends IService<SysProfileRecord> {
    void record(String className, String methodName, long executionTime, String params, String exception);
    Map<String, Object> getProfilingStats(String startDate);
}

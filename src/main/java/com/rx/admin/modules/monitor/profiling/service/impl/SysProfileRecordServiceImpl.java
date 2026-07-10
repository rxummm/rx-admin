package com.rx.admin.modules.monitor.profiling.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.monitor.profiling.entity.SysProfileRecord;
import com.rx.admin.modules.monitor.profiling.mapper.SysProfileRecordMapper;
import com.rx.admin.modules.monitor.profiling.service.SysProfileRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProfileRecordServiceImpl extends ServiceImpl<SysProfileRecordMapper, SysProfileRecord> implements SysProfileRecordService {

    @Override
    @Async
    public void record(String className, String methodName, long executionTime, String params, String exception) {
        try {
            SysProfileRecord record = new SysProfileRecord();
            record.setClassName(className);
            record.setMethodName(methodName);
            record.setExecutionTime(executionTime);
            record.setParams(params);
            record.setException(exception);
            record.setThreadName(Thread.currentThread().getName());
            save(record);
        } catch (Exception e) {
            log.debug("记录性能数据失败: {}.{} - {}ms", className, methodName, executionTime, e);
        }
    }

    @Override
    public Map<String, Object> getProfilingStats(String startDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("slowMethods", baseMapper.getSlowMethods());
        result.put("dailyStats", baseMapper.getDailyStats(startDate));
        return result;
    }
}

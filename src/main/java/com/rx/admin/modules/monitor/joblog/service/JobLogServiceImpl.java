package com.rx.admin.modules.monitor.joblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.joblog.entity.SysJobLog;
import com.rx.admin.modules.monitor.joblog.mapper.SysJobLogMapper;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@SuppressWarnings("null")
public class JobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements JobLogService {

    @Override
    public PageResult<SysJobLog> pageQuery(int page, int size, Long jobId, Integer status,
                                            String startTime, String endTime) {
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            wrapper.eq(SysJobLog::getJobId, jobId);
        }
        if (status != null) {
            wrapper.eq(SysJobLog::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SysJobLog::getStartTime, LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SysJobLog::getStartTime, LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(SysJobLog::getStartTime);
        Page<SysJobLog> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}

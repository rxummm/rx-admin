package com.rx.admin.modules.monitor.exportlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;
import com.rx.admin.modules.monitor.exportlog.mapper.SysExportLogMapper;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExportLogServiceImpl extends ServiceImpl<SysExportLogMapper, SysExportLog> implements ExportLogService {

    @Override
    public PageResult<SysExportLog> pageQuery(int page, int size, String username, String exportType,
                                               String startTime, String endTime) {
        LambdaQueryWrapper<SysExportLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysExportLog::getUsername, username);
        }
        if (exportType != null && !exportType.isEmpty()) {
            wrapper.eq(SysExportLog::getExportType, exportType);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SysExportLog::getCreateTime, LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SysExportLog::getCreateTime, LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(SysExportLog::getCreateTime);
        Page<SysExportLog> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }
}

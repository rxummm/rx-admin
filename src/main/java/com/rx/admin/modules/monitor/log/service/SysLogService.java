package com.rx.admin.modules.monitor.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

@Service
public class SysLogService extends ServiceImpl<SysLogMapper, SysLog> {

    public PageResult<SysLog> pageQuery(int page, int size, String keyword, Integer status, String startTime, String endTime) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysLog::getUsername, keyword)
                    .or().like(SysLog::getOperation, keyword)
                    .or().like(SysLog::getModule, keyword);
        }
        if (status != null) { wrapper.eq(SysLog::getStatus, status); }
        if (startTime != null && !startTime.isBlank()) { wrapper.ge(SysLog::getCreateTime, startTime); }
        if (endTime != null && !endTime.isBlank()) { wrapper.le(SysLog::getCreateTime, endTime); }
        wrapper.orderByDesc(SysLog::getCreateTime);

        IPage<SysLog> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }
}

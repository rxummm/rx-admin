package com.rx.admin.modules.content.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notify.entity.SysNotifyRecord;
import com.rx.admin.modules.content.notify.mapper.SysNotifyRecordMapper;

import org.springframework.stereotype.Service;

@Service
public class NotifyRecordServiceImpl extends ServiceImpl<SysNotifyRecordMapper, SysNotifyRecord>
        implements NotifyRecordService {

    @Override
    public PageResult<SysNotifyRecord> pageQuery(int page, int size, String channel, Integer status) {
        LambdaQueryWrapper<SysNotifyRecord> wrapper = new LambdaQueryWrapper<>();
        if (channel != null && !channel.isEmpty()) {
            wrapper.eq(SysNotifyRecord::getChannel, channel);
        }
        if (status != null) {
            wrapper.eq(SysNotifyRecord::getStatus, status);
        }
        wrapper.orderByDesc(SysNotifyRecord::getCreateTime);
        Page<SysNotifyRecord> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }
}

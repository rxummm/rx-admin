package com.rx.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysMessageTemplate;
import com.rx.admin.mapper.SysMessageTemplateMapper;
import com.rx.admin.service.MessageTemplateService;
import org.springframework.stereotype.Service;

@Service
public class MessageTemplateServiceImpl extends ServiceImpl<SysMessageTemplateMapper, SysMessageTemplate>
        implements MessageTemplateService {

    @Override
    public PageResult<SysMessageTemplate> pageQuery(int page, int size, String name) {
        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(SysMessageTemplate::getName, name);
        }
        wrapper.orderByDesc(SysMessageTemplate::getCreateTime);
        Page<SysMessageTemplate> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}

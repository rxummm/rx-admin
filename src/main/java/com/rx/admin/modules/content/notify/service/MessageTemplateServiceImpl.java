package com.rx.admin.modules.content.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notify.entity.SysMessageTemplate;
import com.rx.admin.modules.content.notify.mapper.SysMessageTemplateMapper;
import com.rx.admin.modules.content.notify.dto.MessageTemplateCreateDTO;
import com.rx.admin.modules.content.notify.dto.MessageTemplateUpdateDTO;

import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
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

    @Override
    public void addTemplate(MessageTemplateCreateDTO dto) {
        SysMessageTemplate template = new SysMessageTemplate();
        template.setName(dto.getName());
        template.setCode(dto.getCode());
        template.setTitleTemplate(dto.getTitleTemplate());
        template.setContentTemplate(dto.getContentTemplate());
        template.setChannels(dto.getChannels());
        template.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        save(template);
    }

    @Override
    public void updateTemplate(MessageTemplateUpdateDTO dto) {
        SysMessageTemplate template = new SysMessageTemplate();
        template.setId(dto.getId());
        template.setName(dto.getName());
        template.setCode(dto.getCode());
        template.setTitleTemplate(dto.getTitleTemplate());
        template.setContentTemplate(dto.getContentTemplate());
        template.setChannels(dto.getChannels());
        template.setStatus(dto.getStatus());
        updateById(template);
    }
}

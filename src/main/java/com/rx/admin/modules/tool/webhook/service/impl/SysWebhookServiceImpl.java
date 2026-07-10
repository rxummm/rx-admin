package com.rx.admin.modules.tool.webhook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.tool.webhook.dto.SysWebhookCreateDTO;
import com.rx.admin.modules.tool.webhook.dto.SysWebhookQueryDTO;
import com.rx.admin.modules.tool.webhook.entity.SysWebhook;
import com.rx.admin.modules.tool.webhook.mapper.SysWebhookMapper;
import com.rx.admin.modules.tool.webhook.service.SysWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysWebhookServiceImpl extends ServiceImpl<SysWebhookMapper, SysWebhook> implements SysWebhookService {

    @Override
    public PageResult<SysWebhook> queryPage(SysWebhookQueryDTO query) {
        LambdaQueryWrapper<SysWebhook> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            w.like(SysWebhook::getName, query.getKeyword());
        }
        if (query.getStatus() != null) {
            w.eq(SysWebhook::getStatus, query.getStatus());
        }
        w.orderByDesc(SysWebhook::getCreateTime);
        Page<SysWebhook> page = page(new Page<>(query.getPage(), query.getSize()), w);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEntity(SysWebhookCreateDTO dto) {
        SysWebhook entity = new SysWebhook();
        entity.setName(dto.getName());
        entity.setUrl(dto.getUrl());
        entity.setSecret(dto.getSecret());
        entity.setEvents(dto.getEvents());
        entity.setHeaders(dto.getHeaders());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setRetryCount(dto.getRetryCount() != null ? dto.getRetryCount() : 3);
        entity.setTimeoutMs(dto.getTimeoutMs() != null ? dto.getTimeoutMs() : 5000);
        entity.setDescription(dto.getDescription());
        save(entity);
    }
}

package com.rx.admin.modules.tool.emailTemplate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.tool.emailTemplate.dto.SysEmailTemplateCreateDTO;
import com.rx.admin.modules.tool.emailTemplate.entity.SysEmailTemplate;
import com.rx.admin.modules.tool.emailTemplate.mapper.SysEmailTemplateMapper;
import com.rx.admin.modules.tool.emailTemplate.service.SysEmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysEmailTemplateServiceImpl extends ServiceImpl<SysEmailTemplateMapper, SysEmailTemplate> implements SysEmailTemplateService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEntity(SysEmailTemplateCreateDTO dto) {
        SysEmailTemplate entity = new SysEmailTemplate();
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setSubject(dto.getSubject());
        entity.setBody(dto.getBody());
        entity.setVariables(dto.getVariables());
        entity.setCategory(dto.getCategory());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        save(entity);
    }
}

package com.rx.admin.modules.tool.emailTemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.tool.emailTemplate.dto.SysEmailTemplateCreateDTO;
import com.rx.admin.modules.tool.emailTemplate.entity.SysEmailTemplate;

public interface SysEmailTemplateService extends IService<SysEmailTemplate> {
    void addEntity(SysEmailTemplateCreateDTO dto);
}

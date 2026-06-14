package com.rx.admin.modules.content.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notify.entity.SysMessageTemplate;
import com.rx.admin.modules.content.notify.dto.MessageTemplateCreateDTO;
import com.rx.admin.modules.content.notify.dto.MessageTemplateUpdateDTO;

public interface MessageTemplateService extends IService<SysMessageTemplate> {
    PageResult<SysMessageTemplate> pageQuery(int page, int size, String name);

    /** 新增模板（从 DTO 收口） */
    void addTemplate(MessageTemplateCreateDTO dto);

    /** 更新模板（从 DTO 收口） */
    void updateTemplate(MessageTemplateUpdateDTO dto);
}

package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysMessageTemplate;

public interface MessageTemplateService extends IService<SysMessageTemplate> {
    PageResult<SysMessageTemplate> pageQuery(int page, int size, String name);
}

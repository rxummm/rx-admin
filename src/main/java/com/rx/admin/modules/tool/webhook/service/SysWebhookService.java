package com.rx.admin.modules.tool.webhook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.tool.webhook.dto.SysWebhookCreateDTO;
import com.rx.admin.modules.tool.webhook.dto.SysWebhookQueryDTO;
import com.rx.admin.modules.tool.webhook.entity.SysWebhook;

public interface SysWebhookService extends IService<SysWebhook> {
    PageResult<SysWebhook> queryPage(SysWebhookQueryDTO query);
    void addEntity(SysWebhookCreateDTO dto);
}

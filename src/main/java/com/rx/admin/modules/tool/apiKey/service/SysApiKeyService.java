package com.rx.admin.modules.tool.apiKey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.tool.apiKey.dto.SysApiKeyCreateDTO;
import com.rx.admin.modules.tool.apiKey.entity.SysApiKey;

import java.util.Map;

public interface SysApiKeyService extends IService<SysApiKey> {
    Map<String, String> generateApiKey(SysApiKeyCreateDTO dto, Long createdBy);
}

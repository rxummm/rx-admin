package com.rx.admin.modules.tool.apiKey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.tool.apiKey.dto.SysApiKeyCreateDTO;
import com.rx.admin.modules.tool.apiKey.entity.SysApiKey;
import com.rx.admin.modules.tool.apiKey.mapper.SysApiKeyMapper;
import com.rx.admin.modules.tool.apiKey.service.SysApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysApiKeyServiceImpl extends ServiceImpl<SysApiKeyMapper, SysApiKey> implements SysApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> generateApiKey(SysApiKeyCreateDTO dto, Long createdBy) {
        String apiKey = generateRandomString(32);
        String apiSecret = generateRandomString(64);

        SysApiKey entity = new SysApiKey();
        entity.setName(dto.getName());
        entity.setApiKey(apiKey);
        entity.setApiSecret(apiSecret);
        entity.setPermissions(dto.getPermissions());
        entity.setRateLimit(dto.getRateLimit() != null ? dto.getRateLimit() : 100);
        entity.setIpWhitelist(dto.getIpWhitelist());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(createdBy);
        entity.setStatus(1);
        entity.setUseCount(0L);
        save(entity);

        Map<String, String> result = new HashMap<>();
        result.put("id", String.valueOf(entity.getId()));
        result.put("apiKey", apiKey);
        result.put("apiSecret", apiSecret);
        log.info("API密钥生成: name={}, apiKey={}", dto.getName(), apiKey);
        return result;
    }

    private String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

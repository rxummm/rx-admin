package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.SysConfig;
import com.rx.admin.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    /**
     * 获取配置值（Caffeine 缓存，key 为 configKey）
     */
    @Cacheable(value = "config", key = "#key", unless = "#result == null")
    public String getValue(String key) {
        SysConfig config = lambdaQuery().eq(SysConfig::getConfigKey, key).one();
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 批量获取配置（前端根据 keys 获取，Caffeine 缓存）
     */
    @Cacheable(value = "config", key = "'values:' + #keys.hashCode()")
    public Map<String, String> getValues(List<String> keys) {
        List<SysConfig> list = lambdaQuery().in(SysConfig::getConfigKey, keys).list();
        Map<String, String> result = new HashMap<>();
        for (SysConfig c : list) {
            result.put(c.getConfigKey(), c.getConfigValue());
        }
        return result;
    }

    /**
     * 按分组获取配置（Caffeine 缓存，读多写少）
     */
    @Cacheable(value = "config", key = "'grouped'")
    public Map<String, List<SysConfig>> getGrouped() {
        List<SysConfig> list = list();
        Map<String, List<SysConfig>> grouped = new HashMap<>();
        for (SysConfig c : list) {
            grouped.computeIfAbsent(c.getGroupName(), k -> new java.util.ArrayList<>()).add(c);
        }
        return grouped;
    }

    /**
     * 更新配置值（调用方 Controller 负责清除缓存）
     */
    public void updateValue(String key, String value) {
        SysConfig config = lambdaQuery().eq(SysConfig::getConfigKey, key).one();
        if (config != null) {
            config.setConfigValue(value);
            updateById(config);
        }
    }

    /**
     * 检查验证码是否开启
     */
    public boolean isCaptchaEnabled() {
        return "true".equals(getValue("captcha.enabled"));
    }
}
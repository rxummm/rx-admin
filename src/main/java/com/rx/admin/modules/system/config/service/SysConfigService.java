package com.rx.admin.modules.system.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.system.config.entity.SysConfig;
import com.rx.admin.modules.system.config.mapper.SysConfigMapper;
import com.rx.admin.modules.system.config.dto.ConfigCreateDTO;
import com.rx.admin.modules.system.config.dto.ConfigUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

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
     * 新增配置
     */
    public void addConfig(ConfigCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, dto.getConfigKey()));
        if (count > 0) {
            throw new IllegalArgumentException("配置键已存在");
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType());
        config.setDescription(dto.getDescription());
        config.setGroupName(dto.getGroupName());
        config.setSortOrder(dto.getSortOrder());
        save(config);
    }

    /**
     * 更新配置
     */
    public void updateConfig(ConfigUpdateDTO dto) {
        SysConfig config = getById(dto.getId());
        if (config == null) {
            throw new IllegalArgumentException("配置不存在");
        }
        if (StringUtils.hasText(dto.getConfigKey())) config.setConfigKey(dto.getConfigKey());
        if (dto.getConfigValue() != null) config.setConfigValue(dto.getConfigValue());
        if (StringUtils.hasText(dto.getConfigType())) config.setConfigType(dto.getConfigType());
        if (StringUtils.hasText(dto.getDescription())) config.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getGroupName())) config.setGroupName(dto.getGroupName());
        if (dto.getSortOrder() != null) config.setSortOrder(dto.getSortOrder());
        updateById(config);
    }

    /**
     * 检查验证码是否开启
     */
    public boolean isCaptchaEnabled() {
        return "true".equals(getValue("captcha.enabled"));
    }
}

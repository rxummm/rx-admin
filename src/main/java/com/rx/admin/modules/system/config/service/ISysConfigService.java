package com.rx.admin.modules.system.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.system.config.entity.SysConfig;
import com.rx.admin.modules.system.config.dto.ConfigCreateDTO;
import com.rx.admin.modules.system.config.dto.ConfigUpdateDTO;

import java.util.List;
import java.util.Map;

public interface ISysConfigService extends IService<SysConfig> {

    String getValue(String key);

    Map<String, String> getValues(List<String> keys);

    Map<String, List<SysConfig>> getGrouped();

    void updateValue(String key, String value);

    void addConfig(ConfigCreateDTO dto);

    void updateConfig(ConfigUpdateDTO dto);

    boolean isCaptchaEnabled();

    void deleteConfigBatch(List<Long> ids);
}
package com.rx.admin.modules.system.i18n.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.system.i18n.dto.SysI18nKeyCreateDTO;
import com.rx.admin.modules.system.i18n.dto.SysI18nTranslationDTO;
import com.rx.admin.modules.system.i18n.entity.SysI18nKey;
import com.rx.admin.modules.system.i18n.entity.SysI18nLocale;

import java.util.List;
import java.util.Map;

public interface SysI18nService extends IService<SysI18nKey> {
    List<SysI18nLocale> listLocales();
    List<SysI18nKey> listKeys(String module);
    Map<String, String> getTranslations(String localeCode);
    void addKey(SysI18nKeyCreateDTO dto);
    void saveTranslation(SysI18nTranslationDTO dto);
}

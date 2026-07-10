package com.rx.admin.modules.system.i18n.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.system.i18n.dto.SysI18nKeyCreateDTO;
import com.rx.admin.modules.system.i18n.dto.SysI18nTranslationDTO;
import com.rx.admin.modules.system.i18n.entity.SysI18nKey;
import com.rx.admin.modules.system.i18n.entity.SysI18nLocale;
import com.rx.admin.modules.system.i18n.entity.SysI18nTranslation;
import com.rx.admin.modules.system.i18n.mapper.SysI18nKeyMapper;
import com.rx.admin.modules.system.i18n.mapper.SysI18nLocaleMapper;
import com.rx.admin.modules.system.i18n.mapper.SysI18nTranslationMapper;
import com.rx.admin.modules.system.i18n.service.SysI18nService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysI18nServiceImpl extends ServiceImpl<SysI18nKeyMapper, SysI18nKey> implements SysI18nService {

    private final SysI18nLocaleMapper localeMapper;
    private final SysI18nTranslationMapper translationMapper;

    @Override
    public List<SysI18nLocale> listLocales() {
        return localeMapper.selectList(null);
    }

    @Override
    public List<SysI18nKey> listKeys(String module) {
        LambdaQueryWrapper<SysI18nKey> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            w.eq(SysI18nKey::getModule, module);
        }
        w.orderByAsc(SysI18nKey::getKeyPath);
        return list(w);
    }

    @Override
    public Map<String, String> getTranslations(String localeCode) {
        Map<String, String> result = new HashMap<>();
        List<SysI18nKey> keys = list();
        for (SysI18nKey key : keys) {
            LambdaQueryWrapper<SysI18nTranslation> w = new LambdaQueryWrapper<>();
            w.eq(SysI18nTranslation::getKeyId, key.getId())
                    .eq(SysI18nTranslation::getLocaleCode, localeCode);
            SysI18nTranslation trans = translationMapper.selectOne(w);
            if (trans != null) {
                result.put(key.getKeyPath(), trans.getTranslation());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addKey(SysI18nKeyCreateDTO dto) {
        SysI18nKey key = new SysI18nKey();
        key.setKeyPath(dto.getKeyPath());
        key.setModule(dto.getModule());
        key.setDescription(dto.getDescription());
        save(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTranslation(SysI18nTranslationDTO dto) {
        LambdaQueryWrapper<SysI18nTranslation> w = new LambdaQueryWrapper<>();
        w.eq(SysI18nTranslation::getKeyId, dto.getKeyId())
                .eq(SysI18nTranslation::getLocaleCode, dto.getLocaleCode());
        SysI18nTranslation exist = translationMapper.selectOne(w);
        if (exist != null) {
            exist.setTranslation(dto.getTranslation());
            translationMapper.updateById(exist);
        } else {
            SysI18nTranslation trans = new SysI18nTranslation();
            trans.setKeyId(dto.getKeyId());
            trans.setLocaleCode(dto.getLocaleCode());
            trans.setTranslation(dto.getTranslation());
            translationMapper.insert(trans);
        }
    }
}

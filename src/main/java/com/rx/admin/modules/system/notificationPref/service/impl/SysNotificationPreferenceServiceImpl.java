package com.rx.admin.modules.system.notificationPref.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.system.notificationPref.dto.NotificationPrefUpdateDTO;
import com.rx.admin.modules.system.notificationPref.entity.SysNotificationPreference;
import com.rx.admin.modules.system.notificationPref.mapper.SysNotificationPreferenceMapper;
import com.rx.admin.modules.system.notificationPref.service.SysNotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysNotificationPreferenceServiceImpl extends ServiceImpl<SysNotificationPreferenceMapper, SysNotificationPreference> implements SysNotificationPreferenceService {

    @Override
    public List<SysNotificationPreference> getUserPreferences(Long userId) {
        LambdaQueryWrapper<SysNotificationPreference> w = new LambdaQueryWrapper<>();
        w.eq(SysNotificationPreference::getUserId, userId);
        return list(w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdatePreference(Long userId, NotificationPrefUpdateDTO dto) {
        LambdaQueryWrapper<SysNotificationPreference> w = new LambdaQueryWrapper<>();
        w.eq(SysNotificationPreference::getUserId, userId)
                .eq(SysNotificationPreference::getEventType, dto.getEventType());
        SysNotificationPreference exist = getOne(w, false);
        if (exist != null) {
            exist.setEmailEnabled(dto.getEmailEnabled());
            exist.setWebsocketEnabled(dto.getWebsocketEnabled());
            exist.setBrowserEnabled(dto.getBrowserEnabled());
            exist.setQuietStart(dto.getQuietStart());
            exist.setQuietEnd(dto.getQuietEnd());
            updateById(exist);
        } else {
            SysNotificationPreference pref = new SysNotificationPreference();
            pref.setUserId(userId);
            pref.setEventType(dto.getEventType());
            pref.setEmailEnabled(dto.getEmailEnabled() != null ? dto.getEmailEnabled() : 1);
            pref.setWebsocketEnabled(dto.getWebsocketEnabled() != null ? dto.getWebsocketEnabled() : 1);
            pref.setBrowserEnabled(dto.getBrowserEnabled() != null ? dto.getBrowserEnabled() : 1);
            pref.setQuietStart(dto.getQuietStart());
            pref.setQuietEnd(dto.getQuietEnd());
            save(pref);
        }
    }
}

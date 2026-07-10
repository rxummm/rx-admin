package com.rx.admin.modules.system.notificationPref.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.system.notificationPref.dto.NotificationPrefUpdateDTO;
import com.rx.admin.modules.system.notificationPref.entity.SysNotificationPreference;

import java.util.List;

public interface SysNotificationPreferenceService extends IService<SysNotificationPreference> {
    List<SysNotificationPreference> getUserPreferences(Long userId);
    void saveOrUpdatePreference(Long userId, NotificationPrefUpdateDTO dto);
}

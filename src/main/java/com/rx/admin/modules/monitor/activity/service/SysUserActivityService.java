package com.rx.admin.modules.monitor.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.monitor.activity.entity.SysUserActivity;

import java.time.LocalDate;
import java.util.Map;

public interface SysUserActivityService extends IService<SysUserActivity> {
    void recordActivity(Long userId, String activityType, String module, String detail, String ipAddress);
    Map<String, Object> getHeatmapData(LocalDate startDate, LocalDate endDate);
}

package com.rx.admin.modules.monitor.activity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.monitor.activity.entity.SysUserActivity;
import com.rx.admin.modules.monitor.activity.mapper.SysUserActivityMapper;
import com.rx.admin.modules.monitor.activity.service.SysUserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserActivityServiceImpl extends ServiceImpl<SysUserActivityMapper, SysUserActivity> implements SysUserActivityService {

    @Override
    @Async
    public void recordActivity(Long userId, String activityType, String module, String detail, String ipAddress) {
        try {
            SysUserActivity activity = new SysUserActivity();
            activity.setUserId(userId);
            activity.setActivityType(activityType);
            activity.setModule(module);
            activity.setDetail(detail);
            activity.setIpAddress(ipAddress);
            activity.setActivityDate(LocalDate.now());
            activity.setHour(LocalTime.now().getHour());
            save(activity);
        } catch (Exception e) {
            log.debug("记录用户活动失败: userId={}, type={}", userId, activityType, e);
        }
    }

    @Override
    public Map<String, Object> getHeatmapData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("heatmap", baseMapper.getHeatmapData(startDate, endDate));
        result.put("typeStats", baseMapper.getActivityTypeStats(startDate, endDate));
        result.put("topUsers", baseMapper.getTopUsers(startDate, endDate));
        return result;
    }
}

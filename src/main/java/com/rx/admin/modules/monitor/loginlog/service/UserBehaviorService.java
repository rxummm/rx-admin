package com.rx.admin.modules.monitor.loginlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户行为分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorService {

    private final SysLoginLogMapper loginLogMapper;

    /**
     * 获取用户登录频率统计
     */
    public Map<String, Object> getLoginFrequency(int days) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SysLoginLog::getLoginTime, since)
               .eq(SysLoginLog::getStatus, 1);
        List<SysLoginLog> logs = loginLogMapper.selectList(wrapper);

        // 按用户分组统计
        Map<String, Long> userLoginCount = logs.stream()
                .filter(l -> l.getUsername() != null)
                .collect(Collectors.groupingBy(SysLoginLog::getUsername, Collectors.counting()));

        result.put("totalLogins", logs.size());
        result.put("uniqueUsers", userLoginCount.size());
        result.put("topUsers", userLoginCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList()));

        return result;
    }

    /**
     * 获取活跃时段分布
     */
    public List<Map<String, Object>> getActiveTimeDistribution() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SysLoginLog::getLoginTime, since)
               .eq(SysLoginLog::getStatus, 1);
        List<SysLoginLog> logs = loginLogMapper.selectList(wrapper);

        // 按小时分组
        Map<Integer, Long> hourDistribution = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getLoginTime().getHour(), Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", hour);
            item.put("count", hourDistribution.getOrDefault(hour, 0L));
            result.add(item);
        }
        return result;
    }

    /**
     * 获取操作偏好统计
     */
    public Map<String, Object> getOperationPreference() {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(7);

        // 成功登录次数
        LambdaQueryWrapper<SysLoginLog> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.ge(SysLoginLog::getLoginTime, since)
                     .eq(SysLoginLog::getStatus, 1);
        long successCount = loginLogMapper.selectCount(successWrapper);
        result.put("successCount", successCount);

        // 失败登录次数
        LambdaQueryWrapper<SysLoginLog> failWrapper = new LambdaQueryWrapper<>();
        failWrapper.ge(SysLoginLog::getLoginTime, since)
                  .eq(SysLoginLog::getStatus, 0);
        long failCount = loginLogMapper.selectCount(failWrapper);
        result.put("failCount", failCount);

        // 成功率
        long total = successCount + failCount;
        result.put("successRate", total > 0 ? (double) successCount / total * 100 : 0);

        return result;
    }
}

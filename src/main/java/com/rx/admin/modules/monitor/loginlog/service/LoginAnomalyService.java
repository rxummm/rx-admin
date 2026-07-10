package com.rx.admin.modules.monitor.loginlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;
import com.rx.admin.modules.content.message.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录异常检测服务
 * 检测异常登录行为并发送告警
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAnomalyService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysMessageService sysMessageService;

    // 配置阈值
    private static final int FAILED_LOGIN_THRESHOLD = 5; // 同IP连续失败次数阈值
    private static final int FAILED_LOGIN_WINDOW_MINUTES = 30; // 时间窗口（分钟）
    private static final int UNUSUAL_HOUR_START = 0; // 异常时间段开始（凌晨）
    private static final int UNUSUAL_HOUR_END = 6; // 异常时间段结束（凌晨6点）
    private static final int ANOMALY_CHECK_WINDOW_MINUTES = 60; // 异常检测时间窗口

    /**
     * 定时检测登录异常（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000)
    public void detectAnomalies() {
        try {
            LocalDateTime windowStart = LocalDateTime.now().minusMinutes(ANOMALY_CHECK_WINDOW_MINUTES);
            
            // 1. 检测频繁失败登录
            detectFailedLogins(windowStart);
            
            // 2. 检测异常时间登录
            detectUnusualTimeLogins(windowStart);
            
        } catch (Exception e) {
            log.error("登录异常检测失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检测频繁失败登录
     */
    private void detectFailedLogins(LocalDateTime windowStart) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginLog::getStatus, 0) // 失败
               .ge(SysLoginLog::getLoginTime, windowStart)
               .orderByAsc(SysLoginLog::getIp);

        List<SysLoginLog> failedLogs = loginLogMapper.selectList(wrapper);
        
        // 按IP分组统计失败次数
        Map<String, List<SysLoginLog>> failedByIp = failedLogs.stream()
                .filter(log -> log.getIp() != null && !log.getIp().isEmpty())
                .collect(Collectors.groupingBy(SysLoginLog::getIp));

        for (Map.Entry<String, List<SysLoginLog>> entry : failedByIp.entrySet()) {
            String ip = entry.getKey();
            List<SysLoginLog> logs = entry.getValue();
            
            if (logs.size() >= FAILED_LOGIN_THRESHOLD) {
                // 获取涉及的用户名
                Set<String> usernames = logs.stream()
                        .map(SysLoginLog::getUsername)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                String message = String.format(
                    "⚠️ 登录异常告警：IP %s 在 %d 分钟内失败登录 %d 次，涉及用户: %s",
                    ip, ANOMALY_CHECK_WINDOW_MINUTES, logs.size(), String.join(", ", usernames)
                );
                
                log.warn(message);
                sendAlert("登录异常", message);
            }
        }
    }

    /**
     * 检测异常时间登录
     */
    private void detectUnusualTimeLogins(LocalDateTime windowStart) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginLog::getStatus, 1) // 成功
               .ge(SysLoginLog::getLoginTime, windowStart);

        List<SysLoginLog> successLogs = loginLogMapper.selectList(wrapper);
        
        // 筛选异常时间段的登录
        List<SysLoginLog> unusualLogins = successLogs.stream()
                .filter(log -> {
                    int hour = log.getLoginTime().getHour();
                    return hour >= UNUSUAL_HOUR_START && hour < UNUSUAL_HOUR_END;
                })
                .collect(Collectors.toList());

        if (!unusualLogins.isEmpty()) {
            // 按用户分组
            Map<String, List<SysLoginLog>> unusualByUser = unusualLogins.stream()
                    .filter(log -> log.getUsername() != null)
                    .collect(Collectors.groupingBy(SysLoginLog::getUsername));

            for (Map.Entry<String, List<SysLoginLog>> entry : unusualByUser.entrySet()) {
                String username = entry.getKey();
                List<SysLoginLog> logs = entry.getValue();
                
                Set<String> ips = logs.stream()
                        .map(SysLoginLog::getIp)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                String message = String.format(
                    "⚠️ 异常时间登录告警：用户 %s 在凌晨 %d:00-%d:00 有 %d 次登录，来源IP: %s",
                    username, UNUSUAL_HOUR_START, UNUSUAL_HOUR_END, logs.size(), String.join(", ", ips)
                );
                
                log.warn(message);
                sendAlert("异常时间登录", message);
            }
        }
    }

    /**
     * 获取登录异常统计
     */
    public Map<String, Object> getAnomalyStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);
        
        // 今日失败登录次数
        LambdaQueryWrapper<SysLoginLog> failWrapper = new LambdaQueryWrapper<>();
        failWrapper.eq(SysLoginLog::getStatus, 0)
                   .ge(SysLoginLog::getLoginTime, todayStart);
        long todayFailCount = loginLogMapper.selectCount(failWrapper);
        stats.put("todayFailCount", todayFailCount);
        
        // 今日成功登录次数
        LambdaQueryWrapper<SysLoginLog> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(SysLoginLog::getStatus, 1)
                     .ge(SysLoginLog::getLoginTime, todayStart);
        long todaySuccessCount = loginLogMapper.selectCount(successWrapper);
        stats.put("todaySuccessCount", todaySuccessCount);
        
        // 近7天异常时间段登录次数
        LambdaQueryWrapper<SysLoginLog> unusualWrapper = new LambdaQueryWrapper<>();
        unusualWrapper.eq(SysLoginLog::getStatus, 1)
                     .ge(SysLoginLog::getLoginTime, weekAgo);
        List<SysLoginLog> weekLogs = loginLogMapper.selectList(unusualWrapper);
        long unusualCount = weekLogs.stream()
                .filter(log -> {
                    int hour = log.getLoginTime().getHour();
                    return hour >= UNUSUAL_HOUR_START && hour < UNUSUAL_HOUR_END;
                })
                .count();
        stats.put("weekUnusualHourCount", unusualCount);
        
        // 高风险IP（失败次数>=3）
        LambdaQueryWrapper<SysLoginLog> ipFailWrapper = new LambdaQueryWrapper<>();
        ipFailWrapper.eq(SysLoginLog::getStatus, 0)
                    .ge(SysLoginLog::getLoginTime, weekAgo);
        List<SysLoginLog> weekFails = loginLogMapper.selectList(ipFailWrapper);
        Map<String, Long> failByIp = weekFails.stream()
                .filter(log -> log.getIp() != null)
                .collect(Collectors.groupingBy(SysLoginLog::getIp, Collectors.counting()));
        List<String> highRiskIps = failByIp.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        stats.put("highRiskIps", highRiskIps);
        stats.put("highRiskIpCount", highRiskIps.size());
        
        return stats;
    }

    /**
     * 获取按IP统计的失败登录
     */
    public List<Map<String, Object>> getFailedLoginsByIp(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginLog::getStatus, 0)
               .ge(SysLoginLog::getLoginTime, since)
               .orderByDesc(SysLoginLog::getLoginTime);
        
        List<SysLoginLog> logs = loginLogMapper.selectList(wrapper);
        
        return logs.stream()
                .filter(log -> log.getIp() != null)
                .collect(Collectors.groupingBy(
                        SysLoginLog::getIp,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> result = new LinkedHashMap<>();
                                    result.put("ip", list.get(0).getIp());
                                    result.put("count", list.size());
                                    result.put("usernames", list.stream()
                                            .map(SysLoginLog::getUsername)
                                            .filter(Objects::nonNull)
                                            .distinct()
                                            .collect(Collectors.toList()));
                                    result.put("lastAttempt", list.get(0).getLoginTime());
                                    return result;
                                }
                        )
                ))
                .values()
                .stream()
                .sorted((a, b) -> ((Long) b.get("count")).compareTo((Long) a.get("count")))
                .collect(Collectors.toList());
    }

    /**
     * 发送告警消息
     */
    private void sendAlert(String title, String content) {
        try {
            // 发送给管理员（userId=1）
            sysMessageService.sendSystemMessage(title, content, 1L);
        } catch (Exception e) {
            log.warn("发送登录异常告警失败: {}", e.getMessage());
        }
    }
}

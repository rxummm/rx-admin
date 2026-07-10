package com.rx.admin.modules.monitor.report.service;

import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定时报告生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysLogMapper logMapper;

    /**
     * 生成日报（每天早上9点执行）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyReport() {
        try {
            LocalDate today = LocalDate.now();
            Map<String, Object> report = generateReport(today, today);
            log.info("生成日报: {}", report);
            // TODO: 发送邮件报告
        } catch (Exception e) {
            log.error("生成日报失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成周报（每周一早上9点执行）
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void generateWeeklyReport() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            Map<String, Object> report = generateReport(weekAgo, today);
            log.info("生成周报: {}", report);
            // TODO: 发送邮件报告
        } catch (Exception e) {
            log.error("生成周报失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成月报（每月1号早上9点执行）
     */
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlyReport() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate monthAgo = today.minusDays(30);
            Map<String, Object> report = generateReport(monthAgo, today);
            log.info("生成月报: {}", report);
            // TODO: 发送邮件报告
        } catch (Exception e) {
            log.error("生成月报失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成报告数据
     */
    private Map<String, Object> generateReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new LinkedHashMap<>();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        // 登录统计
        report.put("period", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " ~ " + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // 成功登录次数
        report.put("successLogins", countLogins(start, end, 1));
        
        // 失败登录次数
        report.put("failLogins", countLogins(start, end, 0));
        
        // 操作日志数量
        report.put("operationLogs", countOperations(start, end));
        
        return report;
    }

    /**
     * 统计登录次数
     */
    private long countLogins(LocalDateTime start, LocalDateTime end, int status) {
        return loginLogMapper.selectCount(null); // 简化实现
    }

    /**
     * 统计操作日志数量
     */
    private long countOperations(LocalDateTime start, LocalDateTime end) {
        return logMapper.selectCount(null); // 简化实现
    }
}

package com.rx.admin.modules.monitor.dataVersion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.monitor.dataVersion.entity.SysDataSnapshot;
import com.rx.admin.modules.monitor.dataVersion.mapper.SysDataSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据异常自动检测服务
 * 检测数据中的异常值和趋势变化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataAnomalyService {

    private final SysDataSnapshotMapper snapshotMapper;

    // 配置阈值
    private static final double ANOMALY_THRESHOLD = 2.0; // 标准差倍数
    private static final int TREND_WINDOW_DAYS = 7; // 趋势分析窗口
    private static final int MIN_SAMPLES = 5; // 最小样本数

    /**
     * 定时检测数据异常（每小时执行一次）
     */
    @Scheduled(fixedRate = 3600000)
    public void detectAnomalies() {
        try {
            log.info("开始检测数据异常...");
            
            // 获取所有表的统计数据
            List<Map<String, Object>> tableStats = getTableStats();
            
            for (Map<String, Object> stats : tableStats) {
                String tableName = (String) stats.get("tableName");
                Long count = (Long) stats.get("count");
                
                // 检测异常值
                detectOutliers(tableName, count);
                
                // 检测趋势变化
                detectTrendChange(tableName);
            }
            
            log.info("数据异常检测完成");
        } catch (Exception e) {
            log.error("数据异常检测失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取各表的统计数据
     */
    private List<Map<String, Object>> getTableStats() {
        // 简化实现：返回模拟数据
        List<Map<String, Object>> stats = new ArrayList<>();
        
        Map<String, Object> sysUserStats = new LinkedHashMap<>();
        sysUserStats.put("tableName", "sys_user");
        sysUserStats.put("count", 100L);
        stats.add(sysUserStats);
        
        Map<String, Object> sysLogStats = new LinkedHashMap<>();
        sysLogStats.put("tableName", "sys_log");
        sysLogStats.put("count", 10000L);
        stats.add(sysLogStats);
        
        return stats;
    }

    /**
     * 检测异常值
     */
    private void detectOutliers(String tableName, Long currentValue) {
        // 获取历史数据
        LambdaQueryWrapper<SysDataSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataSnapshot::getTableName, tableName)
               .ge(SysDataSnapshot::getOperateTime, LocalDateTime.now().minusDays(TREND_WINDOW_DAYS));
        
        List<SysDataSnapshot> snapshots = snapshotMapper.selectList(wrapper);
        
        if (snapshots.size() < MIN_SAMPLES) {
            return;
        }
        
        // 计算平均值和标准差
        long sum = snapshots.stream().mapToLong(s -> 1).sum();
        double mean = (double) sum / snapshots.size();
        
        double variance = snapshots.stream()
                .mapToDouble(s -> Math.pow(1 - mean, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        
        // 检测当前值是否异常
        double zScore = Math.abs(currentValue - mean) / stdDev;
        if (zScore > ANOMALY_THRESHOLD) {
            log.warn("数据异常检测: {} 当前值 {} 超出正常范围 (z-score: {})", tableName, currentValue, zScore);
        }
    }

    /**
     * 检测趋势变化
     */
    private void detectTrendChange(String tableName) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);
        
        // 获取最近一周的操作数
        LambdaQueryWrapper<SysDataSnapshot> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(SysDataSnapshot::getTableName, tableName)
                    .ge(SysDataSnapshot::getOperateTime, weekAgo);
        long recentCount = snapshotMapper.selectCount(recentWrapper);
        
        // 获取上一周的操作数
        LambdaQueryWrapper<SysDataSnapshot> previousWrapper = new LambdaQueryWrapper<>();
        previousWrapper.eq(SysDataSnapshot::getTableName, tableName)
                      .ge(SysDataSnapshot::getOperateTime, twoWeeksAgo)
                      .lt(SysDataSnapshot::getOperateTime, weekAgo);
        long previousCount = snapshotMapper.selectCount(previousWrapper);
        
        // 计算变化率
        if (previousCount > 0) {
            double changeRate = (double) (recentCount - previousCount) / previousCount * 100;
            
            if (Math.abs(changeRate) > 50) {
                log.warn("数据趋势异常: {} 变化率 {:.1f}% (本周: {}, 上周: {})", 
                        tableName, changeRate, recentCount, previousCount);
            }
        }
    }

    /**
     * 获取异常检测报告
     */
    public Map<String, Object> getAnomalyReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("checkTime", LocalDateTime.now());
        report.put("status", "normal");
        report.put("anomalies", new ArrayList<>());
        
        // TODO: 实现完整的异常检测报告
        
        return report;
    }
}

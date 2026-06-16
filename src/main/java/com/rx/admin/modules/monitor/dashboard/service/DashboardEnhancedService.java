package com.rx.admin.modules.monitor.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;
import com.rx.admin.modules.monitor.exportlog.mapper.SysExportLogMapper;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.mapper.SysLogMapper;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DashboardEnhancedService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysExportLogMapper exportLogMapper;
    private final SysLogMapper sysLogMapper;

    public Map<String, Object> getLoginStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayLogins", loginLogMapper.selectCount(
            new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, todayStart)
                .eq(SysLoginLog::getStatus, 1)));
        data.put("todayFailLogins", loginLogMapper.selectCount(
            new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, todayStart)
                .eq(SysLoginLog::getStatus, 0)));
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        var trendQuery = new QueryWrapper<SysLoginLog>()
            .select("DATE(login_time) as date", "COUNT(*) as count")
            .ge("login_time", sevenDaysAgo)
            .eq("status", 1)
            .groupBy("DATE(login_time)")
            .orderByAsc("DATE(login_time)");
        List<Map<String, Object>> trendData = loginLogMapper.selectMaps(trendQuery);
        Map<String, Long> trendMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            trendMap.put(date, 0L);
        }
        for (Map<String, Object> row : trendData) {
            Object dateObj = row.get("date");
            Object countObj = row.get("count");
            if (dateObj != null && countObj != null) {
                String date = dateObj.toString();
                long count = ((Number) countObj).longValue();
                trendMap.put(date, count);
            }
        }
        data.put("trend", trendMap);
        return data;
    }

    public Map<String, Object> getExportStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayExports", exportLogMapper.selectCount(null));
        data.put("todayExcelExports", exportLogMapper.selectCount(
            new LambdaQueryWrapper<SysExportLog>()
                .ge(SysExportLog::getCreateTime, todayStart)
                .eq(SysExportLog::getExportType, "excel")));
        data.put("todayPdfExports", exportLogMapper.selectCount(
            new LambdaQueryWrapper<SysExportLog>()
                .ge(SysExportLog::getCreateTime, todayStart)
                .eq(SysExportLog::getExportType, "pdf")));
        return data;
    }

    public List<Map<String, Object>> getOperationTop10() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        var topQuery = new QueryWrapper<SysLog>()
            .select("operation", "COUNT(*) as count")
            .ge("create_time", todayStart)
            .groupBy("operation")
            .orderByDesc("count")
            .last("LIMIT 10");
        List<Map<String, Object>> rows = sysLogMapper.selectMaps(topQuery);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("operation", row.get("operation"));
            item.put("count", ((Number) row.get("count")).longValue());
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> computeEnhanced() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.putAll(getLoginStats());
        Map<String, Object> exportData = getExportStats();
        data.put("exportStats", exportData);
        data.put("operationTop10", getOperationTop10());
        return data;
    }
}

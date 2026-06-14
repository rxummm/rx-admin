package com.rx.admin.controller;

import com.rx.admin.common.result.Result;
import com.rx.admin.mapper.SysLoginLogMapper;
import com.rx.admin.mapper.SysExportLogMapper;

import com.rx.admin.mapper.SysLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "仪表盘增强")
@RestController
@RequestMapping("/api/dashboard/enhanced")
public class DashboardEnhancedController {

    private final SysLoginLogMapper loginLogMapper;
    private final SysExportLogMapper exportLogMapper;
    private final SysLogMapper sysLogMapper;

    public DashboardEnhancedController(SysLoginLogMapper loginLogMapper, SysExportLogMapper exportLogMapper,
                                        SysLogMapper sysLogMapper) {
        this.loginLogMapper = loginLogMapper;
        this.exportLogMapper = exportLogMapper;
        this.sysLogMapper = sysLogMapper;
    }

    @Operation(summary = "登录统计")
    @GetMapping("/login-stats")
    public Result<?> loginStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        // 今日成功登录数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayLogins", loginLogMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.rx.admin.entity.SysLoginLog>()
                .ge(com.rx.admin.entity.SysLoginLog::getLoginTime, todayStart)
                .eq(com.rx.admin.entity.SysLoginLog::getStatus, 1)));
        // 今日失败登录数
        data.put("todayFailLogins", loginLogMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.rx.admin.entity.SysLoginLog>()
                .ge(com.rx.admin.entity.SysLoginLog::getLoginTime, todayStart)
                .eq(com.rx.admin.entity.SysLoginLog::getStatus, 0)));
        // 最近7天每日登录趋势
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        var trendQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.rx.admin.entity.SysLoginLog>()
            .select("DATE(login_time) as date", "COUNT(*) as count")
            .ge("login_time", sevenDaysAgo)
            .eq("status", 1)
            .groupBy("DATE(login_time)")
            .orderByAsc("DATE(login_time)");
        List<Map<String, Object>> trendData = loginLogMapper.selectMaps(trendQuery);
        // 填充缺失日期
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
        return Result.ok(data);
    }

    @Operation(summary = "导出统计")
    @GetMapping("/export-stats")
    public Result<?> exportStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayExports", exportLogMapper.selectCount(null));
        data.put("todayExcelExports", exportLogMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.rx.admin.entity.SysExportLog>()
                .ge(com.rx.admin.entity.SysExportLog::getCreateTime, todayStart)
                .eq(com.rx.admin.entity.SysExportLog::getExportType, "excel")));
        data.put("todayPdfExports", exportLogMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.rx.admin.entity.SysExportLog>()
                .ge(com.rx.admin.entity.SysExportLog::getCreateTime, todayStart)
                .eq(com.rx.admin.entity.SysExportLog::getExportType, "pdf")));
        return Result.ok(data);
    }

    @Operation(summary = "操作日志Top10")
    @GetMapping("/operation-top10")
    public Result<?> operationTop10() {
        // 从sys_log中查询今天操作频次Top10
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        var topQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.rx.admin.entity.SysLog>()
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
        return Result.ok(result);
    }
}
package com.rx.admin.modules.monitor.logAnalysis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.mapper.SysLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "日志分析")
@RestController
@RequestMapping("/api/monitor/log-analysis")
@RequiredArgsConstructor
public class LogAnalysisController {

    private final SysLogMapper sysLogMapper;

    @Operation(summary = "获取日志分析摘要")
    @GetMapping("/summary")
    @SaCheckPermission("monitor:log-analysis:list")
    public Result<Map<String, Object>> summary() {
        List<SysLog> today = sysLogMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysLog>()
                .ge(SysLog::getCreateTime, LocalDate.now().atStartOfDay())
        );
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("totalToday", today.size());
        r.put("errorToday", today.stream().filter(l -> l.getStatus() != null && l.getStatus() == 0).count());
        r.put("activeUsers", today.stream().map(SysLog::getUsername).filter(Objects::nonNull).distinct().count());
        r.put("topOperations", today.stream().filter(l -> l.getOperation() != null)
                .collect(Collectors.groupingBy(SysLog::getOperation, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(5)
                .map(e -> Map.of("operation", e.getKey(), "count", e.getValue())).collect(Collectors.toList()));
        return Result.ok(r);
    }

    @Operation(summary = "获取小时级日志分布")
    @GetMapping("/hourly")
    @SaCheckPermission("monitor:log-analysis:list")
    public Result<List<Map<String, Object>>> hourly() {
        List<SysLog> today = sysLogMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysLog>()
                .ge(SysLog::getCreateTime, LocalDate.now().atStartOfDay())
        );
        Map<String, Long> hourMap = new LinkedHashMap<>();
        for (int i = 0; i < 24; i++) hourMap.put(String.format("%02d", i), 0L);
        today.forEach(l -> {
            if (l.getCreateTime() != null) {
                String h = String.format("%02d", l.getCreateTime().getHour());
                hourMap.merge(h, 1L, (a, b) -> a + b);
            }
        });
        return Result.ok(hourMap.entrySet().stream().<Map<String, Object>>map(
            e -> new HashMap<>(Map.of("hour", e.getKey(), "count", e.getValue()))).collect(Collectors.toList()));
    }

    @Operation(summary = "获取操作类型分布")
    @GetMapping("/type-distribution")
    @SaCheckPermission("monitor:log-analysis:list")
    public Result<List<Map<String, Object>>> typeDistribution() {
        List<SysLog> all = sysLogMapper.selectList(null);
        return Result.ok(all.stream().filter(l -> l.getOperation() != null)
                .collect(Collectors.groupingBy(SysLog::getOperation, Collectors.counting()))
                .entrySet().stream().<Map<String, Object>>map(
                    e -> new HashMap<>(Map.of("type", e.getKey(), "count", e.getValue())))
                .collect(Collectors.toList()));
    }

    @Operation(summary = "获取日志趋势")
    @GetMapping("/trend")
    @SaCheckPermission("monitor:log-analysis:list")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0 ; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            List<SysLog> dayLogs = sysLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysLog>()
                    .ge(SysLog::getCreateTime, date.atTime(LocalTime.MIN))
                    .le(SysLog::getCreateTime, date.atTime(LocalTime.MAX))
            );
            result.add(Map.of("date", date.toString(), "count", dayLogs.size(),
                    "errorCount", dayLogs.stream().filter(l -> l.getStatus() != null && l.getStatus() == 0).count()));
        }
        return Result.ok(result);
    }
}
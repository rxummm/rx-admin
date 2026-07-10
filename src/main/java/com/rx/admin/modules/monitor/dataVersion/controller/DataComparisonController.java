package com.rx.admin.modules.monitor.dataVersion.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.dataVersion.service.DataComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据对比分析控制器
 */
@Tag(name = "数据对比分析")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/data-comparison")
@RequiredArgsConstructor
public class DataComparisonController {

    private final DataComparisonService comparisonService;

    @Operation(summary = "对比两个时间点的数据差异")
    @GetMapping("/compare")
    public Result<Map<String, Object>> compareData(
            @RequestParam String tableName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.ok(comparisonService.compareData(tableName, startTime, endTime));
    }

    @Operation(summary = "获取数据变更趋势")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getChangeTrend(
            @RequestParam String tableName,
            @RequestParam(defaultValue = "7") int days) {
        return Result.ok(comparisonService.getChangeTrend(tableName, days));
    }
}

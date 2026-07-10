package com.rx.admin.modules.monitor.report.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 报告管理控制器
 */
@Tag(name = "报告管理")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "手动触发日报生成")
    @PostMapping("/daily")
    public Result<Void> triggerDailyReport() {
        reportService.generateDailyReport();
        return Result.ok();
    }

    @Operation(summary = "手动触发周报生成")
    @PostMapping("/weekly")
    public Result<Void> triggerWeeklyReport() {
        reportService.generateWeeklyReport();
        return Result.ok();
    }

    @Operation(summary = "手动触发月报生成")
    @PostMapping("/monthly")
    public Result<Void> triggerMonthlyReport() {
        reportService.generateMonthlyReport();
        return Result.ok();
    }
}

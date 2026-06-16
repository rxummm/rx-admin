package com.rx.admin.modules.monitor.dashboard.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.dashboard.service.DashboardEnhancedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仪表盘增强")
@RestController
@ApiVersion(1)
@RequestMapping("/dashboard/enhanced")
@RequiredArgsConstructor
public class DashboardEnhancedController {

    private final DashboardEnhancedService dashboardEnhancedService;

    @Operation(summary = "登录统计")
    @GetMapping("/login-stats")
    @SaCheckPermission("monitor:dashboard:query")
    public Result<?> loginStats() {
        return Result.ok(dashboardEnhancedService.getLoginStats());
    }

    @Operation(summary = "导出统计")
    @GetMapping("/export-stats")
    @SaCheckPermission("monitor:dashboard:query")
    public Result<?> exportStats() {
        return Result.ok(dashboardEnhancedService.getExportStats());
    }

    @Operation(summary = "操作日志Top10")
    @GetMapping("/operation-top10")
    @SaCheckPermission("monitor:dashboard:query")
    public Result<?> operationTop10() {
        return Result.ok(dashboardEnhancedService.getOperationTop10());
    }
}
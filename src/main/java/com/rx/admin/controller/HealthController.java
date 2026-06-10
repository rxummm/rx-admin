package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.Result;
import com.rx.admin.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "系统健康监控")
@RestController
@RequestMapping("/api/monitor/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/system")
    @SaCheckPermission("monitor:health:list")
    @Operation(summary = "获取系统健康状态")
    public Result<Map<String, Object>> systemHealth() {
        return Result.ok(healthService.getSystemHealth());
    }

    @GetMapping("/gc")
    @SaCheckPermission("monitor:health:list")
    @Operation(summary = "获取GC统计")
    public Result<Map<String, Object>> gcStats() {
        return Result.ok(healthService.getGcStats());
    }
}

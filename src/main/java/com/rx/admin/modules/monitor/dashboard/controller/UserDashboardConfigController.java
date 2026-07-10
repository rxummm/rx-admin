package com.rx.admin.modules.monitor.dashboard.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.dashboard.entity.SysUserDashboardConfig;
import com.rx.admin.modules.monitor.dashboard.service.UserDashboardConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户仪表盘配置控制器
 */
@Tag(name = "仪表盘配置")
@RestController
@ApiVersion(1)
@RequestMapping("/dashboard/config")
@RequiredArgsConstructor
public class UserDashboardConfigController {

    private final UserDashboardConfigService configService;

    @Operation(summary = "获取用户仪表盘配置")
    @GetMapping
    public Result<List<SysUserDashboardConfig>> getConfig() {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        return Result.ok(configService.getUserConfig(userId));
    }

    @Operation(summary = "保存用户仪表盘配置")
    @PostMapping
    public Result<Void> saveConfig(@RequestBody List<SysUserDashboardConfig> configs) {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        configService.saveConfig(userId, configs);
        return Result.ok();
    }

    @Operation(summary = "更新单个组件配置")
    @PutMapping("/widget")
    public Result<Void> updateWidget(@RequestBody SysUserDashboardConfig config) {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        configService.updateWidget(userId, config);
        return Result.ok();
    }
}

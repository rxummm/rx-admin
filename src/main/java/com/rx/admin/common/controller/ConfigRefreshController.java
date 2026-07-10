package com.rx.admin.common.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.config.AppConfig;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 配置热更新控制器
 * 通过修改 AppConfig 的字段实现热更新
 */
@Slf4j
@Tag(name = "配置热更新")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/config/refresh")
@RequiredArgsConstructor
public class ConfigRefreshController {

    private final AppConfig appConfig;

    /**
     * 刷新缓存配置
     */
    @Operation(summary = "刷新缓存配置")
    @PostMapping("/cache")
    @OperateLog(module = "配置管理", operation = "刷新缓存配置")
    public Result<Void> refreshCacheConfig(@RequestBody Map<String, Long> updates) {
        if (updates.containsKey("configTtlSeconds")) {
            appConfig.getCache().setConfigTtlSeconds(updates.get("configTtlSeconds"));
        }
        if (updates.containsKey("menuTtlSeconds")) {
            appConfig.getCache().setMenuTtlSeconds(updates.get("menuTtlSeconds"));
        }
        if (updates.containsKey("dashboardRefreshMs")) {
            appConfig.getCache().setDashboardRefreshMs(updates.get("dashboardRefreshMs"));
        }
        
        log.info("缓存配置已刷新: {}", updates);
        return Result.ok();
    }

    /**
     * 获取当前配置
     */
    @Operation(summary = "获取当前配置")
    @GetMapping
    public Result<AppConfig> getConfig() {
        return Result.ok(appConfig);
    }
}

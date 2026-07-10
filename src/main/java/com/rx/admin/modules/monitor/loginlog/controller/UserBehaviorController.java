package com.rx.admin.modules.monitor.loginlog.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.loginlog.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户行为分析控制器
 */
@Tag(name = "用户行为分析")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/user-behavior")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;

    @Operation(summary = "获取用户登录频率统计")
    @GetMapping("/login-frequency")
    public Result<Map<String, Object>> getLoginFrequency(
            @RequestParam(defaultValue = "7") int days) {
        return Result.ok(userBehaviorService.getLoginFrequency(days));
    }

    @Operation(summary = "获取活跃时段分布")
    @GetMapping("/active-time")
    public Result<List<Map<String, Object>>> getActiveTimeDistribution() {
        return Result.ok(userBehaviorService.getActiveTimeDistribution());
    }

    @Operation(summary = "获取操作偏好统计")
    @GetMapping("/operation-preference")
    public Result<Map<String, Object>> getOperationPreference() {
        return Result.ok(userBehaviorService.getOperationPreference());
    }
}

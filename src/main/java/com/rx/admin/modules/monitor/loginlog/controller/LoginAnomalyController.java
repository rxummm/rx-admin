package com.rx.admin.modules.monitor.loginlog.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.loginlog.service.LoginAnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 登录异常检测控制器
 */
@Tag(name = "登录异常检测")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/login-anomaly")
@RequiredArgsConstructor
public class LoginAnomalyController {

    private final LoginAnomalyService loginAnomalyService;

    @Operation(summary = "获取登录异常统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAnomalyStats() {
        return Result.ok(loginAnomalyService.getAnomalyStats());
    }

    @Operation(summary = "获取按IP统计的失败登录")
    @GetMapping("/failed-by-ip")
    public Result<List<Map<String, Object>>> getFailedLoginsByIp(
            @RequestParam(defaultValue = "7") int days) {
        return Result.ok(loginAnomalyService.getFailedLoginsByIp(days));
    }

    @Operation(summary = "手动触发异常检测")
    @PostMapping("/detect")
    @OperateLog(module = "登录异常检测", operation = "手动触发检测")
    public Result<Void> triggerDetection() {
        loginAnomalyService.detectAnomalies();
        return Result.ok();
    }
}

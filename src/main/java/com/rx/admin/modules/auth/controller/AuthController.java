package com.rx.admin.modules.auth.controller;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.common.utils.WebUtils;
import com.rx.admin.modules.auth.dto.LoginRequest;
import com.rx.admin.modules.auth.dto.ProfileUpdateDTO;
import com.rx.admin.modules.auth.dto.RegisterRequest;

import com.rx.admin.modules.auth.service.AuthService;

import com.rx.admin.modules.auth.service.CaptchaService;

import com.rx.admin.modules.auth.service.LoginAttemptService;

import com.rx.admin.modules.monitor.loginlog.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//按审计建议，LoginRequest 已使用 DTO；getUserInfo/getRouters 返回 Map 因数据格式不固定

@RequiredArgsConstructor
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;
    private final LoadingCache<String, RateLimiter> loginRateLimiters;
    private final CaptchaService captchaService;
    private final LoginLogService loginLogService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperateLog(module = "认证管理", operation = "用户登录")
    public Result<Map<String, Object>> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 检查是否被锁定
        if (loginAttemptService.isLocked(username)) {
            long remaining = loginAttemptService.getRemainingLockSeconds(username);
            return Result.fail(429, "账号已被锁定，请 " + (remaining / 60 + 1) + " 分钟后重试");
        }

        // IP 级别限流（Caffeine 自动清理过期 IP）
        String clientIp = WebUtils.getClientIp(request);
        if (!loginRateLimiters.get(clientIp).tryAcquire()) {
            return Result.fail(429, "请求过于频繁，请稍后再试");
        }

        // 验证码校验（当前强制开启，后续接入 sys_config 后改为配置开关）
        if (loginRequest.getCaptchaUuid() == null || loginRequest.getCaptchaCode() == null) {
            return Result.fail(400, "验证码不能为空");
        }
        if (!captchaService.validate(loginRequest.getCaptchaUuid(), loginRequest.getCaptchaCode())) {
            return Result.fail(400, "验证码错误或已过期");
        }

        try {
            Map<String, Object> result = authService.login(username, password);
            loginAttemptService.loginSucceeded(username);
            loginLogService.recordLogin(username, WebUtils.getClientIp(request),
                    request.getHeader("User-Agent"),
                    "", true, null);
            return Result.ok(result);
        } catch (Exception e) {
            loginAttemptService.loginFailed(username);
            String failReason = e.getMessage();
            if (failReason != null && failReason.length() > 200) failReason = failReason.substring(0, 200);
            loginLogService.recordLogin(username, WebUtils.getClientIp(request),
                    request.getHeader("User-Agent"),
                    "", false, failReason);
            throw e;
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @OperateLog(module = "认证管理", operation = "用户注册")
    public Result<?> register(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.register(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getNickname());
        return Result.ok("注册成功");
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    @OperateLog(module = "认证管理", operation = "用户退出")
    public Result<?> logout() {
        authService.logout();
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo() {
        return Result.ok(authService.getUserInfo());
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/update-profile")
    @OperateLog(module = "认证管理", operation = "更新个人信息")
    public Result<?> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        authService.updateProfile(dto.getNickname(), dto.getEmail(), dto.getPhone(),
                dto.getGender(), dto.getPassword(), dto.getOldPassword());
        return Result.ok();
    }

    @Operation(summary = "获取路由菜单")
    @GetMapping("/routers")
    public Result<Map<String, Object>> getRouters() {
        return Result.ok(authService.getRouters());
    }

    @Operation(summary = "会话心跳检测（用于前端检测被踢下线的轻量接口）")
    @GetMapping("/ping")
    public Result<Void> ping() {
        return Result.ok();
    }
}

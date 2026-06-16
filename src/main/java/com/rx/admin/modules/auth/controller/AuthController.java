package com.rx.admin.modules.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.result.Result;
import com.rx.admin.common.utils.WebUtils;
import com.rx.admin.modules.auth.dto.LoginRequest;
import com.rx.admin.modules.auth.dto.ProfileUpdateDTO;
import com.rx.admin.modules.auth.dto.RegisterRequest;
import com.rx.admin.modules.auth.service.IAuthService;
import com.rx.admin.modules.auth.service.CaptchaService;
import com.rx.admin.modules.auth.service.LoginAttemptService;
import com.rx.admin.modules.auth.vo.LoginResponseVO;
import com.rx.admin.modules.auth.vo.UserInfoVO;
import com.rx.admin.modules.monitor.loginlog.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final LoginAttemptService loginAttemptService;
    private final LoadingCache<String, RateLimiter> loginRateLimiters;
    private final CaptchaService captchaService;
    private final LoginLogService loginLogService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperateLog(module = "认证管理", operation = "用户登录")
    public Result<LoginResponseVO> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (loginAttemptService.isLocked(username)) {
            long remaining = loginAttemptService.getRemainingLockSeconds(username);
            return Result.fail(ErrorCode.TOO_MANY_REQUESTS, "账号已被锁定，请 " + (remaining / 60 + 1) + " 分钟后重试");
        }

        String clientIp = WebUtils.getClientIp(request);
        if (!loginRateLimiters.get(clientIp).tryAcquire()) {
            return Result.fail(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }

        if (loginRequest.getCaptchaUuid() == null || loginRequest.getCaptchaCode() == null) {
            return Result.fail(ErrorCode.BAD_REQUEST, "验证码不能为空");
        }
        if (!captchaService.validate(loginRequest.getCaptchaUuid(), loginRequest.getCaptchaCode())) {
            return Result.fail(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
        }

        try {
            LoginResponseVO result = authService.login(username, password);
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
    public Result<UserInfoVO> getUserInfo() {
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

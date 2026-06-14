package com.rx.admin.modules.monitor.online.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.online.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Tag(name = "在线用户")
@Slf4j
@RestController
@RequestMapping("/api/monitor/online")
@RequiredArgsConstructor
public class SysOnlineController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "获取在线用户列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        // 优先保证当前用户在线
        try {
            String currentToken = StpUtil.getTokenValue();
            Object loginId = StpUtil.getLoginIdByToken(currentToken);
            if (currentToken != null && loginId != null) {
                onlineUserService.userLoggedIn(currentToken, Long.valueOf(loginId.toString()));
            }
        } catch (Exception e) {
            log.warn("记录当前用户在线状态失败: {}", e.getMessage());
        }

        return Result.ok(onlineUserService.getOnlineUsers());
    }

    @Operation(summary = "踢出在线用户")
    @DeleteMapping("/{tokenValue}")
    @SaCheckPermission("monitor:online:kick")
    public Result<Void> kickOut(@PathVariable String tokenValue) {
        // Sa-Token 踢出
        StpUtil.kickoutByTokenValue(tokenValue);
        // 移除在线记录
        onlineUserService.userLoggedOut(tokenValue);
        return Result.ok();
    }
}
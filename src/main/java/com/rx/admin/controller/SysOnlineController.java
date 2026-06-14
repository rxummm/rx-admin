package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.result.Result;
import com.rx.admin.service.OnlineUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/monitor/online")
@RequiredArgsConstructor
public class SysOnlineController {

    private final OnlineUserService onlineUserService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        // 优先保证当前用户在线
        try {
            String currentToken = StpUtil.getTokenValue();
            Object loginId = StpUtil.getLoginIdByToken(currentToken);
            if (currentToken != null && loginId != null) {
                onlineUserService.userLoggedIn(currentToken, Long.valueOf(loginId.toString()));
            }
        } catch (Exception ignored) {}

        return Result.ok(onlineUserService.getOnlineUsers());
    }

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
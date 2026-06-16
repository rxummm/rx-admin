package com.rx.admin.modules.monitor.notification.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.modules.monitor.notification.service.SseSessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Tag(name = "统一通知中心 SSE")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationStreamController {

    private final SseSessionManager sseSessionManager;

    @Operation(summary = "SSE 统一推送（通知/消息/仪表盘/健康，事件名区分）")
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream() {
        Long userId = StpUtil.getLoginIdAsLong();
        return sseSessionManager.subscribe(userId);
    }
}
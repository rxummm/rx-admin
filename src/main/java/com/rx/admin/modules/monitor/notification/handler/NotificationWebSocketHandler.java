package com.rx.admin.modules.monitor.notification.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.modules.monitor.notification.service.NotificationSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final NotificationSessionManager sessionManager;
    private final Map<WebSocketSession, Long> sessionUserMap = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler(NotificationSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = authenticate(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessionUserMap.put(session, userId);
        sessionManager.registerWebSocketSession(userId, session);
        log.debug("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = sessionUserMap.get(session);
        if (userId == null) return;

        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = sessionUserMap.remove(session);
        if (userId != null) {
            sessionManager.removeWebSocketSession(userId, session);
            log.debug("WebSocket disconnected: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = sessionUserMap.remove(session);
        if (userId != null) {
            sessionManager.removeWebSocketSession(userId, session);
        }
        log.debug("WebSocket transport error: {}", exception.getMessage());
    }

    private Long authenticate(WebSocketSession session) {
        try {
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            if (query == null) return null;

            Map<String, String> params = UriComponentsBuilder.fromUriString("?" + query).build()
                    .getQueryParams().toSingleValueMap();

            String token = params.get("token");
            if (token == null || token.isEmpty()) return null;

            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) return null;

            return Long.parseLong(loginId.toString());
        } catch (Exception e) {
            log.debug("WebSocket auth failed: {}", e.getMessage());
            return null;
        }
    }
}

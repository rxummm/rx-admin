package com.rx.admin.modules.monitor.notification.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationSessionManager {

    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final Map<Long, List<WebSocketSession>> userWebSocketSessions = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeSse(userId, emitter));
        emitter.onTimeout(() -> removeSse(userId, emitter));
        emitter.onError(e -> removeSse(userId, emitter));
        log.debug("SSE subscribed: userId={}", userId);
        return emitter;
    }

    public void registerWebSocketSession(Long userId, WebSocketSession session) {
        userWebSocketSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void removeWebSocketSession(Long userId, WebSocketSession session) {
        List<WebSocketSession> sessions = userWebSocketSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userWebSocketSessions.remove(userId);
            }
        }
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        sendSseToUser(userId, eventName, data);
        sendWebSocketToUser(userId, eventName, data);
    }

    private void sendSseToUser(Long userId, String eventName, Object data) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) return;
        emitters.forEach(e -> {
            try {
                e.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException ex) {
                log.debug("SSE send failed to userId={}, removing: {}", userId, ex.getMessage());
                removeSse(userId, e);
                try { e.completeWithError(ex); } catch (Exception inner) { log.debug("SSE completeWithError failed", inner); }
            } catch (Exception ex) {
                log.debug("SSE send error to userId={}, removing: {}", userId, ex.getMessage());
                removeSse(userId, e);
                try { e.completeWithError(ex); } catch (Exception inner) { log.debug("SSE completeWithError failed", inner); }
            }
        });
    }

    private void sendWebSocketToUser(Long userId, String eventName, Object data) {
        List<WebSocketSession> sessions = userWebSocketSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        String message = convertToJson(eventName, data);
        TextMessage textMessage = new TextMessage(message);

        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(textMessage);
                    }
                }
            } catch (IOException ex) {
                log.debug("WebSocket send failed to userId={}, removing: {}", userId, ex.getMessage());
                removeWebSocketSession(userId, session);
            }
        });
    }

    public void broadcast(String eventName, Object data) {
        userEmitters.forEach((userId, emitters) -> sendSseToUser(userId, eventName, data));
        userWebSocketSessions.forEach((userId, sessions) -> sendWebSocketToUser(userId, eventName, data));
    }

    private String convertToJson(String eventName, Object data) {
        String dataJson;
        if (data instanceof String) {
            dataJson = (String) data;
        } else {
            dataJson = data != null ? data.toString() : "null";
        }
        return "{\"event\":\"" + eventName + "\",\"data\":" + dataJson + "}";
    }

    private void removeSse(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public int getActiveSessionCount() {
        int sseCount = userEmitters.values().stream().mapToInt(List::size).sum();
        int wsCount = userWebSocketSessions.values().stream().mapToInt(List::size).sum();
        return sseCount + wsCount;
    }

    public int getSseSessionCount() {
        return userEmitters.values().stream().mapToInt(List::size).sum();
    }

    public int getWebSocketSessionCount() {
        return userWebSocketSessions.values().stream().mapToInt(List::size).sum();
    }

    @PreDestroy
    public void shutdown() {
        userEmitters.forEach((userId, emitters) ->
            emitters.forEach(e -> {
                try { e.complete(); } catch (Exception inner) { log.debug("SSE complete failed during shutdown", inner); }
            })
        );
        userEmitters.clear();

        userWebSocketSessions.forEach((userId, sessions) ->
            sessions.forEach(s -> {
                try {
                    if (s.isOpen()) s.close();
                } catch (Exception inner) { log.debug("WebSocket close failed during shutdown", inner); }
            })
        );
        userWebSocketSessions.clear();
    }
}

package com.rx.admin.modules.monitor.notification.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@SuppressWarnings("null")
public class SseSessionManager {

    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));
        log.debug("SSE subscribed: userId={}", userId);
        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) return;
        emitters.forEach(e -> {
            try {
                e.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException ex) {
                log.debug("SSE send failed to userId={}, removing: {}", userId, ex.getMessage());
                remove(userId, e);
                try { e.completeWithError(ex); } catch (Exception inner) { log.debug("SSE completeWithError failed", inner); }
            } catch (Exception ex) {
                log.debug("SSE send error to userId={}, removing: {}", userId, ex.getMessage());
                remove(userId, e);
                try { e.completeWithError(ex); } catch (Exception inner) { log.debug("SSE completeWithError failed", inner); }
            }
        });
    }

    public void broadcast(String eventName, Object data) {
        userEmitters.forEach((userId, emitters) -> sendToUser(userId, eventName, data));
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public int getActiveSessionCount() {
        return userEmitters.values().stream().mapToInt(List::size).sum();
    }

    @PreDestroy
    public void shutdown() {
        userEmitters.forEach((userId, emitters) ->
            emitters.forEach(e -> {
                try { e.complete(); } catch (Exception inner) { log.debug("SSE complete failed during shutdown", inner); }
            })
        );
        userEmitters.clear();
    }
}
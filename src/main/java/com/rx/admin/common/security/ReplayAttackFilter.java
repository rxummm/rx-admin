package com.rx.admin.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * API 防重放过滤器
 * 验证请求头 X-Timestamp（时间戳）和 X-Nonce（随机字符串）
 * 同一 nonce 不可重复使用，时间戳偏差超过 app.replay.time-window-ms 拒绝
 * 仅对写操作（POST/PUT/DELETE）生效
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@SuppressWarnings("null")
public class ReplayAttackFilter extends OncePerRequestFilter {

    @Value("${app.replay.time-window-ms:300000}")
    private long timeWindowMs;

    @Value("${app.replay.max-nonce-cache:10000}")
    private int maxNonceCache;

    private Cache<String, Long> usedNonces;

    @PostConstruct
    public void init() {
        this.usedNonces = Caffeine.newBuilder()
                .maximumSize(maxNonceCache)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    private static final Set<String> SKIP_PATHS = Set.of(
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/captcha",
            "/api/v1/auth/ping"
    );

    private static final Set<String> PROTECTED_PREFIXES = Set.of(
            "/api/v1/auth/login", "/api/v1/auth/register",
            "/api/v1/monitor/online/", "/api/v1/sys/user/",
            "/api/v1/sys/role/", "/api/v1/sys/menu/", "/api/v1/sys/dept/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || SKIP_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isProtected = false;
        for (String prefix : PROTECTED_PREFIXES) {
            if (path.startsWith(prefix)) {
                isProtected = true;
                break;
            }
        }
        if (!isProtected) {
            chain.doFilter(request, response);
            return;
        }

        String timestampStr = request.getHeader("X-Timestamp");
        if (timestampStr == null) {
            writeError(response, "missing X-Timestamp header");
            return;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            writeError(response, "invalid X-Timestamp format");
            return;
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > timeWindowMs) {
            writeError(response, "request expired or timestamp out of window");
            return;
        }

        String nonce = request.getHeader("X-Nonce");
        if (nonce == null || nonce.isBlank()) {
            writeError(response, "missing X-Nonce header");
            return;
        }
        if (usedNonces.getIfPresent(nonce) != null) {
            writeError(response, "nonce already used (replay attack detected)");
            return;
        }
        usedNonces.put(nonce, now);

        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(400);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":400,\"message\":\"" + message + "\"}");
        response.getWriter().flush();
    }
}

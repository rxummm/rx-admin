package com.rx.admin.common.security;

import com.rx.admin.common.utils.WebUtils;
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * IP 黑白名单过滤器
 * 支持两种模式（通过 app.ip-filter.mode 配置）：
 * - "blacklist"：禁止列表中的 IP 访问（默认）
 * - "whitelist"：仅允许列表中的 IP 访问
 * 配置示例：
 * <pre>
 * app:
 *   ip-filter:
 *     enabled: true
 *     mode: blacklist
 *     ips: 192.168.1.100,10.0.0.5
 * </pre>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@SuppressWarnings("null")
public class IpFilter extends OncePerRequestFilter {

    @Value("${app.ip-filter.enabled:false}")
    private boolean enabled;

    @Value("${app.ip-filter.mode:blacklist}")
    private String mode;

    @Value("${app.ip-filter.ips:}")
    private String ipsConfig;

    private volatile Set<String> ipSet = Collections.emptySet();
    private volatile String lastIpsConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        // 延迟加载并缓存 IP 列表
        Set<String> ips = getIpSet();
        if (ips.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = WebUtils.getClientIp(request);
        boolean isInList = ips.contains(clientIp);

        if ("whitelist".equalsIgnoreCase(mode)) {
            // 白名单模式：不在列表中的 IP 拒绝访问
            if (!isInList) {
                log.warn("IP {} not in whitelist, access denied", clientIp);
                writeError(response, 403, "Access denied: IP not in whitelist");
                return;
            }
        } else {
            // 黑名单模式（默认）：在列表中的 IP 拒绝访问
            if (isInList) {
                log.warn("IP {} in blacklist, access denied", clientIp);
                writeError(response, 403, "Access denied: IP is blacklisted");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 获取并缓存 IP 集合
     */
    private Set<String> getIpSet() {
        String config = ipsConfig;
        if (config.equals(lastIpsConfig)) {
            return ipSet;
        }
        synchronized (this) {
            if (config.equals(lastIpsConfig)) {
                return ipSet;
            }
            Set<String> newSet = new HashSet<>();
            for (String ip : config.split(",")) {
                String trimmed = ip.trim();
                if (!trimmed.isEmpty()) {
                    newSet.add(trimmed);
                }
            }
            this.ipSet = Collections.unmodifiableSet(newSet);
            this.lastIpsConfig = config;
        }
        return ipSet;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
        response.getWriter().flush();
    }
}

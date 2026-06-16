package com.rx.admin.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Web 层工具类
 *
 * @author RX Admin
 * @since 2026-06-13
 */
@Slf4j
public class WebUtils {

    private WebUtils() {}

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 优先级：X-Forwarded-For > X-Real-IP > Proxy-Client-IP > WL-Proxy-Client-IP > request.getRemoteAddr()
     * 多级代理时取 X-Forwarded-For 的第一个 IP。
     * IPv6 本地回环（::1 / 0:0:0:0:0:0:0:1）规范化为 127.0.0.1。
     * </p>
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            if (idx > 0) {
                ip = ip.substring(0, idx).trim();
            }
            return normalizeIpv6(ip);
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return normalizeIpv6(ip.trim());
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return normalizeIpv6(ip.trim());
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return normalizeIpv6(ip.trim());
        }

        return normalizeIpv6(request.getRemoteAddr());
    }

    private static String normalizeIpv6(String ip) {
        if (ip == null) return null;
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "::".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}

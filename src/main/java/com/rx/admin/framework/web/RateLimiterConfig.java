package com.rx.admin.framework.web;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求频率限制配置
 * 对登录接口进行限流，防止暴力破解
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 登录接口限流器（按 IP 区分）
     * 每秒最多 3 次请求
     */
    @Bean
    public ConcurrentHashMap<String, RateLimiter> loginRateLimiters() {
        return new ConcurrentHashMap<>();
    }

    /**
     * 默认每个 IP 的限流：每秒 3 个请求
     */
    public static final double LOGIN_RATE_PER_SECOND = 3.0;
}

package com.rx.admin.framework.web;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 请求频率限制配置
 * 对登录接口进行限流，防止暴力破解
 *
 * 使用 Caffeine LoadingCache 管理各 IP 的限流器，
 * 空闲 IP 自动过期（1小时），避免内存泄漏。
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 登录接口限流器（按 IP 区分）
     * 每秒最多 3 次请求
     */
    @Bean
    public LoadingCache<String, RateLimiter> loginRateLimiters() {
        return Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(10000)
                .build(key -> RateLimiter.create(LOGIN_RATE_PER_SECOND));
    }

    /**
     * 默认每个 IP 的限流：每秒 3 个请求
     */
    public static final double LOGIN_RATE_PER_SECOND = 3.0;
}

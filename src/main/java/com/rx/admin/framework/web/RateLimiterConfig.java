package com.rx.admin.framework.web;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 请求频率限制配置
 * 支持按IP/用户/接口配置请求频率限制
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
     * 通用接口限流器（按 IP 区分）
     * 每秒最多 10 次请求
     */
    @Bean
    public LoadingCache<String, RateLimiter> apiRateLimiters() {
        return Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build(key -> RateLimiter.create(API_RATE_PER_SECOND));
    }

    /**
     * 写操作限流器（按用户区分）
     * 每秒最多 5 次请求
     */
    @Bean
    public LoadingCache<String, RateLimiter> writeRateLimiters() {
        return Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(5000)
                .build(key -> RateLimiter.create(WRITE_RATE_PER_SECOND));
    }

    /**
     * 登录接口限流：每秒 3 个请求
     */
    public static final double LOGIN_RATE_PER_SECOND = 3.0;

    /**
     * 通用 API 限流：每秒 10 个请求
     */
    public static final double API_RATE_PER_SECOND = 10.0;

    /**
     * 写操作限流：每秒 5 个请求
     */
    public static final double WRITE_RATE_PER_SECOND = 5.0;
}

package com.rx.admin.framework.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置
 * <p>
 * 缓存策略：
 * - config 缓存：系统配置，典型读多写少。写操作（add/update/delete）时 allEntries 清除。
 * - menu 缓存：菜单树，按用户隔离。菜单变更时 allEntries 清除。
 * - dashboard 缓存：由 @Scheduled 定时刷新，不使用 Spring Cache（直接内存字段）。
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * config 缓存过期时间（秒），默认 600 秒 = 10 分钟
     */
    @Value("${app.cache.config-ttl-seconds:600}")
    private long configTtlSeconds;

    /**
     * menu 缓存过期时间（秒），默认 3600 秒 = 1 小时
     */
    @Value("${app.cache.menu-ttl-seconds:3600}")
    private long menuTtlSeconds;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        CaffeineCache configCache = new CaffeineCache("config",
                Caffeine.newBuilder()
                        .expireAfterWrite(configTtlSeconds, TimeUnit.SECONDS)
                        .maximumSize(100)
                        .recordStats()
                        .build());

        CaffeineCache menuCache = new CaffeineCache("menu",
                Caffeine.newBuilder()
                        .expireAfterWrite(menuTtlSeconds, TimeUnit.SECONDS)
                        .maximumSize(500)
                        .recordStats()
                        .build());

        manager.setCaches(Arrays.asList(configCache, menuCache));
        return manager;
    }
}

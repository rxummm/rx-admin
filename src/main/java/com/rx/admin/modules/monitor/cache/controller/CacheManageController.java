package com.rx.admin.modules.monitor.cache.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "缓存管理")
@RestController
@RequestMapping("/api/monitor/cache")
@SaCheckRole("admin")
public class CacheManageController {

    private final CacheManager cacheManager;

    public CacheManageController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Operation(summary = "获取所有缓存信息")
    @GetMapping("/list")
    public Result<?> listCaches() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("name", name);
                // Caffeine cache 通过 unwrap 获取统计信息
                Object nativeCache = cache.getNativeCache();
                info.put("nativeType", nativeCache.getClass().getSimpleName());
                try {
                    // 反射获取Caffeine统计信息
                    var statsMethod = nativeCache.getClass().getMethod("stats");
                    var stats = statsMethod.invoke(nativeCache);
                    var ssClass = stats.getClass();
                    info.put("hitCount", ssClass.getMethod("hitCount").invoke(stats));
                    info.put("missCount", ssClass.getMethod("missCount").invoke(stats));
                    info.put("hitRate", String.format("%.1f%%",
                            (double) ssClass.getMethod("hitRate").invoke(stats) * 100));
                    info.put("evictionCount", ssClass.getMethod("evictionCount").invoke(stats));
                } catch (Exception ignored) {
                    info.put("note", "无法获取详细统计信息");
                }
                list.add(info);
            }
        }
        return Result.ok(list);
    }

    @Operation(summary = "清除指定缓存")
    @DeleteMapping("/clear/{cacheName}")
    public Result<?> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return Result.ok("缓存 " + cacheName + " 已清除", null);
        }
        return Result.fail(404, "缓存不存在");
    }

    @Operation(summary = "清除所有缓存")
    @DeleteMapping("/clear-all")
    public Result<?> clearAllCache() {
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
        return Result.ok("所有缓存已清除", null);
    }
}

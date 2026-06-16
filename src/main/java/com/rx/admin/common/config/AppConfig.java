package com.rx.admin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private MenuConfig menu = new MenuConfig();
    private CacheConfig cache = new CacheConfig();
    private CorsConfig cors = new CorsConfig();
    private CaptchaConfig captcha = new CaptchaConfig();
    private RateLimitConfig rateLimit = new RateLimitConfig();
    private SecurityConfig security = new SecurityConfig();
    private TechBlogConfig techblog = new TechBlogConfig();
    private IpFilterConfig ipFilter = new IpFilterConfig();

    @Data
    public static class MenuConfig {
        private String excludedTopIds = "1,24,30,36";
        private Long excludedPermissionMenuId = 300L;
    }

    @Data
    public static class CacheConfig {
        private long configTtlSeconds = 600;
        private long menuTtlSeconds = 3600;
        private long dashboardRefreshMs = 30000;
    }

    @Data
    public static class CorsConfig {
        private String allowedOrigins = "*";
    }

    @Data
    public static class CaptchaConfig {
        private long expireMs = 300000;
        private long cleanupIntervalMs = 60000;
    }

    @Data
    public static class RateLimitConfig {
        private int global = 100;
        private int login = 10;
        private int api = 50;
    }

    @Data
    public static class SecurityConfig {
        private int loginAttemptMax = 5;
        private int loginLockSeconds = 1800;
        private String[] authExcludePaths = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/captcha"
        };
        private String[] swaggerPaths = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc.html"
        };
        private String[] actuatorPaths = {"/actuator/**"};
    }

    @Data
    public static class TechBlogConfig {
        private long requestDelayMs = 1000;
        private long pageTimeoutMs = 15000;
        private long articleTimeoutMs = 30000;
    }

    @Data
    public static class IpFilterConfig {
        private boolean enabled = false;
        private String mode = "blacklist";
    }
}

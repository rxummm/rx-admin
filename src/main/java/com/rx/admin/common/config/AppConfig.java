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
    private ApiConfig api = new ApiConfig();
    private AudioConfig audio = new AudioConfig();
    private OcrConfig ocr = new OcrConfig();

    @Data
    public static class MenuConfig {
        private String excludedTopIds = "1,24,30,36";
        private Long excludedPermissionMenuId = 300L;
    }

    @Data
    public static class CacheConfig {
        private long configTtlSeconds = 600;
        private long menuTtlSeconds = 3600;
        private long dictTtlSeconds = 1800;
        private long calendarTtlSeconds = 120;
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
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/captcha"
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

    @Data
    public static class ApiConfig {
        /**
         * API 前缀，默认 /api
         */
        private String prefix = "/api";

        /**
         * 默认版本号，默认 v1
         */
        private String defaultVersion = "v1";

        /**
         * 是否启用版本控制，false 则忽略版本号
         */
        private boolean enabled = true;
    }

    @Data
    public static class AudioConfig {
        private String whisperPath = "whisper";
        private String modelPath = "/opt/whisper/models";
        private String defaultModel = "small";
        private String defaultLanguage = "zh";
        private String tempDir = "/tmp/audio";
        private int maxFileSizeMb = 100;
        private boolean enabled = true;
        private int threads = 4;  // Whisper CPU 线程数，默认 4
        // WhisperX 说话人分离配置（可选高级模式）
        private boolean whisperxEnabled = false;
        private String whisperxApiUrl = "http://localhost:8880";
        private String whisperxApiKey = "";
    }

    @Data
    public static class OcrConfig {
        private boolean enabled = true;
        private String tessdataPath = "D:\\tesseract-ocr\\tessdata";
        private String defaultLanguage = "chi_sim+eng";
        private String tempDir = "D:\\temp\\ocr";
        private int maxFileSizeMb = 50;
    }
}
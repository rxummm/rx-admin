package com.rx.admin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置类
 * 支持热更新的配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private CacheConfig cache = new CacheConfig();
    private SecurityConfig security = new SecurityConfig();
    private MenuConfig menu = new MenuConfig();
    private TechBlogConfig techblog = new TechBlogConfig();
    private OcrConfig ocr = new OcrConfig();
    private AudioConfig audio = new AudioConfig();

    @Data
    public static class CacheConfig {
        private long configTtlSeconds = 600;
        private long menuTtlSeconds = 3600;
        private long dashboardRefreshMs = 30000;
    }

    @Data
    public static class SecurityConfig {
        private long captchaExpireMs = 300000;
        private long captchaCleanupIntervalMs = 60000;
        private long replayTimeWindowMs = 300000;
        private int replayMaxNonceCache = 10000;
        private String[] authExcludePaths = new String[]{};
        private String[] swaggerPaths = new String[]{};
        private String[] actuatorPaths = new String[]{};
    }

    @Data
    public static class MenuConfig {
        private String defaultPassword = "admin123";
    }

    @Data
    public static class TechBlogConfig {
        private long requestDelayMs = 1000;
        private long pageTimeoutMs = 15000;
        private long articleTimeoutMs = 30000;
    }

    @Data
    public static class OcrConfig {
        private String tempDir = "ocr-temp";
        private String tessdataPath = "D:\\tessdata";
        private String defaultLanguage = "chi_sim+eng";
    }

    @Data
    public static class AudioConfig {
        private String tempDir = "audio-temp";
        private String storageDir = "audio-storage";
        private boolean whisperxEnabled = false;
        private String defaultLanguage = "zh";
        private String defaultModel = "demo";
        private String whisperPath = "";
        private String whisperxApiUrl = "";
        private String whisperxApiKey = "";
        private String modelPath = "";
        private int threads = 4;
    }
}

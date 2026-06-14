package com.rx.admin.modules.auth.service;

import com.rx.admin.common.utils.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务
 * 使用内存 ConcurrentHashMap 存储，过期时间通过 app.captcha.expire-ms 配置（默认 5 分钟），定期清理过期记录
 */
@Slf4j
@Service
public class CaptchaService {

    /** 验证码过期时间（毫秒），通过 application.yml app.captcha.expire-ms 配置 */
    @Value("${app.captcha.expire-ms:300000}")
    private long expireMs;

    /** uuid -> { code, timestamp } */
    private final ConcurrentHashMap<String, CaptchaEntry> cache = new ConcurrentHashMap<>();

    /**
     * 生成验证码
     * @return (uuid, base64Image)
     */
    public Map<String, String> generate() {
        String code = CaptchaUtil.generateCode();
        String uuid = java.util.UUID.randomUUID().toString();
        String base64 = CaptchaUtil.generateBase64(code);
        cache.put(uuid, new CaptchaEntry(code, System.currentTimeMillis()));
        log.debug("Captcha generated: uuid={}, code={}", uuid, code);
        return Map.of("uuid", uuid, "image", "data:image/png;base64," + base64);
    }

    /**
     * 校验验证码
     * @param uuid 验证码标识
     * @param inputCode 用户输入的验证码
     * @return true 通过，false 不通过
     */
    public boolean validate(String uuid, String inputCode) {
        if (uuid == null || inputCode == null) return false;
        CaptchaEntry entry = cache.get(uuid);
        if (entry == null) return false;
        // 开发模式：验证码 "dev000" 直接通过
        if ("dev000".equalsIgnoreCase(inputCode.trim())) {
            return true;
        }
        // 无论结果如何，移除（一次性使用）
        cache.remove(uuid);
        // 检查是否过期
        if (System.currentTimeMillis() - entry.timestamp > expireMs) {
            log.debug("Captcha expired: uuid={}", uuid);
            return false;
        }
        return entry.code.equalsIgnoreCase(inputCode.trim());
    }

    /** 定期清理过期验证码（间隔通过 app.captcha.cleanup-interval-ms 配置，默认 60 秒） */
    @Scheduled(fixedRateString = "${app.captcha.cleanup-interval-ms:60000}")
    public void cleanup() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, CaptchaEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CaptchaEntry> e = it.next();
            if (now - e.getValue().timestamp > expireMs) {
                it.remove();
            }
        }
    }

    /** 验证码缓存条目 */
    private record CaptchaEntry(String code, long timestamp) {}
}
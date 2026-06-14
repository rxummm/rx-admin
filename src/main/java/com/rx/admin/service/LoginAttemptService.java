package com.rx.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败次数限制服务
 * 同一用户名连续失败 5 次后锁定 30 分钟
 */
@Slf4j
@Service
public class LoginAttemptService {

    /** 最大失败次数 */
    private static final int MAX_ATTEMPTS = 5;

    /** 锁定时长（分钟） */
    private static final long LOCK_DURATION_MINUTES = 30;

    /** 记录每个用户名的失败次数 */
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();

    /** 记录每个用户名的锁定时间 */
    private final ConcurrentHashMap<String, Long> lockTimes = new ConcurrentHashMap<>();

    /**
     * 登录失败时调用
     */
    public void loginFailed(String username) {
        int count = attempts.merge(username, 1, (a, b) -> a + b);
        if (count >= MAX_ATTEMPTS) {
            lockTimes.put(username, Instant.now().toEpochMilli());
            log.warn("用户 {} 连续登录失败 {} 次，已锁定 {} 分钟", username, count, LOCK_DURATION_MINUTES);
        }
    }

    /**
     * 检查用户是否被锁定
     * @return true 表示被锁定
     */
    public boolean isLocked(String username) {
        Long lockTime = lockTimes.get(username);
        if (lockTime == null) {
            return false;
        }
        long elapsed = Instant.now().toEpochMilli() - lockTime;
        if (elapsed > LOCK_DURATION_MINUTES * 60 * 1000) {
            // 锁定已过期，清除记录
            lockTimes.remove(username);
            attempts.remove(username);
            return false;
        }
        return true;
    }

    /**
     * 获取剩余锁定时间（秒）
     */
    public long getRemainingLockSeconds(String username) {
        Long lockTime = lockTimes.get(username);
        if (lockTime == null) return 0;
        long elapsed = Instant.now().toEpochMilli() - lockTime;
        long remaining = (LOCK_DURATION_MINUTES * 60 * 1000) - elapsed;
        return Math.max(0, remaining / 1000);
    }

    /**
     * 登录成功时清除失败记录
     */
    public void loginSucceeded(String username) {
        attempts.remove(username);
        lockTimes.remove(username);
    }
}
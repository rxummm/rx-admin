package com.rx.admin.common.aspect;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.rx.admin.common.annotation.RateLimit;
import com.rx.admin.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 接口限流切面
 * 拦截标注 @RateLimit 的方法，进行频率限制
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final LoadingCache<String, RateLimiter> apiRateLimiters;

    /**
     * 拦截 @RateLimit 注解的方法
     */
    @Around("@annotation(com.rx.admin.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return joinPoint.proceed();
        }
        
        // 生成限流 key
        String key = generateKey(rateLimit.type(), joinPoint);
        
        // 获取限流器
        RateLimiter rateLimiter = apiRateLimiters.get(key);
        
        // 尝试获取令牌
        if (!rateLimiter.tryAcquire()) {
            log.warn("接口限流: {} 请求频率过高", method.getName());
            throw new BusinessException("请求过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }
    
    /**
     * 生成限流 key
     */
    private String generateKey(RateLimit.LimitType type, ProceedingJoinPoint joinPoint) {
        StringBuilder keyBuilder = new StringBuilder();
        
        switch (type) {
            case IP:
                // 从请求中获取 IP（简化实现）
                keyBuilder.append("ip:").append("default");
                break;
            case USER:
                // 从 Sa-Token 获取用户 ID
                keyBuilder.append("user:").append("default");
                break;
            case API:
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                keyBuilder.append("api:").append(signature.getDeclaringTypeName())
                          .append(":").append(signature.getName());
                break;
        }
        
        return keyBuilder.toString();
    }
}

package com.rx.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 标注此注解的接口会进行频率限制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流类型
     */
    LimitType type() default LimitType.IP;
    
    /**
     * 每秒允许的请求数
     */
    double rate() default 10.0;
    
    /**
     * 限流粒度
     */
    enum LimitType {
        /** 按 IP 限流 */
        IP,
        /** 按用户限流 */
        USER,
        /** 按接口限流 */
        API
    }
}

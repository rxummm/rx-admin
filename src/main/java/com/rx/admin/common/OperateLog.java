package com.rx.admin.common;

import java.lang.annotation.*;

/**
 * 操作日志注解，标注在 Controller 方法上自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    /** 操作模块 */
    String module() default "";
    /** 操作描述 */
    String operation() default "";
}

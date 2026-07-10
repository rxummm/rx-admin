package com.rx.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 操作二次确认注解
 * 标注此注解的接口需要在执行前进行密码确认
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationConfirm {
    
    /**
     * 确认提示信息
     */
    String value() default "此操作不可撤销，确认继续？";
    
    /**
     * 是否需要输入密码验证
     */
    boolean requirePassword() default true;
}

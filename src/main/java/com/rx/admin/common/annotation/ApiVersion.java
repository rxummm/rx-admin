package com.rx.admin.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 版本注解
 * <p>
 * 用于标记 Controller 的 API 版本，配合 ApiVersionHandlerMapping 实现版本前缀
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/sys/user")
 * @ApiVersion(1)  // 生成路径：/api/v1/sys/user
 * public class SysUserController { }
 *
 * @RestController
 * @RequestMapping("/sys/user")
 * @ApiVersion(2)  // 生成路径：/api/v2/sys/user
 * public class SysUserControllerV2 { }
 * }
 * </pre>
 *
 * @author RX
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

    /**
     * API 版本号，从 1 开始
     *
     * @return 版本号
     */
    int value() default 1;
}

package com.rx.admin.common;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标记Mapper方法需要数据权限过滤
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /** 部门字段名（用于部门级数据过滤） */
    String deptColumn() default "dept_id";
    /** 用户字段名（用于仅本人数据过滤） */
    String userColumn() default "create_by";
}

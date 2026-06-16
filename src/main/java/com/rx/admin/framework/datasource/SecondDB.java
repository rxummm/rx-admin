package com.rx.admin.framework.datasource;

import java.lang.annotation.*;

/**
 * 标记第二数据源（rxusysadmin）的 Mapper 接口
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecondDB {
}

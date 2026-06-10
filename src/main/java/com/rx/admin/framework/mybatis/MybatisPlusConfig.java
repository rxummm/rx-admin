package com.rx.admin.framework.mybatis;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.rx.admin.common.handler.DataScopeInnerInterceptor;
import com.rx.admin.service.DataScopeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class MybatisPlusConfig {

    private final DataScopeService dataScopeService;

    public MybatisPlusConfig(@Lazy DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 数据权限插件（必须放在分页插件之后，确保先分页再过滤）
        interceptor.addInnerInterceptor(new DataScopeInnerInterceptor(dataScopeService));
        return interceptor;
    }
}

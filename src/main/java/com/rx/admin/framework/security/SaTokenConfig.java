package com.rx.admin.framework.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SaTokenConfig implements WebMvcConfigurer {

    private final AppConfig appConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePaths = new java.util.ArrayList<>();
        excludePaths.addAll(Arrays.asList(appConfig.getSecurity().getAuthExcludePaths()));
        excludePaths.addAll(Arrays.asList(appConfig.getSecurity().getSwaggerPaths()));
        excludePaths.addAll(Arrays.asList(appConfig.getSecurity().getActuatorPaths()));

        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(excludePaths);
    }
}

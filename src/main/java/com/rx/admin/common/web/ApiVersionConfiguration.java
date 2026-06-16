package com.rx.admin.common.web;

import com.rx.admin.common.annotation.ApiVersion;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API 版本控制配置
 * <p>
 * 注册自定义的 RequestMappingHandlerMapping，支持 @ApiVersion 注解
 * 带 @ApiVersion(1) 注解的 Controller 会自动在路径前加上 /api/v1 前缀
 * </p>
 *
 * @author RX
 * @version 1.0
 */
@Configuration
public class ApiVersionConfiguration implements WebMvcRegistrations {

    private static final String API_PREFIX = "/api";

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiVersionRequestMappingHandlerMapping();
    }

    /**
     * 自定义 RequestMappingHandlerMapping，自动给带 @ApiVersion 注解的 Controller 添加版本前缀
     */
    private static class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

        @Override
        @SuppressWarnings({"null", "unused"})
        protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
            RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
            if (info == null) {
                return null;
            }

            ApiVersion apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
            if (apiVersion == null) {
                apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
            }

            if (apiVersion == null) {
                return info;
            }

            String versionPrefix = API_PREFIX + "/v" + apiVersion.value();

            // Spring MVC 6.x 同时支持 PathPattern 和 传统 Pattern
            // 优先使用 PathPattern（Spring Boot 3.x 默认）
            var pathPatterns = info.getPathPatternsCondition();
            if (pathPatterns != null && !pathPatterns.getPatterns().isEmpty()) {
                List<String> newPatterns = new ArrayList<>();
                for (Object pattern : pathPatterns.getPatterns()) {
                    String patternStr = pattern.toString();
                    if (patternStr.isEmpty() || "/".equals(patternStr)) {
                        newPatterns.add(versionPrefix);
                    } else {
                        newPatterns.add(versionPrefix + patternStr);
                    }
                }
                return info.mutate()
                        .paths(newPatterns.toArray(new String[0]))
                        .build();
            }

            // 回退到传统的 PatternsRequestCondition
            var patternsCondition = info.getPatternsCondition();
            if (patternsCondition != null) {
                Set<String> patterns = patternsCondition.getPatterns();
                if (patterns.isEmpty()) {
                    return info;
                }
                List<String> newPatterns = new ArrayList<>();
                for (String pattern : patterns) {
                    if (pattern.isEmpty() || "/".equals(pattern)) {
                        newPatterns.add(versionPrefix);
                    } else {
                        newPatterns.add(versionPrefix + pattern);
                    }
                }
                return info.mutate()
                        .paths(newPatterns.toArray(new String[0]))
                        .build();
            }

            return info;
        }
    }
}
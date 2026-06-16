package com.rx.admin.common.security;

import cn.dev33.satoken.exception.NotLoginException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet 过滤器：捕获 Sa-Token 拦截器抛出的 NotLoginException，
 * 区分"被踢出"和普通未登录/过期，返回统一 JSON 格式。
 * 拦截器层的异常 @RestControllerAdvice 无法处理，必须在 Filter 级捕获。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@SuppressWarnings("null")
public class NotLoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (NotLoginException e) {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            if (NotLoginException.KICK_OUT.equals(e.getType())) {
                resp.getWriter().write("{\"code\":401,\"message\":\"KICK_OUT\"}");
            } else {
                resp.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            }
            resp.getWriter().flush();
        }
    }
}
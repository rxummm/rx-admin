package com.rx.admin.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.utils.WebUtils;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 操作日志 AOP 切面，拦截标注 @OperateLog 的方法，自动记录操作日志
 * 支持参数脱敏和异步保存
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final SysLogService logService;
    private final HttpServletRequest request;

    /** 需要脱敏的参数字段名 */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "oldPassword", "newPassword", "confirmPassword",
            "token", "secret", "accessKey", "secretKey"
    );

    @Pointcut("@annotation(com.rx.admin.common.annotation.OperateLog)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        SysLog sysLog = new SysLog();
        sysLog.setStatus(1); // 默认成功

        try {
            // 记录请求信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            OperateLog annotation = signature.getMethod().getAnnotation(OperateLog.class);

            sysLog.setModule(annotation.module());
            sysLog.setOperation(annotation.operation());
            sysLog.setMethod(point.getTarget().getClass().getName() + "." + signature.getName() + "()");

            // 参数脱敏处理
            String rawParams = Arrays.stream(point.getArgs())
                    .map(arg -> arg != null ? arg.toString() : "null")
                    .collect(Collectors.joining(", "));
            sysLog.setParams(sanitizeParams(rawParams));

            // 截断过长参数
            if (sysLog.getParams() != null && sysLog.getParams().length() > 2000) {
                sysLog.setParams(sysLog.getParams().substring(0, 2000) + "...");
            }

            // 记录操作用户
            try {
                Object loginId = StpUtil.getLoginIdDefaultNull();
                if (loginId != null) {
                    sysLog.setUserId(Long.valueOf(loginId.toString()));
                }
                sysLog.setUsername(loginId != null ? loginId.toString() : "anonymous");
            } catch (Exception e) {
                sysLog.setUsername("anonymous");
            }

            // 记录 IP（使用 WebUtils 统一实现）
            sysLog.setIp(WebUtils.getClientIp(request));

            // 执行目标方法
            Object result = point.proceed();
            sysLog.setCostTime(System.currentTimeMillis() - start);

            // 记录返回结果
            if (result != null) {
                String resultStr = result.toString();
                if (resultStr.length() > 1000) {
                    resultStr = resultStr.substring(0, 1000) + "...";
                }
                sysLog.setResult(resultStr);
            }

            return result;
        } catch (Throwable e) {
            log.error("操作日志切面捕获异常: {} - {}", e.getClass().getName(), e.getMessage());
            sysLog.setStatus(0);
            sysLog.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 500
                    ? e.getMessage().substring(0, 500) : e.getMessage());
            sysLog.setCostTime(System.currentTimeMillis() - start);
            throw e;
        } finally {
            // 异步保存日志
            saveLogAsync(sysLog);
        }
    }

    /**
     * 异步保存操作日志
     */
    @Async
    public void saveLogAsync(SysLog sysLog) {
        try {
            logService.save(sysLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 对参数中的敏感字段进行脱敏处理
     * 将 password=xxx 替换为 password=****
     */
    private String sanitizeParams(String params) {
        if (params == null || params.isEmpty()) return params;
        String result = params;
        for (String field : SENSITIVE_FIELDS) {
            // 匹配 field=任意内容，替换为 field=****
            result = result.replaceAll(
                    "(?i)" + field + "=([^,} )&\\s]+)",
                    field + "=****"
            );
        }
        return result;
    }
}

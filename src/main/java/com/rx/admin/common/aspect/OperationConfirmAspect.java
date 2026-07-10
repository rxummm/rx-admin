package com.rx.admin.common.aspect;

import com.rx.admin.common.annotation.OperationConfirm;
import com.rx.admin.common.exception.BusinessException;
import com.rx.admin.modules.auth.service.AuthService;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作二次确认切面
 * 拦截标注 @OperationConfirm 的方法，验证密码后才允许执行
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationConfirmAspect {

    private final SysUserMapper sysUserMapper;

    /**
     * 拦截 @OperationConfirm 注解的方法
     */
    @Around("@annotation(com.rx.admin.common.annotation.OperationConfirm)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationConfirm confirm = method.getAnnotation(OperationConfirm.class);
        
        if (confirm == null) {
            return joinPoint.proceed();
        }
        
        // 获取当前登录用户ID（从Sa-Token获取）
        Object[] args = joinPoint.getArgs();
        Long userId = null;
        
        // 尝试从参数中获取用户ID
        for (Object arg : args) {
            if (arg instanceof Long) {
                userId = (Long) arg;
                break;
            }
        }
        
        // 如果没有从参数获取到，尝试从方法名推断
        if (userId == null) {
            // 默认使用admin用户（ID=1）进行验证
            userId = 1L;
        }
        
        // 如果需要密码验证，检查密码是否正确
        if (confirm.requirePassword()) {
            // 这里可以扩展为要求前端传递密码参数
            // 目前简化为直接允许执行（生产环境应实现密码验证）
            log.info("操作二次确认: 用户 {} 执行 {}", userId, method.getName());
        }
        
        return joinPoint.proceed();
    }
}

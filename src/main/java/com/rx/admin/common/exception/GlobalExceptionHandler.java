package com.rx.admin.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.rx.admin.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import org.springframework.http.HttpHeaders;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===================== 辅助方法 =====================

    /**
     * 构造带 Content-Type: application/json 的 ResponseEntity，
     * 避免 SSE 等端点抛出异常时 Spring 因 Content-Type 不匹配而无法写入响应。
     */
    private ResponseEntity<Result<Object>> json(int code, String message, HttpStatus status) {
        return json(code, message, null, status);
    }

    private ResponseEntity<Result<Object>> json(int code, String message, Object data, HttpStatus status) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Result<Object> body;
        if (data == null) {
            body = Result.fail(code, message);
        } else {
            body = Result.fail(code, message, data);
        }
        return new ResponseEntity<>(body, headers, status);
    }

    // ===================== 业务异常 =====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException e) {
        return json(e.getCode(), e.getMessage(), e.getData(), HttpStatus.OK);
    }

    // ===================== 认证/授权 =====================

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Object>> handleNotLogin(NotLoginException e) {
        if (NotLoginException.KICK_OUT.equals(e.getType())) {
            return json(401, "KICK_OUT", HttpStatus.UNAUTHORIZED);
        }
        return json(401, "未登录或登录已过期", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Result<Object>> handleNotPermission(NotPermissionException e) {
        return json(403, "没有操作权限", HttpStatus.FORBIDDEN);
    }

    // ===================== 参数校验 =====================

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Object>> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return json(400, msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Object>> handleMissingParam(MissingServletRequestParameterException e) {
        return json(400, "缺少必要参数: " + e.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Object>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return json(400, "请求体格式错误，请检查JSON格式", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return json(400, msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleIllegalArgument(IllegalArgumentException e) {
        return json(400, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Result<Object>> handleTypeMismatch(TypeMismatchException e) {
        return json(400, "参数类型错误: " + e.getPropertyName(), HttpStatus.BAD_REQUEST);
    }

    // ===================== 请求映射 =====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Object>> handleNoResourceFound(NoResourceFoundException e) {
        return json(404, "请求的资源不存在", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return json(405, "请求方法不支持: " + e.getMethod(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return json(415, "不支持的媒体类型", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // ===================== 数据/事务 =====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据完整性异常", e);
        return json(409, "数据操作冲突，可能存在重复记录或关联数据未清理", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Result<Object>> handleTransactionSystem(TransactionSystemException e) {
        log.error("事务异常", e);
        Throwable root = e.getRootCause() != null ? e.getRootCause() : e;
        return json(500, "操作失败: " + root.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<Result<Object>> handleUnexpectedRollback(UnexpectedRollbackException e) {
        log.error("事务回滚异常", e);
        return json(500, "操作失败，事务已回滚", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== 兜底 =====================

    /**
     * 异步请求（如 SSE/长轮询）写入时客户端已断开。
     * Spring 6.x 会把它包装为 AsyncRequestNotUsableException 抛到 HandlerExceptionResolver 链，
     * 这是客户端主动关闭 / 刷新 / 断网的正常行为，不应作为系统异常打印 ERROR 日志。
     * <p>
     * 响应已经写过一半或连接已关闭，无法再回写业务结果，返回 204 让框架静默。
     * </p>
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Result<Object>> handleAsyncRequestNotUsable(AsyncRequestNotUsableException e) {
        log.debug("异步请求客户端已断开: {}", e.getMessage());
        // 客户端连接已关闭，无法可靠写入；返回 204 No Content 让框架丢弃响应体
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.error("系统异常", e);
        return json(500, "系统繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
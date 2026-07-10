package com.rx.admin.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;

import org.springframework.http.HttpHeaders;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===================== 辅助方法 =====================

    private ResponseEntity<Result<Object>> json(ErrorCode errorCode, HttpStatus status) {
        return json(errorCode.getCode(), errorCode.getMessage(), null, status);
    }

    private ResponseEntity<Result<Object>> json(ErrorCode errorCode, Object data, HttpStatus status) {
        return json(errorCode.getCode(), errorCode.getMessage(), data, status);
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
            return json(ErrorCode.UNAUTHORIZED, "KICK_OUT", HttpStatus.UNAUTHORIZED);
        }
        return json(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Result<Object>> handleNotPermission(NotPermissionException e) {
        return json(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<Object>> handleNotRole(NotRoleException e) {
        return json(ErrorCode.FORBIDDEN, "无角色权限", HttpStatus.FORBIDDEN);
    }

    // ===================== 参数校验 =====================

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Object>> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return json(ErrorCode.BAD_REQUEST, msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Object>> handleMissingParam(MissingServletRequestParameterException e) {
        return json(ErrorCode.BAD_REQUEST, "缺少必要参数: " + e.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Object>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return json(ErrorCode.BAD_REQUEST, "请求体格式错误，请检查JSON格式", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return json(ErrorCode.BAD_REQUEST, msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleIllegalArgument(IllegalArgumentException e) {
        return json(ErrorCode.BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Result<Object>> handleTypeMismatch(TypeMismatchException e) {
        return json(ErrorCode.BAD_REQUEST, "参数类型错误: " + e.getPropertyName(), HttpStatus.BAD_REQUEST);
    }

    // ===================== 请求映射 =====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Object>> handleNoResourceFound(NoResourceFoundException e) {
        return json(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法: {}", e.getMethod());
        return json(ErrorCode.METHOD_NOT_ALLOWED, "请求方法不支持: " + e.getMethod(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的媒体类型: {}", e.getContentType());
        return json(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "不支持的媒体类型", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // ===================== 数据/事务 =====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据完整性异常", e);
        return json(ErrorCode.DATA_INTEGRITY_VIOLATION, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Result<Object>> handleTransactionSystem(TransactionSystemException e) {
        log.error("事务异常", e);
        Throwable root = e.getRootCause() != null ? e.getRootCause() : e;
        return json(ErrorCode.INTERNAL_ERROR, "操作失败: " + root.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<Result<Object>> handleUnexpectedRollback(UnexpectedRollbackException e) {
        log.error("事务回滚异常", e);
        return json(ErrorCode.INTERNAL_ERROR, "操作失败，事务已回滚", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== 校验/约束 =====================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Object>> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return json(ErrorCode.BAD_REQUEST, msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Result<Object>> handleMissingPathVariable(MissingPathVariableException e) {
        return json(ErrorCode.BAD_REQUEST, "缺少路径参数: " + e.getVariableName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Result<Object>> handleHttpMessageNotWritable(HttpMessageNotWritableException e) {
        log.error("响应序列化失败", e);
        return json(ErrorCode.INTERNAL_ERROR, "响应数据序列化失败", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== 数据库 =====================

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Object>> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一约束冲突", e);
        return json(ErrorCode.CONFLICT, "数据重复，请检查唯一字段", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<Result<Object>> handleBadSqlGrammar(BadSqlGrammarException e) {
        log.error("SQL语法错误", e);
        return json(ErrorCode.INTERNAL_ERROR, "数据库查询异常", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== 异步超时 =====================

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Result<Object>> handleAsyncRequestTimeout(AsyncRequestTimeoutException e) {
        log.debug("异步请求超时: {}", e.getMessage());
        return json(ErrorCode.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // ===================== 兜底 =====================

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Result<Object>> handleAsyncRequestNotUsable(AsyncRequestNotUsableException e) {
        log.debug("异步请求客户端已断开: {}", e.getMessage());
        return json(ErrorCode.SERVICE_UNAVAILABLE, "连接已断开", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Result<Object>> handleIOException(IOException e) {
        String msg = e.getMessage();
        if (msg != null && (msg.contains("中止") || msg.contains("Broken pipe")
                || msg.contains("Connection reset") || msg.contains("断开")
                || msg.contains("aborted"))) {
            log.debug("客户端连接已断开: {}", msg);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.error("IO异常", e);
        return json(ErrorCode.INTERNAL_ERROR, "系统繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.error("系统异常", e);
        return json(ErrorCode.INTERNAL_ERROR, "系统繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
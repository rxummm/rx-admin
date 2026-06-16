package com.rx.admin.common.exception;

/**
 * 业务异常基类
 * <p>
 * 用于 Service 层抛出带状态码的业务异常，由 {@link GlobalExceptionHandler} 统一处理。
 * </p>
 *
 * @author RX Admin
 * @since 2026-06-13
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final Object data;

    public BusinessException(int code, String message) {
        this(code, message, null);
    }

    public BusinessException(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}

package com.rx.admin.common.exception;

public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "系统繁忙"),

    MENU_NOT_FOUND(10001, "菜单不存在"),
    MENU_HAS_CHILDREN(10002, "菜单存在子节点，无法删除"),
    MENU_NAME_EXISTS(10003, "菜单名称已存在"),

    USER_NOT_FOUND(20001, "用户不存在"),
    USERNAME_EXISTS(20002, "用户名已存在"),
    EMAIL_EXISTS(20003, "邮箱已存在"),
    PHONE_EXISTS(20004, "手机号已存在"),
    OLD_PASSWORD_ERROR(20005, "旧密码错误"),

    ROLE_NOT_FOUND(30001, "角色不存在"),
    ROLE_HAS_USERS(30002, "角色存在关联用户，无法删除"),

    DEPT_NOT_FOUND(40001, "部门不存在"),
    DEPT_HAS_CHILDREN(40002, "部门存在子部门，无法删除"),
    DEPT_HAS_USERS(40003, "部门存在关联用户，无法删除"),

    DICT_NOT_FOUND(50001, "字典不存在"),

    CONFIG_NOT_FOUND(60001, "配置不存在"),

    NOTIFICATION_NOT_FOUND(70001, "通知不存在"),
    BLOG_NOT_FOUND(70002, "文章不存在"),

    CAPTCHA_ERROR(80001, "验证码错误"),
    LOGIN_FAILED(80002, "登录失败"),
    ACCOUNT_LOCKED(80003, "账户已锁定"),
    ACCOUNT_DISABLED(80004, "账户已禁用"),

    REPLAY_ATTACK(90001, "请求重复"),
    DATA_INTEGRITY_VIOLATION(90002, "数据完整性冲突"),
    FILE_UPLOAD_FAILED(90003, "文件上传失败"),
    EXPORT_FAILED(90004, "导出失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

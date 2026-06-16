package com.rx.admin.common.constant;

public class MessageConstants {

    public static class User {
        public static final String USERNAME_EXISTS = "用户名已存在";
        public static final String PASSWORD_NOT_EMPTY = "密码不能为空";
        public static final String PASSWORD_INVALID = "密码需以字母开头，包含数字，至少6位";
        public static final String PASSWORD_SAME_AS_USERNAME = "密码不能与用户名相同";
        public static final String PASSWORD_SAME_AS_NICKNAME = "密码不能与昵称相同";
        public static final String EMAIL_NOT_EMPTY = "邮箱不能为空";
        public static final String EMAIL_INVALID = "邮箱格式不正确";
        public static final String EMAIL_TOO_LONG = "邮箱长度不能超过100个字符";
        public static final String PHONE_NOT_EMPTY = "手机号不能为空";
        public static final String PHONE_INVALID = "手机号格式不正确，请输入11位有效手机号";
        public static final String USER_ID_NOT_EMPTY = "用户ID不能为空";
        public static final String USER_NOT_FOUND = "用户不存在";
        public static final String CANNOT_DELETE_SELF = "不能删除自己";
        public static final String WELCOME_MESSAGE = "您的账号已创建成功，用户名：";
        public static final String ROLE_PREFIX = "，角色：";
        public static final String PASSWORD_CHANGED_NOTIFICATION = "密码修改通知";
        public static final String PASSWORD_CHANGED_CONTENT = "您的账号密码已被管理员修改，如非本人操作请立即联系管理员。";
        public static final String ACCOUNT_ENABLED_NOTIFICATION = "账号已启用";
        public static final String ACCOUNT_ENABLED_CONTENT = "您的账号已被启用，现在可以正常登录系统。";
        public static final String ACCOUNT_DISABLED_NOTIFICATION = "账号已禁用";
        public static final String ACCOUNT_DISABLED_CONTENT = "您的账号已被禁用，如有疑问请联系管理员。";
        public static final String ROLE_CHANGED_NOTIFICATION = "角色变更通知";
        public static final String ROLE_CHANGED_PREFIX = "您的角色已被管理员更新。";
        public static final String ROLE_ADDED_PREFIX = " 新增：";
        public static final String ROLE_REMOVED_PREFIX = " 移除：";
        public static final String ROLE_CURRENT_PREFIX = " 当前角色：";
        public static final String ROLE_NONE = "无";
        public static final String SEND_WELCOME_MESSAGE_FAILED = "发送欢迎消息失败";
        public static final String SEND_NOTIFICATION_FAILED = "发送消息通知失败";
    }

    public static class Role {
        public static final String ROLE_NOT_FOUND = "角色不存在";
        public static final String ROLE_NAME_EXISTS = "角色名称已存在";
        public static final String ROLE_HAS_USERS = "角色存在关联用户，无法删除";
    }

    public static class Dept {
        public static final String DEPT_NOT_FOUND = "部门不存在";
        public static final String DEPT_HAS_CHILDREN = "部门存在子部门，无法删除";
        public static final String DEPT_HAS_USERS = "部门存在关联用户，无法删除";
    }

    public static class Menu {
        public static final String MENU_NOT_FOUND = "菜单不存在";
        public static final String MENU_HAS_CHILDREN = "菜单存在子节点，无法删除";
        public static final String MENU_NAME_EXISTS = "菜单名称已存在";
    }

    public static class Dict {
        public static final String DICT_TYPE_NOT_FOUND = "字典类型不存在";
        public static final String DICT_DATA_NOT_FOUND = "字典数据不存在";
        public static final String DICT_TYPE_EXISTS = "字典类型已存在";
    }

    public static class Config {
        public static final String CONFIG_NOT_FOUND = "配置不存在";
        public static final String CONFIG_KEY_EXISTS = "配置键已存在";
    }

    public static class Notice {
        public static final String NOTICE_NOT_FOUND = "通知不存在";
    }

    public static class Message {
        public static final String MESSAGE_NOT_FOUND = "消息不存在";
    }

    public static class Blog {
        public static final String BLOG_NOT_FOUND = "文章不存在";
        public static final String IDS_NOT_EMPTY = "ids不能为空";
    }

    public static class IpRule {
        public static final String IP_RULE_MODE_INVALID = "模式只能是 BLACK / WHITE / OFF";
    }

    public static class Auth {
        public static final String CAPTCHA_NOT_EMPTY = "验证码不能为空";
        public static final String CAPTCHA_INVALID = "验证码错误或已过期";
        public static final String ACCOUNT_LOCKED = "账号已被锁定，请 %d 分钟后重试";
        public static final String TOO_MANY_REQUESTS = "请求过于频繁，请稍后再试";
    }

    public static class Database {
        public static final String SQL_NOT_EMPTY = "SQL不能为空";
        public static final String SQL_ONLY_READ = "仅支持 SELECT/SHOW/DESCRIBE/EXPLAIN/WITH 等只读语句";
        public static final String SQL_EXECUTE_ERROR = "SQL执行错误: %s";
        public static final String GET_TABLES_FAILED = "获取表列表失败: %s";
        public static final String GET_TABLE_COLUMNS_FAILED = "获取表结构失败: %s";
        public static final String GET_POOL_STATUS_FAILED = "获取连接池状态失败: %s";
    }

    public static class Cache {
        public static final String CACHE_NOT_FOUND = "缓存不存在";
        public static final String CACHE_CLEARED = "缓存 %s 已清除";
        public static final String ALL_CACHE_CLEARED = "所有缓存已清除";
    }

    public static class DevTools {
        public static final String INPUT_NOT_EMPTY = "输入不能为空";
        public static final String JSON_FORMAT_ERROR = "JSON格式错误: %s";
        public static final String PARAM_INCOMPLETE = "参数不完整";
    }
}
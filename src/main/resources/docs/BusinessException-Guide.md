# 业务异常基类

用于在 Service 层抛出带状态码的业务异常，由 `GlobalExceptionHandler` 统一处理。

## 使用示例

```java
// Service 中抛出
public void updateUser(UpdateUserDTO dto) {
    if (dto.getEmail() == null) {
        throw new BusinessException(400, "邮箱不能为空");
    }
    // ...
}

// 带数据的异常（前端可根据 data 做特殊处理）
public void assignRole(Long userId, Long roleId) {
    if (userRoleMapper.exists(userId, roleId)) {
        throw new BusinessException(409, "用户已拥有该角色", Map.of("userId", userId, "roleId", roleId));
    }
}
```

## 与 GlobalExceptionHandler 集成

`GlobalExceptionHandler` 中需添加：

```java
@ExceptionHandler(BusinessException.class)
public Result<Void> handleBusinessException(BusinessException e) {
    return Result.error(e.getCode(), e.getMessage(), e.getData());
}
```

## 错误码约定

| 范围 | 含义 |
|------|------|
| 400-499 | 客户端错误（参数校验、业务规则） |
| 500-599 | 服务端错误（数据库、外部服务） |
| 409 | 冲突（如重复关联） |
| 403 | 权限不足 |
| 404 | 资源不存在 |

/**
 * 错误码常量 - 与后端 ErrorCode 枚举保持一致
 * 用于前端精准判断特定业务错误
 */
export const ERROR_CODE = {
  // 通用成功/失败
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_ERROR: 500,

  // 菜单模块 (10001-10099)
  MENU_NOT_FOUND: 10001,
  MENU_HAS_CHILDREN: 10002,
  MENU_NAME_EXISTS: 10003,

  // 用户模块 (20001-20099)
  USER_NOT_FOUND: 20001,
  USERNAME_EXISTS: 20002,
  EMAIL_EXISTS: 20003,
  PHONE_EXISTS: 20004,
  OLD_PASSWORD_ERROR: 20005,

  // 角色模块 (30001-30099)
  ROLE_NOT_FOUND: 30001,
  ROLE_HAS_USERS: 30002,

  // 部门模块 (40001-40099)
  DEPT_NOT_FOUND: 40001,
  DEPT_HAS_CHILDREN: 40002,
  DEPT_HAS_USERS: 40003,

  // 字典模块 (50001-50099)
  DICT_NOT_FOUND: 50001,

  // 配置模块 (60001-60099)
  CONFIG_NOT_FOUND: 60001,

  // 通知模块 (70001-70099)
  NOTIFICATION_NOT_FOUND: 70001,

  // 认证模块 (80001-80099)
  CAPTCHA_ERROR: 80001,
  LOGIN_FAILED: 80002,
  ACCOUNT_LOCKED: 80003,
  ACCOUNT_DISABLED: 80004,

  // 安全模块 (90001-90099)
  REPLAY_ATTACK: 90001,
  DATA_INTEGRITY_VIOLATION: 90002,
  FILE_UPLOAD_FAILED: 90003,
  EXPORT_FAILED: 90004
}

/**
 * 根据错误码获取中文描述
 */
export function getErrorMessage(code) {
  const entry = Object.entries(ERROR_CODE).find(([, value]) => value === code)
  if (entry) {
    return entry[0]
      .replace(/_/g, '')
      .replace(/([A-Z])/g, ' $1')
      .trim()
  }
  return `未知错误 (${code})`
}

/**
 * 判断是否为业务错误（非通用 HTTP 错误）
 */
export function isBusinessError(code) {
  return code >= 10001
}

/**
 * 判断是否为成功响应
 */
export function isSuccess(code) {
  return code === ERROR_CODE.SUCCESS
}

/**
 * 判断是否为需要登录的错误
 */
export function isUnauthorized(code) {
  return code === ERROR_CODE.UNAUTHORIZED
}

/**
 * 判断是否为权限不足的错误
 */
export function isForbidden(code) {
  return code === ERROR_CODE.FORBIDDEN
}

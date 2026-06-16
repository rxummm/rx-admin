/**
 * modules/system API 聚合入口
 * 按业务模块组织 API 调用，便于维护和迁移
 *
 * 用法：import { userApi, roleApi, menuApi } from '@/api/modules/system'
 */

export { default as userApi } from '@/api/user.js'
export { default as roleApi } from '@/api/role.js'
export { default as menuApi } from '@/api/menu.js'
export { default as deptApi } from '@/api/dept.js'
export { default as dictApi } from '@/api/dict.js'
export { default as configApi } from '@/api/config.js'
export { default as fileApi } from '@/api/file.js'
export { default as ipRuleApi } from '@/api/ipRule.js'

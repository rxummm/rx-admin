/**
 * 用户模块 类型定义
 * 对齐后端 DTO/VO 结构
 *
 * 后端对应包：com.rx.admin.modules.system.user.dto / .vo
 */

/**
 * @typedef {Object} UserCreateDTO 创建用户请求
 * @property {string} username - 用户名
 * @property {string} password - 密码
 * @property {string} nickname - 昵称
 * @property {string} [email] - 邮箱
 * @property {string} [phone] - 手机号
 * @property {number} [gender] - 性别 0未知 1男 2女
 * @property {number} [status] - 状态 0禁用 1启用
 * @property {number} [deptId] - 部门ID
 * @property {number[]} [roleIds] - 角色ID列表
 */

/**
 * @typedef {Object} UserUpdateDTO 更新用户请求
 * @property {number} id - 用户ID
 * @property {string} username - 用户名
 * @property {string} nickname - 昵称
 * @property {string} [email] - 邮箱
 * @property {string} [phone] - 手机号
 * @property {string} [password] - 密码（不填则不修改）
 * @property {number} [gender] - 性别
 * @property {number} [status] - 状态
 * @property {number} [deptId] - 部门ID
 * @property {number[]} [roleIds] - 角色ID列表
 */

/**
 * @typedef {Object} UserQueryDTO 查询用户参数
 * @property {string} [keyword] - 搜索关键词
 * @property {string} [username] - 用户名精确搜索
 * @property {number} [status] - 状态筛选
 * @property {number} [deptId] - 部门筛选
 */

/**
 * @typedef {Object} UserVO 用户视图对象
 * @property {number} id - 用户ID
 * @property {string} username - 用户名
 * @property {string} nickname - 昵称
 * @property {string} [email] - 邮箱（脱敏后）
 * @property {string} [phone] - 手机号（脱敏后）
 * @property {string} [avatar] - 头像URL
 * @property {number} gender - 性别
 * @property {number} status - 状态
 * @property {number} [deptId] - 部门ID
 * @property {string} [deptName] - 部门名称
 * @property {number[]} [roleIds] - 角色ID列表
 * @property {string[]} [roleNames] - 角色名称列表
 * @property {string} [remark] - 备注
 * @property {string} createTime - 创建时间
 * @property {string} updateTime - 更新时间
 */

export default {}

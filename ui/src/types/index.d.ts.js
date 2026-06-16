/**
 * RX Admin 前端类型定义 (JSDoc)
 * 与后端 modules/ DTO/VO 结构对齐
 * @module types
 */

// ==================== 用户管理 ====================

/**
 * @typedef {Object} UserCreateDTO
 * @property {string} username - 用户名
 * @property {string} password - 密码
 * @property {string} nickname - 昵称
 * @property {string} email - 邮箱
 * @property {string} phone - 手机号
 * @property {number} deptId - 部门ID
 * @property {number} status - 状态(0禁用1启用)
 * @property {number[]} roleIds - 角色ID列表
 */

/**
 * @typedef {Object} UserUpdateDTO
 * @property {number} id - 用户ID
 * @property {string} nickname - 昵称
 * @property {string} email - 邮箱
 * @property {string} phone - 手机号
 * @property {number} deptId - 部门ID
 * @property {number} status - 状态
 * @property {number[]} roleIds - 角色ID列表
 */

/**
 * @typedef {Object} UserQueryDTO
 * @property {string} username - 用户名
 * @property {string} nickname - 昵称
 * @property {number} deptId - 部门ID
 * @property {number} status - 状态
 * @property {number} page - 页码
 * @property {number} pageSize - 每页条数
 */

/**
 * @typedef {Object} UserVO
 * @property {number} id - 用户ID
 * @property {string} username - 用户名
 * @property {string} nickname - 昵称
 * @property {string} email - 邮箱
 * @property {string} phone - 手机号
 * @property {number} deptId - 部门ID
 * @property {string} deptName - 部门名称
 * @property {number} status - 状态
 * @property {number[]} roleIds - 角色ID列表
 * @property {string[]} roleNames - 角色名称列表
 * @property {string} createTime - 创建时间
 * @property {string} updateTime - 更新时间
 */

// ==================== 角色管理 ====================

/**
 * @typedef {Object} RoleCreateDTO
 * @property {string} roleName - 角色名称
 * @property {string} roleCode - 角色编码
 * @property {string} description - 描述
 * @property {number} sort - 排序
 * @property {number} status - 状态
 * @property {number} dataScope - 数据权限范围
 * @property {string} dataDeptIds - 自定义数据权限部门ID
 * @property {number[]} menuIds - 菜单ID列表
 */

/**
 * @typedef {Object} RoleUpdateDTO
 * @property {number} id - 角色ID
 * @property {string} roleName - 角色名称
 * @property {string} roleCode - 角色编码
 * @property {string} description - 描述
 * @property {number} sort - 排序
 * @property {number} status - 状态
 * @property {number} dataScope - 数据权限范围
 * @property {string} dataDeptIds - 自定义数据权限部门ID
 * @property {number[]} menuIds - 菜单ID列表
 */

/**
 * @typedef {Object} RoleQueryDTO
 * @property {string} roleName - 角色名称
 * @property {string} roleCode - 角色编码
 * @property {number} status - 状态
 * @property {number} page - 页码
 * @property {number} pageSize - 每页条数
 */

/**
 * @typedef {Object} RoleVO
 * @property {number} id - 角色ID
 * @property {string} roleName - 角色名称
 * @property {string} roleCode - 角色编码
 * @property {string} description - 描述
 * @property {number} sort - 排序
 * @property {number} status - 状态
 * @property {number} dataScope - 数据权限范围
 * @property {string} dataDeptIds - 自定义数据权限部门ID
 * @property {number[]} menuIds - 菜单ID列表
 * @property {string} createTime - 创建时间
 * @property {string} updateTime - 更新时间
 */

// ==================== 菜单管理 ====================

/**
 * @typedef {Object} MenuCreateDTO
 * @property {number} parentId - 父菜单ID
 * @property {string} menuName - 菜单名称
 * @property {number} menuType - 菜单类型(0目录1菜单2按钮)
 * @property {string} path - 路由路径
 * @property {string} component - 组件路径
 * @property {string} perms - 权限标识
 * @property {string} icon - 图标
 * @property {number} sort - 排序
 * @property {number} visible - 是否可见
 * @property {number} status - 状态
 */

/**
 * @typedef {Object} MenuVO
 * @property {number} id - 菜单ID
 * @property {number} parentId - 父菜单ID
 * @property {string} menuName - 菜单名称
 * @property {number} menuType - 菜单类型
 * @property {string} path - 路由路径
 * @property {string} component - 组件路径
 * @property {string} perms - 权限标识
 * @property {string} icon - 图标
 * @property {number} sort - 排序
 * @property {number} visible - 是否可见
 * @property {number} status - 状态
 * @property {string} createTime - 创建时间
 * @property {MenuVO[]} children - 子菜单
 */

// ==================== 部门管理 ====================

/**
 * @typedef {Object} DeptCreateDTO
 * @property {number} parentId - 父部门ID
 * @property {string} deptName - 部门名称
 * @property {string} leader - 负责人
 * @property {string} phone - 电话
 * @property {string} email - 邮箱
 * @property {number} sort - 排序
 * @property {number} status - 状态
 */

/**
 * @typedef {Object} DeptVO
 * @property {number} id - 部门ID
 * @property {number} parentId - 父部门ID
 * @property {string} deptName - 部门名称
 * @property {string} leader - 负责人
 * @property {string} phone - 电话
 * @property {string} email - 邮箱
 * @property {number} sort - 排序
 * @property {number} status - 状态
 * @property {string} createTime - 创建时间
 * @property {DeptVO[]} children - 子部门
 */

// ==================== 系统配置 ====================

/**
 * @typedef {Object} ConfigCreateDTO
 * @property {string} configKey - 配置键
 * @property {string} configValue - 配置值
 * @property {string} configType - 配置类型
 * @property {string} description - 描述
 * @property {string} groupName - 分组名称
 * @property {number} sortOrder - 排序
 */

/**
 * @typedef {Object} ConfigVO
 * @property {number} id - 配置ID
 * @property {string} configKey - 配置键
 * @property {string} configValue - 配置值
 * @property {string} configType - 配置类型
 * @property {string} description - 描述
 * @property {string} groupName - 分组名称
 * @property {number} sortOrder - 排序
 * @property {string} createTime - 创建时间
 */

// ==================== 字典管理 ====================

/**
 * @typedef {Object} DictTypeCreateDTO
 * @property {string} dictName - 字典名称
 * @property {string} dictType - 字典类型
 * @property {number} status - 状态
 * @property {string} remark - 备注
 */

/**
 * @typedef {Object} DictTypeVO
 * @property {number} id - 字典类型ID
 * @property {string} dictName - 字典名称
 * @property {string} dictType - 字典类型
 * @property {number} status - 状态
 * @property {string} remark - 备注
 * @property {string} createTime - 创建时间
 */

/**
 * @typedef {Object} DictDataCreateDTO
 * @property {number} typeId - 字典类型ID
 * @property {string} dictLabel - 字典标签
 * @property {string} dictValue - 字典值
 * @property {string} cssClass - CSS样式
 * @property {string} listClass - 列表样式
 * @property {number} sort - 排序
 * @property {number} status - 状态
 * @property {string} remark - 备注
 */

/**
 * @typedef {Object} DictDataVO
 * @property {number} id - 字典数据ID
 * @property {number} typeId - 字典类型ID
 * @property {string} dictLabel - 字典标签
 * @property {string} dictValue - 字典值
 * @property {string} cssClass - CSS样式
 * @property {string} listClass - 列表样式
 * @property {number} sort - 排序
 * @property {number} status - 状态
 * @property {string} remark - 备注
 * @property {string} createTime - 创建时间
 */

// ==================== IP规则 ====================

/**
 * @typedef {Object} IpRuleCreateDTO
 * @property {string} ipAddress - IP地址
 * @property {string} ruleType - 规则类型(BLACK/WHITE)
 * @property {string} description - 描述
 * @property {number} status - 状态
 */

/**
 * @typedef {Object} IpRuleVO
 * @property {number} id - 规则ID
 * @property {string} ipAddress - IP地址
 * @property {string} ruleType - 规则类型
 * @property {string} description - 描述
 * @property {number} status - 状态
 * @property {string} createTime - 创建时间
 */

// ==================== 文件管理 ====================

/**
 * @typedef {Object} FileVO
 * @property {number} id - 文件ID
 * @property {string} originalName - 原始文件名
 * @property {string} storedName - 存储文件名
 * @property {string} path - 文件路径
 * @property {number} size - 文件大小
 * @property {string} mimeType - MIME类型
 * @property {string} storageType - 存储类型
 * @property {string} category - 分类
 * @property {number} uploader - 上传者
 * @property {string} createTime - 创建时间
 */

// ==================== 通知公告 ====================

/**
 * @typedef {Object} NoticeCreateDTO
 * @property {string} title - 标题
 * @property {string} content - 内容
 * @property {string} noticeType - 通知类型
 * @property {string} category - 分类
 * @property {string} linkPath - 链接路径
 * @property {number} status - 状态
 */

/**
 * @typedef {Object} NoticeVO
 * @property {number} id - 通知ID
 * @property {string} title - 标题
 * @property {string} content - 内容
 * @property {string} noticeType - 通知类型
 * @property {string} category - 分类
 * @property {string} linkPath - 链接路径
 * @property {number} status - 状态
 * @property {number} createBy - 创建者ID
 * @property {string} createByName - 创建者名称
 * @property {string} createTime - 创建时间
 */

// ==================== 消息中心 ====================

/**
 * @typedef {Object} MessageCreateDTO
 * @property {number} receiverId - 接收者ID
 * @property {string} title - 标题
 * @property {string} content - 内容
 * @property {string} messageType - 消息类型
 * @property {string} linkPath - 链接路径
 */

/**
 * @typedef {Object} MessageVO
 * @property {number} id - 消息ID
 * @property {number} senderId - 发送者ID
 * @property {number} receiverId - 接收者ID
 * @property {string} senderName - 发送者名称
 * @property {string} receiverUsername - 接收者用户名
 * @property {string} title - 标题
 * @property {string} content - 内容
 * @property {string} messageType - 消息类型
 * @property {number} isRead - 是否已读
 * @property {string} readTime - 阅读时间
 * @property {string} linkPath - 链接路径
 * @property {string} createTime - 创建时间
 */

// ==================== 定时任务 ====================

/**
 * @typedef {Object} JobCreateDTO
 * @property {string} jobName - 任务名称
 * @property {string} beanName - Bean名称
 * @property {string} methodName - 方法名
 * @property {string} cronExpression - Cron表达式
 * @property {string} params - 参数
 * @property {number} status - 状态
 * @property {string} remark - 备注
 */

/**
 * @typedef {Object} JobVO
 * @property {number} id - 任务ID
 * @property {string} jobName - 任务名称
 * @property {string} beanName - Bean名称
 * @property {string} methodName - 方法名
 * @property {string} cronExpression - Cron表达式
 * @property {string} params - 参数
 * @property {number} status - 状态
 * @property {string} remark - 备注
 * @property {string} createTime - 创建时间
 */

// ==================== 操作日志 ====================

/**
 * @typedef {Object} OperateLogVO
 * @property {number} id - 日志ID
 * @property {string} module - 操作模块
 * @property {string} action - 操作动作
 * @property {string} method - 方法名
 * @property {string} requestUrl - 请求URL
 * @property {string} requestMethod - 请求方法
 * @property {string} requestParams - 请求参数
 * @property {string} operatorName - 操作人
 * @property {string} ip - IP地址
 * @property {number} costTime - 耗时(ms)
 * @property {number} status - 状态
 * @property {string} errorMsg - 错误信息
 * @property {string} createTime - 操作时间
 */

// ==================== 登录日志 ====================

/**
 * @typedef {Object} LoginLogVO
 * @property {number} id - 日志ID
 * @property {string} username - 用户名
 * @property {string} ip - IP地址
 * @property {string} loginLocation - 登录地点
 * @property {string} browser - 浏览器
 * @property {string} os - 操作系统
 * @property {number} status - 状态
 * @property {string} msg - 消息
 * @property {string} loginTime - 登录时间
 */

// ==================== 慢查询 ====================

/**
 * @typedef {Object} SlowQueryVO
 * @property {number} id - 记录ID
 * @property {string} sql - SQL语句
 * @property {number} costTime - 耗时(ms)
 * @property {string} params - 参数
 * @property {string} createTime - 记录时间
 */

// ==================== 收藏夹 ====================

/**
 * @typedef {Object} FavoriteCreateDTO
 * @property {number} menuId - 菜单ID
 * @property {string} name - 名称
 * @property {string} path - 路径
 * @property {string} icon - 图标
 * @property {number} sortOrder - 排序
 */

/**
 * @typedef {Object} FavoriteVO
 * @property {number} id - 收藏ID
 * @property {number} userId - 用户ID
 * @property {number} menuId - 菜单ID
 * @property {string} name - 名称
 * @property {string} path - 路径
 * @property {string} icon - 图标
 * @property {number} sortOrder - 排序
 * @property {string} createTime - 收藏时间
 */

// ==================== 技术博客 ====================

/**
 * @typedef {Object} TechBlogCreateDTO
 * @property {string} title - 标题
 * @property {string} content - 内容
 * @property {string} summary - 摘要
 * @property {string} category - 分类
 * @property {string} tags - 标签
 * @property {string} coverUrl - 封面URL
 * @property {number} status - 状态
 */

/**
 * @typedef {Object} TechBlogVO
 * @property {number} id - 文章ID
 * @property {string} title - 标题
 * @property {string} content - 内容
 * @property {string} summary - 摘要
 * @property {string} category - 分类
 * @property {string} tags - 标签
 * @property {string} coverUrl - 封面URL
 * @property {number} status - 状态
 * @property {number} viewCount - 浏览量
 * @property {string} createTime - 创建时间
 * @property {string} updateTime - 更新时间
 */

// ==================== 通用响应 ====================

/**
 * @typedef {Object} PageResult
 * @template T
 * @property {T[]} records - 数据列表
 * @property {number} total - 总记录数
 * @property {number} page - 当前页码
 * @property {number} pageSize - 每页条数
 */

/**
 * @typedef {Object} Result
 * @template T
 * @property {number} code - 状态码(200成功)
 * @property {string} message - 提示信息
 * @property {T} data - 响应数据
 */

export {}

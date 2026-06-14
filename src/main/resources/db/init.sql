-- =============================================
-- RX Admin 初始化数据库脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS rx_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rx_admin;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(加密)',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(100) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
    -- 测试阶段暂时移除邮箱唯一约束
    -- UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT '' COMMENT '角色描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 菜单/权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型 1目录 2菜单 3按钮',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(200) DEFAULT '' COMMENT '组件路径',
    perms VARCHAR(200) DEFAULT '' COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '' COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见 0隐藏 1显示',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 操作日志表
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT '' COMMENT '操作用户名',
    module VARCHAR(50) DEFAULT '' COMMENT '操作模块',
    operation VARCHAR(100) DEFAULT '' COMMENT '操作类型',
    method VARCHAR(200) DEFAULT '' COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    ip VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态 0失败 1成功',
    error_msg VARCHAR(2000) DEFAULT '' COMMENT '错误信息',
    cost_time BIGINT DEFAULT 0 COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    leader VARCHAR(50) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 字典类型表
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    type_id BIGINT NOT NULL COMMENT '字典类型ID',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典键值',
    css_class VARCHAR(100) DEFAULT '' COMMENT '样式属性',
    list_class VARCHAR(100) DEFAULT '' COMMENT '表格回显样式',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 通知公告表
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    notice_type VARCHAR(20) DEFAULT '1' COMMENT '公告类型 1通知 2公告',
    status TINYINT DEFAULT 1 COMMENT '状态 0关闭 1正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建者ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化管理员用户 (密码: admin123)
-- Spring Security BCrypt 加密后的密码: admin123
INSERT INTO sys_user (username, password, nickname, email, status) VALUES
('admin', '$2a$10$P.erhfUqGdaTlJg8N2xquejjm3k2lJSpUMLSsDMH5Gnzoc/NM1AmK', '超级管理员', 'admin@rx.com', 1);

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, description, sort) VALUES
('超级管理员', 'admin', '拥有所有权限', 1),
('普通用户', 'user', '普通用户权限', 2),
('运维管理员', 'operator', '拥有所有查看权限，无新增/编辑/删除权限', 3);

-- 初始化菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
-- 仪表盘
(12, 0, '仪表盘', 2, '/dashboard', 'dashboard/index', 'dashboard', 'DataAnalysis', 0),
-- 知识图谱
(2000, 0, '知识图谱', 2, '/dashboard/knowledgeGraph', 'dashboard/knowledgeGraph/index', '', 'Connection', 1),

-- 系统管理
(1, 0, '系统管理', 1, '/system', '', '', 'Setting', 2),
(2, 1, '用户管理', 2, '/system/user', 'system/user/index', 'sys:user:list', 'User', 1),
(3, 2, '用户查询', 3, '', '', 'sys:user:query', '', 1),
(4, 2, '用户新增', 3, '', '', 'sys:user:add', '', 2),
(5, 2, '用户编辑', 3, '', '', 'sys:user:edit', '', 3),
(6, 2, '用户删除', 3, '', '', 'sys:user:delete', '', 4),
(7, 1, '角色管理', 2, '/system/role', 'system/role/index', 'sys:role:list', 'UserFilled', 2),
(8, 7, '角色查询', 3, '', '', 'sys:role:query', '', 1),
(9, 7, '角色新增', 3, '', '', 'sys:role:add', '', 2),
(10, 7, '角色编辑', 3, '', '', 'sys:role:edit', '', 3),
(11, 7, '角色删除', 3, '', '', 'sys:role:delete', '', 4),
(14, 1, '菜单管理', 2, '/system/menu', 'system/menu/index', 'sys:menu:list', 'Menu', 3),
(15, 14, '菜单查询', 3, '', '', 'sys:menu:query', '', 1),
(16, 14, '菜单新增', 3, '', '', 'sys:menu:add', '', 2),
(17, 14, '菜单编辑', 3, '', '', 'sys:menu:edit', '', 3),
(18, 14, '菜单删除', 3, '', '', 'sys:menu:delete', '', 4),
(19, 1, '部门管理', 2, '/system/dept', 'system/dept/index', 'sys:dept:list', 'OfficeBuilding', 4),
(20, 19, '部门查询', 3, '', '', 'sys:dept:query', '', 1),
(21, 19, '部门新增', 3, '', '', 'sys:dept:add', '', 2),
(22, 19, '部门编辑', 3, '', '', 'sys:dept:edit', '', 3),
(23, 19, '部门删除', 3, '', '', 'sys:dept:delete', '', 4),

-- 系统工具
(24, 0, '系统工具', 1, '/tool', '', '', 'Tools', 200),
(25, 24, '字典管理', 2, '/tool/dict', 'tool/dict/index', 'sys:dict:list', 'Notebook', 1),
(26, 25, '字典查询', 3, '', '', 'sys:dict:query', '', 1),
(27, 25, '字典新增', 3, '', '', 'sys:dict:add', '', 2),
(28, 25, '字典编辑', 3, '', '', 'sys:dict:edit', '', 3),
(29, 25, '字典删除', 3, '', '', 'sys:dict:delete', '', 4),
(102, 24, '行政区划', 2, '/tool/region', 'tool/region/index', 'tool:region:list', 'MapLocation', 2),
(103, 102, '区划查询', 3, '', '', 'tool:region:query', '', 1),
(104, 102, '区划新增', 3, '', '', 'tool:region:add', '', 2),
(105, 102, '区划编辑', 3, '', '', 'tool:region:edit', '', 3),
(106, 102, '区划删除', 3, '', '', 'tool:region:delete', '', 4),

-- 内容管理
(30, 0, '内容管理', 1, '/content', '', '', 'Reading', 300),
(31, 30, '通知公告', 2, '/content/notice', 'content/notice/index', 'content:notice:list', 'Bell', 1),
(32, 31, '公告查询', 3, '', '', 'content:notice:query', '', 1),
(33, 31, '公告新增', 3, '', '', 'content:notice:add', '', 2),
(34, 31, '公告编辑', 3, '', '', 'content:notice:edit', '', 3),
(35, 31, '公告删除', 3, '', '', 'content:notice:delete', '', 4),

-- AS400管理
(107, 0, 'AS400管理', 1, '/as400', '', '', 'Monitor', 350),
(108, 107, '对象列表', 2, '/as400/objects', 'as400/objects/index', 'as400:objects:list', 'List', 1),
(109, 108, 'AS400查询', 3, '', '', 'as400:objects:query', '', 1),

-- 系统监控
(36, 0, '系统监控', 1, '/monitor', '', '', 'Monitor', 400),
(13, 36, '操作日志', 2, '/monitor/log', 'monitor/log/index', 'sys:log:list', 'Document', 1),
(37, 36, '在线用户', 2, '/monitor/online', 'monitor/online/index', 'monitor:online:list', 'Connection', 2),
(38, 37, '强制下线', 3, '', '', 'monitor:online:kick', '', 1);

-- 用户角色关联 (admin -> 超级管理员)
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 角色菜单关联 (超级管理员拥有所有菜单)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 角色菜单关联 (运维管理员拥有所有查询权限)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE perms LIKE '%:query' OR perms = 'dashboard' OR menu_type = 1;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, 37);

-- 角色菜单关联 (普通用户只拥有仪表盘和系统监控)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 12), (2, 36), (2, 13), (2, 37);

-- 初始部门数据
INSERT INTO sys_dept (id, parent_id, dept_name, leader, phone, sort) VALUES
(1, 0, '总公司', '张总', '13800000001', 0),
(2, 1, '技术部', '李经理', '13800000002', 1),
(3, 1, '产品部', '王经理', '13800000003', 2),
(4, 1, '市场部', '赵经理', '13800000004', 3),
(5, 2, '前端组', '孙组长', '13800000005', 1),
(6, 2, '后端组', '周组长', '13800000006', 2);

-- 初始字典类型数据
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark) VALUES
(1, '用户性别', 'sys_user_sex', 1, '用户性别列表'),
(2, '通知类型', 'sys_notice_type', 1, '通知公告类型'),
(3, '系统状态', 'sys_normal_disable', 1, '通用状态');

-- 初始字典数据
INSERT INTO sys_dict_data (type_id, dict_label, dict_value, css_class, sort, status) VALUES
(1, '男', '1', '', 1, 1),
(1, '女', '2', '', 2, 1),
(1, '未知', '0', '', 3, 1),
(2, '通知', '1', 'primary', 1, 1),
(2, '公告', '2', 'warning', 2, 1),
(3, '正常', '1', 'success', 1, 1),
(3, '停用', '0', 'danger', 2, 1);

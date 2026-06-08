-- =============================================
-- 权限申请功能 - 建表 + 菜单注册 SQL
-- =============================================

-- 权限申请表
DROP TABLE IF EXISTS sys_permission_request;
CREATE TABLE sys_permission_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    username VARCHAR(50) NOT NULL COMMENT '申请人用户名',
    menu_ids TEXT NOT NULL COMMENT '申请的菜单ID列表(JSON数组)',
    menu_names TEXT COMMENT '申请的菜单名称列表(JSON数组)',
    status TINYINT DEFAULT 0 COMMENT '状态 0待审批 1已通过 2已拒绝',
    audit_user_id BIGINT DEFAULT NULL COMMENT '审批人ID',
    audit_username VARCHAR(50) DEFAULT '' COMMENT '审批人用户名',
    audit_remark VARCHAR(500) DEFAULT '' COMMENT '审批备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限申请表';

-- 权限申请菜单（所有登录用户可见，仅非admin用户显示）
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (300, 0, '权限申请', 2, '/permission/request', 'permission/request/index', '', 'Key', 500, 1, 1, 0, NOW(), NOW());

-- 将权限申请菜单分配给 user 角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 300);

-- 修改 user 角色（role_id=2）默认权限：仪表盘 + 四大名著
-- 先清除 user 角色旧权限
DELETE FROM sys_role_menu WHERE role_id = 2;
-- 仪表盘
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 12);
-- 四大名著（一级目录）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 229);
-- 四大名著（二级目录：红楼梦/西游/三国/水浒）
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu WHERE parent_id IN (230, 236, 242, 247) AND menu_type = 2;
-- 四大名著（三级页面菜单）
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu WHERE parent_id IN (231, 232, 233, 237, 238, 239, 243, 244, 248, 249) AND menu_type = 2;
-- 权限申请
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 300);

-- =============================================
-- 用户直接授权表（个性化权限）
-- 不通过角色，直接将菜单/按钮权限关联到用户
-- =============================================
DROP TABLE IF EXISTS sys_user_menu;
CREATE TABLE sys_user_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    menu_id BIGINT NOT NULL COMMENT '菜单/按钮ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    UNIQUE KEY uk_user_menu (user_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户直接授权表';

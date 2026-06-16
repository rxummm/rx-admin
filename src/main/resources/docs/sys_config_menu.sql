-- ============================================================
-- 系统配置管理 - 菜单 SQL
-- 插入到 系统管理(1) 下，排序在 部门管理 之后
-- ============================================================

-- 1. 插入菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_type, `path`, component, perms, icon, sort, visible, status)
SELECT 1, '系统配置', 2, '/system/config', 'system/config/index', 'system:config:list', 'Setting', 5, 1, 1
WHERE NOT EXISTS (SELECT id FROM sys_menu WHERE perms = 'system:config:list');

-- 2. 插入按钮权限（子菜单）
INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status)
SELECT (SELECT id FROM sys_menu WHERE perms = 'system:config:list' AND menu_type = 2), '查询配置', 3, 'system:config:query', 1, 1, 1
WHERE NOT EXISTS (SELECT id FROM sys_menu WHERE perms = 'system:config:query');

INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status)
SELECT (SELECT id FROM sys_menu WHERE perms = 'system:config:list' AND menu_type = 2), '新增配置', 3, 'system:config:add', 2, 1, 1
WHERE NOT EXISTS (SELECT id FROM sys_menu WHERE perms = 'system:config:add');

INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status)
SELECT (SELECT id FROM sys_menu WHERE perms = 'system:config:list' AND menu_type = 2), '修改配置', 3, 'system:config:edit', 3, 1, 1
WHERE NOT EXISTS (SELECT id FROM sys_menu WHERE perms = 'system:config:edit');

INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status)
SELECT (SELECT id FROM sys_menu WHERE perms = 'system:config:list' AND menu_type = 2), '删除配置', 3, 'system:config:delete', 4, 1, 1
WHERE NOT EXISTS (SELECT id FROM sys_menu WHERE perms = 'system:config:delete');

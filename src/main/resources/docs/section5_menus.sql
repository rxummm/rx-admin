-- ============================================================
-- Section 5 功能增强 - 菜单 SQL（使用变量避免子查询多行问题）
-- 执行前已清理旧数据的可以直接执行
-- ============================================================

-- 5.3 定时任务管理
INSERT INTO sys_menu (parent_id, menu_name, menu_type, `path`, component, perms, icon, sort, visible, status)
VALUES (1, '定时任务', 2, '/monitor/job', 'monitor/job/index', 'monitor:job:query', 'Timer', 6, 1, 1);

SET @jobMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(@jobMenuId, '查询任务', 3, 'monitor:job:query', 1, 1, 1),
(@jobMenuId, '新增任务', 3, 'monitor:job:add', 2, 1, 1),
(@jobMenuId, '修改任务', 3, 'monitor:job:edit', 3, 1, 1),
(@jobMenuId, '删除任务', 3, 'monitor:job:delete', 4, 1, 1);

-- 5.4 文件管理
INSERT INTO sys_menu (parent_id, menu_name, menu_type, `path`, component, perms, icon, sort, visible, status)
VALUES (1, '文件管理', 2, '/system/file', 'system/file/index', 'system:file:list', 'FolderOpened', 7, 1, 1);

SET @fileMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(@fileMenuId, '查询文件', 3, 'system:file:query', 1, 1, 1),
(@fileMenuId, '上传文件', 3, 'system:file:upload', 2, 1, 1),
(@fileMenuId, '删除文件', 3, 'system:file:delete', 3, 1, 1);

-- 5.2 系统配置管理
INSERT INTO sys_menu (parent_id, menu_name, menu_type, `path`, component, perms, icon, sort, visible, status)
VALUES (1, '系统配置', 2, '/system/config', 'system/config/index', 'system:config:list', 'Setting', 5, 1, 1);

SET @configMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(@configMenuId, '查询配置', 3, 'system:config:query', 1, 1, 1),
(@configMenuId, '新增配置', 3, 'system:config:add', 2, 1, 1),
(@configMenuId, '修改配置', 3, 'system:config:edit', 3, 1, 1),
(@configMenuId, '删除配置', 3, 'system:config:delete', 4, 1, 1);

-- 分配菜单给 admin 角色（角色ID=1）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (@jobMenuId, @fileMenuId, @configMenuId)
  AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 同时分配子按钮权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE parent_id IN (@jobMenuId, @fileMenuId, @configMenuId)
  AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

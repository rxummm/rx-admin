-- ============================================================
-- 慢查询监控菜单 SQL
-- 插入到 系统监控 菜单组下
-- ============================================================

-- 获取系统监控菜单ID (假设 parent 名为 "系统监控")
SET @parentId = (SELECT id FROM sys_menu WHERE menu_name = '系统监控' AND parent_id = 0 LIMIT 1);
-- 如果找不到，使用 fallback ID（根据您的数据库调整）
SET @parentId = COALESCE(@parentId, 36);

-- 插入菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_type, `path`, component, perms, icon, sort, visible, status)
SELECT @parentId, '慢查询监控', 2, '/monitor/slow-query', 'monitor/slow-query/index', 'monitor:slow-query:list', 'Timer', 5, 1, 1
WHERE NOT EXISTS (SELECT id FROM sys_menu WHERE perms = 'monitor:slow-query:list');

SET @menuId = LAST_INSERT_ID();
SET @menuId = COALESCE(@menuId, (SELECT id FROM sys_menu WHERE perms = 'monitor:slow-query:list'));

-- 按钮权限
INSERT INTO sys_menu (parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(@menuId, '查询', 3, 'monitor:slow-query:query', 1, 1, 1),
(@menuId, '删除', 3, 'monitor:slow-query:delete', 2, 1, 1),
(@menuId, '清空', 3, 'monitor:slow-query:clear', 3, 1, 1);

-- 分配给 admin 角色 (角色ID=1)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE perms LIKE 'monitor:slow-query:%'
  AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

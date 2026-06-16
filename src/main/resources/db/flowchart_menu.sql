-- ============================================
-- 流程图 - 菜单注册 SQL（挂载到常用工具下）
-- ============================================

-- ============ 先删除旧数据（如果存在） ============
DELETE FROM sys_role_menu WHERE menu_id = 360;
DELETE FROM sys_menu WHERE id = 360;

-- ========== 二级菜单：流程图（父ID=349 常用工具） ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (360, 349, '流程图', 2, '/common-tools/flow-chart', 'tool/flowChart/index', 'common-tools:flow:list', 'Connection', 4, 1, 1, 0, NOW(), NOW());

-- ============ 将菜单分配给超级管理员角色（role_id=1） ============
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 360);

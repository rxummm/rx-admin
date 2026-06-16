-- ============================================
-- 接口分析工具 - 菜单注册 SQL
-- 在 sys_menu 表中新增菜单项
-- ============================================

-- 在"系统工具"菜单下新增"接口分析"子菜单
-- parent_id=24（系统工具），menu_type=2（菜单），sort=3
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (110, 24, '接口分析', 2, '/tool/analysis', 'tool/analysis/index', 'tool:analysis:list', 'Connection', 3, 1, 1, 0, NOW(), NOW());

-- 按钮级权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (111, 110, '接口分析查询', 3, '', '', 'tool:analysis:query', '', 1, 1, 1, 0, NOW(), NOW());

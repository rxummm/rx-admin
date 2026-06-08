-- ============================================
-- 文学作品管理 - 菜单注册 SQL
-- 在 sys_menu 表中新增菜单项（rx_admin 库）
-- 执行前确认 id 不冲突（当前使用 220-227）
-- ============================================

-- 二级菜单：文学作品管理（parent_id=200 历代文学，menu_type=2 菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (220, 200, '文学作品', 2, '/classics/literature/works', 'classics/literature/works/index', 'classics:literature:work:list', 'Document', 2, 1, 1, 0, NOW(), NOW());

-- 按钮级权限（parent_id=220）
-- 查询
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (221, 220, '作品查询', 3, '', '', 'classics:literature:work:query', '', 1, 1, 1, 0, NOW(), NOW());

-- 新增
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (222, 220, '作品新增', 3, '', '', 'classics:literature:work:add', '', 2, 1, 1, 0, NOW(), NOW());

-- 编辑
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (223, 220, '作品编辑', 3, '', '', 'classics:literature:work:edit', '', 3, 1, 1, 0, NOW(), NOW());

-- 删除
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (224, 220, '作品删除', 3, '', '', 'classics:literature:work:delete', '', 4, 1, 1, 0, NOW(), NOW());

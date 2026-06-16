-- ============================================
-- 历代文学 - 菜单注册 SQL
-- 在 sys_menu 表中新增菜单项（rx_admin 库）
-- ============================================

-- 一级菜单：历代文学（parent_id=0，menu_type=1 目录）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (200, 0, '历代文学', 1, '/classics/literature', '', '', 'Collection', 350, 1, 1, 0, NOW(), NOW());

-- 二级菜单：历代文学主页（parent_id=200，menu_type=2 菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (201, 200, '历代文学数据', 2, '/classics/literature', 'classics/literature/index', 'classics:literature:list', 'Collection', 1, 1, 1, 0, NOW(), NOW());

-- 按钮级权限（parent_id=201）
-- 查询
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (202, 201, '历代文学查询', 3, '', '', 'classics:literature:query', '', 1, 1, 1, 0, NOW(), NOW());

-- 作者权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (203, 201, '作者新增', 3, '', '', 'classics:literature:author:add', '', 2, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (204, 201, '作者编辑', 3, '', '', 'classics:literature:author:edit', '', 3, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (205, 201, '作者删除', 3, '', '', 'classics:literature:author:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 朝代权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (206, 201, '朝代新增', 3, '', '', 'classics:literature:dynasty:add', '', 5, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (207, 201, '朝代编辑', 3, '', '', 'classics:literature:dynasty:edit', '', 6, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (208, 201, '朝代删除', 3, '', '', 'classics:literature:dynasty:delete', '', 7, 1, 1, 0, NOW(), NOW());

-- 体裁权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (209, 201, '体裁新增', 3, '', '', 'classics:literature:genre:add', '', 8, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (210, 201, '体裁编辑', 3, '', '', 'classics:literature:genre:edit', '', 9, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (211, 201, '体裁删除', 3, '', '', 'classics:literature:genre:delete', '', 10, 1, 1, 0, NOW(), NOW());

-- 内容分类权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (212, 201, '分类新增', 3, '', '', 'classics:literature:category:add', '', 11, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (213, 201, '分类编辑', 3, '', '', 'classics:literature:category:edit', '', 12, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (214, 201, '分类删除', 3, '', '', 'classics:literature:category:delete', '', 13, 1, 1, 0, NOW(), NOW());

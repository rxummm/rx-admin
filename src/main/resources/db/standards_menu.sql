-- 开发规范菜单（系统工具 id=24 下新增子菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status)
VALUES (113, 24, '开发规范', 2, '/tool/standards', 'tool/standards/index', 'tool:standards', 'Notebook', 5, 1, 1);

-- 为管理员角色分配菜单权限（管理员角色 id 为 1）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 113);

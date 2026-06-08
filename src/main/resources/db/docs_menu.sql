-- 项目文档菜单（系统工具 id=24 下新增子菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status)
VALUES (112, 24, '项目文档', 2, '/tool/docs', 'tool/docs/index', 'tool:docs', 'Document', 4, 1, 1);

-- 为管理员角色分配菜单权限（管理员角色 id 为 1）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 112);

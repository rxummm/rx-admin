-- 高频查询复合索引
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user_role ON sys_user_role (user_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role_menu ON sys_role_menu (role_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_status ON sys_menu (parent_id, status, deleted);
CREATE INDEX IF NOT EXISTS idx_sys_menu_perms ON sys_menu (perms);
CREATE INDEX IF NOT EXISTS idx_sys_user_menu_user_menu ON sys_user_menu (user_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_status_deleted ON sys_role (status, deleted);

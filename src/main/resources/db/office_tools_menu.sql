-- ============================================
-- 办公工具 - 菜单迁移 SQL
-- 1. 将 "常用工具" 改名为 "办公工具"
-- 2. 新增 "邮件发送" 子菜单
-- ============================================

-- ============ 步骤1: 更新父菜单名称和图标 ============
UPDATE sys_menu
SET menu_name = '办公工具', icon = 'Briefcase'
WHERE id = 349;

-- ============ 步骤2: 删除旧邮件发送菜单数据（如果存在） ============
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 358 AND 362;
DELETE FROM sys_menu WHERE id BETWEEN 358 AND 362;

-- ============ 步骤3: 新增二级菜单：邮件发送 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (358, 349, '邮件发送', 2, '/common-tools/email-sender', 'tool/emailSender/index', 'office-tools:email:send', 'Message', 4, 1, 1, 0, NOW(), NOW());

-- ============ 步骤4: 按钮级权限：邮件发送 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (359, 358, '发送邮件', 3, '', '', 'office-tools:email:send', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (360, 358, '上传附件', 3, '', '', 'office-tools:email:upload', '', 2, 1, 1, 0, NOW(), NOW());

-- ============ 步骤5: 将邮件发送菜单分配给超级管理员角色（role_id=1） ============
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 358 AND 362;

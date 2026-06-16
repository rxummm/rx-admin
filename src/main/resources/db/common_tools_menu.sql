-- ============================================
-- 办公工具 - 菜单注册 SQL
-- 结构：办公工具（一级目录）> Excel解析 / 文档格式转换 / 文档上传 / 邮件发送（二级菜单）
-- ============================================

-- ============ 先删除旧数据（如果存在） ============
DELETE FROM sys_role_menu WHERE menu_id = 349;
DELETE FROM sys_menu WHERE id = 349;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 349 AND 370;
DELETE FROM sys_menu WHERE id BETWEEN 349 AND 370;

-- ============ 一级菜单：办公工具（parent_id=0，menu_type=1 目录） ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (349, 0, '办公工具', 1, '/common-tools', '', '', 'Briefcase', 360, 1, 1, 0, NOW(), NOW());

-- ========== 二级菜单：Excel解析 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (350, 349, 'Excel解析', 2, '/common-tools/excel-parser', 'tool/excelParser/index', 'common-tools:excel:list', 'Grid', 1, 1, 1, 0, NOW(), NOW());

-- ========== 二级菜单：文档格式转换 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (351, 349, '文档格式转换', 2, '/common-tools/doc-converter', 'tool/docConverter/index', 'common-tools:convert:list', 'Switch', 2, 1, 1, 0, NOW(), NOW());

-- ========== 二级菜单：文档上传 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (352, 349, '文档上传', 2, '/common-tools/doc-upload', 'tool/docUpload/index', 'common-tools:upload:list', 'UploadFilled', 3, 1, 1, 0, NOW(), NOW());

-- ============ 按钮级权限：Excel解析 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (353, 350, 'Excel上传解析', 3, '', '', 'common-tools:excel:upload', '', 1, 1, 1, 0, NOW(), NOW());

-- ============ 按钮级权限：文档格式转换 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (354, 351, 'PDF转Word', 3, '', '', 'common-tools:convert:pdf2word', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (355, 351, 'Word转PDF', 3, '', '', 'common-tools:convert:word2pdf', '', 2, 1, 1, 0, NOW(), NOW());

-- ============ 按钮级权限：文档上传 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (356, 352, '文档上传', 3, '', '', 'common-tools:upload:upload', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (357, 352, '文档删除', 3, '', '', 'common-tools:upload:delete', '', 2, 1, 1, 0, NOW(), NOW());

-- ========== 二级菜单：邮件发送 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (358, 349, '邮件发送', 2, '/common-tools/email-sender', 'tool/emailSender/index', 'office-tools:email:send', 'Message', 4, 1, 1, 0, NOW(), NOW());

-- ============ 按钮级权限：邮件发送 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (359, 358, '发送邮件', 3, '', '', 'office-tools:email:send', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (360, 358, '上传附件', 3, '', '', 'office-tools:email:upload', '', 2, 1, 1, 0, NOW(), NOW());

-- ============================================
-- 将办公工具所有菜单分配给超级管理员角色（role_id=1）
-- ============================================
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 349 AND 370;

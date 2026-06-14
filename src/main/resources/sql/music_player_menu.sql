-- =============================================
-- 音乐播放器 - 菜单注册SQL
-- 音乐播放为一级菜单，与常用工具平级
-- =============================================

-- 先删除旧的（如果存在）
DELETE FROM `sys_role_menu` WHERE menu_id IN (SELECT id FROM sys_menu WHERE menu_name = '音乐播放');
DELETE FROM `sys_menu` WHERE menu_name = '音乐播放';

-- 一级菜单：音乐播放（parent_id=0，menu_type=2 菜单，component 必须设置才能注册路由）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (NULL, 0, '音乐播放', 2, '/musicPlayer', 'tool/musicPlayer/index', '', 'Headset', 358, 1, 1, 0, NOW(), NOW());

-- 兜底：如果菜单已存在，强制更新 type/component/icon
UPDATE `sys_menu` SET `menu_type` = 2, `component` = 'tool/musicPlayer/index', `icon` = 'Headset' WHERE `menu_name` = '音乐播放';

-- 分配给超级管理员（忽略重复，避免报错）
INSERT IGNORE INTO `sys_role_menu` (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE menu_name = '音乐播放' LIMIT 1;

-- ============================================================
-- 知识图谱菜单
-- 放在仪表盘下方 (sort=1, 仅次于仪表盘 sort=0)
-- ============================================================

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (NULL, 0, '知识图谱', 2, '/dashboard/knowledgeGraph', 'dashboard/knowledgeGraph/index', '', 'Connection', 1, 1, 1, 0, NOW(), NOW());

-- 给超级管理员 (role_id=1) 分配权限
INSERT IGNORE INTO `sys_role_menu` (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name = '知识图谱' LIMIT 1;

-- =====================================================
-- RX Admin 系统增强 - 菜单注册 SQL (v2.1)
-- 固定 ID 481-510，幂等设计，可安全重复执行
-- =====================================================

-- =====================================================
-- 阶段1: 清理旧数据
-- =====================================================
DELETE FROM sys_role_menu WHERE menu_id >= 481 AND menu_id <= 510;
DELETE FROM sys_menu WHERE id >= 481 AND id <= 510;

-- =====================================================
-- 阶段2: 插入菜单
-- =====================================================

-- -----------------------------------------------------
-- 1. 登录日志 (归属 系统监控 parent_id=36)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (481, 36, '登录日志', 2, '/monitor/login-log', 'monitor/login-log/index', 'monitor:login-log:list', 'List', 3, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 2. 导出审计 (归属 系统监控 parent_id=36)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (482, 36, '导出审计', 2, '/monitor/export-log', 'monitor/export-log/index', 'monitor:export-log:list', 'Download', 4, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 3. 任务执行日志 (归属 系统监控 parent_id=36)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (483, 36, '任务执行日志', 2, '/monitor/job-log', 'monitor/job-log/index', 'monitor:job-log:list', 'Tickets', 7, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 4. 缓存管理 (归属 系统监控 parent_id=36)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (484, 36, '缓存管理', 2, '/monitor/cache-manage', 'monitor/cache-manage/index', 'monitor:cache:list', 'Coin', 8, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 5. 通知中心 (归属 内容管理 parent_id=30)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (485, 30, '通知中心', 2, '/content/notify-center', 'content/notify-center/index', 'content:notify-center:list', 'ChatLineSquare', 3, 1, 1, 0, NOW(), NOW());

-- 通知中心 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES
(491, 485, '模板查询', 3, '', '', 'content:notify-center:template:query', '', 1, 1, 1, 0, NOW(), NOW()),
(492, 485, '模板新增', 3, '', '', 'content:notify-center:template:add',   '', 2, 1, 1, 0, NOW(), NOW()),
(493, 485, '模板编辑', 3, '', '', 'content:notify-center:template:edit',  '', 3, 1, 1, 0, NOW(), NOW()),
(494, 485, '模板删除', 3, '', '', 'content:notify-center:template:delete','', 4, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 6. 数据库工具 (归属 系统工具 parent_id=24)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (486, 24, '数据库工具', 2, '/tool/dbConsole', 'tool/dbConsole/index', 'tool:dbConsole:list', 'Coin', 5, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 7. 开发工具 (归属 系统工具 parent_id=24)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (487, 24, '开发工具', 2, '/tool/devTools', 'tool/devTools/index', 'tool:devTools:list', 'Tools', 6, 1, 1, 0, NOW(), NOW());

-- =====================================================
-- 阶段3: 分配角色权限
-- =====================================================

-- 3a. admin(role_id=1) 拥有全部新菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id BETWEEN 481 AND 510;

-- 3b. operator(role_id=3) 拥有所有新页面 + 查询按钮
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE (id BETWEEN 481 AND 510) AND (menu_type = 2 OR perms LIKE '%:query');

-- 3c. user(role_id=2) 默认不分配（仅保留仪表盘和系统监控基础页面）

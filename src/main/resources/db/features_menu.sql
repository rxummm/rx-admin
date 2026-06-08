-- =====================================================
-- RX Admin 新功能菜单 SQL (v2.0) - 真·幂等版本
-- 固定 ID 401-480，按名称清理所有旧记录，可安全无限次重复执行
-- =====================================================

-- =====================================================
-- 阶段1: 彻底清理所有 v2.0 菜单（按名称匹配，覆盖动态ID遗留）
-- =====================================================

-- 1a. 删除子菜单(按钮)的角色关联（通过父菜单名称定位）
DELETE smr FROM sys_role_menu smr
INNER JOIN sys_menu sm_child ON smr.menu_id = sm_child.id
INNER JOIN sys_menu sm_parent ON sm_child.parent_id = sm_parent.id
WHERE sm_parent.menu_name IN ('IP黑白名单', '消息中心');

-- 1b. 删除父菜单的角色关联
DELETE smr FROM sys_role_menu smr
INNER JOIN sys_menu sm ON smr.menu_id = sm.id
WHERE sm.menu_name IN ('IP黑白名单', '消息中心', '健康监控', '日志分析', '代码生成', '批量导入', 'API调试', '数据备份');

-- 1c. 删除固定ID范围的角色关联（兜底）
DELETE FROM sys_role_menu WHERE menu_id >= 401 AND menu_id <= 480;

-- 1d. 删除子菜单（通过父菜单名称定位）
DELETE sm_child FROM sys_menu sm_child
INNER JOIN sys_menu sm_parent ON sm_child.parent_id = sm_parent.id
WHERE sm_parent.menu_name IN ('IP黑白名单', '消息中心');

-- 1e. 删除父菜单
DELETE FROM sys_menu WHERE menu_name IN ('IP黑白名单', '消息中心', '健康监控', '日志分析', '代码生成', '批量导入', 'API调试', '数据备份');

-- 1f. 删除固定ID范围残留（兜底）
DELETE FROM sys_menu WHERE id >= 401 AND id <= 480;


-- =====================================================
-- 阶段2: 插入菜单（固定ID 401-470）
-- =====================================================

-- -----------------------------------------------------
-- 1. IP黑白名单 (归属 系统管理 parent_id=1)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (401, 1, 'IP黑白名单', 2, '/system/ip-rule', 'system/ipRule/index', 'system:ip-rule:list', 'Switch', 6, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES
(402, 401, '查询', 3, '', '', 'system:ip-rule:query', '', 1, 1, 1, 0, NOW(), NOW()),
(403, 401, '新增', 3, '', '', 'system:ip-rule:add', '', 2, 1, 1, 0, NOW(), NOW()),
(404, 401, '修改', 3, '', '', 'system:ip-rule:edit', '', 3, 1, 1, 0, NOW(), NOW()),
(405, 401, '删除', 3, '', '', 'system:ip-rule:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 2. 消息中心 (归属 内容管理 parent_id=30)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (410, 30, '消息中心', 2, '/content/message', 'content/message/index', 'content:message:list', 'Message', 2, 1, 1, 0, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES
(411, 410, '删除消息', 3, '', '', 'content:message:delete', '', 1, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 3. 健康监控 (归属 系统监控 parent_id=36)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (420, 36, '健康监控', 2, '/monitor/health', 'monitor/health/index', 'monitor:health:list', 'Monitor', 5, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 4. 日志分析 (归属 系统监控 parent_id=36)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (430, 36, '日志分析', 2, '/monitor/log-analysis', 'monitor/logAnalysis/index', 'monitor:log-analysis:list', 'TrendCharts', 6, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 5. 代码生成 (归属 系统工具 parent_id=24)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (440, 24, '代码生成', 2, '/tool/gen', 'tool/gen/index', 'tool:gen:list', 'Edit', 8, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 6. 批量导入 (归属 系统工具 parent_id=24)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (450, 24, '批量导入', 2, '/tool/import', 'tool/importData/index', 'tool:import:list', 'Upload', 9, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 7. API调试 (归属 系统工具 parent_id=24)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (460, 24, 'API调试', 2, '/tool/api-debug', 'tool/apiDebug/index', 'tool:api-debug:list', 'Connection', 10, 1, 1, 0, NOW(), NOW());

-- -----------------------------------------------------
-- 8. 数据备份 (归属 系统工具 parent_id=24)
-- -----------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (470, 24, '数据备份', 2, '/tool/backup', 'tool/backup/index', 'tool:backup:list', 'FolderOpened', 11, 1, 1, 0, NOW(), NOW());

-- =====================================================
-- 阶段3: 给 admin 角色(role_id=1)分配新菜单权限
-- =====================================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id BETWEEN 401 AND 480;

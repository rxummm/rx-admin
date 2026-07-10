-- ============================================================
-- RX Admin v3.0 新增功能菜单
-- 说明: 新增工作流、Webhook、归档、通知偏好、API密钥、
--       邮件模板、数据版本、热力图、看板、甘特图、知识库、
--       多语言、性能分析、Schema Diff 菜单及权限
-- 可安全重复执行 (INSERT IGNORE)
-- ============================================================

-- ==================== 工作流管理 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(500, 0, '工作流管理', 'M', NULL, NULL, 'workflow', 12, 1, 0, NOW(), NOW()),
(501, 500, '流程定义', 'C', 'workflow/definition/index', 'wf:definition:query', 'list', 1, 1, 0, NOW(), NOW()),
(502, 501, '新增流程', 'F', NULL, 'wf:definition:add', '-', 1, 1, 0, NOW(), NOW()),
(503, 501, '修改流程', 'F', NULL, 'wf:definition:edit', '-', 2, 1, 0, NOW(), NOW()),
(504, 501, '删除流程', 'F', NULL, 'wf:definition:delete', '-', 3, 1, 0, NOW(), NOW()),
(505, 500, '流程实例', 'C', 'workflow/instance/index', 'wf:instance:query', 'log', 2, 1, 0, NOW(), NOW()),
(506, 505, '发起流程', 'F', NULL, 'wf:instance:start', '-', 1, 1, 0, NOW(), NOW()),
(507, 505, '取消流程', 'F', NULL, 'wf:instance:cancel', '-', 2, 1, 0, NOW(), NOW()),
(508, 500, '我的任务', 'C', 'workflow/task/index', 'wf:task:query', 'clipboard', 3, 1, 0, NOW(), NOW()),
(509, 508, '审批任务', 'F', NULL, 'wf:task:approve', '-', 1, 1, 0, NOW(), NOW()),
(510, 508, '转办任务', 'F', NULL, 'wf:task:transfer', '-', 2, 1, 0, NOW(), NOW());

-- ==================== Webhook 管理 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(520, 0, 'Webhook管理', 'M', NULL, NULL, 'connection', 13, 1, 0, NOW(), NOW()),
(521, 520, 'Webhook配置', 'C', 'tool/webhook/index', 'tool:webhook:query', 'setting', 1, 1, 0, NOW(), NOW()),
(522, 521, '新增Webhook', 'F', NULL, 'tool:webhook:add', '-', 1, 1, 0, NOW(), NOW()),
(523, 521, '修改Webhook', 'F', NULL, 'tool:webhook:edit', '-', 2, 1, 0, NOW(), NOW()),
(524, 521, '删除Webhook', 'F', NULL, 'tool:webhook:delete', '-', 3, 1, 0, NOW(), NOW()),
(525, 520, '投递日志', 'C', 'tool/webhookLog/index', 'tool:webhook-log:query', 'log', 2, 1, 0, NOW(), NOW());

-- ==================== 数据归档 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(530, 0, '数据归档', 'M', NULL, NULL, 'archive', 14, 1, 0, NOW(), NOW()),
(531, 530, '归档配置', 'C', 'tool/archive/index', 'tool:archive:query', 'setting', 1, 1, 0, NOW(), NOW()),
(532, 531, '新增配置', 'F', NULL, 'tool:archive:add', '-', 1, 1, 0, NOW(), NOW()),
(533, 531, '修改配置', 'F', NULL, 'tool:archive:edit', '-', 2, 1, 0, NOW(), NOW()),
(534, 531, '删除配置', 'F', NULL, 'tool:archive:delete', '-', 3, 1, 0, NOW(), NOW()),
(535, 530, '归档日志', 'C', 'tool/archiveLog/index', 'tool:archive-log:query', 'log', 2, 1, 0, NOW(), NOW());

-- ==================== 通知偏好 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(540, 0, '通知偏好', 'C', 'system/notificationPref/index', 'sys:notification-pref:query', 'bell', 15, 1, 0, NOW(), NOW()),
(541, 540, '修改偏好', 'F', NULL, 'sys:notification-pref:edit', '-', 1, 1, 0, NOW(), NOW());

-- ==================== API 密钥管理 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(550, 0, 'API密钥管理', 'M', NULL, NULL, 'key', 16, 1, 0, NOW(), NOW()),
(551, 550, '密钥列表', 'C', 'tool/apiKey/index', 'tool:api-key:query', 'list', 1, 1, 0, NOW(), NOW()),
(552, 551, '新增密钥', 'F', NULL, 'tool:api-key:add', '-', 1, 1, 0, NOW(), NOW()),
(553, 551, '删除密钥', 'F', NULL, 'tool:api-key:delete', '-', 2, 1, 0, NOW(), NOW()),
(554, 551, '禁用密钥', 'F', NULL, 'tool:api-key:toggle', '-', 3, 1, 0, NOW(), NOW());

-- ==================== 邮件模板 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(560, 0, '邮件模板', 'M', NULL, NULL, 'mail', 17, 1, 0, NOW(), NOW()),
(561, 560, '模板管理', 'C', 'tool/emailTemplate/index', 'tool:email-template:query', 'setting', 1, 1, 0, NOW(), NOW()),
(562, 561, '新增模板', 'F', NULL, 'tool:email-template:add', '-', 1, 1, 0, NOW(), NOW()),
(563, 561, '修改模板', 'F', NULL, 'tool:email-template:edit', '-', 2, 1, 0, NOW(), NOW()),
(564, 561, '删除模板', 'F', NULL, 'tool:email-template:delete', '-', 3, 1, 0, NOW(), NOW());

-- ==================== 数据版本追踪 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(570, 0, '数据版本', 'C', 'monitor/dataVersion/index', 'monitor:data-version:query', 'history', 18, 1, 0, NOW(), NOW());

-- ==================== 用户活动热力图 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(580, 0, '用户活动', 'C', 'monitor/activityHeatmap/index', 'monitor:activity:query', 'chart', 19, 1, 0, NOW(), NOW());

-- ==================== 看板管理 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(590, 0, '看板管理', 'M', NULL, NULL, 'grid', 20, 1, 0, NOW(), NOW()),
(591, 590, '我的看板', 'C', 'tool/kanban/index', 'tool:kanban:query', 'board', 1, 1, 0, NOW(), NOW()),
(592, 591, '新增看板', 'F', NULL, 'tool:kanban:add', '-', 1, 1, 0, NOW(), NOW()),
(593, 591, '修改看板', 'F', NULL, 'tool:kanban:edit', '-', 2, 1, 0, NOW(), NOW()),
(594, 591, '删除看板', 'F', NULL, 'tool:kanban:delete', '-', 3, 1, 0, NOW(), NOW());

-- ==================== 甘特图 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(600, 0, '甘特图', 'M', NULL, NULL, 'timeline', 21, 1, 0, NOW(), NOW()),
(601, 600, '项目管理', 'C', 'tool/gantt/index', 'tool:gantt:query', 'project', 1, 1, 0, NOW(), NOW()),
(602, 601, '新增项目', 'F', NULL, 'tool:gantt:add', '-', 1, 1, 0, NOW(), NOW()),
(603, 601, '修改项目', 'F', NULL, 'tool:gantt:edit', '-', 2, 1, 0, NOW(), NOW()),
(604, 601, '删除项目', 'F', NULL, 'tool:gantt:delete', '-', 3, 1, 0, NOW(), NOW());

-- ==================== 知识库 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(610, 0, '知识库', 'M', NULL, NULL, 'book', 22, 1, 0, NOW(), NOW()),
(611, 610, '空间管理', 'C', 'tool/wiki/index', 'tool:wiki:query', 'folder', 1, 1, 0, NOW(), NOW()),
(612, 611, '新增空间', 'F', NULL, 'tool:wiki:add', '-', 1, 1, 0, NOW(), NOW()),
(613, 611, '修改空间', 'F', NULL, 'tool:wiki:edit', '-', 2, 1, 0, NOW(), NOW()),
(614, 611, '删除空间', 'F', NULL, 'tool:wiki:delete', '-', 3, 1, 0, NOW(), NOW()),
(615, 610, '页面编辑', 'C', 'tool/wiki/page', 'tool:wiki-page:query', 'edit', 2, 1, 0, NOW(), NOW());

-- ==================== 多语言管理 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(620, 0, '多语言管理', 'M', NULL, NULL, 'language', 23, 1, 0, NOW(), NOW()),
(621, 620, '翻译管理', 'C', 'system/i18n/index', 'sys:i18n:query', 'translation', 1, 1, 0, NOW(), NOW()),
(622, 621, '新增翻译', 'F', NULL, 'sys:i18n:add', '-', 1, 1, 0, NOW(), NOW()),
(623, 621, '修改翻译', 'F', NULL, 'sys:i18n:edit', '-', 2, 1, 0, NOW(), NOW()),
(624, 621, '删除翻译', 'F', NULL, 'sys:i18n:delete', '-', 3, 1, 0, NOW(), NOW());

-- ==================== 性能分析 ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(630, 0, '性能分析', 'C', 'monitor/profiling/index', 'monitor:profiling:query', 'dashboard', 24, 1, 0, NOW(), NOW());

-- ==================== Schema Diff ====================
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `component`, `perms`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`) VALUES
(640, 0, 'Schema对比', 'C', 'tool/schemaDiff/index', 'tool:schema-diff:query', 'data-line', 25, 1, 0, NOW(), NOW());

-- ==================== 角色权限关联 (admin角色) ====================
-- admin 角色自动获取所有权限（通过 * 通配符），无需手动关联
-- 如需给非 admin 角色分配，取消注释并修改 role_id
-- INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
-- (2, 500), (2, 501), (2, 505), (2, 508),
-- (2, 520), (2, 521), (2, 525),
-- (2, 530), (2, 531), (2, 535),
-- (2, 540),
-- (2, 550), (2, 551),
-- (2, 560), (2, 561),
-- (2, 570),
-- (2, 580),
-- (2, 590), (2, 591),
-- (2, 600), (2, 601),
-- (2, 610), (2, 611), (2, 615),
-- (2, 620), (2, 621),
-- (2, 630),
-- (2, 640);

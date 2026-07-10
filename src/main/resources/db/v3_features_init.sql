-- ============================================================
-- RX Admin v3.0 新增功能表结构
-- 版本: V5
-- 说明: 工作流引擎、Webhook、数据归档、通知偏好、API密钥、
--       邮件模板、数据版本追踪、用户活动热力图、看板、
--       甘特图、知识库、多语言管理、性能分析、Schema快照
-- 可安全重复执行 (CREATE TABLE IF NOT EXISTS)
-- ============================================================

-- ==================== 1. 工作流引擎 ====================

-- 流程定义表
CREATE TABLE IF NOT EXISTS `wf_process_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '流程名称',
  `code` varchar(50) NOT NULL COMMENT '流程编码',
  `description` varchar(500) DEFAULT NULL COMMENT '流程描述',
  `category` varchar(50) DEFAULT NULL COMMENT '流程分类',
  `form_config` json DEFAULT NULL COMMENT '表单配置(JSON)',
  `process_config` json DEFAULT NULL COMMENT '流程节点配置(JSON)',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `version` int DEFAULT 1 COMMENT '版本号',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wf_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程定义';

-- 流程实例表
CREATE TABLE IF NOT EXISTS `wf_process_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `definition_id` bigint NOT NULL COMMENT '流程定义ID',
  `title` varchar(200) NOT NULL COMMENT '流程标题',
  `business_key` varchar(100) DEFAULT NULL COMMENT '业务关联键',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `initiator_id` bigint NOT NULL COMMENT '发起人ID',
  `initiator_name` varchar(50) DEFAULT NULL COMMENT '发起人名称',
  `current_node` varchar(100) DEFAULT NULL COMMENT '当前节点编码',
  `form_data` json DEFAULT NULL COMMENT '表单数据(JSON)',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/COMPLETED/REJECTED/CANCELLED',
  `start_time` datetime DEFAULT NULL COMMENT '发起时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wf_inst_def` (`definition_id`),
  KEY `idx_wf_inst_initiator` (`initiator_id`),
  KEY `idx_wf_inst_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程实例';

-- 任务表
CREATE TABLE IF NOT EXISTS `wf_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `node_code` varchar(100) NOT NULL COMMENT '节点编码',
  `node_name` varchar(100) DEFAULT NULL COMMENT '节点名称',
  `task_type` varchar(20) DEFAULT 'APPROVE' COMMENT 'APPROVE/CC/NOTIFY',
  `assignee_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `assignee_name` varchar(50) DEFAULT NULL COMMENT '处理人名称',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/COMPLETED/REJECTED/TRANSFERRED',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `due_time` datetime DEFAULT NULL COMMENT '截止时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wf_task_instance` (`instance_id`),
  KEY `idx_wf_task_assignee` (`assignee_id`),
  KEY `idx_wf_task_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流任务';

-- ==================== 2. Webhook 管理 ====================

CREATE TABLE IF NOT EXISTS `sys_webhook` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT 'Webhook名称',
  `url` varchar(500) NOT NULL COMMENT '回调URL',
  `secret` varchar(200) DEFAULT NULL COMMENT '签名密钥',
  `events` varchar(500) NOT NULL COMMENT '触发事件(逗号分隔)',
  `headers` json DEFAULT NULL COMMENT '自定义请求头(JSON)',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `retry_count` int DEFAULT 3 COMMENT '重试次数',
  `timeout_ms` int DEFAULT 5000 COMMENT '超时时间(ms)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Webhook配置';

CREATE TABLE IF NOT EXISTS `sys_webhook_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `webhook_id` bigint NOT NULL COMMENT 'Webhook ID',
  `event` varchar(50) NOT NULL COMMENT '事件类型',
  `payload` json DEFAULT NULL COMMENT '请求体',
  `response_code` int DEFAULT NULL COMMENT '响应状态码',
  `response_body` text DEFAULT NULL COMMENT '响应体',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `retry_count` int DEFAULT 0 COMMENT '已重试次数',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_webhook_log_wid` (`webhook_id`),
  KEY `idx_webhook_log_event` (`event`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Webhook投递日志';

-- ==================== 3. 数据归档 ====================

CREATE TABLE IF NOT EXISTS `sys_archive_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `table_name` varchar(100) NOT NULL COMMENT '归档表名',
  `archive_table` varchar(100) NOT NULL COMMENT '归档目标表名',
  `condition_field` varchar(100) NOT NULL COMMENT '归档条件字段',
  `retain_days` int DEFAULT 365 COMMENT '保留天数',
  `batch_size` int DEFAULT 1000 COMMENT '每批归档条数',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `last_archive_time` datetime DEFAULT NULL COMMENT '上次归档时间',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据归档配置';

CREATE TABLE IF NOT EXISTS `sys_archive_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` bigint NOT NULL COMMENT '归档配置ID',
  `table_name` varchar(100) NOT NULL COMMENT '归档表名',
  `archived_count` int DEFAULT 0 COMMENT '归档条数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) DEFAULT 'RUNNING' COMMENT 'RUNNING/SUCCESS/FAILED',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_archive_log_config` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据归档日志';

-- ==================== 4. 通知偏好设置 ====================

CREATE TABLE IF NOT EXISTS `sys_notification_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `event_type` varchar(50) NOT NULL COMMENT '事件类型',
  `email_enabled` int DEFAULT 1 COMMENT '邮件通知 1开/0关',
  `websocket_enabled` int DEFAULT 1 COMMENT 'WebSocket通知 1开/0关',
  `browser_enabled` int DEFAULT 1 COMMENT '浏览器通知 1开/0关',
  `quiet_start` varchar(5) DEFAULT NULL COMMENT '免打扰开始(HH:mm)',
  `quiet_end` varchar(5) DEFAULT NULL COMMENT '免打扰结束(HH:mm)',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notif_pref_user_event` (`user_id`, `event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知偏好设置';

-- ==================== 5. API 密钥管理 ====================

CREATE TABLE IF NOT EXISTS `sys_api_key` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '密钥名称',
  `api_key` varchar(64) NOT NULL COMMENT 'API密钥',
  `api_secret` varchar(128) NOT NULL COMMENT 'API密钥(加密)',
  `permissions` varchar(500) DEFAULT NULL COMMENT '权限范围(逗号分隔)',
  `rate_limit` int DEFAULT 100 COMMENT '每分钟请求限制',
  `ip_whitelist` varchar(500) DEFAULT NULL COMMENT 'IP白名单',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `last_used_time` datetime DEFAULT NULL COMMENT '最近使用时间',
  `use_count` bigint DEFAULT 0 COMMENT '使用次数',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_key` (`api_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API密钥管理';

-- ==================== 6. 邮件模板管理 ====================

CREATE TABLE IF NOT EXISTS `sys_email_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `code` varchar(50) NOT NULL COMMENT '模板编码',
  `subject` varchar(200) NOT NULL COMMENT '邮件主题',
  `body` text NOT NULL COMMENT '邮件正文(HTML)',
  `variables` varchar(500) DEFAULT NULL COMMENT '变量列表(逗号分隔)',
  `category` varchar(50) DEFAULT NULL COMMENT '模板分类',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email_tpl_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件模板';

-- ==================== 7. 数据版本追踪 ====================

CREATE TABLE IF NOT EXISTS `sys_data_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `table_name` varchar(100) NOT NULL COMMENT '表名',
  `record_id` bigint NOT NULL COMMENT '记录ID',
  `version` int NOT NULL COMMENT '版本号',
  `operation` varchar(20) NOT NULL COMMENT 'INSERT/UPDATE/DELETE',
  `old_data` json DEFAULT NULL COMMENT '变更前数据',
  `new_data` json DEFAULT NULL COMMENT '变更后数据',
  `diff_data` json DEFAULT NULL COMMENT '差异数据',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_data_ver_table_record` (`table_name`, `record_id`),
  KEY `idx_data_ver_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据版本追踪';

-- ==================== 8. 用户活动热力图 ====================

CREATE TABLE IF NOT EXISTS `sys_user_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `activity_type` varchar(50) NOT NULL COMMENT '活动类型(LOGIN/VIEW/CREATE/UPDATE/DELETE/EXPORT)',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `detail` varchar(200) DEFAULT NULL COMMENT '操作详情',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `activity_date` date NOT NULL COMMENT '活动日期',
  `hour` tinyint NOT NULL COMMENT '小时(0-23)',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_act_date` (`activity_date`),
  KEY `idx_user_act_user_date` (`user_id`, `activity_date`),
  KEY `idx_user_act_type` (`activity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活动热力图';

-- ==================== 9. 看板 ====================

CREATE TABLE IF NOT EXISTS `kanban_board` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '看板名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板';

CREATE TABLE IF NOT EXISTS `kanban_column` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '看板ID',
  `name` varchar(100) NOT NULL COMMENT '列名称',
  `color` varchar(20) DEFAULT '#409eff' COMMENT '列颜色',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `wip_limit` int DEFAULT 0 COMMENT '在制品限制(0=不限)',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_kanban_col_board` (`board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板列';

CREATE TABLE IF NOT EXISTS `kanban_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '看板ID',
  `column_id` bigint NOT NULL COMMENT '列ID',
  `title` varchar(200) NOT NULL COMMENT '卡片标题',
  `description` text DEFAULT NULL COMMENT '描述',
  `priority` varchar(10) DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/CRITICAL',
  `assignee_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `assignee_name` varchar(50) DEFAULT NULL COMMENT '负责人名称',
  `due_date` date DEFAULT NULL COMMENT '截止日期',
  `tags` varchar(200) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_kanban_card_board` (`board_id`),
  KEY `idx_kanban_card_column` (`column_id`),
  KEY `idx_kanban_card_assignee` (`assignee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板卡片';

-- ==================== 10. 甘特图 ====================

CREATE TABLE IF NOT EXISTS `gantt_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `status` varchar(20) DEFAULT 'PLANNING' COMMENT 'PLANNING/ACTIVE/COMPLETED/ARCHIVED',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甘特图项目';

CREATE TABLE IF NOT EXISTS `gantt_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父任务ID',
  `name` varchar(200) NOT NULL COMMENT '任务名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `progress` int DEFAULT 0 COMMENT '进度(0-100)',
  `status` varchar(20) DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/DONE/BLOCKED',
  `assignee_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `assignee_name` varchar(50) DEFAULT NULL COMMENT '负责人名称',
  `priority` varchar(10) DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_gantt_task_project` (`project_id`),
  KEY `idx_gantt_task_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甘特图任务';

-- ==================== 11. 知识库/Wiki ====================

CREATE TABLE IF NOT EXISTS `wiki_space` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '空间名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `visibility` varchar(20) DEFAULT 'PUBLIC' COMMENT 'PUBLIC/PRIVATE',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库空间';

CREATE TABLE IF NOT EXISTS `wiki_page` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父页面ID',
  `title` varchar(200) NOT NULL COMMENT '页面标题',
  `content` longtext DEFAULT NULL COMMENT '页面内容(Markdown)',
  `slug` varchar(200) DEFAULT NULL COMMENT 'URL别名',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `is_published` int DEFAULT 0 COMMENT '1已发布/0草稿',
  `author_id` bigint DEFAULT NULL COMMENT '作者ID',
  `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wiki_page_space` (`space_id`),
  KEY `idx_wiki_page_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库页面';

CREATE TABLE IF NOT EXISTS `wiki_page_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `page_id` bigint NOT NULL COMMENT '页面ID',
  `version` int NOT NULL COMMENT '版本号',
  `title` varchar(200) NOT NULL COMMENT '页面标题',
  `content` longtext DEFAULT NULL COMMENT '页面内容',
  `author_id` bigint DEFAULT NULL COMMENT '作者ID',
  `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wiki_ver_page` (`page_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库页面版本';

-- ==================== 12. 多语言管理 ====================

CREATE TABLE IF NOT EXISTS `sys_i18n_locale` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(10) NOT NULL COMMENT '语言代码(如 zh-CN)',
  `name` varchar(50) NOT NULL COMMENT '语言名称',
  `native_name` varchar(50) DEFAULT NULL COMMENT '本地名称',
  `is_default` int DEFAULT 0 COMMENT '1默认语言',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_i18n_locale_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多语言语言';

CREATE TABLE IF NOT EXISTS `sys_i18n_key` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `key_path` varchar(200) NOT NULL COMMENT '翻译键(如 menu.dashboard)',
  `module` varchar(50) DEFAULT NULL COMMENT '所属模块',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_i18n_key_path` (`key_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多语言翻译键';

CREATE TABLE IF NOT EXISTS `sys_i18n_translation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `key_id` bigint NOT NULL COMMENT '翻译键ID',
  `locale_code` varchar(10) NOT NULL COMMENT '语言代码',
  `translation` text NOT NULL COMMENT '翻译内容',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_i18n_trans` (`key_id`, `locale_code`),
  KEY `idx_i18n_trans_locale` (`locale_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多语言翻译';

-- ==================== 13. 性能分析 ====================

CREATE TABLE IF NOT EXISTS `sys_profile_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_name` varchar(200) NOT NULL COMMENT '类名',
  `method_name` varchar(100) NOT NULL COMMENT '方法名',
  `execution_time` bigint NOT NULL COMMENT '执行时间(ms)',
  `params` text DEFAULT NULL COMMENT '参数摘要',
  `exception` varchar(500) DEFAULT NULL COMMENT '异常信息',
  `thread_name` varchar(100) DEFAULT NULL COMMENT '线程名',
  `trace_id` varchar(100) DEFAULT NULL COMMENT '追踪ID',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_prof_class_method` (`class_name`, `method_name`),
  KEY `idx_prof_time` (`create_time`),
  KEY `idx_prof_exec_time` (`execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='性能分析记录';

-- ==================== 14. 数据库 Schema 快照 ====================

CREATE TABLE IF NOT EXISTS `sys_schema_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `db_name` varchar(100) NOT NULL COMMENT '数据库名',
  `table_name` varchar(100) NOT NULL COMMENT '表名',
  `ddl_sql` text NOT NULL COMMENT 'DDL语句',
  `column_count` int DEFAULT 0 COMMENT '字段数',
  `index_count` int DEFAULT 0 COMMENT '索引数',
  `row_count` bigint DEFAULT 0 COMMENT '行数估算',
  `snapshot_time` datetime DEFAULT NULL COMMENT '快照时间',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_schema_snap_table` (`table_name`),
  KEY `idx_schema_snap_time` (`snapshot_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库Schema快照';

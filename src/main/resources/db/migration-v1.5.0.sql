-- ============================================================
-- NOTE: This file MUST be executed with --default-character-set=utf8mb4
-- e.g.: mysql -u root -proot rx_admin --default-character-set=utf8mb4 < this_file.sql
-- ============================================================

-- ============================================================
-- RX Admin 优化功能 - 数据库迁移
-- ============================================================

-- 1. 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) DEFAULT NULL COMMENT '配置值',
  `config_type` varchar(50) DEFAULT 'text' COMMENT '配置类型(text/boolean/number/image)',
  `description` varchar(255) DEFAULT NULL COMMENT '配置描述',
  `group_name` varchar(50) DEFAULT 'system' COMMENT '分组',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- 2. 通知已读记录表
CREATE TABLE IF NOT EXISTS `sys_notice_read` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `notice_id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知已读记录';

-- 3. 定时任务表
CREATE TABLE IF NOT EXISTS `sys_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(100) NOT NULL COMMENT '任务名称',
  `bean_name` varchar(200) DEFAULT NULL COMMENT 'Spring Bean名称',
  `method_name` varchar(100) DEFAULT NULL COMMENT '执行方法',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT 'cron表达式',
  `params` varchar(500) DEFAULT NULL COMMENT '参数',
  `status` tinyint DEFAULT 1 COMMENT '状态(1=正常,0=暂停)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务';

-- 4. 文件管理表
CREATE TABLE IF NOT EXISTS `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `stored_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `path` varchar(500) NOT NULL COMMENT '存储路径',
  `size` bigint DEFAULT 0 COMMENT '文件大小(字节)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `storage_type` varchar(20) DEFAULT 'local' COMMENT '存储类型(local/minio)',
  `category` varchar(50) DEFAULT 'other' COMMENT '分类(image/doc/video/audio/other)',
  `uploader` bigint DEFAULT NULL COMMENT '上传者ID',
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_uploader` (`uploader`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理';

-- 5. 初始化系统配置
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `group_name`, `sort_order`) VALUES
('site.title', 'RX Admin', 'text', '网站标题', 'system', 1),
('site.logo', '', 'image', '网站LOGO', 'system', 2),
('site.favicon', '', 'image', '网站图标', 'system', 3),
('site.icp', '', 'text', '备案号', 'system', 4),
('captcha.enabled', 'true', 'boolean', '登录验证码开关', 'security', 5),
('register.enabled', 'true', 'boolean', '注册功能开关', 'security', 6),
('default.role', '2', 'text', '新用户默认角色ID', 'system', 7);
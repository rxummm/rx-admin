-- 协作文档表
CREATE TABLE IF NOT EXISTS `sys_collaborative_doc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `content` longtext COMMENT '文档内容（Markdown格式）',
  `space_id` bigint DEFAULT NULL COMMENT '所属空间ID',
  `creator_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建者姓名',
  `last_editor_id` bigint DEFAULT NULL COMMENT '最后编辑者ID',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后编辑者姓名',
  `last_edit_time` datetime DEFAULT NULL COMMENT '最后编辑时间',
  `status` varchar(20) DEFAULT 'draft' COMMENT '文档状态：draft/published/archived',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `edit_lock` varchar(50) DEFAULT NULL COMMENT '编辑锁（用户ID）',
  `lock_time` datetime DEFAULT NULL COMMENT '锁定时间',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_space_id` (`space_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作文档';

-- 协作文档版本表
CREATE TABLE IF NOT EXISTS `sys_collaborative_doc_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` bigint NOT NULL COMMENT '文档ID',
  `version_number` int NOT NULL COMMENT '版本号',
  `content` longtext COMMENT '版本内容',
  `title` varchar(200) DEFAULT NULL COMMENT '版本标题',
  `editor_id` bigint DEFAULT NULL COMMENT '编辑者ID',
  `editor_name` varchar(50) DEFAULT NULL COMMENT '编辑者姓名',
  `change_note` varchar(200) DEFAULT NULL COMMENT '版本说明',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_doc_id` (`doc_id`),
  KEY `idx_version` (`doc_id`, `version_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作文档版本';

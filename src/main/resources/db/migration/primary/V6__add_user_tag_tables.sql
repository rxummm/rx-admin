-- 用户标签表
CREATE TABLE IF NOT EXISTS `sys_user_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `tag_color` varchar(20) DEFAULT '#409eff' COMMENT '标签颜色',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `status` int DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签';

-- 用户标签关联表
CREATE TABLE IF NOT EXISTS `sys_user_tag_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签关联';

-- 看板任务依赖表
CREATE TABLE IF NOT EXISTS `kanban_dependency` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_id` bigint NOT NULL COMMENT '任务ID',
  `depends_on_id` bigint NOT NULL COMMENT '依赖的任务ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_dependency` (`card_id`, `depends_on_id`),
  KEY `idx_card_id` (`card_id`),
  KEY `idx_depends_on_id` (`depends_on_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板任务依赖';

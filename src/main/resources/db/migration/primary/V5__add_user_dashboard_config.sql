-- 用户仪表盘配置表
CREATE TABLE IF NOT EXISTS `sys_user_dashboard_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `widget_type` varchar(50) NOT NULL COMMENT '组件类型',
  `widget_title` varchar(100) DEFAULT NULL COMMENT '组件标题',
  `sort_order` int DEFAULT 0 COMMENT '排序序号',
  `enabled` int DEFAULT 1 COMMENT '是否启用: 1-启用, 0-禁用',
  `config` text COMMENT '组件配置（JSON格式）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户仪表盘配置';

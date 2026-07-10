-- 数据快照表
CREATE TABLE IF NOT EXISTS `sys_data_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `table_name` varchar(100) NOT NULL COMMENT '表名',
  `record_id` bigint NOT NULL COMMENT '记录ID',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型：INSERT/UPDATE/DELETE',
  `before_data` text COMMENT '操作前数据（JSON格式）',
  `after_data` text COMMENT '操作后数据（JSON格式）',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人用户名',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `rolled_back` int DEFAULT 0 COMMENT '是否已回滚：0-未回滚，1-已回滚',
  `rollback_time` datetime DEFAULT NULL COMMENT '回滚时间',
  PRIMARY KEY (`id`),
  KEY `idx_table_name` (`table_name`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据快照';

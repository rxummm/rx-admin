-- 团队日程表
CREATE TABLE IF NOT EXISTS `sys_team_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '日程标题',
  `description` text COMMENT '日程描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `all_day` tinyint(1) DEFAULT 0 COMMENT '是否全天事件',
  `recurrence` varchar(200) DEFAULT NULL COMMENT '重复规则（RRULE格式）',
  `location` varchar(200) DEFAULT NULL COMMENT '地点',
  `participants` varchar(500) DEFAULT NULL COMMENT '参与者ID列表（逗号分隔）',
  `creator_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建者姓名',
  `schedule_type` varchar(20) DEFAULT 'event' COMMENT '日程类型：meeting/event/reminder',
  `status` varchar(20) DEFAULT 'confirmed' COMMENT '状态：tentative/confirmed/cancelled',
  `color` varchar(20) DEFAULT '#409eff' COMMENT '颜色',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队日程';

-- ============================================
-- 表格数据导出配置表
-- 通过配置即可让任意菜单页面出现"导出"按钮
-- ============================================

CREATE TABLE IF NOT EXISTS sys_export_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  menu_id BIGINT NOT NULL COMMENT '关联菜单ID（sys_menu.id）',
  export_types VARCHAR(50) NOT NULL DEFAULT 'excel,pdf' COMMENT '允许的导出类型，逗号分隔: excel,pdf',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出配置表';

-- ============ 初始化：为已有管理页面启用导出功能 ============

-- 用户管理（menu_id 需根据实际 sys_menu 中的 id 调整）
INSERT INTO sys_export_config (menu_id, export_types, enabled) VALUES
  ((SELECT id FROM sys_menu WHERE path = '/system/user' AND deleted = 0 LIMIT 1), 'excel,pdf', 1)
ON DUPLICATE KEY UPDATE enabled = 1;

-- 角色管理
INSERT INTO sys_export_config (menu_id, export_types, enabled) VALUES
  ((SELECT id FROM sys_menu WHERE path = '/system/role' AND deleted = 0 LIMIT 1), 'excel,pdf', 1)
ON DUPLICATE KEY UPDATE enabled = 1;

-- 部门管理
INSERT INTO sys_export_config (menu_id, export_types, enabled) VALUES
  ((SELECT id FROM sys_menu WHERE path = '/system/dept' AND deleted = 0 LIMIT 1), 'excel,pdf', 1)
ON DUPLICATE KEY UPDATE enabled = 1;

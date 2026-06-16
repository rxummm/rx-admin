-- ===================================================
-- 常用工具模块 - 初始化SQL
-- 数据库: rx_admin
-- ===================================================

CREATE TABLE IF NOT EXISTS `shared_files` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name` VARCHAR(500) NOT NULL COMMENT '原始文件名',
    `stored_name` VARCHAR(500) NOT NULL COMMENT '存储文件名(UUID)',
    `file_path` VARCHAR(1000) NOT NULL COMMENT '存储路径',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT '' COMMENT '文件类型/扩展名',
    `upload_user` VARCHAR(100) DEFAULT 'system' COMMENT '上传用户',
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    PRIMARY KEY (`id`),
    INDEX `idx_file_type` (`file_type`),
    INDEX `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储表';

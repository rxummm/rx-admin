-- ============================================
-- OCR 文档识别 - 建表 SQL
-- ============================================

-- ============ 先删除旧数据 ============
DROP TABLE IF EXISTS ocr_recognition;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 600 AND 603;
DELETE FROM sys_menu WHERE id BETWEEN 600 AND 603;

-- ============ 创建 OCR 识别记录表 ============
CREATE TABLE ocr_recognition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_type VARCHAR(20) COMMENT '文件类型(pdf/docx/png/jpg等)',
    file_size BIGINT COMMENT '文件大小(字节)',
    language VARCHAR(20) DEFAULT 'chi_sim+eng' COMMENT '识别语言',
    ocr_engine VARCHAR(50) DEFAULT 'tesseract' COMMENT 'OCR引擎',
    result_text MEDIUMTEXT COMMENT '识别结果全文',
    page_count INT DEFAULT 1 COMMENT '页数/图片数',
    char_count INT DEFAULT 0 COMMENT '识别字符数',
    confidence FLOAT COMMENT '平均置信度',
    status TINYINT DEFAULT 2 COMMENT '状态(0-失败 1-成功 2-识别中)',
    error_message VARCHAR(500) COMMENT '错误信息',
    duration_ms BIGINT COMMENT '识别耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_file_name (file_name),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCR识别记录表';

-- ============ 注册菜单 ============
-- OCR 文档识别（挂在系统工具 parent_id=24 下）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (600, 24, 'OCR文档识别', 2, '/ocr/recognition', 'ocr/recognition/index', 'ocr:recognition:list', 'Document', 52, 1, 1, 0, NOW(), NOW());

-- ============ 按钮级权限 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (601, 600, 'OCR识别', 3, '', '', 'ocr:recognition:recognize', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (602, 600, '查看识别详情', 3, '', '', 'ocr:recognition:view', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (603, 600, '删除识别记录', 3, '', '', 'ocr:recognition:delete', '', 3, 1, 1, 0, NOW(), NOW());

-- ============ 分配给超级管理员角色 ============
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 600 AND 603;

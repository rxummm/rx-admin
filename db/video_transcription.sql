-- ============================================================
-- 视频转写模块数据库初始化脚本
-- ============================================================
-- 执行前请确认已连接到正确的数据库（默认 rx_admin）
-- 执行顺序：先创建表，再插入菜单和权限
-- ============================================================

-- 临时禁用外键检查，避免删除顺序问题
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 创建视频转写分段表（子表，有外键）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS video_segment;
CREATE TABLE video_segment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    transcription_id BIGINT NOT NULL COMMENT '转写ID',
    start_time DOUBLE DEFAULT 0 COMMENT '开始时间(秒)',
    end_time DOUBLE DEFAULT 0 COMMENT '结束时间(秒)',
    text TEXT COMMENT '分段文本',
    speaker_label VARCHAR(20) DEFAULT '' COMMENT '说话人标签(SPEAKER_00等)',
    speaker_name VARCHAR(50) DEFAULT '' COMMENT '说话人名称(用户可编辑)',
    confidence FLOAT DEFAULT 0 COMMENT '置信度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    INDEX idx_transcription_id (transcription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频转写分段表';

-- ------------------------------------------------------------
-- 2. 创建视频转写主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS video_transcription;
CREATE TABLE video_transcription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    file_name VARCHAR(255) DEFAULT '' COMMENT '原始文件名',
    file_path VARCHAR(500) DEFAULT '' COMMENT '文件存储路径',
    audio_path VARCHAR(500) DEFAULT '' COMMENT '提取的音频路径',
    language VARCHAR(20) DEFAULT 'zh' COMMENT '语言代码(zh/en)',
    full_text TEXT COMMENT '完整转写文本',
    duration DOUBLE DEFAULT 0 COMMENT '音频时长(秒)',
    model_name VARCHAR(50) DEFAULT '' COMMENT '使用的模型名称',
    speaker_count INT DEFAULT 0 COMMENT '说话人数量',
    status TINYINT DEFAULT 2 COMMENT '状态(0-失败 1-成功 2-待转写)',
    srt_path VARCHAR(500) DEFAULT '' COMMENT 'SRT字幕路径',
    ass_path VARCHAR(500) DEFAULT '' COMMENT 'ASS字幕路径',
    error_message VARCHAR(500) DEFAULT '' COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FULLTEXT INDEX idx_full_text (full_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频转写主表';

-- 添加外键约束（主表创建完成后）
ALTER TABLE video_segment
    ADD CONSTRAINT fk_video_segment_transcription
    FOREIGN KEY (transcription_id)
    REFERENCES video_transcription(id) ON DELETE CASCADE;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;

-- ------------------------------------------------------------
-- 3. 注册菜单到 sys_menu 表
-- ------------------------------------------------------------
-- 先检查是否已存在，避免重复插入
SET @menu_id = NULL;
SELECT id INTO @menu_id FROM sys_menu WHERE component = 'video/transcription/index' LIMIT 1;

-- 如果不存在，则插入菜单（父菜单ID=24 对应"系统工具"）
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '视频转写', 24, 2, '/tool/video/transcription', 'video/transcription/index',
       'video:transcription:list', 'fa-solid fa-video', 51, 1, 1
WHERE @menu_id IS NULL;

-- 记录新插入的菜单ID
SELECT LAST_INSERT_ID() INTO @new_menu_id;
SET @video_menu_id = IF(@menu_id IS NOT NULL, @menu_id, @new_menu_id);

-- ------------------------------------------------------------
-- 4. 注册按钮权限（子菜单）
-- ------------------------------------------------------------
-- 上传权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '上传转写', @video_menu_id, 3, '', '', 'video:transcription:upload', '', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:transcription:upload');

-- 查看权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '查看详情', @video_menu_id, 3, '', '', 'video:transcription:view', '', 2, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:transcription:view');

-- 删除权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '删除记录', @video_menu_id, 3, '', '', 'video:transcription:delete', '', 3, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:transcription:delete');

-- 修改说话人名称权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '修改说话人名称', @video_menu_id, 3, '', '', 'video:transcription:update', '', 4, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:transcription:update');

-- 列表查询权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '列表查询', @video_menu_id, 3, '', '', 'video:transcription:list', '', 5, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:transcription:list' AND menu_name <> '视频转写');

-- ------------------------------------------------------------
-- 5. 给超级管理员角色添加权限（角色ID=1 对应"超级管理员"）
-- ------------------------------------------------------------
-- 先获取所有视频相关的菜单ID
DROP TEMPORARY TABLE IF EXISTS temp_video_menu_ids;
CREATE TEMPORARY TABLE temp_video_menu_ids (menu_id BIGINT);

INSERT INTO temp_video_menu_ids (menu_id)
SELECT id FROM sys_menu WHERE perms LIKE 'video:%';

-- 给超级管理员（role_id=1）添加视频菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM temp_video_menu_ids
WHERE menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

DROP TEMPORARY TABLE IF EXISTS temp_video_menu_ids;

-- ------------------------------------------------------------
-- 完成
-- ------------------------------------------------------------
SELECT '视频转写模块数据库初始化完成！' AS result;
SELECT CONCAT('菜单ID: ', @video_menu_id) AS menu_info;
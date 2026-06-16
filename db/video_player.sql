-- ============================================================
-- 视频播放模块数据库初始化脚本
-- ============================================================
-- 执行前请确认已连接到正确的数据库（默认 rx_admin）
-- 执行顺序：先创建表，再插入菜单和权限
-- ============================================================

-- 临时禁用外键检查，避免删除顺序问题
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 创建视频文件主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS video_file;
CREATE TABLE video_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) COMMENT '视频标题',
    file_name VARCHAR(255) COMMENT '文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_size BIGINT COMMENT '文件大小(字节)',
    duration INT COMMENT '时长(秒)',
    width INT COMMENT '分辨率宽',
    height INT COMMENT '分辨率高',
    video_type VARCHAR(20) COMMENT '视频格式(mp4/webm/ogg/mkv/avi/flv/mov)',
    play_count INT DEFAULT 0 COMMENT '播放次数',
    last_play_time DATETIME COMMENT '最后播放时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)',
    INDEX idx_title (title),
    INDEX idx_video_type (video_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频文件表';

-- ------------------------------------------------------------
-- 2. 创建视频播放记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS video_play_record;
CREATE TABLE video_play_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    video_id BIGINT NOT NULL COMMENT '视频ID',
    video_title VARCHAR(255) COMMENT '视频标题(冗余)',
    username VARCHAR(50) COMMENT '播放用户',
    played_seconds INT DEFAULT 0 COMMENT '播放时长(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_video_id (video_id),
    INDEX idx_username (username),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频播放记录表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;

-- ------------------------------------------------------------
-- 3. 注册菜单到 sys_menu 表
-- ------------------------------------------------------------
SET @menu_id = NULL;
SELECT id INTO @menu_id FROM sys_menu WHERE component = 'video/player/index' LIMIT 1;

-- 如果已存在则更新名称和图标
UPDATE sys_menu
SET menu_name = '视频播放', icon = 'fa-solid fa-film', path = '/tool/videoPlayer', perms = 'video:player:list', sort = 45, status = 1, visible = 1
WHERE @menu_id IS NOT NULL AND component = 'video/player/index';

-- 如果不存在则插入
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '视频播放', 24, 2, '/tool/videoPlayer', 'video/player/index',
       'video:player:list', 'fa-solid fa-film', 45, 1, 1
WHERE @menu_id IS NULL;

SELECT LAST_INSERT_ID() INTO @new_menu_id;
SET @video_menu_id = IF(@menu_id IS NOT NULL, @menu_id, @new_menu_id);

-- ------------------------------------------------------------
-- 4. 注册按钮权限（子菜单）
-- ------------------------------------------------------------
-- 扫描权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '扫描视频', @video_menu_id, 3, '', '', 'video:player:scan', '', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:player:scan');

-- 列表查询权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '列表查询', @video_menu_id, 3, '', '', 'video:player:list', '', 2, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:player:list');

-- 删除权限
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
SELECT '删除记录', @video_menu_id, 3, '', '', 'video:player:delete', '', 3, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @video_menu_id AND perms = 'video:player:delete');

-- ------------------------------------------------------------
-- 5. 给超级管理员角色添加权限（角色ID=1）
-- ------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS temp_video_menu_ids;
CREATE TEMPORARY TABLE temp_video_menu_ids (menu_id BIGINT);

INSERT INTO temp_video_menu_ids (menu_id)
SELECT id FROM sys_menu WHERE perms LIKE 'video:player:%';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM temp_video_menu_ids
WHERE menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

DROP TEMPORARY TABLE IF EXISTS temp_video_menu_ids;

-- ------------------------------------------------------------
-- 完成
-- ------------------------------------------------------------
SELECT '视频播放模块数据库初始化完成！' AS result;
SELECT CONCAT('菜单ID: ', @video_menu_id) AS menu_info;

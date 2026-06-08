-- =============================================
-- 音乐播放器 - 数据库初始化脚本
-- =============================================

-- 歌曲表
CREATE TABLE IF NOT EXISTS `songs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `title` VARCHAR(255) NOT NULL COMMENT '歌曲标题',
    `artist` VARCHAR(255) DEFAULT '' COMMENT '艺术家',
    `album` VARCHAR(255) DEFAULT '' COMMENT '专辑',
    `mp3_path` VARCHAR(500) NOT NULL COMMENT 'MP3文件路径',
    `lrc_path` VARCHAR(500) DEFAULT NULL COMMENT 'LRC歌词文件路径',
    `lrc_content` TEXT DEFAULT NULL COMMENT '歌词内容(缓存)',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `duration` INT DEFAULT 0 COMMENT '时长(秒)',
    `play_count` INT DEFAULT 0 COMMENT '播放次数',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_title` (`title`),
    INDEX `idx_play_count` (`play_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲表';

-- 播放记录表
CREATE TABLE IF NOT EXISTS `play_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `song_id` BIGINT NOT NULL COMMENT '歌曲ID',
    `song_title` VARCHAR(255) DEFAULT '' COMMENT '歌曲标题',
    `username` VARCHAR(100) DEFAULT 'anonymous' COMMENT '播放用户',
    `played_seconds` INT DEFAULT 0 COMMENT '播放时长(秒)',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_song_id` (`song_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放记录表';

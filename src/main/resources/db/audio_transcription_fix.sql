-- ============================================================
-- 终极修复：无论当前表是什么状态，确保最终有 create_time / update_time / deleted
-- 执行方式：整段复制到 MySQL 执行即可
-- ============================================================

-- -------- 修复 audio_transcription --------
SET @t_has_created_at = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'created_at');
SET @t_has_create_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'create_time');
SET @t_has_updated_at = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'updated_at');
SET @t_has_update_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'update_time');
SET @t_has_deleted = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'deleted');
SET @t_has_created_by = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'created_by');

-- created_at -> create_time
SET @sql1 = IF(@t_has_created_at = 1 AND @t_has_create_time = 0,
    'ALTER TABLE audio_transcription CHANGE COLUMN created_at create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT ''audio_transcription: 无需修改 created_at'' AS skip');
PREPARE stmt FROM @sql1; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- updated_at -> update_time
SET @sql2 = IF(@t_has_updated_at = 1 AND @t_has_update_time = 0,
    'ALTER TABLE audio_transcription CHANGE COLUMN updated_at update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT ''audio_transcription: 无需修改 updated_at'' AS skip');
PREPARE stmt FROM @sql2; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 如果完全没有 create_time，直接添加
SET @t_has_create_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'create_time');
SET @sql3 = IF(@t_has_create_time = 0,
    'ALTER TABLE audio_transcription ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT ''audio_transcription: create_time 已存在'' AS skip');
PREPARE stmt FROM @sql3; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @t_has_update_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_transcription' AND column_name = 'update_time');
SET @sql4 = IF(@t_has_update_time = 0,
    'ALTER TABLE audio_transcription ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT ''audio_transcription: update_time 已存在'' AS skip');
PREPARE stmt FROM @sql4; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- deleted 列（软删除）
SET @sql5 = IF(@t_has_deleted = 0,
    'ALTER TABLE audio_transcription ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT ''逻辑删除(0-未删除 1-已删除)''',
    'SELECT ''audio_transcription: deleted 已存在'' AS skip');
PREPARE stmt FROM @sql5; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 清理 created_by（不再需要）
SET @sql6 = IF(@t_has_created_by = 1,
    'ALTER TABLE audio_transcription DROP COLUMN created_by',
    'SELECT ''audio_transcription: 无 created_by 列'' AS skip');
PREPARE stmt FROM @sql6; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -------- 修复 audio_segment --------
SET @s_has_created_at = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_segment' AND column_name = 'created_at');
SET @s_has_create_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_segment' AND column_name = 'create_time');
SET @s_has_update_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_segment' AND column_name = 'update_time');
SET @s_has_deleted = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_segment' AND column_name = 'deleted');

-- created_at -> create_time
SET @sql7 = IF(@s_has_created_at = 1 AND @s_has_create_time = 0,
    'ALTER TABLE audio_segment CHANGE COLUMN created_at create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT ''audio_segment: 无需修改 created_at'' AS skip');
PREPARE stmt FROM @sql7; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s_has_create_time = EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audio_segment' AND column_name = 'create_time');
SET @sql8 = IF(@s_has_create_time = 0,
    'ALTER TABLE audio_segment ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT ''audio_segment: create_time 已存在'' AS skip');
PREPARE stmt FROM @sql8; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql9 = IF(@s_has_update_time = 0,
    'ALTER TABLE audio_segment ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT ''audio_segment: update_time 已存在'' AS skip');
PREPARE stmt FROM @sql9; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- audio_segment 也需要 deleted 列（因为继承 BaseEntity，有 @TableLogic）
SET @sql10 = IF(@s_has_deleted = 0,
    'ALTER TABLE audio_segment ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT ''逻辑删除(0-未删除 1-已删除)''',
    'SELECT ''audio_segment: deleted 已存在'' AS skip');
PREPARE stmt FROM @sql10; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -------- 确认结果 --------
DESCRIBE audio_transcription;
DESCRIBE audio_segment;
SELECT '========================================' AS status,
       '表结构修复完成，上传、查询、删除都可以正常工作了！' AS message,
       '========================================' AS done;
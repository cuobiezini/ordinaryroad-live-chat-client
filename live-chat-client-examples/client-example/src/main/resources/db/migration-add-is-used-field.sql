-- ============================================
-- 入场记录表添加 is_used 字段迁移脚本
-- 执行时间: 2026-06-13
-- 说明: 为 live_entry_history 表添加"是否已被使用过"字段
-- ============================================

USE `live`;

-- 检查字段是否已存在，避免重复添加
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.columns 
    WHERE table_schema = 'live' 
      AND table_name = 'live_entry_history' 
      AND column_name = 'is_used'
);

-- 如果字段不存在，则添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `live_entry_history` ADD COLUMN `is_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否已被使用过: 0-未使用, 1-已使用'' AFTER `display_id`',
    'SELECT ''字段 is_used 已存在，跳过添加'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查索引是否已存在
SET @index_exists = (
    SELECT COUNT(*) 
    FROM information_schema.statistics 
    WHERE table_schema = 'live' 
      AND table_name = 'live_entry_history' 
      AND index_name = 'idx_is_used'
);

-- 如果索引不存在，则添加
SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `live_entry_history` ADD INDEX `idx_is_used` (`is_used`)',
    'SELECT ''索引 idx_is_used 已存在，跳过添加'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证修改结果
SELECT 
    COLUMN_NAME AS '字段名',
    DATA_TYPE AS '数据类型',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM information_schema.columns
WHERE table_schema = 'live'
  AND table_name = 'live_entry_history'
ORDER BY ORDINAL_POSITION;

-- 显示索引信息
SHOW INDEX FROM `live_entry_history`;

-- ============================================
-- 使用说明
-- ============================================
-- 1. 此脚本支持重复执行（幂等性）
-- 2. 会自动检测字段和索引是否已存在
-- 3. 新增字段默认值为 0（未使用）
-- 4. 添加了 is_used 字段的索引，便于查询优化
-- ============================================

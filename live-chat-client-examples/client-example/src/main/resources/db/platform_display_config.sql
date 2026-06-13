-- ============================================
-- 平台展示配置表 DDL
-- 用于控制哪些直播平台在前端页面中显示
-- ============================================

CREATE TABLE IF NOT EXISTS `platform_display_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台标识（bilibili/douyu/kuaishou/douyin）',
    `platform_name` VARCHAR(50) NOT NULL COMMENT '平台中文名称',
    `enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用显示（0=禁用，1=启用）',
    `display_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序（数字越小越靠前）',
    `icon` VARCHAR(10) DEFAULT NULL COMMENT '平台图标emoji',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台展示配置表';

-- ============================================
-- 插入默认数据
-- 默认只启用抖音平台
-- ============================================

INSERT INTO `platform_display_config` (`platform`, `platform_name`, `enabled`, `display_order`, `icon`, `remark`) VALUES
('douyin', '抖音', 1, 1, '⚫', '默认启用的平台'),
('bilibili', 'B站', 0, 2, '🟣', '哔哩哔哩直播平台'),
('douyu', '斗鱼', 0, 3, '🔴', '斗鱼直播平台'),
('kuaishou', '快手', 0, 4, '🟠', '快手直播平台');

-- ============================================
-- 查询示例
-- ============================================

-- 查看所有平台配置
-- SELECT * FROM platform_display_config ORDER BY display_order;

-- 查看已启用的平台
-- SELECT * FROM platform_display_config WHERE enabled = 1 ORDER BY display_order;

-- 启用B站平台
-- UPDATE platform_display_config SET enabled = 1 WHERE platform = 'bilibili';

-- 禁用抖音平台
-- UPDATE platform_display_config SET enabled = 0 WHERE platform = 'douyin';

-- 修改显示顺序
-- UPDATE platform_display_config SET display_order = 1 WHERE platform = 'bilibili';
-- UPDATE platform_display_config SET display_order = 2 WHERE platform = 'douyin';

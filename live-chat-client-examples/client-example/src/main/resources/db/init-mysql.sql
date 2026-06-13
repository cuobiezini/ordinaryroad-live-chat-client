-- ============================================
-- 直播客户端配置数据库初始化脚本 (MySQL)
-- 数据库名称: live
-- 创建时间: 2026-04-04
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `live` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `live`;

-- ============================================
-- 平台配置表
-- ============================================
DROP TABLE IF EXISTS `platform_config`;

CREATE TABLE `platform_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台标识：bilibili, douyu, kuaishou, douyin',
    `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
    `cookie` TEXT COMMENT 'Cookie认证信息',
    `auto_reconnect` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否自动重连：0-否，1-是',
    `room_info_get_type` VARCHAR(50) COMMENT '房间信息获取方式（快手专用）：COOKIE, NOT_COOKIE',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-否，1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注说明',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform` (`platform`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播平台配置表';

-- ============================================
-- 插入默认配置数据
-- ============================================
INSERT INTO `platform_config` (`platform`, `room_id`, `cookie`, `auto_reconnect`, `room_info_get_type`, `enabled`, `remark`) 
VALUES 
    ('bilibili', '', '', 1, NULL, 1, 'B站直播间配置'),
    ('douyu', '', '', 1, NULL, 1, '斗鱼直播间配置'),
    ('kuaishou', '', '', 1, 'NOT_COOKIE', 1, '快手直播间配置'),
    ('douyin', '', '', 1, NULL, 1, '抖音直播间配置')
ON DUPLICATE KEY UPDATE 
    `updated_at` = CURRENT_TIMESTAMP;

-- ============================================
-- 查询验证
-- ============================================
SELECT * FROM `platform_config`;

-- ============================================
-- 使用说明
-- ============================================
-- 1. 执行此脚本前，请确保MySQL服务已启动
-- 2. 确认用户名(root)和密码(123456)正确
-- 3. 脚本会自动创建数据库和表结构
-- 4. 会自动插入4个平台的默认配置
-- 5. 应用启动时JPA会自动同步表结构（ddl-auto: update）

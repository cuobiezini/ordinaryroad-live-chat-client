-- ============================================
-- 直播客户端数据库完整初始化脚本 (MySQL)
-- 数据库名称: live
-- 创建时间: 2026-04-04
-- 最后更新: 2026-06-13
-- 
-- 说明:
-- 1. 此脚本包含所有表结构和初始数据
-- 2. 支持重复执行（幂等性）
-- 3. 按模块分组，便于维护
-- ============================================

-- ============================================
-- 第一部分：数据库创建
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `live` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `live`;

-- ============================================
-- 第二部分：核心业务表
-- ============================================

-- ----------------------------
-- 1. 直播平台配置表
-- ----------------------------
DROP TABLE IF EXISTS `platform_config`;
CREATE TABLE `platform_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台标识：bilibili, douyu, kuaishou, douyin',
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

-- 插入默认配置数据
INSERT INTO `platform_config` (`platform`, `cookie`, `auto_reconnect`, `room_info_get_type`, `enabled`, `remark`) 
VALUES 
    ('kuaishou', '', 1, 'NOT_COOKIE', 1, '快手直播间配置'),
    ('douyin', '', 1, NULL, 1, '抖音直播间配置')
ON DUPLICATE KEY UPDATE 
    `updated_at` = CURRENT_TIMESTAMP;

-- ----------------------------
-- 2. 弹幕消息记录表
-- ----------------------------
DROP TABLE IF EXISTS `live_danmu_history`;
CREATE TABLE `live_danmu_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台标识: douyin, kuaishou',
    `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
    `uid` VARCHAR(100) NOT NULL COMMENT '用户唯一标识UID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
    `display_id` VARCHAR(100) DEFAULT NULL COMMENT '展示ID (抖音号/快手号)',
    `badge_name` VARCHAR(50) DEFAULT NULL COMMENT '粉丝勋章名称',
    `badge_level` INT DEFAULT '0' COMMENT '粉丝勋章等级',
    `content` TEXT NOT NULL COMMENT '弹幕消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_platform` (`room_id`, `platform`),
    KEY `idx_uid` (`uid`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播弹幕记录表';

-- ----------------------------
-- 3. 直播间统计数据表 (实时人数/点赞)
-- ----------------------------
DROP TABLE IF EXISTS `live_room_stats_history`;
CREATE TABLE `live_room_stats_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台标识: douyin, kuaishou',
    `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
    `liked_count` BIGINT DEFAULT '0' COMMENT '累计点赞数',
    `watching_count` INT DEFAULT '0' COMMENT '当前在线观看人数',
    `watched_count` BIGINT DEFAULT '0' COMMENT '累计观看人数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采样时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_platform` (`room_id`, `platform`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间统计数据历史表';

-- ----------------------------
-- 4. 用户入场记录表 (主要针对抖音)
-- ----------------------------
DROP TABLE IF EXISTS `live_entry_history`;
CREATE TABLE `live_entry_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台标识: douyin',
    `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
    `uid` VARCHAR(100) NOT NULL COMMENT '用户唯一标识UID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
    `display_id` VARCHAR(100) DEFAULT NULL COMMENT '抖音号',
    `is_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已被使用过: 0-未使用, 1-已使用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '进入时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_platform` (`room_id`, `platform`),
    KEY `idx_uid` (`uid`),
    KEY `idx_is_used` (`is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间用户入场记录表';

-- ----------------------------
-- 5. 礼物赠送记录表
-- ----------------------------
DROP TABLE IF EXISTS `live_gift_history`;
CREATE TABLE `live_gift_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台标识: douyin, kuaishou',
    `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
    `uid` VARCHAR(100) NOT NULL COMMENT '赠送者UID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '赠送者昵称',
    `gift_id` VARCHAR(50) DEFAULT NULL COMMENT '礼物ID',
    `gift_name` VARCHAR(50) DEFAULT NULL COMMENT '礼物名称',
    `gift_count` INT DEFAULT '1' COMMENT '礼物数量',
    `gift_price` INT DEFAULT '0' COMMENT '礼物单价',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '赠送时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_platform` (`room_id`, `platform`),
    KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播礼物赠送记录表';

-- ============================================
-- 第三部分：系统管理表
-- ============================================

-- ----------------------------
-- 6. 系统用户表 (后台管理系统登录认证)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（登录账号）',
    `password` VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` VARCHAR(50) NOT NULL DEFAULT 'ROLE_ADMIN' COMMENT '角色（ROLE_USER/ROLE_ADMIN）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用（0=禁用，1=启用）',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 插入默认管理员账号
-- 用户名: admin
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `role`, `enabled`, `remark`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'admin@ordinaryroad.tech', 'ROLE_ADMIN', 1, '默认管理员账号，请及时修改密码')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- ----------------------------
-- 7. 平台展示配置表 (控制前端页面显示的平台)
-- ----------------------------
DROP TABLE IF EXISTS `platform_display_config`;
CREATE TABLE `platform_display_config` (
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

-- 插入默认数据（默认只启用抖音和快手平台）
INSERT INTO `platform_display_config` (`platform`, `platform_name`, `enabled`, `display_order`, `icon`, `remark`) VALUES
('douyin', '抖音', 1, 1, '⚫', '默认启用的平台'),
('kuaishou', '快手', 0, 2, '🟠', '快手直播平台');

-- ----------------------------
-- 8. API Key 配置表 (外部接口调用认证)
-- ----------------------------
DROP TABLE IF EXISTS `api_key_config`;
CREATE TABLE `api_key_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `api_key` VARCHAR(128) NOT NULL COMMENT 'API Key（唯一标识）',
    `api_secret` VARCHAR(256) DEFAULT NULL COMMENT 'API Secret（用于签名验证，可选）',
    `app_name` VARCHAR(100) NOT NULL COMMENT '调用方名称/备注',
    `ip_whitelist` TEXT DEFAULT NULL COMMENT 'IP 白名单（多个IP用逗号分隔，为空表示不限制）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `max_qps` INT DEFAULT 10 COMMENT '每秒最大请求数（QPS限流）',
    `max_daily_requests` INT DEFAULT 10000 COMMENT '每日最大请求数',
    `remark` TEXT DEFAULT NULL COMMENT '备注说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key` (`api_key`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_app_name` (`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API Key 配置表';

-- 插入示例数据（测试用）
INSERT INTO `api_key_config` 
(`api_key`, `api_secret`, `app_name`, `ip_whitelist`, `enabled`, `max_qps`, `max_daily_requests`, `remark`)
VALUES
('test-api-key-123456', 
 'test-secret-789012', 
 '测试应用', 
 '',  -- IP白名单为空表示不限制
 1,   -- 启用
 5,   -- 每秒最多5次请求
 1000, -- 每天最多1000次请求
 '这是一个测试用的 API Key，仅用于开发环境');

-- ============================================
-- 第四部分：验证查询
-- ============================================

-- 查看所有表
SHOW TABLES;

-- 查看各表记录数
SELECT 'platform_config' AS table_name, COUNT(*) AS record_count FROM platform_config
UNION ALL
SELECT 'live_danmu_history', COUNT(*) FROM live_danmu_history
UNION ALL
SELECT 'live_room_stats_history', COUNT(*) FROM live_room_stats_history
UNION ALL
SELECT 'live_entry_history', COUNT(*) FROM live_entry_history
UNION ALL
SELECT 'live_gift_history', COUNT(*) FROM live_gift_history
UNION ALL
SELECT 'sys_user', COUNT(*) FROM sys_user
UNION ALL
SELECT 'platform_display_config', COUNT(*) FROM platform_display_config
UNION ALL
SELECT 'api_key_config', COUNT(*) FROM api_key_config;

-- ============================================
-- 使用说明
-- ============================================
-- 1. 执行此脚本前，请确保MySQL服务已启动
-- 2. 确认数据库用户名和密码正确
-- 3. 脚本会自动创建数据库和所有表结构
-- 4. 会自动插入所有默认配置数据
-- 5. 支持重复执行（幂等性），可安全地多次运行
-- 6. 应用启动时JPA会自动同步表结构（ddl-auto: update）
-- 
-- 执行命令：
-- mysql -u root -p < schema.sql
-- ============================================

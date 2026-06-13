-- ============================================
-- 直播间数据采集初始化脚本 (MySQL)
-- 适用平台: 抖音 (Douyin), 快手 (Kuaishou)
-- 创建时间: 2026-04-04
-- ============================================

-- 使用数据库
USE `live`;

-- ----------------------------
-- 1. 弹幕消息记录表
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
-- 2. 直播间统计数据表 (实时人数/点赞)
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
-- 3. 用户入场记录表 (主要针对抖音)
-- ----------------------------
DROP TABLE IF EXISTS `live_entry_history`;
CREATE TABLE `live_entry_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` VARCHAR(20) NOT NULL COMMENT '平台标识: douyin',
  `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
  `uid` VARCHAR(100) NOT NULL COMMENT '用户唯一标识UID',
  `username` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  `display_id` VARCHAR(100) DEFAULT NULL COMMENT '抖音号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '进入时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_platform` (`room_id`, `platform`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间用户入场记录表';

-- ----------------------------
-- 4. 礼物赠送记录表
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

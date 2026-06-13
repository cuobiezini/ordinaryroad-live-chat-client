-- ============================================
-- 系统用户表 DDL
-- 用于后台管理系统登录认证
-- ============================================

CREATE TABLE IF NOT EXISTS `sys_user` (
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

-- ============================================
-- 插入默认管理员账号
-- 用户名: admin
-- 密码: admin123 (BCrypt加密后的值)
-- ============================================

-- 注意：实际使用时，密码应该通过BCryptPasswordEncoder加密
-- 这里提供一个预计算的BCrypt哈希值供参考
-- 原始密码: admin123
-- BCrypt哈希: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `role`, `enabled`, `remark`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'admin@ordinaryroad.tech', 'ROLE_ADMIN', 1, '默认管理员账号，请及时修改密码')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- ============================================
-- 查询示例
-- ============================================

-- 查看所有用户
-- SELECT id, username, real_name, role, enabled, created_at FROM sys_user;

-- 查看启用的用户
-- SELECT * FROM sys_user WHERE enabled = 1;

-- 禁用用户
-- UPDATE sys_user SET enabled = 0 WHERE username = 'admin';

-- 重置密码（需要重新生成BCrypt哈希）
-- UPDATE sys_user SET password = '$2a$10$...' WHERE username = 'admin';

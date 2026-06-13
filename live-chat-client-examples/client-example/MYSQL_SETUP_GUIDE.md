# MySQL 数据库配置指南

## 📋 概述

本项目已从H2嵌入式数据库迁移到MySQL数据库，提供更稳定、可靠的数据持久化能力。

---

## 🔧 数据库配置信息

### 连接参数

| 参数 | 值 | 说明 |
|------|-----|------|
| 数据库类型 | MySQL 8.0+ | 推荐使用MySQL 8.0或更高版本 |
| 主机地址 | localhost | 本地MySQL服务器 |
| 端口 | 3306 | MySQL默认端口 |
| 数据库名 | live | 直播平台配置数据库 |
| 用户名 | root | 数据库用户 |
| 密码 | 123456 | 数据库密码 |
| 字符集 | utf8mb4 | 支持完整Unicode字符 |

### JDBC连接URL

```
jdbc:mysql://localhost:3306/live?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

**参数说明：**
- `useUnicode=true`: 启用Unicode支持
- `characterEncoding=utf8`: 使用UTF-8编码
- `useSSL=false`: 禁用SSL（本地开发环境）
- `serverTimezone=Asia/Shanghai`: 设置时区为上海
- `allowPublicKeyRetrieval=true`: 允许公钥检索（MySQL 8.0+需要）

---

## 🚀 快速开始

### 第一步：确保MySQL已安装并运行

#### Windows系统
```powershell
# 检查MySQL服务状态
Get-Service MySQL80

# 如果未启动，启动MySQL服务
Start-Service MySQL80
```

#### Linux/Mac系统
```bash
# 检查MySQL状态
sudo systemctl status mysql

# 启动MySQL
sudo systemctl start mysql
```

### 第二步：初始化数据库

#### 方式1：使用提供的SQL脚本（推荐）

```bash
# 进入项目目录
cd C:\Users\123\IdeaProjects\ordinaryroad-live-chat-client\live-chat-client-examples\client-example

# 执行初始化脚本
mysql -u root -p123456 < src/main/resources/db/init-mysql.sql
```

或者在MySQL客户端中执行：
```sql
source C:/Users/123/IdeaProjects/ordinaryroad-live-chat-client/live-chat-client-examples/client-example/src/main/resources/db/init-mysql.sql;
```

#### 方式2：手动创建数据库和表

```sql
-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `live` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 使用数据库
USE `live`;

-- 3. 创建表（JPA会自动创建，也可手动执行init-mysql.sql中的建表语句）
```

#### 方式3：让JPA自动创建（最简单）

应用启动时，由于配置了`spring.jpa.hibernate.ddl-auto=update`，JPA会自动创建表结构。

**优点：** 无需手动执行SQL  
**缺点：** 不会插入默认数据

### 第三步：启动应用

```bash
mvn spring-boot:run
```

应用启动时会：
1. 连接到MySQL数据库
2. 自动创建或更新表结构
3. 通过`ConfigPersistenceService`初始化默认配置

### 第四步：验证连接

```bash
# 查询所有配置
curl http://localhost:8080/api/live-chat/configs
```

如果返回4个平台的配置数据，说明MySQL连接成功！

---

## 📊 数据库表结构

### platform_config 表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键ID | PRIMARY KEY, AUTO_INCREMENT |
| platform | VARCHAR(50) | 平台标识 | NOT NULL, UNIQUE |
| room_id | VARCHAR(100) | 直播间ID | NOT NULL |
| cookie | TEXT | Cookie认证信息 | - |
| auto_reconnect | TINYINT(1) | 是否自动重连 | NOT NULL, DEFAULT 1 |
| room_info_get_type | VARCHAR(50) | 房间信息获取方式 | 快手专用 |
| enabled | TINYINT(1) | 是否启用 | NOT NULL, DEFAULT 1 |
| created_at | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | NOT NULL, ON UPDATE CURRENT_TIMESTAMP |
| remark | VARCHAR(500) | 备注说明 | - |

**索引：**
- 主键索引：`id`
- 唯一索引：`platform`
- 普通索引：`enabled`

---

## 🔍 常用MySQL操作

### 查看数据库

```sql
-- 查看所有数据库
SHOW DATABASES;

-- 使用live数据库
USE live;

-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESCRIBE platform_config;
```

### 查询配置数据

```sql
-- 查看所有平台配置
SELECT * FROM platform_config;

-- 查看B站配置
SELECT * FROM platform_config WHERE platform = 'bilibili';

-- 查看启用的配置
SELECT * FROM platform_config WHERE enabled = 1;
```

### 更新配置

```sql
-- 更新B站直播间ID
UPDATE platform_config 
SET room_id = '7777' 
WHERE platform = 'bilibili';

-- 更新Cookie
UPDATE platform_config 
SET cookie = 'your_new_cookie' 
WHERE platform = 'bilibili';

-- 禁用某个平台
UPDATE platform_config 
SET enabled = 0 
WHERE platform = 'douyu';
```

### 备份数据库

```bash
# 备份整个数据库
mysqldump -u root -p123456 live > live_backup_$(date +%Y%m%d).sql

# 只备份表结构
mysqldump -u root -p123456 --no-data live > live_structure.sql

# 只备份数据
mysqldump -u root -p123456 --no-create-info live > live_data.sql
```

### 恢复数据库

```bash
mysql -u root -p123456 live < live_backup_20260404.sql
```

---

## ⚙️ 修改数据库配置

### 修改密码

编辑 `application.yaml`：

```yaml
spring:
  datasource:
    username: root
    password: your_new_password  # 修改这里
```

### 修改数据库名称

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name?...
```

### 远程MySQL服务器

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/live?...
    username: remote_user
    password: remote_password
```

---

## 🐛 常见问题排查

### 问题1：连接失败 - Access denied

**错误信息：**
```
Access denied for user 'root'@'localhost' (using password: YES)
```

**解决方案：**
1. 检查用户名和密码是否正确
2. 确认MySQL服务已启动
3. 检查用户权限：
```sql
-- 查看用户权限
SELECT User, Host FROM mysql.user;

-- 授予权限
GRANT ALL PRIVILEGES ON live.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### 问题2：数据库不存在

**错误信息：**
```
Unknown database 'live'
```

**解决方案：**
```sql
-- 手动创建数据库
CREATE DATABASE `live` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;
```

或者执行初始化脚本：
```bash
mysql -u root -p123456 < src/main/resources/db/init-mysql.sql
```

### 问题3：时区错误

**错误信息：**
```
The server time zone value '???ú±ê×??±??' is unrecognized
```

**解决方案：**
已在JDBC URL中配置：`serverTimezone=Asia/Shanghai`

或者在MySQL中设置：
```sql
SET GLOBAL time_zone = '+8:00';
```

### 问题4：SSL连接警告

**警告信息：**
```
Establishing SSL connection without server's identity verification is not recommended
```

**解决方案：**
已在JDBC URL中配置：`useSSL=false`（仅用于开发环境）

生产环境建议：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/live?useSSL=true&requireSSL=true
```

### 问题5：表不存在

**错误信息：**
```
Table 'live.platform_config' doesn't exist
```

**解决方案：**
1. 检查`spring.jpa.hibernate.ddl-auto`配置是否为`update`或`create`
2. 重启应用，JPA会自动创建表
3. 或手动执行初始化脚本

### 问题6：中文乱码

**解决方案：**
1. 确认数据库字符集为utf8mb4：
```sql
SHOW CREATE DATABASE live;
```

2. 确认表字符集为utf8mb4：
```sql
SHOW CREATE TABLE platform_config;
```

3. 如果不是，修改：
```sql
ALTER DATABASE live CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE platform_config CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 🔒 安全建议

### 生产环境配置

1. **修改默认密码**
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:strong_password_here}
```

2. **启用SSL连接**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/live?useSSL=true&requireSSL=true
```

3. **使用环境变量**
```bash
export DB_PASSWORD=your_secure_password
```

4. **限制数据库用户权限**
```sql
-- 创建专用用户
CREATE USER 'live_chat'@'localhost' IDENTIFIED BY 'secure_password';

-- 授予最小权限
GRANT SELECT, INSERT, UPDATE, DELETE ON live.platform_config TO 'live_chat'@'localhost';
FLUSH PRIVILEGES;
```

5. **禁用ddl-auto（生产环境）**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 或 none
```

---

## 📈 性能优化

### 1. 连接池配置

Spring Boot默认使用HikariCP，可在`application.yaml`中优化：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时（毫秒）
      idle-timeout: 600000         # 空闲超时（毫秒）
      max-lifetime: 1800000        # 最大生命周期（毫秒）
```

### 2. 索引优化

已为常用查询字段添加索引：
- `platform`: 唯一索引（快速查找平台配置）
- `enabled`: 普通索引（快速筛选启用配置）

### 3. JPA优化

```yaml
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_second_level_cache: true  # 启用二级缓存
          region.factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

---

## 🔄 从H2迁移到MySQL

如果您之前使用的是H2数据库，迁移步骤：

### 1. 导出H2数据

访问 H2 控制台（如果还保留）：
```
http://localhost:8080/h2-console
```

导出数据为CSV或SQL格式。

### 2. 导入到MySQL

```sql
-- 在MySQL中执行导入
LOAD DATA INFILE 'platform_config.csv'
INTO TABLE platform_config
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
```

### 3. 验证数据

```sql
SELECT COUNT(*) FROM platform_config;
```

---

## 📞 技术支持

### MySQL官方文档
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)
- [Connector/J Documentation](https://dev.mysql.com/doc/connector-j/8.0/en/)

### 常用工具
- **MySQL Workbench**: 图形化管理工具
- **phpMyAdmin**: Web版管理界面
- **Navicat**: 商业数据库管理工具
- **DBeaver**: 免费开源的通用数据库工具

### 检查MySQL版本
```bash
mysql --version
```

推荐版本：MySQL 8.0+

---

## ✅ 检查清单

启动应用前，请确认：

- [ ] MySQL服务已启动
- [ ] 数据库`live`已创建
- [ ] 用户名和密码正确（root / 123456）
- [ ] 防火墙允许3306端口（如果是远程MySQL）
- [ ] `application.yaml`配置正确
- [ ] Maven依赖已更新（mysql-connector-j）
- [ ] 表结构已创建（自动或手动）

---

## 🎉 完成！

现在您的项目已成功迁移到MySQL数据库，享受更稳定、可靠的數據持久化能力！

**下一步：**
1. 启动应用测试连接
2. 通过API配置直播间
3. 体验MySQL带来的稳定性提升

祝使用愉快！🚀

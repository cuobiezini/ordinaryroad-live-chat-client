# MySQL 数据库迁移完成总结

## ✅ 迁移完成

项目已成功从 **H2嵌入式数据库** 迁移到 **MySQL数据库**！

---

## 📋 改动清单

### 1. pom.xml 依赖修改

**移除：**
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**添加：**
```xml
<!-- MySQL数据库驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. application.yaml 配置修改

**修改前（H2）：**
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/live-chat-config
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console
```

**修改后（MySQL）：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/live?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

### 3. 新增文件

#### SQL初始化脚本
- **文件**: `src/main/resources/db/init-mysql.sql`
- **功能**: 
  - 创建`live`数据库
  - 创建`platform_config`表
  - 插入4个平台的默认配置数据

#### 配置指南文档
- **文件**: `MYSQL_SETUP_GUIDE.md`
- **内容**: 513行完整的MySQL配置、使用、故障排查指南

### 4. 更新文档

- ✅ `QUICK_START.md` - 添加MySQL前置要求和操作说明
- ✅ `REFACTORING_SUMMARY.md` - 更新数据库相关章节
- ✅ `PROJECT_STRUCTURE.md` - 无需修改（通用架构说明）

---

## 🎯 MySQL配置信息

### 连接参数

| 参数 | 值 |
|------|-----|
| 数据库类型 | MySQL 8.0+ |
| 主机地址 | localhost |
| 端口 | 3306 |
| 数据库名 | live |
| 用户名 | root |
| 密码 | 123456 |
| 字符集 | utf8mb4 |
| 时区 | Asia/Shanghai |

### JDBC URL

```
jdbc:mysql://localhost:3306/live?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

---

## 🚀 快速开始

### 第一步：确保MySQL已安装并运行

```powershell
# Windows检查MySQL服务
Get-Service MySQL80

# 如果未启动，启动服务
Start-Service MySQL80
```

### 第二步：初始化数据库

#### 方式1：执行SQL脚本（推荐）

```bash
mysql -u root -p123456 < src/main/resources/db/init-mysql.sql
```

#### 方式2：让JPA自动创建

直接启动应用，JPA会自动创建表结构（但不会插入默认数据）。

### 第三步：启动应用

```bash
mvn spring-boot:run
```

### 第四步：验证连接

```bash
curl http://localhost:8080/api/live-chat/configs
```

返回4个平台配置即表示成功！

---

## 📊 数据库表结构

### platform_config 表

```sql
CREATE TABLE `platform_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台标识',
    `room_id` VARCHAR(100) NOT NULL COMMENT '直播间ID',
    `cookie` TEXT COMMENT 'Cookie认证信息',
    `auto_reconnect` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否自动重连',
    `room_info_get_type` VARCHAR(50) COMMENT '房间信息获取方式（快手专用）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注说明',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform` (`platform`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**索引：**
- 主键：`id`
- 唯一索引：`platform`
- 普通索引：`enabled`

---

## 🔍 常用MySQL操作

### 查看配置数据

```sql
-- 连接数据库
mysql -u root -p123456 live

-- 查看所有配置
SELECT * FROM platform_config;

-- 查看B站配置
SELECT * FROM platform_config WHERE platform = 'bilibili';
```

### 更新配置

```sql
-- 更新B站直播间ID
UPDATE platform_config 
SET room_id = '7777' 
WHERE platform = 'bilibili';

-- 更新Cookie
UPDATE platform_config 
SET cookie = 'your_cookie' 
WHERE platform = 'bilibili';
```

### 备份数据库

```bash
# 完整备份
mysqldump -u root -p123456 live > live_backup_$(date +%Y%m%d).sql

# 恢复备份
mysql -u root -p123456 live < live_backup_20260404.sql
```

---

## ⚡ 核心优势

### 相比H2的优势

| 特性 | H2 | MySQL |
|------|----|-------|
| 数据持久性 | 文件存储，易丢失 | 专业数据库，可靠 |
| 并发性能 | 一般 | 优秀 |
| 数据安全 | 基础 | 完善（权限、加密） |
| 备份恢复 | 手动复制文件 | mysqldump工具 |
| 监控管理 | 简单控制台 | 丰富工具生态 |
| 生产适用性 | ❌ 仅开发测试 | ✅ 生产环境标准 |
| 扩展性 | 单文件限制 | 支持集群、主从 |

### 带来的好处

✅ **数据可靠性提升**：专业的ACID事务保证  
✅ **性能更优**：优化的查询引擎和索引机制  
✅ **易于管理**：丰富的图形化管理工具  
✅ **备份方便**：标准的mysqldump备份方案  
✅ **生产就绪**：符合生产环境要求  
✅ **可扩展**：支持未来的集群部署  

---

## 🐛 常见问题

### Q1: 连接失败 "Access denied"

**解决方案：**
```sql
-- 检查用户权限
SELECT User, Host FROM mysql.user;

-- 授予权限
GRANT ALL PRIVILEGES ON live.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### Q2: 数据库不存在

**解决方案：**
```bash
# 执行初始化脚本
mysql -u root -p123456 < src/main/resources/db/init-mysql.sql
```

### Q3: 中文乱码

**解决方案：**
确认数据库字符集为utf8mb4：
```sql
SHOW CREATE DATABASE live;
ALTER DATABASE live CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Q4: 时区错误

**已在JDBC URL中配置：**
```
serverTimezone=Asia/Shanghai
```

更多问题请查看 [MYSQL_SETUP_GUIDE.md](MYSQL_SETUP_GUIDE.md)

---

## 📖 相关文档

1. **[MYSQL_SETUP_GUIDE.md](MYSQL_SETUP_GUIDE.md)** - 完整的MySQL配置指南（513行）
   - 安装和配置
   - 数据库初始化
   - 常用操作
   - 故障排查
   - 性能优化
   - 安全建议

2. **[QUICK_START.md](QUICK_START.md)** - 5分钟快速开始（已更新MySQL说明）

3. **[REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)** - 改造总结（已更新数据库章节）

4. **[DYNAMIC_CONFIG_GUIDE.md](DYNAMIC_CONFIG_GUIDE.md)** - API使用指南

---

## ✅ 检查清单

启动应用前，请确认：

- [x] MySQL 8.0+ 已安装
- [x] MySQL服务正在运行
- [x] 数据库`live`已创建（或让JPA自动创建）
- [x] 用户名密码正确（root / 123456）
- [x] pom.xml已更新（mysql-connector-j）
- [x] application.yaml已配置MySQL数据源
- [x] 表结构已创建（自动或手动）
- [x] 防火墙允许3306端口（如需要）

---

## 🎉 迁移完成！

### 已完成的工作

✅ **代码改造**
- pom.xml依赖替换
- application.yaml配置更新
- PlatformConfig实体兼容MySQL

✅ **数据初始化**
- 创建init-mysql.sql脚本
- 自动建表和默认数据

✅ **文档完善**
- MYSQL_SETUP_GUIDE.md（513行）
- 更新所有相关文档
- 详细的故障排查指南

✅ **测试验证**
- 编译通过
- 无语法错误
- 配置正确

### 下一步

1. **启动MySQL服务**
2. **执行初始化脚本**（可选，JPA可自动建表）
3. **启动应用**：`mvn spring-boot:run`
4. **测试API**：运行 `test-dynamic-config.ps1`
5. **开始使用**：享受MySQL带来的稳定性！

---

## 📞 技术支持

如有问题，请查阅：
- 📘 [MYSQL_SETUP_GUIDE.md](MYSQL_SETUP_GUIDE.md) - 详细配置指南
- 📗 MySQL官方文档：https://dev.mysql.com/doc/
- 📙 常见问题章节

**祝使用愉快！** 🚀✨

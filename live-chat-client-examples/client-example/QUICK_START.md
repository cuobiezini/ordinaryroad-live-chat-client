# 🚀 动态配置系统 - 5分钟快速开始

## ⚠️ 前置要求

**数据库准备：**
- ✅ MySQL 8.0+ 已安装并运行
- ✅ 数据库 `live` 已创建（或让应用自动创建）
- ✅ 用户名：`root`，密码：`123456`

📖 **详细MySQL配置指南**: [MYSQL_SETUP_GUIDE.md](MYSQL_SETUP_GUIDE.md)

### 快速初始化MySQL

```bash
# 方式1：执行初始化脚本（推荐）
mysql -u root -p123456 < src/main/resources/db/init-mysql.sql

# 方式2：让JPA自动创建表结构（启动应用时自动执行）
```

---

## 第一步：启动应用

```bash
cd C:\Users\123\IdeaProjects\ordinaryroad-live-chat-client\live-chat-client-examples\client-example
mvn spring-boot:run
```

等待看到以下日志表示启动成功：
```
初始化直播平台默认配置...
默认配置初始化完成
```

---

## 第二步：配置直播间（以B站为例）

### 方式1：使用curl命令

```bash
# 更新B站配置
curl -X PUT http://localhost:8080/api/live-chat/config/bilibili ^
  -H "Content-Type: application/json" ^
  -d "{\"roomId\": \"7777\", \"cookie\": \"你的B站Cookie\"}"

# 连接B站客户端
curl -X POST http://localhost:8080/api/live-chat/connect/bilibili
```

### 方式2：使用PowerShell测试脚本

```powershell
.\test-dynamic-config.ps1
```

这会自动执行所有功能测试！

---

## 第三步：验证配置

### 查看配置是否保存

```bash
curl http://localhost:8080/api/live-chat/config/bilibili
```

响应示例：
```json
{
  "id": 1,
  "platform": "bilibili",
  "roomId": "7777",
  "cookie": "你的Cookie",
  "autoReconnect": true,
  "enabled": true
}
```

### 查看客户端状态

```bash
curl http://localhost:8080/api/live-chat/status/bilibili
```

响应示例：
```json
{
  "platform": "bilibili",
  "connected": true,
  "status": "CONNECTED"
}
```

---

## 第四步：发送弹幕测试

```bash
curl -X POST http://localhost:8080/api/live-chat/send-danmu/bilibili ^
  -H "Content-Type: application/json" ^
  -d "{\"message\": \"Hello from API!\"}"
```

---

## 🎯 常用操作速查

### 1. 查看所有平台配置
```bash
curl http://localhost:8080/api/live-chat/configs
```

### 2. 批量配置多个平台
```bash
curl -X PUT http://localhost:8080/api/live-chat/configs/batch ^
  -H "Content-Type: application/json" ^
  -d "[{\"platform\":\"bilibili\",\"roomId\":\"7777\"},{\"platform\":\"douyu\",\"roomId\":\"74751\"}]"
```

### 3. 重新连接客户端（配置更新后）
```bash
curl -X POST http://localhost:8080/api/live-chat/reconnect/bilibili
```

### 4. 断开客户端
```bash
curl -X POST http://localhost:8080/api/live-chat/disconnect/bilibili
```

### 5. 初始化所有客户端
```bash
curl -X POST http://localhost:8080/api/live-chat/initialize
```

---

## 🔍 查看数据库（可选）

### 方式1：使用MySQL命令行

```bash
# 连接数据库
mysql -u root -p123456 live

# 查看所有配置
SELECT * FROM platform_config;

# 退出
exit;
```

### 方式2：使用图形化工具

推荐使用以下工具之一：
- **MySQL Workbench** (官方工具)
- **DBeaver** (免费开源)
- **Navicat** (商业软件)
- **phpMyAdmin** (Web界面)

连接信息：
- **主机**: localhost
- **端口**: 3306
- **数据库**: live
- **用户名**: root
- **密码**: 123456

---

## ⚡ 核心优势

✅ **无需重启**：配置修改立即生效  
✅ **自动保存**：数据持久化到数据库  
✅ **统一管理**：一个API管理所有平台  
✅ **实时监控**：随时查询客户端状态  
✅ **批量操作**：一次性配置多个平台  

---

## 📖 更多帮助

- 📘 完整API文档：查看 [DYNAMIC_CONFIG_GUIDE.md](DYNAMIC_CONFIG_GUIDE.md)
- 📝 改造总结：查看 [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)
- 🧪 测试脚本：运行 `test-dynamic-config.ps1`

---

## ❓ 常见问题

**Q: 配置更新后没生效？**  
A: 执行重连命令：`curl -X POST http://localhost:8080/api/live-chat/reconnect/{platform}`

**Q: 如何获取Cookie？**  
A: 在浏览器中登录对应平台，按F12打开开发者工具，在Network标签中复制请求的Cookie头。

**Q: 数据库文件在哪里？**  
A: MySQL数据库，数据存储在MySQL的数据目录中。可以通过以下命令查看：
```sql
SHOW VARIABLES LIKE 'datadir';
```

**Q: 如何备份数据库？**  
A: 使用mysqldump命令：
```bash
mysqldump -u root -p123456 live > live_backup.sql
```

**Q: 如何关闭H2控制台？**  
A: 已移除H2数据库，不再需要此配置。

---

## 🎉 开始使用吧！

现在你已经掌握了基本用法，可以：
1. 配置你的直播间ID和Cookie
2. 连接客户端开始接收弹幕
3. 通过API动态调整配置
4. 享受灵活的管理体验！

**祝使用愉快！** 🚀

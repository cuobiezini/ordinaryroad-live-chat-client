# 🎬 直播弹幕客户端 - 后台管理系统使用指南

## 📋 目录
- [功能介绍](#功能介绍)
- [快速开始](#快速开始)
- [使用说明](#使用说明)
- [API接口](#api接口)
- [常见问题](#常见问题)

---

## ✨ 功能介绍

后台管理系统提供了一个直观的Web界面来管理多平台直播弹幕客户端，主要功能包括：

### 核心功能
- ✅ **可视化配置管理** - 通过Web界面配置各平台的直播间ID、Cookie等参数
- ✅ **实时连接控制** - 一键连接/断开/重连各平台客户端
- ✅ **状态实时监控** - 实时显示各平台客户端的连接状态
- ✅ **弹幕发送功能** - 支持向任意平台发送弹幕消息
- ✅ **操作日志记录** - 实时显示所有操作的执行日志
- ✅ **自动重连配置** - 可配置客户端断线后是否自动重连

### 支持的平台
- 🟣 **B站 (bilibili)** - Bilibili直播平台
- 🔴 **斗鱼 (douyu)** - 斗鱼直播平台
- 🟠 **快手 (kuaishou)** - 快手直播平台
- ⚫ **抖音 (douyin)** - 抖音直播平台

---

## 🚀 快速开始

### 1. 前置要求

确保以下环境已准备就绪：
- ✅ Java 17+
- ✅ Maven 3.6+
- ✅ MySQL 8.0+（数据库`live`已创建）

### 2. 启动应用

```bash
cd C:\Users\123\IdeaProjects\ordinaryroad-live-chat-client\live-chat-client-examples\client-example
mvn spring-boot:run
```

等待看到以下日志表示启动成功：
```
初始化直播平台默认配置...
默认配置初始化完成
Started LiveChatExampleApplication in X.XXX seconds
```

### 3. 访问管理界面

打开浏览器访问：
```
http://localhost:8080
```

**页面结构：**
- **总览页** (`/`) - 显示所有平台的概览信息和连接状态
- **B站配置页** (`/bilibili`) - B站直播间配置和管理
- **斗鱼配置页** (`/douyu`) - 斗鱼直播间配置和管理
- **快手配置页** (`/kuaishou`) - 快手直播间配置和管理
- **抖音配置页** (`/douyin`) - 抖音直播间配置和管理

---

## 📖 使用说明

### 界面布局

系统采用多页面设计，每个平台有独立的配置页面：

#### 📊 总览页 (`/`)
- **平台卡片概览** - 显示所有平台的连接状态和基本信息
- **快速导航** - 点击卡片可跳转到对应平台的详细配置页
- **操作日志** - 实时显示所有操作的执行日志

#### 🔧 平台配置页 (`/{platform}`)
每个平台页面包含三个主要区域：

**1️⃣ 平台配置区**
- **直播间ID** - 输入要监听的直播间ID
- **Cookie** - 输入浏览器Cookie（用于身份验证）
- **房间信息获取方式** - 快手平台专用选项
- **自动重连** - 勾选后客户端断线会自动重连
- **操作按钮** - 保存配置、连接、断开、重连

**2️⃣ 弹幕发送区**
- **弹幕内容** - 输入要发送的弹幕文本
- **发送按钮** - 点击发送弹幕到当前平台

**3️⃣ 操作日志区**
- 实时显示所有操作的执行结果
- 包含时间戳和操作详情
- 不同颜色区分信息类型（成功/失败/警告）

#### 🧭 导航栏
每个页面顶部都有导航栏，可以快速切换：
- 📊 总览
- 🟣 B站
- 🔴 斗鱼
- 🟠 快手
- ⚫ 抖音

### 操作流程

#### 🔧 配置并连接直播间

1. **导航到平台页面**
   - 点击顶部导航栏中的对应平台
   - 或在总览页点击平台卡片

2. **填写直播间ID**
   - 在对应平台页面中输入直播间ID
   - 例如B站：`7777`

3. **填写Cookie（可选）**
   - 部分平台需要Cookie才能正常监听
   - 在浏览器中按F12打开开发者工具
   - 在Network标签中找到请求，复制Cookie头的内容

4. **保存配置**
   - 点击“保存配置”按钮
   - 配置会自动保存到MySQL数据库

5. **连接客户端**
   - 点击“连接”按钮
   - 等待状态变为“已连接”（绿色徽章）

#### 💬 发送弹幕

1. 导航到对应平台页面（例如 `/bilibili`）
2. 在“发送弹幕”区域输入要发送的弹幕内容
3. 点击“发送弹幕”按钮
4. 查看操作日志确认发送结果

#### 🔄 重新连接

如果配置更新后需要生效：
1. 在平台页面修改配置并保存
2. 点击“重连”按钮
3. 客户端会使用最新配置重新连接

---

## 🔌 API接口

后台管理系统基于RESTful API构建，你也可以直接调用API进行操作：

### 配置管理

#### 获取所有平台配置
```bash
GET http://localhost:8080/api/live-chat/configs
```

#### 获取指定平台配置
```bash
GET http://localhost:8080/api/live-chat/config/{platform}
```

#### 更新平台配置
```bash
POST http://localhost:8080/api/live-chat/config/updateConfig/{platform}
Content-Type: application/json

{
  "roomId": "7777",
  "cookie": "your_cookie_here",
  "autoReconnect": true,
  "roomInfoGetType": "NOT_COOKIE"  // 仅快手需要
}
```

#### 批量更新配置
```bash
PUT http://localhost:8080/api/live-chat/configs/batch
Content-Type: application/json

[
  {"platform": "bilibili", "roomId": "7777"},
  {"platform": "douyu", "roomId": "74751"}
]
```

#### 删除平台配置
```bash
DELETE http://localhost:8080/api/live-chat/config/{platform}
```

### 连接控制

#### 连接客户端
```bash
POST http://localhost:8080/api/live-chat/connect/{platform}
```

#### 断开客户端
```bash
POST http://localhost:8080/api/live-chat/disconnect/{platform}
```

#### 重新连接客户端
```bash
POST http://localhost:8080/api/live-chat/reconnect/{platform}
```

### 弹幕发送

#### 发送弹幕
```bash
POST http://localhost:8080/api/live-chat/send-danmu/{platform}
Content-Type: application/json

{
  "message": "Hello from API!"
}
```

### 状态查询

#### 查询单个客户端状态
```bash
GET http://localhost:8080/api/live-chat/status/{platform}
```

响应示例：
```json
{
  "platform": "bilibili",
  "connected": true,
  "status": "CONNECTED"
}
```

#### 查询所有客户端状态
```bash
GET http://localhost:8080/api/live-chat/status
```

响应示例：
```json
{
  "bilibili": {"connected": true, "status": "CONNECTED"},
  "douyu": {"connected": false, "status": "DISCONNECTED"},
  "kuaishou": {"connected": false, "status": "DISCONNECTED"},
  "douyin": {"connected": false, "status": "DISCONNECTED"}
}
```

### 初始化

#### 初始化所有客户端
```bash
POST http://localhost:8080/api/live-chat/initialize
```

---

## ❓ 常见问题

### Q1: 如何获取Cookie？

**A:** 以Chrome浏览器为例：
1. 登录对应直播平台
2. 按F12打开开发者工具
3. 切换到Network标签
4. 刷新页面
5. 找到任意请求，右键选择"Copy" -> "Copy request headers"
6. 从中提取Cookie字段的内容

### Q2: 配置保存后没有生效？

**A:** 
1. 确保点击了"保存配置"按钮
2. 点击"重连"按钮使新配置生效
3. 查看操作日志确认是否有错误信息

### Q3: 连接失败怎么办？

**A:** 检查以下几点：
- 直播间ID是否正确
- Cookie是否有效（某些平台必需）
- 网络连接是否正常
- 查看操作日志中的错误信息

### Q4: 数据库在哪里查看？

**A:** 
- 数据库类型：MySQL
- 数据库名：`live`
- 主机：`localhost:3306`
- 用户名：`root`
- 密码：`123456`（可在application.yaml中修改）

可使用以下工具查看：
- MySQL Workbench（官方工具）
- DBeaver（免费开源）
- Navicat（商业软件）

### Q5: 如何修改数据库配置？

**A:** 编辑 `src/main/resources/application.yaml` 文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/live?...
    username: your_username
    password: your_password
```

### Q6: 支持同时监听多个直播间吗？

**A:** 支持！每个平台可以配置一个直播间ID。如果需要同一平台监听多个直播间，可以使用原有的Multiply API：
```bash
POST http://localhost:8080/client/multiply/newClientAndStart/{roomId}?platform=bilibili
```

### Q7: 日志太多怎么清理？

**A:** 日志会自动限制为最多100条，旧日志会自动删除。刷新页面也会清空日志。

### Q8: 如何备份配置数据？

**A:** 使用mysqldump命令：
```bash
mysqldump -u root -p123456 live > live_backup.sql
```

恢复数据：
```bash
mysql -u root -p123456 live < live_backup.sql
```

---

## 🎯 最佳实践

### 1. Cookie安全
- 不要将Cookie分享给他人
- 定期更新Cookie
- 生产环境建议使用环境变量存储敏感信息

### 2. 配置管理
- 修改配置后记得点击"重连"
- 重要配置建议先测试再应用到生产环境
- 定期备份数据库

### 3. 监控维护
- 关注操作日志中的错误信息
- 定期检查客户端连接状态
- 启用自动重连以提高稳定性

---

## 📞 技术支持

如遇到问题，请：
1. 查看操作日志中的错误信息
2. 检查浏览器控制台是否有JavaScript错误
3. 查看后端日志文件
4. 参考项目主README文档

---

## 🎉 开始使用吧！

现在你已经了解了后台管理系统的所有功能，快去体验吧！

**祝使用愉快！** 🚀

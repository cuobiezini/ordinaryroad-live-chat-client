# 直播客户端动态配置管理系统

## 📋 概述

本系统实现了直播平台配置的动态管理和持久化存储，支持在运行时通过REST API灵活调整各平台配置，无需重启应用。

### ✨ 核心特性

- ✅ **动态配置管理**：运行时更新roomId、cookie、autoReconnect等配置
- ✅ **数据持久化**：使用H2数据库自动保存配置，重启不丢失
- ✅ **统一API接口**：提供完整的RESTful API进行配置和客户端控制
- ✅ **多平台支持**：Bilibili、斗鱼、快手、抖音四大平台
- ✅ **批量操作**：支持批量更新多个平台配置
- ✅ **状态监控**：实时查询客户端连接状态

---

## 🏗️ 架构设计

### 核心组件

```
┌─────────────────────────────────────────┐
│   UnifiedLiveChatController (API层)     │
│   - 提供REST API接口                     │
│   - /api/live-chat/*                    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   UnifiedConfigService (业务层)          │
│   - 配置管理逻辑                         │
│   - 客户端控制                           │
│   - 配置同步                             │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   ConfigPersistenceService (持久化层)    │
│   - 数据库CRUD操作                       │
│   - 默认配置初始化                        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   PlatformConfigRepository (数据访问层)  │
│   - JPA Repository                      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   H2 Database (数据存储)                 │
│   - ./data/live-chat-config.mv.db       │
└─────────────────────────────────────────┘
```

### 数据模型

**PlatformConfig 实体字段：**
- `id`: 主键ID（自增）
- `platform`: 平台标识（bilibili/douyu/kuaishou/douyin）
- `roomId`: 直播间ID
- `cookie`: Cookie认证信息
- `autoReconnect`: 是否自动重连
- `roomInfoGetType`: 房间信息获取方式（快手专用）
- `enabled`: 是否启用
- `createdAt`: 创建时间
- `updatedAt`: 更新时间
- `remark`: 备注说明

---

## 🚀 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

应用启动时会自动：
- 初始化H2数据库
- 创建`platform_config`表
- 为4个平台创建默认配置

### 2. 访问H2控制台（可选）

浏览器访问：http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:file:./data/live-chat-config`
- 用户名: `sa`
- 密码: （留空）

### 3. 配置直播平台

#### 方式一：通过API更新配置

```bash
# 更新B站配置
curl -X PUT http://localhost:8080/api/live-chat/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "7777",
    "cookie": "your_bilibili_cookie_here",
    "autoReconnect": true
  }'

# 更新快手配置
curl -X PUT http://localhost:8080/api/live-chat/config/kuaishou \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "xxx040913",
    "cookie": "your_kuaishou_cookie",
    "roomInfoGetType": "COOKIE"
  }'
```

#### 方式二：直接修改数据库

通过H2控制台或SQL工具直接编辑`platform_config`表。

### 4. 连接客户端

```bash
# 连接B站客户端
curl -X POST http://localhost:8080/api/live-chat/connect/bilibili

# 连接所有客户端
curl -X POST http://localhost:8080/api/live-chat/initialize
```

### 5. 发送弹幕

```bash
curl -X POST http://localhost:8080/api/live-chat/send-danmu/bilibili \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello World!"
  }'
```

---

## 📖 API 接口文档

### 基础URL

```
http://localhost:8080/api/live-chat
```

### 配置管理接口

#### 1. 获取所有配置

```http
GET /configs
```

**响应示例：**
```json
[
  {
    "id": 1,
    "platform": "bilibili",
    "roomId": "7777",
    "cookie": "******",
    "autoReconnect": true,
    "roomInfoGetType": null,
    "enabled": true,
    "createdAt": "2026-04-04T10:00:00",
    "updatedAt": "2026-04-04T10:00:00",
    "remark": null
  }
]
```

#### 2. 获取指定平台配置

```http
GET /config/{platform}
```

**参数：**
- `platform`: bilibili | douyu | kuaishou | douyin

#### 3. 更新平台配置

```http
PUT /config/{platform}
Content-Type: application/json

{
  "roomId": "new_room_id",
  "cookie": "new_cookie",
  "autoReconnect": true,
  "roomInfoGetType": "COOKIE"
}
```

**说明：**
- 所有字段均为可选，只更新提供的字段
- `roomInfoGetType`仅对快手平台有效

#### 4. 批量更新配置

```http
PUT /configs/batch
Content-Type: application/json

[
  {
    "platform": "bilibili",
    "roomId": "7777",
    "cookie": "cookie1"
  },
  {
    "platform": "douyu",
    "roomId": "74751",
    "cookie": "cookie2"
  }
]
```

#### 5. 删除平台配置

```http
DELETE /config/{platform}
```

**注意：** 删除配置会自动断开对应客户端连接。

---

### 客户端控制接口

#### 6. 连接客户端

```http
POST /connect/{platform}
```

**功能：** 从数据库加载最新配置并启动客户端。

#### 7. 断开客户端

```http
POST /disconnect/{platform}
```

#### 8. 重新连接客户端

```http
POST /reconnect/{platform}
```

**功能：** 先断开再连接，适用于配置更新后重新生效。

#### 9. 发送弹幕

```http
POST /send-danmu/{platform}
Content-Type: application/json

{
  "message": "弹幕内容"
}
```

---

### 状态查询接口

#### 10. 查询单个客户端状态

```http
GET /status/{platform}
```

**响应示例：**
```json
{
  "platform": "bilibili",
  "connected": true,
  "status": "CONNECTED"
}
```

#### 11. 查询所有客户端状态

```http
GET /status
```

**响应示例：**
```json
{
  "bilibili": {
    "connected": true,
    "status": "CONNECTED"
  },
  "douyu": {
    "connected": false,
    "status": "DISCONNECTED"
  },
  "kuaishou": {
    "connected": false,
    "status": "DISCONNECTED"
  },
  "douyin": {
    "connected": false,
    "status": "DISCONNECTED"
  }
}
```

#### 12. 初始化所有客户端

```http
POST /initialize
```

**功能：** 从数据库加载所有配置并初始化客户端。

---

## 💡 使用场景示例

### 场景1：动态切换直播间

```bash
# 1. 更新B站直播间ID
curl -X PUT http://localhost:8080/api/live-chat/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{"roomId": "12345"}'

# 2. 重新连接以应用新配置
curl -X POST http://localhost:8080/api/live-chat/reconnect/bilibili
```

### 场景2：更新Cookie（登录状态过期）

```bash
# 1. 更新Cookie
curl -X PUT http://localhost:8080/api/live-chat/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{"cookie": "new_cookie_value"}'

# 2. 重新连接
curl -X POST http://localhost:8080/api/live-chat/reconnect/bilibili
```

### 场景3：批量管理多个平台

```bash
# 批量更新配置
curl -X PUT http://localhost:8080/api/live-chat/configs/batch \
  -H "Content-Type: application/json" \
  -d '[
    {"platform": "bilibili", "roomId": "7777"},
    {"platform": "douyu", "roomId": "74751"},
    {"platform": "kuaishou", "roomId": "xxx040913"}
  ]'

# 初始化所有客户端
curl -X POST http://localhost:8080/api/live-chat/initialize
```

### 场景4：监控客户端状态

```bash
# 定时检查所有客户端状态
while true; do
  curl -s http://localhost:8080/api/live-chat/status | jq
  sleep 30
done
```

---

## 🔧 高级配置

### 自定义H2数据库路径

修改`application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:/custom/path/live-chat-config
```

### 启用SQL日志（调试用）

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### 禁用H2控制台（生产环境）

```yaml
spring:
  h2:
    console:
      enabled: false
```

---

## 📊 数据持久化说明

### 存储位置

- **开发环境**: `./data/live-chat-config.mv.db`
- **生产环境**: 建议配置外部数据库（MySQL/PostgreSQL）

### 自动初始化

应用启动时：
1. 检查数据库是否存在配置
2. 如果不存在，为4个平台创建默认配置
3. 默认配置值：
   - `roomId`: 空字符串
   - `cookie`: 空字符串
   - `autoReconnect`: true
   - `enabled`: true
   - `roomInfoGetType`: NOT_COOKIE（仅快手）

### 配置同步机制

```
用户修改配置 → 保存到数据库 → 同步到运行中的客户端
     ↓                              ↓
  持久化成功                   实时更新生效
```

**注意：** 
- 如果客户端正在运行，配置更新会立即同步
- 如果客户端未启动，下次连接时会加载最新配置

---

## ⚠️ 注意事项

1. **Cookie安全性**
   - Cookie包含敏感信息，建议在生产环境加密存储
   - API响应中建议脱敏处理

2. **并发控制**
   - 同一平台的配置更新是串行的
   - 避免频繁更新导致客户端不稳定

3. **配置验证**
   - 更新配置前建议先验证格式
   - roomId不能为空
   - roomInfoGetType只能是`COOKIE`或`NOT_COOKIE`

4. **客户端状态**
   - 更新配置后需要重新连接才能完全生效
   - 某些配置（如roomId）可能需要重连

5. **数据库备份**
   - 定期备份`./data/live-chat-config.mv.db`文件
   - 或使用数据库导出功能

---

## 🐛 故障排查

### 问题1：配置更新后未生效

**解决方案：**
```bash
# 重新连接客户端
curl -X POST http://localhost:8080/api/live-chat/reconnect/{platform}
```

### 问题2：数据库文件损坏

**解决方案：**
```bash
# 删除数据库文件，重启应用会自动重建
rm -rf ./data/live-chat-config.*
```

### 问题3：客户端连接失败

**检查步骤：**
1. 确认配置正确：`GET /config/{platform}`
2. 检查网络连通性
3. 查看应用日志
4. 验证Cookie是否过期

### 问题4：H2控制台无法访问

**解决方案：**
1. 确认`spring.h2.console.enabled=true`
2. 检查端口是否正确（默认8080）
3. 访问路径：`http://localhost:8080/h2-console`

---

## 📝 迁移指南

### 从旧版本迁移

如果您之前使用的是静态配置（application.yaml），迁移步骤：

1. **保留原有配置**：application.yaml中的配置仍作为默认值
2. **启动应用**：系统会自动创建数据库并初始化
3. **通过API更新**：使用新的REST API管理配置
4. **验证功能**：测试配置更新和持久化

### 兼容旧API

原有的`/client/*`接口仍然可用，但建议使用新的`/api/live-chat/*`接口以获得完整功能。

---

## 🎯 最佳实践

1. **配置更新流程**
   ```
   更新配置 → 验证配置 → 重新连接 → 监控状态
   ```

2. **批量操作**
   - 使用`/configs/batch`接口一次性更新多个平台
   - 然后调用`/initialize`初始化所有客户端

3. **监控告警**
   - 定期检查`/status`接口
   - 发现断线自动重连

4. **日志记录**
   - 关键操作记录日志
   - 便于问题追踪

---

## 📞 技术支持

如有问题，请查看：
- 应用日志：`logs/spring.log`
- H2数据库：通过控制台直接查询
- API文档：启动后访问Swagger UI（如果集成）

---

## 📄 许可证

MIT License

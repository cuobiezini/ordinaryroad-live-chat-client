# 配置改造方案说明

## 📌 项目改造概述

本文档介绍如何将直播配置从 `application.yaml` 配置文件改为用户输入的完整方案。

改造后，敏感信息（roomId 和 Cookie）不再硬编码在配置文件中，而是通过以下方式提供：
- ✅ **方式1：启动后交互式输入**（推荐，已实现）
- ✅ **方式2：HTTP API 动态设置**（已实现）
- 🔄 **方式3：环境变量输入**（可选）

---

## 🎯 方案对比

| 方案 | 安全性 | 易用性 | 实时性 | 状态 |
|------|--------|--------|-----------|--------|
| 交互式输入 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ 已实现 |
| HTTP API | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ 已实现 |
| 环境变量 | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | 可选 |

---

## 📋 改造文件清单

### 创建的新文件

#### 1. `ConfigurationInitializer.java`
**路径**：`src/main/java/tech/ordinaryroad/live/chat/client/example/client/config/ConfigurationInitializer.java`

**功能**：
- 监听 Spring Boot 应用启动完成事件
- 启动后自动显示配置向导
- 交互式输入各平台的 roomId 和 Cookie
- 支持跳过某些平台（直接按 Enter）

**工作流程**：
```
应用启动
    ↓
ApplicationReadyEvent 触发
    ↓
显示配置向导界面
    ↓
依次提示输入：
  - B站 (Bilibili) 配置
  - 斗鱼 (Douyu) 配置
  - 快手 (Kuaishou) 配置
  - 抖音 (Douyin) 配置
    ↓
配置完成，应用正常运行
```

#### 2. `ConfigurationController.java`
**路径**：`src/main/java/tech/ordinaryroad/live/chat/client/example/client/controller/ConfigurationController.java`

**功能**：提供 REST API 接口用于获取和设置配置

**API 列表**：

| 方法 | 端点 | 功能 | 示例 |
|------|------|------|------|
| GET | `/config/all` | 获取所有平台配置 | - |
| GET | `/config/{platform}` | 获取指定平台配置 | `/config/bilibili` |
| POST | `/config/{platform}/roomId/{roomId}` | 设置平台的 roomId | `/config/douyu/roomId/74751` |
| POST | `/config/{platform}/cookie` | 设置平台的 Cookie | 需传 query param: `cookie=xxx` |
| POST | `/config/{platform}` | 批量设置平台配置 | 请求体: `{"roomId": "xxx", "cookie": "xxx"}` |

**API 示例**：

```bash
# 1. 获取所有配置
curl -X GET http://localhost:8080/config/all

# 2. 获取 B站配置
curl -X GET http://localhost:8080/config/bilibili

# 3. 设置 B站 roomId
curl -X POST http://localhost:8080/config/bilibili/roomId/12345

# 4. 设置 B站 Cookie
curl -X POST http://localhost:8080/config/bilibili/cookie?cookie=your_cookie_here

# 5. 批量设置配置
curl -X POST http://localhost:8080/config/douyu \
  -H "Content-Type: application/json" \
  -d '{"roomId": "74751", "cookie": "your_cookie_here"}'
```

### 修改的文件

#### 1. `application.yaml`
**路径**：`src/main/resources/application.yaml`

**修改内容**：
- ✂️ 移除所有敏感信息（roomId 和 Cookie）
- 📝 添加注释说明配置方式
- 使用空的配置对象 `{}`

**对比**：
```yaml
# 修改前
config:
  bilibili:
    roomId: 7777
    cookie: ${bilibiliCookie:}

# 修改后
config:
  # 配置由用户输入提供
  bilibili: {}
  douyu: {}
  kuaishou: {}
  douyin: {}
```

---

## 🚀 使用指南

### 方式1：启动后交互式输入（推荐）

**步骤**：

1. **编译项目**
```bash
# Windows
mvn clean package

# 或使用 IDE 直接运行
```

2. **运行应用**
```bash
java -jar ordinaryroad-bilibili-live-chat-example-client.jar
```

3. **按照提示输入**
```
========================================
直播间配置初始化向导
========================================
请按照以下步骤输入各平台的配置信息
如果不需要某个平台，直接按 Enter 跳过

【B站 (Bilibili)】
请输入 B站直播间ID:7777
请输入 B站Cookie:your_cookie_here

【斗鱼 (Douyu)】
请输入 斗鱼直播间ID:74751
请输入 斗鱼Cookie:your_cookie_here

... (依次输入其他平台)

========================================
配置初始化完成！
========================================
```

4. **应用正常运行**
- 配置已保存在内存中
- 可通过 HTTP API 随时修改

---

### 方式2：通过 HTTP API 设置

**步骤**：

1. **启动应用**（按方式1 启动）

2. **通过 API 设置配置**

```bash
# 获取所有配置
curl -X GET http://localhost:8080/config/all -H "Content-Type: application/json"

# 响应示例:
# {
#   "bilibili": {
#     "roomId": 7777,
#     "cookie": "****...rest"
#   },
#   ...
# }
```

```bash
# 设置 B站配置
curl -X POST http://localhost:8080/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "12345",
    "cookie": "your_new_cookie_here"
  }'

# 响应示例:
# {
#   "success": true,
#   "message": "B站配置设置成功"
# }
```

3. **连接直播间**
```bash
# 使用已设置的配置连接
curl -X GET "http://localhost:8080/client/connect?platform=bilibili"
```

---

### 方式3：使用环境变量（可选增强）

如需支持环境变量，可在 `ConfigurationInitializer` 中添加：

```java
// 在 initializeBilibiliConfig() 中添加
String roomIdFromEnv = System.getenv("BILIBILI_ROOM_ID");
String cookieFromEnv = System.getenv("BILIBILI_COOKIE");

if (roomIdFromEnv != null && !roomIdFromEnv.isEmpty()) {
    configurations.getBilibili().setRoomId(Long.parseLong(roomIdFromEnv));
}
```

---

## 🔒 安全性优势

### 改造前的风险
- ❌ 敏感信息硬编码在源代码中
- ❌ 版本控制系统中会保存 Cookie
- ❌ 打包后的 JAR 可逆向查看敏感信息
- ❌ 无法快速更换身份

### 改造后的优势
- ✅ 敏感信息不出现在源代码中
- ✅ 支持运行时动态输入
- ✅ 可采用加密存储（进阶）
- ✅ 支持多个身份快速切换
- ✅ 便于容器化部署（通过环境变量）

---

## 📊 配置流程图

```
┌─────────────────────────────────┐
│  应用启动                        │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  ConfigurationInitializer       │
│  监听 ApplicationReadyEvent      │
└────────────┬────────────────────┘
             │
             ▼
    ┌────────────────────┐
    │  交互式输入向导    │
    │                    │
    │ ┌────────────────┐ │
    │ │ B站配置输入    │ │
    │ ├────────────────┤ │
    │ │ 斗鱼配置输入   │ │
    │ ├────────────────┤ │
    │ │ 快手配置输入   │ │
    │ ├────────────────┤ │
    │ │ 抖音配置输入   │ │
    │ └────────────────┘ │
    └────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  配置保存到内存                 │
│  (LiveChatClientConfigurations) │
└────────────┬────────────────────┘
             │
             ├──────────────┬─────────────────┐
             │              │                 │
             ▼              ▼                 ▼
      ┌────────────┐  ┌────────────────┐ ┌────────┐
      │应用正常运行│  │HTTP API 修改   │ │Web 界面│
      └────────────┘  └────────────────┘ └────────┘
```

---

## 🔧 技术实现细节

### 1. Spring Boot 事件监听

```java
@EventListener(ApplicationReadyEvent.class)
public void initializeConfigurations() {
    // 在应用完全启动后执行
}
```

### 2. 配置属性注入

```java
@Component
public class ConfigurationInitializer {
    
    private final LiveChatClientConfigurations configurations;
    
    public ConfigurationInitializer(LiveChatClientConfigurations configurations) {
        this.configurations = configurations;
    }
    // configurations 由 Spring 自动注入
}
```

### 3. 控制器 REST API

```java
@RestController
@RequestMapping("config")
public class ConfigurationController {
    
    @PostMapping("{platform}")
    public Map<String, Object> setConfig(
        @PathVariable String platform, 
        @RequestBody Map<String, String> config) {
        // 动态设置配置
    }
}
```

---

## ⚠️ 注意事项

### 1. 应用启动流程
- 应用启动后，**必须完整输入配置**才能正常运行
- 需要用户主动输入，不会自动跳过
- 如需非交互式，使用环境变量或 API 方式

### 2. Cookie 有效期
- Cookie 有有效期限制，需要定期更新
- 可通过 API 动态更新，无需重启应用

### 3. 隐敏感信息展示
- API 返回值中 Cookie 会被部分隐藏（前4位+后4位）
- 完整 Cookie 仅在配置时需要提供

### 4. 多平台配置
- 可以只配置需要的平台
- 不需要的平台直接按 Enter 跳过

---

## 🧪 测试建议

### 1. 本地测试
```bash
# 启动应用并按提示输入
mvn spring-boot:run

# 请输入 B站直播间ID: 1000
# 请输入 B站Cookie: test_cookie

# 输入完后查看日志
# [INFO] 配置初始化完成！
```

### 2. API 功能测试
```bash
# 1. 获取配置
curl http://localhost:8080/config/bilibili

# 2. 修改 roomId
curl -X POST http://localhost:8080/config/bilibili/roomId/2000

# 3. 修改 Cookie
curl -X POST http://localhost:8080/config/bilibili/cookie?cookie=new_cookie

# 4. 批量修改
curl -X POST http://localhost:8080/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{"roomId": "3000", "cookie": "another_cookie"}'
```

### 3. 集成测试
```bash
# 完整流程测试
1. 启动应用
2. 通过 API 设置配置
3. 调用客户端连接 API: /client/connect?platform=bilibili
4. 验证连接是否成功
```

---

## 📞 常见问题

### Q1: 如何在 Docker 容器中使用?
**A**: 使用 API 方式或环境变量方式，在容器启动时自动配置：
```dockerfile
# Dockerfile
FROM openjdk:17-slim
COPY app.jar .
CMD ["java", "-jar", "app.jar"]

# docker-compose.yml
environment:
  - BILIBILI_ROOM_ID=1000
  - BILIBILI_COOKIE=xxx
```

### Q2: 如何隐藏配置输入界面?
**A**: 通过环境变量或配置文件预设置，参考"可选增强"部分。

### Q3: 输入错误如何重新配置?
**A**: 通过 HTTP API 修改任何平台的配置，无需重启应用。

### Q4: 支持热更新吗?
**A**: 支持！通过 API 修改的配置立即生效，已连接的客户端需要重新连接。

---

## 🎓 最佳实践

### 1. 安全性
- ✅ 使用 HTTPS + 认证保护配置 API
- ✅ 定期更新 Cookie
- ✅ 避免在日志中打印完整敏感信息

### 2. 可维护性
- ✅ 记录配置变更历史
- ✅ 提供配置备份/恢复功能
- ✅ 编写配置验证逻辑

### 3. 用户体验
- ✅ 优化配置提示文案
- ✅ 支持配置预设模板
- ✅ 提供 Web 管理界面

---

## 📚 相关文件

- 执行文件：`LiveChatExampleApplication.java`
- 配置类：`LiveChatClientConfigurations.java`
- 初始化器：`ConfigurationInitializer.java`（新建）
- API 控制器：`ConfigurationController.java`（新建）
- 配置文件：`application.yaml`（已修改）
- 原配置文件：`LiveChatClientController.java`（已存在的 API）

---

## 🎉 总结

本改造方案提供了三种灵活的配置方式，完全移除了敏感信息的硬编码，大幅提升了项目的安全性和可维护性。用户可根据实际需求选择合适的配置方式。



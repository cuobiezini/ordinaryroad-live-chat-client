# 配置改造 - 快速开始

## 📦 新增文件

这次改造新增了以下文件：

1. **ConfigurationInitializer.java**
   - 启动后交互式配置输入
   - 自动监听应用启动完成事件

2. **ConfigurationController.java**
   - REST API 接口
   - 动态配置管理

3. **CONFIGURATION_REFACTORING.md**
   - 详细的改造文档
   - 最佳实践指南

## 🚀 立即开始

### 1️⃣ 编译项目

```bash
cd live-chat-client-examples/client-example
mvn clean package
```

### 2️⃣ 运行应用

```bash
# 方式 A: 直接运行 JAR
java -jar target/ordinaryroad-bilibili-live-chat-example-client.jar

# 方式 B: 使用 Maven 插件
mvn spring-boot:run
```

### 3️⃣ 按提示输入配置

应用启动后会显示：

```
========================================
直播间配置初始化向导
========================================
请按照以下步骤输入各平台的配置信息
如果不需要某个平台，直接按 Enter 跳过

【B站 (Bilibili)】
请输入 B站直播间ID:
请输入 B站Cookie:

【斗鱼 (Douyu)】
请输入 斗鱼直播间ID:
请输入 斗鱼Cookie:

【快手 (Kuaishou)】
请输入 快手直播间ID:
请输入 快手Cookie:

【抖音 (Douyin)】
请输入 抖音直播间ID:
请输入 抖音Cookie:

========================================
配置初始化完成！
========================================
```

按照提示逐行输入各平台的信息。

### 4️⃣ 通过 API 管理配置

应用启动后，可以通过 REST API 查看和修改配置：

**获取所有配置**
```bash
curl http://localhost:8080/config/all
```

**获取特定平台配置**
```bash
curl http://localhost:8080/config/bilibili
```

**修改 roomId**
```bash
curl -X POST http://localhost:8080/config/bilibili/roomId/12345
```

**修改 Cookie**
```bash
curl -X POST "http://localhost:8080/config/bilibili/cookie?cookie=your_cookie_here"
```

**批量修改配置**
```bash
curl -X POST http://localhost:8080/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "12345",
    "cookie": "your_cookie_here"
  }'
```

## ✅ 验证

访问应用首页确认启动成功：

```bash
curl http://localhost:8080/config/all
```

应该返回：
```json
{
  "bilibili": {
    "roomId": 你输入的ID,
    "cookie": "****...rest"
  },
  // ... 其他平台
}
```

## 📝 改造要点

| 项目 | 说明 |
|------|------|
| **配置方式** | 从 YAML 文件改为用户输入 + HTTP API |
| **安全性** | ✅ 敏感信息不再硬编码 |
| **灵活性** | ✅ 运行时修改配置，无需重启 |
| **兼容性** | ✅ 保留原有 API（如 /client/connect） |

## 🔗 相关文档

- 📖 [详细改造说明](CONFIGURATION_REFACTORING.md)
- 📋 [原项目 README](README.md)

## 💡 提示

- 首次运行时需要输入配置
- 输入为空时表示跳过该平台
- 可通过 API 随时修改配置
- Cookie 过期时需要更新

---

**需要帮助？** 查看 [CONFIGURATION_REFACTORING.md](CONFIGURATION_REFACTORING.md) 了解更多细节。


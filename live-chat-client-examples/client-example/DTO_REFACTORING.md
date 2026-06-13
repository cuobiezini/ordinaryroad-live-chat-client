# DTO对象改造说明

## ✅ 改造完成

已将`UnifiedLiveChatController`中的Map参数改为强类型DTO对象，提升代码质量和API规范性。

---

## 📦 新增DTO对象

### 1. PlatformConfigUpdateRequest

**文件位置：** `src/main/java/tech/ordinaryroad/live/chat/client/example/client/model/PlatformConfigUpdateRequest.java`

**用途：** 接收平台配置更新请求参数

**字段说明：**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| roomId | String | 直播间ID | "7777" |
| cookie | String | Cookie认证信息 | "your_cookie" |
| autoReconnect | Boolean | 是否自动重连 | true |
| roomInfoGetType | String | 房间信息获取方式（快手专用） | "NOT_COOKIE" |
| enabled | Boolean | 是否启用 | true |
| remark | String | 备注说明 | "B站主直播间" |

**使用示例：**

```json
{
  "roomId": "7777",
  "cookie": "your_bilibili_cookie",
  "autoReconnect": true,
  "remark": "B站主直播间"
}
```

---

### 2. SendDanmuRequest

**文件位置：** `src/main/java/tech/ordinaryroad/live/chat/client/example/client/model/SendDanmuRequest.java`

**用途：** 接收发送弹幕请求参数

**字段说明：**

| 字段 | 类型 | 说明 | 必填 | 示例 |
|------|------|------|------|------|
| message | String | 弹幕内容 | ✅ | "Hello World!" |

**使用示例：**

```json
{
  "message": "Hello World!"
}
```

---

## 🔄 改动对比

### 改造前（使用Map）

```java
@PutMapping("/config/{platform}")
public PlatformConfig updateConfig(
        @PathVariable String platform,
        @RequestBody Map<String, Object> params) {
    
    String roomId = (String) params.get("roomId");
    String cookie = (String) params.get("cookie");
    Boolean autoReconnect = params.get("autoReconnect") != null ? 
            (Boolean) params.get("autoReconnect") : null;
    String roomInfoGetType = (String) params.get("roomInfoGetType");
    
    return unifiedConfigService.updateConfig(platform, roomId, cookie, autoReconnect, roomInfoGetType);
}
```

**缺点：**
- ❌ 类型不安全，需要手动类型转换
- ❌ 无法利用IDE自动补全
- ❌ 缺少字段验证
- ❌ API文档不清晰
- ❌ 容易拼写错误

---

### 改造后（使用DTO）

```java
@PutMapping("/config/{platform}")
public PlatformConfig updateConfig(
        @PathVariable String platform,
        @RequestBody PlatformConfigUpdateRequest request) {
    
    return unifiedConfigService.updateConfig(
            platform, 
            request.getRoomId(), 
            request.getCookie(), 
            request.getAutoReconnect(), 
            request.getRoomInfoGetType()
    );
}
```

**优点：**
- ✅ 类型安全，编译时检查
- ✅ IDE智能提示和自动补全
- ✅ 清晰的字段定义和注释
- ✅ 易于维护和扩展
- ✅ 支持后续添加验证注解

---

## 📝 API调用示例

### 更新配置接口

#### 请求方式
```
PUT /api/live-chat/config/{platform}
Content-Type: application/json
```

#### 请求体（JSON）
```json
{
  "roomId": "7777",
  "cookie": "your_cookie_here",
  "autoReconnect": true,
  "roomInfoGetType": "NOT_COOKIE",
  "enabled": true,
  "remark": "B站主直播间"
}
```

#### cURL示例
```bash
curl -X PUT http://localhost:8080/api/live-chat/config/bilibili \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "7777",
    "cookie": "your_bilibili_cookie",
    "autoReconnect": true,
    "remark": "B站主直播间"
  }'
```

#### PowerShell示例
```powershell
$body = @{
    roomId = "7777"
    cookie = "your_bilibili_cookie"
    autoReconnect = $true
    remark = "B站主直播间"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/live-chat/config/bilibili" `
  -Method Put `
  -Body $body `
  -ContentType "application/json"
```

---

### 发送弹幕接口

#### 请求方式
```
POST /api/live-chat/send-danmu/{platform}
Content-Type: application/json
```

#### 请求体（JSON）
```json
{
  "message": "Hello World!"
}
```

#### cURL示例
```bash
curl -X POST http://localhost:8080/api/live-chat/send-danmu/bilibili \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello World!"
  }'
```

#### PowerShell示例
```powershell
$body = @{
    message = "Hello World!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/live-chat/send-danmu/bilibili" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"
```

---

## 🎯 核心优势

### 1. 类型安全
```java
// 编译时就能发现类型错误
request.getAutoReconnect() // 返回 Boolean，无需类型转换
```

### 2. IDE支持
- ✅ 自动补全字段名
- ✅ 显示字段类型
- ✅ 查看字段注释
- ✅ 重构更安全

### 3. 代码可读性
```java
// 清晰的字段访问
request.getRoomId()
request.getCookie()
request.getAutoReconnect()

// 相比 Map 方式更直观
params.get("roomId")  // 不知道返回什么类型
```

### 4. 易于扩展
```java
// 添加新字段只需在DTO中添加
@Data
public class PlatformConfigUpdateRequest {
    private String roomId;
    private String cookie;
    private String newField;  // 新增字段
}
```

### 5. 支持验证（未来可扩展）
```java
// 可以添加 validation 依赖后启用
@NotBlank(message = "直播间ID不能为空")
private String roomId;

@NotNull(message = "自动重连不能为空")
private Boolean autoReconnect;
```

---

## 📂 项目结构

```
src/main/java/tech/ordinaryroad/live/chat/client/example/client/
├── model/                                    # ⭐ 新增：DTO模型层
│   ├── PlatformConfigUpdateRequest.java     # 配置更新请求DTO
│   └── SendDanmuRequest.java                # 发送弹幕请求DTO
├── controller/
│   └── UnifiedLiveChatController.java       # 已更新：使用DTO
├── service/
├── repository/
└── entity/
```

---

## 🔍 最佳实践

### 1. 字段可选性
所有字段都是可选的，只更新提供的字段：

```json
// 只更新roomId
{
  "roomId": "12345"
}

// 只更新cookie
{
  "cookie": "new_cookie"
}

// 更新多个字段
{
  "roomId": "7777",
  "autoReconnect": false
}
```

### 2. 空值处理
未提供的字段保持原值不变：

```java
// Service层处理
if (request.getRoomId() != null) {
    config.setRoomId(request.getRoomId());
}
// 如果roomId为null，则不更新
```

### 3. 字段验证
当前在Controller层进行基本验证：

```java
if (request.getMessage() == null || request.getMessage().isEmpty()) {
    throw new IllegalArgumentException("弹幕内容不能为空");
}
```

未来可集成Bean Validation：

```java
@NotBlank(message = "弹幕内容不能为空")
private String message;
```

---

## ⚠️ 注意事项

### 1. JSON字段命名
使用驼峰命名（camelCase），与Java字段一致：

```json
{
  "roomId": "7777",              // ✅ 正确
  "room_id": "7777",             // ❌ 错误
  "autoReconnect": true,         // ✅ 正确
  "auto_reconnect": true         // ❌ 错误
}
```

### 2. 布尔值类型
使用JSON布尔值，不是字符串：

```json
{
  "autoReconnect": true,         // ✅ 正确
  "autoReconnect": "true"        // ❌ 错误
}
```

### 3. 空值 vs 缺失
```json
// 字段缺失：不更新该字段
{
  "roomId": "7777"
  // cookie字段缺失，保持不变
}

// 字段为null：根据业务逻辑处理
{
  "roomId": "7777",
  "cookie": null
}
```

---

## 🚀 迁移指南

如果您之前使用Map方式调用API，现在需要改为DTO格式：

### 改造前
```javascript
// JavaScript示例
fetch('/api/live-chat/config/bilibili', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    roomId: '7777',
    cookie: 'xxx'
  })
})
```

### 改造后
```javascript
// 完全相同！JSON格式不变
fetch('/api/live-chat/config/bilibili', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    roomId: '7777',
    cookie: 'xxx'
  })
})
```

**重要：** API的JSON格式完全没有变化，只是后端实现从Map改为DTO，前端调用代码无需修改！

---

## 📊 性能影响

- **序列化性能**: 无影响（Jackson同样处理）
- **内存占用**: 略微增加（对象vs Map）
- **运行效率**: 基本相同
- **开发效率**: 显著提升 ⬆️⬆️⬆️

---

## ✅ 总结

### 改造成果
- ✅ 创建2个DTO类
- ✅ 修改Controller使用DTO
- ✅ 移除Map参数
- ✅ 提升代码质量
- ✅ 增强类型安全

### API兼容性
- ✅ JSON格式完全兼容
- ✅ 前端代码无需修改
- ✅ 向后兼容

### 代码质量
- ✅ 类型安全
- ✅ IDE友好
- ✅ 易于维护
- ✅ 可扩展性强

**这是一次纯粹的后端优化，不影响API使用者！** 🎉

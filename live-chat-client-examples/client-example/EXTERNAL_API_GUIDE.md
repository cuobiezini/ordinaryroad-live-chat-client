# 外部 API 调用指南

## 📋 概述

本文档说明如何安全地调用外部 API 接口获取抖音号数据。

---

## 🔐 安全机制

### 1. API Key 认证
- 所有外部 API 调用必须在 URL 参数中携带有效的 `apiKey`
- API Key 由管理员在后台配置和管理
- 支持启用/禁用特定的 API Key

### 2. IP 白名单
- 可以为每个 API Key 配置允许访问的 IP 地址列表
- 如果未配置白名单，则不限制 IP（生产环境建议配置）
- 多个 IP 用逗号分隔，例如：`192.168.1.100,10.0.0.50`

### 3. 限流保护
- **QPS 限流**：每秒最大请求数（默认 10 次/秒）
- **每日配额**：每天最大请求数（默认 10000 次/天）
- 超过限制将返回错误，无法继续调用

### 4. 审计日志
- 所有 API 调用都会被记录（包括成功和失败）
- 记录内容包括：API Key、IP 地址、调用时间、请求参数等
- 便于问题排查和安全审计

---

## 🚀 快速开始

### 步骤 1：获取 API Key

联系系统管理员获取 API Key，需要提供以下信息：
- 应用名称
- 服务器 IP 地址（用于白名单）
- 预计调用频率

### 步骤 2：执行数据库迁移

```bash
# 执行完整的数据库初始化脚本（包含 api_key_config 表）
mysql -u root -p < src/main/resources/db/schema.sql
```

**注意**：`schema.sql` 是统一的数据库初始化脚本，包含所有表结构和初始数据。

### 步骤 3：配置 Redis（可选但推荐）

为了启用限流功能，需要在 `application.yaml` 中配置 Redis：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your-password
    database: 0
```

> ⚠️ **注意**：如果不配置 Redis，限流功能将不会生效，但不会影响正常调用。

---

## 📡 API 接口说明

### 1. 获取抖音号（取数接口）

**接口地址**：`POST /api/external/fetch-display-id`

**请求参数**：
- URL 参数：`apiKey` - 你的 API Key
- 请求体：JSON 格式

**请求示例**：
```
POST /api/external/fetch-display-id?apiKey=test-api-key-123456
Content-Type: application/json

{
  "platform": "douyin"
}
```

**参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| platform | String | 否 | 平台标识，默认为 "douyin" |

**响应示例（成功）**：
```json
{
  "success": true,
  "code": 200,
  "message": "成功获取抖音号",
  "data": {
    "displayId": "dy_user_123456",
    "uid": "7123456789012345678",
    "username": "用户昵称",
    "platform": "douyin",
    "roomId": "123456789"
  }
}
```

**响应示例（无数据）**：
```json
{
  "success": false,
  "code": 404,
  "message": "没有可用的未使用记录"
}
```

**响应示例（认证失败）**：
```json
{
  "success": false,
  "code": 401,
  "message": "认证失败：API Key 无效或已被禁用"
}
```

**响应示例（限流）**：
```json
{
  "success": false,
  "code": 429,
  "message": "API 调用频率超限，请稍后重试"
}
```

---

### 2. 健康检查（无需认证）

**接口地址**：`GET /api/external/health`

**响应示例**：
```json
{
  "success": true,
  "code": 200,
  "message": "服务正常",
  "timestamp": 1718234567890
}
```

---

## 💻 代码示例

### cURL 示例

```bash
curl -X POST "http://localhost:8080/api/external/fetch-display-id?apiKey=test-api-key-123456" \
  -H "Content-Type: application/json" \
  -d '{"platform": "douyin"}'
```

### Python 示例

```python
import requests
import json

# API Key 作为 URL 参数
url = "http://localhost:8080/api/external/fetch-display-id"
params = {
    "apiKey": "test-api-key-123456"
}
data = {
    "platform": "douyin"
}

response = requests.post(url, params=params, json=data)
result = response.json()

if result["success"]:
    display_id = result["data"]["displayId"]
    uid = result["data"]["uid"]
    print(f"获取成功: displayId={display_id}, uid={uid}")
else:
    print(f"获取失败: {result['message']}")
```

### Java 示例

```java
import java.net.http.*;
import java.net.URI;

public class ExternalApiExample {
    public static void main(String[] args) throws Exception {
        String apiKey = "test-api-key-123456";
        String url = "http://localhost:8080/api/external/fetch-display-id?apiKey=" + apiKey;
        
        String jsonBody = "{\"platform\":\"douyin\"}";
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        System.out.println("响应: " + response.body());
    }
}
```

### JavaScript (Node.js) 示例

```javascript
const axios = require('axios');

async function fetchDisplayId() {
    try {
        const apiKey = 'test-api-key-123456';
        const response = await axios.post(
            `http://localhost:8080/api/external/fetch-display-id?apiKey=${apiKey}`,
            { platform: 'douyin' },
            {
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );
        
        if (response.data.success) {
            const data = response.data.data;
            console.log(`获取成功: displayId=${data.displayId}, uid=${data.uid}`);
        } else {
            console.log(`获取失败: ${response.data.message}`);
        }
    } catch (error) {
        console.error('请求失败:', error.response?.data || error.message);
    }
}

fetchDisplayId();
```

---

## 🔧 管理 API Key

### 创建新的 API Key

```sql
INSERT INTO api_key_config 
(api_key, api_secret, app_name, ip_whitelist, enabled, max_qps, max_daily_requests, remark)
VALUES 
('your-new-api-key', 
 'your-secret', 
 '您的应用名称', 
 '192.168.1.100,10.0.0.50',  -- IP 白名单，留空表示不限制
 1,                           -- 启用
 10,                          -- QPS 限制
 5000,                        -- 每日配额
 '备注说明');
```

### 禁用 API Key

```sql
UPDATE api_key_config 
SET enabled = 0 
WHERE api_key = 'test-api-key-123456';
```

### 查看使用情况

```sql
SELECT 
    api_key,
    app_name,
    enabled,
    max_qps,
    max_daily_requests,
    last_used_at,
    created_at
FROM api_key_config
ORDER BY last_used_at DESC;
```

### 调整限流配置

```sql
-- 修改 QPS 限制为 20
UPDATE api_key_config 
SET max_qps = 20 
WHERE api_key = 'test-api-key-123456';

-- 修改每日配额为 50000
UPDATE api_key_config 
SET max_daily_requests = 50000 
WHERE api_key = 'test-api-key-123456';
```

---

## ⚠️ 注意事项

### 1. 安全性
- ✅ **永远不要**在前端代码中暴露 API Key
- ✅ **定期更换** API Key（建议每 3-6 个月）
- ✅ **配置 IP 白名单**，限制可访问的服务器
- ❌ **不要**将 API Key 提交到 Git 仓库
- ❌ **不要**在日志中打印完整的 API Key

### 2. 性能优化
- 合理设置 QPS 和每日配额，避免过度消耗资源
- 建议在客户端实现缓存机制，减少不必要的调用
- 监控 API 调用情况，及时发现异常

### 3. 错误处理
- 始终检查响应的 `success` 字段
- 对于限流错误（429），实现重试机制（建议使用指数退避）
- 记录所有 API 调用的日志，便于问题排查

### 4. 最佳实践
```python
import time
import random

def fetch_with_retry(max_retries=3):
    """带重试机制的 API 调用"""
    for attempt in range(max_retries):
        try:
            response = requests.post(url, headers=headers, json=data)
            result = response.json()
            
            if result["success"]:
                return result["data"]
            elif result["code"] == 429:
                # 限流错误，等待后重试（指数退避）
                wait_time = (2 ** attempt) + random.uniform(0, 1)
                print(f"触发限流，{wait_time:.2f}秒后重试...")
                time.sleep(wait_time)
            else:
                raise Exception(result["message"])
                
        except Exception as e:
            if attempt == max_retries - 1:
                raise
            print(f"第{attempt+1}次尝试失败: {e}")
            time.sleep(1)
    
    return None
```

---

## 📊 监控与告警

### 推荐的监控指标
1. **API 调用成功率** - 应该 > 95%
2. **平均响应时间** - 应该 < 500ms
3. **限流触发次数** - 频繁触发需要调整配额
4. **认证失败次数** - 可能存在非法调用

### 日志查询示例

```sql
-- 查看最近的 API 调用日志（需要在应用中实现日志记录）
SELECT * FROM api_call_logs 
ORDER BY created_at DESC 
LIMIT 100;

-- 统计每个 API Key 的调用次数
SELECT 
    api_key,
    COUNT(*) as call_count,
    SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) as fail_count
FROM api_call_logs
WHERE DATE(created_at) = CURDATE()
GROUP BY api_key;
```

---

## 🆘 常见问题

### Q1: 收到 401 错误怎么办？
**A**: 检查以下几点：
- API Key 是否正确（注意大小写）
- API Key 是否已启用
- IP 地址是否在白名单中

### Q2: 收到 429 错误怎么办？
**A**: 你的调用频率超过了限制：
- 降低调用频率
- 联系管理员提升配额
- 实现本地缓存减少调用

### Q3: 如何测试 API 是否可用？
**A**: 先调用健康检查接口：
```bash
curl http://localhost:8080/api/external/health
```

### Q4: Redis 未配置会影响使用吗？
**A**: 不会，但限流功能不会生效。建议配置 Redis 以启用完整的限流保护。

---

## 📞 技术支持

如有问题，请联系：
- 邮箱：support@ordinaryroad.tech
- QQ频道：[QQ频道.jpg](assets/QQ频道.jpg)
- 微信频道：[微信频道.jpg](assets/微信频道.jpg)

---

**最后更新时间**：2026-06-13

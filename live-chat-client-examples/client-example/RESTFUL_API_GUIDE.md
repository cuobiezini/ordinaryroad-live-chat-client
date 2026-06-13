# RESTful API 设计规范

## 📋 概述

本项目遵循标准的RESTful API设计规范，根据HTTP方法的语义来设计接口。

---

## 🎯 HTTP方法使用规范

### GET - 查询操作
**适用场景：**
- 查询数据（不修改服务器状态）
- 参数简单且数量较少（通常≤5个）
- 幂等性操作（多次调用结果相同）

**示例：**
```java
// ✅ 正确：简单查询
@GetMapping("/query")
public Map<String, Object> queryEntries(
    @RequestParam(required = false) String roomId,
    @RequestParam(required = false) String platform,
    @RequestParam(required = false) String username,
    @RequestParam(defaultValue = "1") int pageNum,
    @RequestParam(defaultValue = "20") int pageSize
)

// ✅ 正确：获取单条记录
@GetMapping("/{id}")
public Map<String, Object> getEntryDetail(@PathVariable Long id)

// ✅ 正确：获取统计数据
@GetMapping("/statistics")
public Map<String, Object> getStatistics(
    @RequestParam(required = false) String roomId,
    @RequestParam(required = false) String platform
)

// ✅ 正确：导出文件
@GetMapping("/export/csv")
public ResponseEntity<byte[]> exportCsv(
    @RequestParam(required = false) String roomId,
    @RequestParam(required = false) String platform,
    @RequestParam(required = false) String username
)
```

**特点：**
- 参数通过URL传递（Query Parameters）
- 可被浏览器缓存
- 可在地址栏直接访问
- 有长度限制（约2048字符）

---

### POST - 创建/复杂操作
**适用场景：**
- 创建新资源
- 参数过多或结构复杂（需要RequestBody）
- 非幂等性操作
- 批量操作涉及大量数据

**示例：**
```java
// ✅ 正确：创建新资源
@PostMapping("/create")
public Map<String, Object> createEntry(@RequestBody LiveEntryHistory entry)

// ✅ 正确：复杂查询（参数很多时）
@PostMapping("/advanced-query")
public Map<String, Object> advancedQuery(@RequestBody QueryRequest request)

// ❌ 错误：删除操作不应该用POST
@PostMapping("/delete/{id}")  // 应该用 @DeleteMapping
```

**特点：**
- 参数在请求体中（JSON格式）
- 无长度限制
- 不可被缓存
- 支持复杂数据结构

---

### DELETE - 删除操作
**适用场景：**
- 删除单个资源
- 批量删除资源
- 清理过期数据

**示例：**
```java
// ✅ 正确：删除单条记录
@DeleteMapping("/{id}")
public Map<String, Object> deleteEntry(@PathVariable Long id)

// ✅ 正确：批量删除（虽然参数在body，但语义是删除）
@DeleteMapping("/batch-delete")
public Map<String, Object> batchDeleteEntries(@RequestBody List<Long> ids)

// ✅ 正确：清理旧数据
@DeleteMapping("/clean-old-data")
public Map<String, Object> cleanOldData(@RequestParam int days)

// ✅ 正确：删除指定直播间的所有记录
@DeleteMapping("/room/{roomId}")
public Map<String, Object> deleteByRoomId(@PathVariable String roomId)
```

**特点：**
- 明确表示删除操作
- 符合RESTful语义
- 可以是幂等的

---

### PUT/PATCH - 更新操作
**适用场景：**
- PUT：完整替换资源
- PATCH：部分更新资源

**示例：**
```java
// ✅ 正确：完整更新
@PutMapping("/{id}")
public Map<String, Object> updateEntry(
    @PathVariable Long id,
    @RequestBody LiveEntryHistory entry
)

// ✅ 正确：部分更新
@PatchMapping("/{id}")
public Map<String, Object> patchEntry(
    @PathVariable Long id,
    @RequestBody Map<String, Object> updates
)
```

---

## 📊 决策流程图

```
开始设计API
    ↓
是什么操作？
    ├─ 查询 → 参数多吗？
    │         ├─ 少（≤5个）→ GET ✅
    │         └─ 多（>5个）→ POST ✅
    │
    ├─ 创建 → POST ✅
    │
    ├─ 删除 → DELETE ✅
    │
    └─ 更新 → PUT/PATCH ✅
```

---

## 🔍 本项目实际应用

### LiveEntryHistoryController 接口清单

| 接口 | 方法 | 说明 | 原因 |
|------|------|------|------|
| `/api/live-entry/query` | GET | 分页查询 | 参数适中，查询操作 |
| `/api/live-entry/{id}` | GET | 获取详情 | 简单查询 |
| `/api/live-entry/{id}` | DELETE | 删除单条 | 删除操作 |
| `/api/live-entry/batch-delete` | DELETE | 批量删除 | 删除操作（虽有body但语义明确） |
| `/api/live-entry/room/{roomId}` | DELETE | 删除直播间记录 | 删除操作 |
| `/api/live-entry/clean-old-data` | DELETE | 清理旧数据 | 删除操作 |
| `/api/live-entry/statistics` | GET | 获取统计 | 简单查询 |
| `/api/live-entry/export/csv` | GET | 导出CSV | 查询操作 |

---

## 💡 最佳实践

### 1. 优先使用正确的HTTP方法
```java
// ✅ 推荐
@DeleteMapping("/{id}")
@GetMapping("/query")

// ❌ 避免
@PostMapping("/delete")
@PostMapping("/query")
```

### 2. 参数数量的判断标准
```java
// ✅ GET：参数 ≤ 5个
@GetMapping("/search")
public Result search(
    @RequestParam String keyword,
    @RequestParam int page,
    @RequestParam int size
)

// ✅ POST：参数 > 5个或复杂对象
@PostMapping("/complex-search")
public Result complexSearch(@RequestBody SearchRequest request)
```

### 3. 批量操作的语义优先
```java
// ✅ DELETE：虽然是批量，但语义是删除
@DeleteMapping("/batch-delete")
public Result batchDelete(@RequestBody List<Long> ids)

// ❌ POST：不符合语义
@PostMapping("/batch-delete")
```

### 4. 保持接口一致性
```java
// ✅ 同一资源的操作用相同前缀
@GetMapping("/entries/{id}")
@DeleteMapping("/entries/{id}")
@PutMapping("/entries/{id}")

// ❌ 混乱的路径
@GetMapping("/get-entry")
@PostMapping("/remove-entry")
```

---

## 🚫 常见误区

### 误区1：所有操作都用POST
```java
// ❌ 错误
@PostMapping("/get/{id}")
@PostMapping("/delete/{id}")
@PostMapping("/update/{id}")

// ✅ 正确
@GetMapping("/{id}")
@DeleteMapping("/{id}")
@PutMapping("/{id}")
```

### 误区2：DELETE不能带请求体
```java
// ✅ 实际上可以（虽然不是最规范）
@DeleteMapping("/batch-delete")
public Result batchDelete(@RequestBody List<Long> ids)

// ✅ 更规范的做法
@DeleteMapping("/batch")
public Result batchDelete(@RequestParam List<Long> ids)
```

### 误区3：GET不能有请求体
```java
// ❌ HTTP规范不允许GET带body
@GetMapping("/query")
public Result query(@RequestBody QueryRequest request)  // 不推荐

// ✅ 使用Query Parameters
@GetMapping("/query")
public Result query(
    @RequestParam String param1,
    @RequestParam String param2
)
```

---

## 📖 参考资源

- [RFC 7231 - HTTP/1.1 Semantics](https://tools.ietf.org/html/rfc7231)
- [RESTful API Design Guide](https://restfulapi.net/)
- [Spring Framework - Web MVC](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)

---

## 🎯 总结

**核心原则：**
1. **语义优先** - 根据操作类型选择HTTP方法
2. **简洁明了** - 简单查询用GET，复杂操作用POST
3. **保持一致** - 同类操作使用相同的模式
4. **易于理解** - 让使用者一眼就能明白接口的用途

**记住：**
- GET = 查询
- POST = 创建
- PUT = 更新（完整）
- PATCH = 更新（部分）
- DELETE = 删除

**参数过多时用POST：**
- 超过5个参数
- 需要复杂对象
- 嵌套数据结构

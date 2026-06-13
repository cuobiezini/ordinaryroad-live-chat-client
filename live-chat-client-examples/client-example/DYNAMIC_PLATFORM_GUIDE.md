# 🎯 动态平台展示功能使用指南

## 📋 功能概述

动态平台展示功能允许你**灵活控制哪些直播平台在后台管理系统中显示**。默认情况下只显示抖音平台，你可以根据需要启用或禁用其他平台。

---

## ✨ 核心特性

### 1️⃣ **默认展示配置**
- ✅ 默认只启用**抖音**平台
- 🔧 其他平台（B站、斗鱼、快手）初始状态为隐藏

### 2️⃣ **动态控制**
- 🎛️ 在首页底部提供"平台显示管理"区域
- ⚡ 实时启用/禁用任意平台
- 🔄 修改后立即生效，无需重启应用

### 3️⃣ **智能保护**
- 🚫 访问未启用的平台页面会自动跳转到首页
- 💬 显示友好提示信息
- 🔒 防止误操作

### 4️⃣ **持久化存储**
- 💾 所有配置保存到MySQL数据库
- 📊 表名：`platform_display_config`
- 🔁 应用重启后配置依然有效

---

## 🚀 快速开始

### 第一步：启动应用

```bash
cd C:\Users\123\IdeaProjects\ordinaryroad-live-chat-client\live-chat-client-examples\client-example
mvn spring-boot:run
```

启动时会看到日志：
```
初始化平台展示默认配置...
创建默认平台配置: douyin (enabled=true)
创建默认平台配置: bilibili (enabled=false)
创建默认平台配置: douyu (enabled=false)
创建默认平台配置: kuaishou (enabled=false)
平台展示默认配置初始化完成
```

### 第二步：访问首页

打开浏览器访问：http://localhost:8080

**初始状态：**
- ✅ 导航栏只显示"📊 总览"和"⚫ 抖音"
- ✅ 页面只显示抖音平台的卡片
- ✅ 其他平台被隐藏

### 第三步：启用其他平台

1. **滚动到页面底部**
   - 找到"⚙️ 平台显示管理"区域

2. **查看当前状态**
   - 抖音：已显示（绿色徽章）
   - B站：已隐藏（红色徽章）
   - 斗鱼：已隐藏（红色徽章）
   - 快手：已隐藏（红色徽章）

3. **启用平台**
   - 点击B站卡片的"✅ 显示"按钮
   - 系统会提示"平台已启用"
   - 页面自动刷新

4. **验证结果**
   - 导航栏出现"🟣 B站"链接
   - 首页显示B站平台卡片
   - 可以访问 http://localhost:8080/bilibili

---

## 📖 详细使用说明

### 🎛️ 平台显示管理界面

#### 位置
首页底部的"⚙️ 平台显示管理"区域

#### 功能
每个平台卡片显示：
- **平台名称和图标** - 例如：⚫ 抖音
- **当前状态** - 已显示/已隐藏
- **平台标识** - 例如：douyin
- **显示顺序** - 决定导航栏中的排列顺序
- **操作按钮** - 显示/隐藏切换

#### 操作流程

**启用平台：**
1. 找到要启用的平台卡片
2. 点击"✅ 显示"按钮
3. 等待页面自动刷新
4. 该平台立即出现在导航栏和首页

**禁用平台：**
1. 找到要禁用的平台卡片
2. 点击"⛔ 隐藏"按钮
3. 等待页面自动刷新
4. 该平台从导航栏和首页移除

---

## 🔌 API接口

如果需要程序化控制平台显示，可以使用以下API：

### 获取启用的平台列表
```bash
GET http://localhost:8080/api/live-chat/display/enabled-platforms
```

响应示例：
```json
[
  {
    "id": 4,
    "platform": "douyin",
    "platformName": "抖音",
    "enabled": true,
    "displayOrder": 1,
    "icon": "⚫"
  }
]
```

### 获取所有平台配置
```bash
GET http://localhost:8080/api/live-chat/display/all-platforms
```

### 启用平台
```bash
POST http://localhost:8080/api/live-chat/display/enable/{platform}
```

示例：
```bash
curl -X POST http://localhost:8080/api/live-chat/display/enable/bilibili
```

### 禁用平台
```bash
POST http://localhost:8080/api/live-chat/display/disable/{platform}
```

示例：
```bash
curl -X POST http://localhost:8080/api/live-chat/display/disable/bilibili
```

### 批量更新平台状态
```bash
POST http://localhost:8080/api/live-chat/display/batch-update
Content-Type: application/json

{
  "bilibili": true,
  "douyu": true,
  "kuaishou": false,
  "douyin": true
}
```

### 检查平台是否启用
```bash
GET http://localhost:8080/api/live-chat/display/check/{platform}
```

响应示例：
```json
{
  "platform": "bilibili",
  "enabled": true
}
```

---

## 💡 使用场景

### 场景1：专注单一平台
**需求：** 只想监听抖音直播

**操作：**
1. 保持默认配置（只有抖音启用）
2. 其他平台全部隐藏
3. 界面简洁，专注于抖音

### 场景2：多平台运营
**需求：** 同时管理B站和抖音

**操作：**
1. 启用B站和抖音
2. 禁用斗鱼和快手
3. 导航栏只显示这两个平台

### 场景3：临时测试
**需求：** 临时测试斗鱼平台

**操作：**
1. 启用斗鱼
2. 进行测试
3. 测试完成后禁用斗鱼

### 场景4：全平台监控
**需求：** 监控所有平台

**操作：**
1. 启用所有平台
2. 在首页查看所有平台状态
3. 根据需要快速切换

---

## 🗄️ 数据库说明

### 表结构
表名：`platform_display_config`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| platform | VARCHAR(50) | 平台标识（唯一） |
| platform_name | VARCHAR(50) | 平台中文名称 |
| enabled | BOOLEAN | 是否启用显示 |
| display_order | INT | 显示顺序 |
| icon | VARCHAR(10) | 平台图标emoji |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注说明 |

### 默认数据
```sql
INSERT INTO platform_display_config (platform, platform_name, enabled, display_order, icon) VALUES
('douyin', '抖音', true, 1, '⚫'),
('bilibili', 'B站', false, 2, '🟣'),
('douyu', '斗鱼', false, 3, '🔴'),
('kuaishou', '快手', false, 4, '🟠');
```

### 手动修改
如果需要直接修改数据库：

```sql
-- 启用B站
UPDATE platform_display_config SET enabled = true WHERE platform = 'bilibili';

-- 禁用抖音
UPDATE platform_display_config SET enabled = false WHERE platform = 'douyin';

-- 查询所有配置
SELECT * FROM platform_display_config ORDER BY display_order;
```

---

## ❓ 常见问题

### Q1: 为什么我访问/bilibili页面会自动跳转？
**A:** 因为B站平台当前处于"隐藏"状态。你需要先在首页的"平台显示管理"区域启用B站。

### Q2: 启用平台后，之前的配置还在吗？
**A:** 是的，平台配置（直播间ID、Cookie等）存储在`platform_config`表中，与显示配置独立。启用平台后会加载之前的配置。

### Q3: 如何恢复默认配置（只显示抖音）？
**A:** 
方法1：在首页将所有其他平台禁用
方法2：直接清空数据库表，重启应用会自动重建默认配置
```sql
DELETE FROM platform_display_config;
-- 重启应用
```

### Q4: 可以自定义平台显示顺序吗？
**A:** 目前显示顺序在初始化时设定。如需修改，可以直接更新数据库：
```sql
UPDATE platform_display_config SET display_order = 1 WHERE platform = 'bilibili';
UPDATE platform_display_config SET display_order = 2 WHERE platform = 'douyin';
```

### Q5: 禁用平台会影响正在运行的客户端吗？
**A:** 不会影响。禁用平台只是隐藏前端页面，如果客户端已经连接，它会继续运行。建议先断开客户端再禁用平台。

### Q6: 能否添加新的平台？
**A:** 需要在代码中添加新平台的配置。修改`PlatformDisplayService.initDefaultConfigs()`方法，添加新平台信息。

---

## 🎯 最佳实践

### 1. 按需启用
- 只启用实际需要使用的平台
- 减少界面复杂度
- 提高管理效率

### 2. 定期清理
- 长期不用的平台及时禁用
- 保持界面清爽

### 3. 配置备份
- 重要配置定期备份数据库
```bash
mysqldump -u root -p123456 live > live_backup.sql
```

### 4. 团队协作
- 统一团队的平台启用标准
- 文档化各平台的用途

---

## 🎉 总结

动态平台展示功能让你可以：
- ✅ 灵活控制平台显示
- ✅ 简化操作界面
- ✅ 提高管理效率
- ✅ 避免误操作

**开始使用吧！** 🚀

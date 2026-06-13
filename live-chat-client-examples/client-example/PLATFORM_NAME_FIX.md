# 平台名称显示 undefined 问题修复

## 问题描述

**现象**: 入场记录列表中，"平台"列显示为 "undefined"

![问题截图](./assets/platform-undefined-issue.png)

**根本原因**: 
1. 前端代码使用 `getPlatformName(platform)` 获取平台中文名称
2. 但 `platform` 参数是外部传入的（查询条件），而不是每条记录的实际平台字段
3. **更关键的是**: `getPlatformName` 函数在 douyin.html 中不存在，导致调用时返回 `undefined`

## 修复方案

### 修复的文件
- `live-chat-client-examples/client-example/src/main/resources/templates/douyin.html`

### 修复内容

#### 1. 添加 `getPlatformName` 函数（第1146-1158行）

```javascript
/**
 * 获取平台中文名称
 */
function getPlatformName(platform) {
    const names = { 
        'douyin': '抖音', 
        'kuaishou': '快手',
        'bilibili': 'B站',
        'douyu': '斗鱼',
        'huya': '虎牙'
    };
    return names[platform] || platform;
}
```

**功能说明**:
- 将后端返回的平台标识（如 'douyin'）转换为中文名称（如 '抖音'）
- 支持所有主流直播平台
- 如果平台不在映射表中，则直接返回原始值

#### 2. 修改 `renderEntryTable` 函数（第694-695行）

**修复前**:
```javascript
const platformName = getPlatformName(platform);
```

**修复后**:
```javascript
// 使用后端返回的每条记录的platform字段
const platformName = getPlatformName(entry.platform);
```

**修复原因**:
- 后端返回的数据中，每条记录都有自己的 `platform` 字段
- 应该使用 `entry.platform`（每条记录的实际平台）而不是外部传入的 `platform` 参数（查询条件）
- 这样可以确保即使混合查询多个平台的数据，也能正确显示每条记录的平台名称

## 数据流分析

### 后端返回数据结构

```json
{
  "success": true,
  "data": [
    {
      "id": 5268,
      "platform": "douyin",      // ← 每条记录都有这个字段
      "roomId": "450490589687",
      "uid": "95584684795",
      "username": "木...",
      "displayId": "...",
      "isUsed": false,
      "createdAt": "2026-06-13T19:12:56"
    },
    ...
  ],
  "total": 100,
  "pageNum": 1,
  "totalPages": 5
}
```

### 前端处理流程

1. **接收数据**: 后端返回包含 `platform` 字段的记录数组
2. **遍历渲染**: 对每条记录调用 `getPlatformName(entry.platform)`
3. **转换显示**: 将 'douyin' → '抖音'，'kuaishou' → '快手' 等
4. **展示结果**: 表格中显示中文平台名称

## 测试验证

### 测试步骤
1. 刷新页面，重新加载 douyin.html
2. 点击"查询"按钮加载入场记录
3. 观察"平台"列是否显示正确的中文名称

### 预期结果
- ✅ 平台列显示 "抖音"（而不是 "undefined"）
- ✅ 所有记录的平台名称都正确显示
- ✅ 翻页后平台名称仍然正确
- ✅ 筛选不同平台后名称正确

## 相关经验教训

### 前后端字段名一致性

根据项目记忆库中的经验：
> **前后端字段名不一致导致数据不显示**
> 前端访问后端返回数据时，必须确保字段名与后端实体类完全一致。常见问题如：后端使用 createdAt 而前端误用 enterTime。修复方法：通过 search_codebase 定位后端实体类确认实际字段名，再修正前端代码。

**本次问题的启示**:
1. **数据来源要清晰**: 明确数据是来自后端返回的记录，还是外部传入的参数
2. **函数存在性检查**: 使用前确保辅助函数已定义
3. **字段命名规范**: 后端实体类的字段名（如 `platform`）应与前端使用的字段名保持一致

## 其他平台页面

如果其他平台页面（kuaishou.html、bilibili.html、douyu.html、huya.html）也有类似问题，可以采用相同的修复方式：

1. 添加 `getPlatformName` 函数
2. 修改 `renderEntryTable` 中使用 `entry.platform` 而不是外部参数

## 修复日期
2026-06-13

## 修复状态
✅ 已完成并验证

# 🔐 后台管理系统登录鉴权功能使用指南

## 📋 功能概述

后台管理系统现已集成**Spring Security**认证授权机制，所有页面和功能都需要登录后才能访问。系统采用用户名密码认证方式，支持会话管理和权限控制。

---

## ✨ 核心特性

### 1️⃣ **安全认证**
- 🔒 基于Spring Security的企业级安全框架
- 🔑 BCrypt密码加密存储
- 🛡️ CSRF保护（可选）
- ⏱️ 会话超时管理

### 2️⃣ **用户管理**
- 👤 默认管理员账号自动创建
- 📊 用户信息持久化到数据库
- 🕐 记录最后登录时间
- ✅ 支持启用/禁用账号

### 3️⃣ **访问控制**
- 🚫 未登录自动跳转到登录页
- ✅ 登录后访问受保护资源
- 🚪 支持安全登出
- 🔢 单用户会话限制

### 4️⃣ **友好界面**
- 🎨 精美的登录页面设计
- 💬 清晰的错误提示
- 📱 响应式布局
- 🎯 自动聚焦输入框

---

## 🚀 快速开始

### 第一步：启动应用

```bash
cd C:\Users\123\IdeaProjects\ordinaryroad-live-chat-client\live-chat-client-examples\client-example
mvn spring-boot:run
```

启动时会看到日志：
```
检查并初始化默认管理员账号...
✅ 默认管理员账号创建成功
   用户名: admin
   密码: admin123
   ⚠️  请及时修改默认密码！
```

### 第二步：访问系统

打开浏览器访问：http://localhost:8080

**首次访问会自动跳转到登录页面**

### 第三步：登录系统

**默认账号：**
- 用户名：`admin`
- 密码：`admin123`

输入账号密码后点击"🚀 登录"按钮。

### 第四步：使用系统

登录成功后自动跳转到首页，可以：
- ✅ 查看和管理直播平台
- ✅ 配置直播间参数
- ✅ 连接/断开客户端
- ✅ 发送弹幕
- ✅ 管理平台显示

右上角显示当前用户名和"🚪 退出登录"按钮。

---

## 📖 详细使用说明

### 🔐 登录流程

1. **访问任意页面**
   - 如果未登录，自动重定向到 `/login`

2. **输入 credentials**
   - 用户名：区分大小写
   - 密码：区分大小写

3. **提交表单**
   - 点击登录按钮或按Enter键

4. **验证结果**
   - ✅ 成功：跳转到首页（`/`）
   - ❌ 失败：显示错误提示，停留在登录页

### 🚪 登出流程

1. **点击退出登录按钮**
   - 位于页面右上角

2. **确认登出**
   - 系统自动清除会话

3. **跳转登录页**
   - 显示"您已成功登出"提示

4. **重新登录**
   - 需要再次输入账号密码

---

## 🗄️ 数据库说明

### 表结构
表名：`sys_user`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(200) | BCrypt加密密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| role | VARCHAR(50) | 角色（ROLE_ADMIN） |
| enabled | TINYINT(1) | 是否启用 |
| last_login_time | DATETIME | 最后登录时间 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

### 默认数据
```sql
用户名: admin
密码: admin123 (BCrypt加密)
角色: ROLE_ADMIN
状态: 已启用
```

### 手动管理用户

```sql
-- 查看所有用户
SELECT id, username, real_name, role, enabled, created_at FROM sys_user;

-- 创建新用户（需要BCrypt加密密码）
INSERT INTO sys_user (username, password, real_name, role, enabled) 
VALUES ('newuser', '$2a$10$...', '新用户', 'ROLE_ADMIN', 1);

-- 禁用用户
UPDATE sys_user SET enabled = 0 WHERE username = 'admin';

-- 重置密码
UPDATE sys_user SET password = '$2a$10$...' WHERE username = 'admin';
```

---

## 🔧 配置说明

### Security配置

配置文件位置：`SecurityConfig.java`

**主要配置项：**

1. **公开访问的资源**
   ```java
   .requestMatchers("/login", "/login**", "/error").permitAll()
   .requestMatchers("/actuator/**").permitAll()
   ```

2. **需要认证的资源**
   ```java
   .anyRequest().authenticated()
   ```

3. **登录配置**
   ```java
   .loginPage("/login")
   .loginProcessingUrl("/login")
   .defaultSuccessUrl("/")
   .failureUrl("/login?error=true")
   ```

4. **登出配置**
   ```java
   .logoutUrl("/logout")
   .logoutSuccessUrl("/login?logout=true")
   ```

5. **会话管理**
   ```java
   .maximumSessions(1)  // 最大会话数
   .maxSessionsPreventsLogin(false)  // 是否阻止新登录
   ```

### 自定义配置

#### 修改密码加密强度
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // 默认10，范围4-31
}
```

#### 允许静态资源访问
```java
@Bean
public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(
        "/css/**",
        "/js/**",
        "/images/**",
        "/favicon.ico"
    );
}
```

#### 启用CSRF保护
```java
.csrf(csrf -> csrf.enable())
```

---

## 🔌 API接口

### 获取当前用户信息
```bash
GET http://localhost:8080/api/live-chat/user/current
```

响应示例：
```json
{
  "username": "admin",
  "authenticated": true
}
```

---

## ❓ 常见问题

### Q1: 忘记管理员密码怎么办？

**A:** 方法1：直接修改数据库
```sql
-- 将密码重置为 admin123
UPDATE sys_user 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';
```

方法2：删除用户后重启应用
```sql
DELETE FROM sys_user WHERE username = 'admin';
-- 重启应用，会自动重建默认账号
```

### Q2: 如何创建新用户？

**A:** 目前需要通过代码或SQL手动创建。建议使用BCrypt加密密码：

```java
@Autowired
private PasswordEncoder passwordEncoder;

String encodedPassword = passwordEncoder.encode("your_password");
```

或使用在线工具生成BCrypt哈希，然后插入数据库。

### Q3: 为什么登录成功后又跳回登录页？

**A:** 可能的原因：
1. 浏览器禁用了Cookie
2. 会话超时
3. 另一个设备登录了同一账号（如果配置了单会话）

解决方法：清除浏览器缓存，重新登录。

### Q4: 如何延长会话超时时间？

**A:** 在 `application.yaml` 中添加：
```yaml
server:
  servlet:
    session:
      timeout: 30m  # 30分钟
```

### Q5: 可以实现记住我功能吗？

**A:** 可以，在SecurityConfig中添加：
```java
.rememberMe(rememberMe -> rememberMe
    .key("uniqueAndSecret")
    .tokenValiditySeconds(86400)  // 24小时
)
```

### Q6: 如何限制登录尝试次数？

**A:** 可以使用Spring Security的锁定功能：
```java
.authenticationManager(auth -> auth
    .userDetailsService(userDetailsService)
    .passwordEncoder(passwordEncoder())
)
```

并在UserDetailsService中实现账户锁定逻辑。

### Q7: 登录页面可以自定义吗？

**A:** 可以，修改 `login.html` 文件即可。保持表单字段名称不变：
- `username` - 用户名字段
- `password` - 密码字段
- 表单action指向 `/login`
- 表单method为 `post`

---

## 🛡️ 安全建议

### 1. 修改默认密码
**重要！** 首次登录后立即修改默认密码：
```sql
-- 生成新的BCrypt哈希后执行
UPDATE sys_user SET password = '新密码的BCrypt哈希' WHERE username = 'admin';
```

### 2. 启用HTTPS
生产环境务必启用HTTPS，防止密码明文传输。

### 3. 定期审计日志
检查登录日志，发现异常登录行为：
```sql
SELECT * FROM sys_user ORDER BY last_login_time DESC;
```

### 4. 强密码策略
建议使用复杂密码：
- 至少8位长度
- 包含大小写字母
- 包含数字和特殊字符

### 5. 禁用不需要的账号
长期不用的账号及时禁用：
```sql
UPDATE sys_user SET enabled = 0 WHERE username = 'xxx';
```

---

## 🎯 最佳实践

### 开发环境
- 使用默认账号方便测试
- 开启详细的Security日志
- 可以禁用CSRF简化开发

### 生产环境
- 修改默认密码
- 启用HTTPS
- 启用CSRF保护
- 配置合理的会话超时时间
- 定期备份用户数据
- 监控登录失败次数

---

## 📊 技术架构

### 核心技术栈
- **Spring Security 6.x** - 安全框架
- **BCrypt** - 密码加密算法
- **Spring Session** - 会话管理
- **Thymeleaf** - 模板引擎
- **JPA/Hibernate** - 数据持久化

### 认证流程
```
用户请求 → SecurityFilterChain → 是否登录？
  ↓ 否
重定向到 /login → 显示登录页 → 提交表单
  ↓
AuthenticationManager → UserDetailsService
  ↓
查询数据库 → 验证密码 → 创建Session
  ↓ 是
访问受保护资源
```

### 密码加密
```
原始密码: admin123
    ↓ BCrypt加密
加密密码: $2a$10$N9qo8uLOickgx2ZMRZoMye...
    ↓ 存储到数据库
验证时对比哈希值
```

---

## 🎉 总结

登录鉴权功能为你的后台管理系统提供了：
- ✅ 企业级的安全保障
- ✅ 友好的用户体验
- ✅ 灵活的配置选项
- ✅ 完整的用户管理

**现在你的系统已经具备了完善的安全认证机制！** 🚀

---

## 📞 技术支持

如遇到安全问题，请：
1. 检查Security配置是否正确
2. 查看应用日志中的安全相关日志
3. 确认数据库中用户数据正确
4. 检查浏览器Cookie设置

**祝使用愉快！** 🔐

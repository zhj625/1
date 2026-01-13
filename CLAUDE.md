# CLAUDE.md - 项目上下文文件

## 对话语言
**请始终使用中文与用户对话。**

---

## 工作模式
```bash
npx @anthropic-ai/claude-code --dangerously-skip-permissions
```
- 直接执行任务，无需每次询问用户确认
- 遇到重大架构决策或不确定的情况时再询问

---

## 项目简介
**智慧图书馆管理系统** - B/S 架构，前后端分离，用于**毕业设计**。

```
前端 (Vue 3 + Element Plus + Tailwind CSS)
    ↓ RESTful API
后端 (Spring Boot 3.2.1 + Spring Security + JWT)
    ↓ JPA/Hibernate
数据库 (MySQL 8.0)
```

---

## 技术栈

### 后端
Java 17 | Spring Boot 3.2.1 | Spring Security 6.x | Spring Data JPA | JJWT 0.12.3 | MySQL 8.0 | Maven | Lombok

### 前端
Vue 3 (CDN) | Vue Router 4 | Pinia | Element Plus | Tailwind CSS

### 代码规范
- 包结构：`com.library.{controller|service|repository|entity|dto|config|security|exception|common}`
- DTO：请求 `XxxRequest`，响应 `XxxResponse`
- API 路径：`/api/` 前缀

---

## 已完成功能 (全部完成)

| 模块 | 状态 |
|------|------|
| 用户认证 (JWT/BCrypt/登录限制) | ✅ |
| 图书管理 (CRUD/搜索/乐观锁) | ✅ |
| 借阅管理 (借/还/续借/逾期/罚款) | ✅ |
| 分类管理 (树形结构) | ✅ |
| 用户管理 (CRUD/禁用/审核) | ✅ |
| 收藏功能 | ✅ |
| 统计功能 (仪表盘/趋势/高级统计) | ✅ |
| 文件上传 (封面/5MB限制) | ✅ |
| Excel 导出 | ✅ |
| Swagger API 文档 | ✅ |
| 操作日志 (AOP) | ✅ |
| 账号安全 (失败锁定/改密码) | ✅ |
| 站内通知 (到期/逾期提醒) | ✅ |
| 角色权限 (管理员/馆员/用户) | ✅ |
| 图书预约 (队列/通知/3天有效期) | ✅ |
| 新书推荐 | ✅ |
| 图书馆公告 | ✅ |
| 注册审核 | ✅ |
| 罚款管理 (规则配置/记录/免罚) | ✅ |
| 单元测试 (31个用例) | ✅ |
| 前端界面 (admin.html + user.html) | ✅ |

---

## 常用指令

### 启动后端

**方式一：进入子目录运行**
```bash
cd library-management
mvn spring-boot:run
```

**方式二：在项目根目录运行（指定 pom.xml 路径）**
```bash
mvn spring-boot:run -f library-management/pom.xml
```

> ⚠️ 注意：必须在 `library-management` 目录下运行，或使用 `-f` 参数指定 pom.xml 路径，否则会报错 `No plugin found for prefix 'spring-boot'`

后端地址：`http://localhost:8080`
Swagger 文档：`http://localhost:8080/swagger-ui/index.html`

### 前端访问
- 管理后台：浏览器打开 `admin.html`
- 用户前台：浏览器打开 `user.html`

### 默认账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 馆员 | librarian | admin123 |
| 普通用户 | user1 | admin123 |

---

## 关键配置

### 数据库
```yaml
spring.datasource:
  url: jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=Asia/Shanghai
  username: root
  password: 625312zhj
```

### Token 存储
- admin.html：`localStorage.admin_token_pro`
- user.html：`localStorage.token`

### 业务规则
- 借阅上限：5 本/人
- 借阅期限：30 天
- 续借：最多 2 次，每次延长 30 天
- 登录锁定：失败 5 次锁定 15 分钟

---

## 项目结构
```
D:\桌面\1\
├── admin.html          # 管理后台
├── user.html           # 用户前台
├── js/
│   ├── common.js       # 公共JS模块 (HTTP封装/工具函数)
│   └── README.md       # 公共模块使用说明
└── library-management/ # Spring Boot 后端
    └── src/main/java/com/library/
        ├── controller/     # 14 个控制器
        ├── service/impl/   # 业务实现
        ├── repository/     # JPA 仓库
        ├── entity/         # 11 个实体
        ├── dto/            # Request/Response
        ├── annotation/     # @Log 注解
        ├── aspect/         # AOP 切面
        ├── security/       # JWT
        └── config/         # 配置类
```

---

## 最近更新

### 2026-01-13 - 前端代码重构
**目标**: 消除 admin.html 和 user.html 中的重复代码

**改动**:
1. 创建 `js/common.js` 公共模块
   - HTTP 请求封装 (createHttpClient 工厂函数)
   - 工具函数 (formatTime / handleImgError)
   - 全局常量 (API_BASE_URL / 占位图)

2. 重构 `admin.html`
   - 引入 common.js
   - 使用公共 HTTP 客户端
   - 减少 ~94 行重复代码

3. 重构 `user.html`
   - 引入 common.js
   - 使用公共 HTTP 客户端
   - 减少 ~82 行重复代码

**效果**:
- ✅ 总计减少 ~176 行重复代码
- ✅ 统一的 HTTP 请求处理逻辑
- ✅ 更易维护,修改一处两个页面同步更新

---

*最后更新:2026年1月13日*

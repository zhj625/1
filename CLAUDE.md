# CLAUDE.md - 项目上下文文件

> 本文件用于在 `/clear` 后快速恢复工作上下文

---

## 项目简介 (Project Context)

**智慧图书馆管理系统 (Smart Library Management System)** - 一个基于 B/S 架构的现代化图书馆管理系统，采用前后端分离开发模式，分为管理员后台 (`admin.html`) 和用户前台 (`user.html`) 两个独立界面。本项目用于**毕业设计**。

**核心架构**：
```
前端 (Vue 3 + Element Plus + Tailwind CSS)
    ↓ RESTful API (HTTP/JSON)
后端 (Spring Boot 3.2.1 + Spring Security + JWT)
    ↓ JPA/Hibernate
数据库 (MySQL 8.0)
```

---

## 技术栈与规范 (Tech Stack & Guidelines)

### 后端技术栈
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | LTS 版本 |
| Spring Boot | 3.2.1 | 主框架 |
| Spring Security | 6.x | 安全框架 |
| Spring Data JPA | 3.x | ORM 持久层 |
| JJWT | 0.12.3 | JWT Token 生成与验证 |
| Apache POI | 5.2.5 | Excel 文件导出 |
| MySQL | 8.0+ | 数据库 |
| Maven | 3.6+ | 构建工具 |
| Lombok | - | 简化代码 |

### 前端技术栈
| 技术 | 说明 |
|------|------|
| Vue 3 | Composition API，通过 CDN 引入 |
| Vue Router 4 | 前端路由 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| Tailwind CSS | 原子化 CSS 框架 |

### 代码规范
- **后端包结构**：`com.library.{controller|service|repository|entity|dto|config|security|exception|common}`
- **DTO 命名**：请求用 `XxxRequest`，响应用 `XxxResponse`
- **实体继承**：所有实体继承 `BaseEntity`（包含 id, createdAt, updatedAt）
- **API 路径**：统一使用 `/api/` 前缀
- **权限注解**：使用 `@PreAuthorize("hasRole('ADMIN')")` 控制访问

### 数据库规范
- 表名：小写下划线（如 `borrow_record`）
- 字段名：小写下划线（如 `user_id`）
- 主键：`id BIGINT AUTO_INCREMENT`
- 时间字段：`created_at`, `updated_at`

---

## 当前进度与任务 (Current Status)

### 已完成功能模块

| 模块 | 状态 | 说明 |
|------|------|------|
| 用户认证 | ✅ 完成 | JWT 登录/注册，BCrypt 加密 |
| 图书管理 | ✅ 完成 | CRUD，分类筛选，搜索 |
| 借阅管理 | ✅ 完成 | 借书/还书，逾期检测（定时任务） |
| 分类管理 | ✅ 完成 | 树形分类结构 |
| 用户管理 | ✅ 完成 | 管理员 CRUD |
| 收藏功能 | ✅ 完成 | 添加/取消收藏，收藏列表 |
| 统计功能 | ✅ 完成 | 仪表盘数据，借阅趋势，热门排行 |
| 文件上传 | ✅ 完成 | 图书封面上传（5MB 限制） |
| Excel 导出 | ✅ 完成 | 借阅记录导出为 xlsx 格式 |
| 前端界面 | ✅ 完成 | admin.html + user.html |

### 刚刚完成的工作

1. **为所有书籍添加封面图片** (2025-12-30)
   - 修改 `DataInitializer.java` 的 `createBook` 方法，添加 `coverUrl` 参数
   - 为 30+ 本书籍配置豆瓣图书封面 URL
   - 逻辑优化：书籍已存在时自动更新空封面
   - 前端已有 `handleImgError` 容错处理，加载失败显示占位图

2. **实现 Excel 导出功能**
   - 添加 Apache POI 依赖 (poi-ooxml 5.2.5)
   - 新增 `ExcelExportService` 接口和 `ExcelExportServiceImpl` 实现类
   - 在 `BorrowController` 添加导出接口 `GET /api/borrows/export`
   - 在 `BorrowService` 添加 `getAllRecordsForExport` 方法
   - 在 `BorrowRecordRepository` 添加 `findAllByConditions` 查询
   - 在 admin.html 借阅管理页面添加"导出 Excel"按钮
   - 导出字段：订单号、用户名、真实姓名、图书名称、ISBN、作者、借阅日期、应还日期、归还日期、状态、是否逾期、备注

### 待实现功能（建议优先级）

| 优先级 | 功能 | 说明 |
|--------|------|------|
| 高 | Swagger API 文档 | 添加 SpringDoc，自动生成接口文档，答辩展示加分 |
| 高 | 图书续借功能 | 完善借阅流程闭环 |
| 中 | 图书预约 | 当库存为 0 时可预约 |
| 低 | 逾期罚款计算 | 自动计算罚款金额 |
| 低 | 单元测试 | JUnit + Mockito |
| 低 | 操作日志 | AOP 记录关键操作 |

---

## 常用指令 (Useful Commands)

### 后端启动
```bash
cd library-management
mvn spring-boot:run
```
后端运行在 `http://localhost:8080`

### 前端访问
- 管理后台：直接浏览器打开 `admin.html`
- 用户前台：直接浏览器打开 `user.html`

### 数据库初始化
```sql
CREATE DATABASE library_db CHARACTER SET utf8mb4;
```
启动后会自动执行 `schema.sql` 和 `DataInitializer` 初始化数据

### 默认账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user1 | admin123 |

### Maven 常用命令
```bash
mvn clean compile      # 编译
mvn clean package      # 打包
mvn clean install      # 安装到本地仓库
mvn dependency:tree    # 查看依赖树
```

---

## 已知的坑 (Known Issues)

### 1. JWT 密钥长度
- **问题**：JJWT 0.12.x 要求 HS512 密钥至少 512 位（64 字节）
- **解决**：`application.yml` 中的 `jwt.secret` 必须足够长

### 2. 跨域配置
- **问题**：前端直接打开 HTML 文件会遇到 CORS 问题
- **解决**：已在 `CorsConfig.java` 中配置允许所有来源

### 3. 文件上传路径
- **问题**：Windows 和 Linux 路径分隔符不同
- **解决**：使用 `Paths.get()` 而非字符串拼接

### 4. 前端 Token 存储
- **admin.html**：`localStorage.setItem('admin_token_pro', xxx)`
- **user.html**：`localStorage.setItem('token', xxx)`
- **请求头**：`Authorization: Bearer {token}`
- **401 处理**：自动清除 token 并跳转登录页

### 5. 借阅限制
- 每人最多借阅 **5 本**
- 同一本书不能重复借阅（未归还状态）
- 默认借阅期限 **30 天**

### 6. 逾期检测定时任务
- **Cron 表达式**：`0 0 1 * * ?`（每天凌晨 1 点）
- **功能**：自动将超过 dueDate 的记录标记为 OVERDUE

### 7. 数据库连接配置
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

---

## 关键文件路径

```
D:\桌面\1\
├── admin.html                           # 管理后台前端 (1110+ 行)
├── user.html                            # 用户前台前端 (1393+ 行)
├── 项目答辩报告.md                        # 答辩文档
├── CLAUDE.md                            # 本文件
└── library-management/                  # Spring Boot 后端
    ├── pom.xml
    └── src/main/
        ├── java/com/library/
        │   ├── controller/              # 8 个控制器
        │   ├── service/impl/            # 业务实现
        │   ├── repository/              # JPA 仓库
        │   ├── entity/                  # 5 个实体
        │   ├── dto/                     # Request/Response
        │   ├── security/                # JWT 相关
        │   └── config/                  # 配置类
        └── resources/
            ├── application.yml
            └── schema.sql
```

---

## 快速恢复指令

如果需要继续之前的工作，可以说：
- "帮我实现 Swagger API 文档"
- "帮我添加图书续借功能"
- "帮我实现图书预约功能"
- "检查一下项目还有什么问题"

---

*最后更新：2025年12月30日*

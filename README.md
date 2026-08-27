# 智慧图书馆管理系统

面向毕业设计的全栈图书馆管理系统，包含管理员端、用户端和 Spring Boot 后端服务。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.1 / Java 17 / Spring Security / JWT / JPA |
| 前端 | Vue 3 / Element Plus / Tailwind CSS / ECharts |
| 数据库 | MySQL 8.0 |
| 测试 | Postman / Newman / JUnit 5 / Mockito |

## 核心功能

- 图书管理：CRUD、分类、库存、封面生成
- 借阅业务：借阅、续借、预约、归还、逾期罚款
- 用户管理：注册、KYC 审核、角色权限控制（JWT + Spring Security）
- 通知系统：借还书提醒、预约到书通知、罚款催缴
- 数据统计：借阅趋势、热门图书 TOP10、活跃用户分析、Excel 导出

## 测试结果

| 指标 | 结果 |
|---|---|
| Postman 接口请求数 | 48 |
| 断言数 | 50 |
| 失败数 | 0 |
| 平均响应时间 | 30ms |
| JUnit 单元测试 | 33 条，失败 0 |

测试覆盖：JWT 鉴权、角色权限（401/403）、跨角色业务状态流转、负向用例。  
详细测试报告和用例清单见 [test/README.md](test/README.md)。

## 项目结构smart-library/
├── library-management/ # Spring Boot 后端 + 单元测试
├── admin.html # 管理员端页面
├── user.html # 用户端页面
├── js/ # 前端公共脚本
├── images/ # 图片资源
└── test/ # 接口测试文件（Postman + Newman）
## 启动方式

确保 MySQL 已启动并完成数据库配置，然后执行：

```bash
mvn spring-boot:run -f library-management/pom.xml
```

后端默认地址：`http://localhost:8080`  
Swagger UI：`http://localhost:8080/swagger-ui/index.html`

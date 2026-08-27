# 智慧图书馆管理系统

本项目是一个面向毕业设计的智慧图书馆管理系统，包含管理员端、用户端和 Spring Boot 后端服务。

## 项目结构

- `admin.html`：管理端页面
- `user.html`：用户端页面
- `js/`：前端公共脚本
- `images/`：图片资源
- `library-management/`：Spring Boot 后端与单元测试
- `test/`：Postman 自动化测试、测试用例和测试报告

## 启动后端

确保 MySQL 已启动并完成数据库配置，然后执行：

```powershell
mvn spring-boot:run -f library-management\pom.xml
```

后端默认地址：`http://localhost:8080`

## 测试

测试资料和复现方法见 [`test/README.md`](test/README.md)。

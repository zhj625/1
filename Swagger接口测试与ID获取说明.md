# Swagger 接口测试与 ID 获取说明

## 1. 基础地址

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI 文档: `http://localhost:8080/v3/api-docs`
- 登录接口: `POST http://localhost:8080/api/auth/login`
- 用户列表接口: `GET http://localhost:8080/api/users?page=1&size=10`

## 2. 带锁接口如何授权

带 `🔒` 标记的接口需要在请求头中携带 JWT Token。

获取方式：

1. 打开 Swagger UI
2. 找到 `POST /api/auth/login`
3. 发送示例请求：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

4. 复制返回结果中的 `data.token`
5. 点击 Swagger 右上角 `Authorize`
6. 直接粘贴 token 即可

注意：

- 不需要手动加 `Bearer ` 前缀
- Swagger UI 会自动拼成实际请求头：

```http
Authorization: Bearer {token}
```

## 3. 为什么浏览器直接打开 `/api/users` 会报未登录

例如直接访问：

`http://localhost:8080/api/users`

会返回：

```json
{
  "code": 1001,
  "message": "未登录或Token失效"
}
```

原因是浏览器地址栏不会自动带上 JWT 请求头。  
这类接口必须通过 Swagger、Postman、Apifox 等工具调用，或者前端页面在请求头中自动带 token。

## 4. 常见 ID 是怎么来的

这些 `id` 本质上都是数据库中的主键，通常通过“先查列表，再取返回结果里的 `id` 字段”获得。

### 4.1 userId

来源接口：

- `GET http://localhost:8080/api/users?page=1&size=10`
- `GET http://localhost:8080/api/users/pending?page=1&size=10`

用途：

- 查看用户详情
- 审核用户
- 禁用用户
- 删除用户

### 4.2 bookId

来源接口：

- `GET http://localhost:8080/api/books?page=1&size=10`

用途：

- 图书详情
- 借阅
- 预约
- 收藏
- 评论

### 4.3 announcementId

来源接口：

- `GET http://localhost:8080/api/announcements?page=1&size=10`
- `GET http://localhost:8080/api/announcements/published?page=1&size=10`

用途：

- 公告详情
- 发布公告
- 取消发布
- 删除公告

### 4.4 categoryId

来源接口：

- `GET http://localhost:8080/api/categories`

用途：

- 新增图书时指定分类
- 修改图书分类
- 删除分类

### 4.5 borrowId

来源接口：

- `GET http://localhost:8080/api/borrows?page=1&size=10`
- `GET http://localhost:8080/api/borrows/my?page=1&size=10`

用途：

- 归还图书
- 续借图书
- 缴纳罚款

### 4.6 reservationId

来源接口：

- `GET http://localhost:8080/api/reservations/my?page=1&size=10`
- `GET http://localhost:8080/api/reservations?page=1&size=10`

用途：

- 取消预约
- 查看预约详情

## 5. 如何从返回结果中拿 ID

例如查询图书：

`GET http://localhost:8080/api/books?page=1&size=10`

假设返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [
      {
        "id": 39,
        "title": "金融心理学"
      }
    ]
  }
}
```

那么：

- 这本书的 `bookId` 就是 `39`

后续可以用于：

- `GET http://localhost:8080/api/books/39`
- `POST http://localhost:8080/api/reservations/39`
- `POST http://localhost:8080/api/favorites/39`

公告、用户、分类、借阅记录也是同理。

## 6. 推荐测试顺序

### 第一步：先登录拿 token

- `POST http://localhost:8080/api/auth/login`

### 第二步：查基础数据，拿各类 ID

- 用户：`GET http://localhost:8080/api/users?page=1&size=10`
- 图书：`GET http://localhost:8080/api/books?page=1&size=10`
- 分类：`GET http://localhost:8080/api/categories`
- 公告：`GET http://localhost:8080/api/announcements?page=1&size=10`

### 第三步：用查出来的 ID 去测详情、修改、删除类接口

例如：

- `GET http://localhost:8080/api/books/{bookId}`
- `DELETE http://localhost:8080/api/categories/{categoryId}`
- `POST http://localhost:8080/api/announcements/{announcementId}/publish`

## 7. 哪些接口可以直接浏览器打开

通常公开接口可以直接在浏览器访问，例如：

- `http://localhost:8080/api/books?page=1&size=10`
- `http://localhost:8080/api/categories`
- `http://localhost:8080/api/announcements/published?page=1&size=10`

通常受保护接口不能直接浏览器访问，例如：

- `http://localhost:8080/api/users?page=1&size=10`
- `http://localhost:8080/api/borrows/my?page=1&size=10`
- `http://localhost:8080/api/reservations/my?page=1&size=10`

## 8. 常见测试账号

可优先使用：

- 管理员：`admin / admin123`
- 普通用户：`user1 / admin123`

## 9. 测试建议

- 先查列表拿 ID，再测详情/修改/删除
- 所有 `POST/PUT/DELETE` 接口优先在 Swagger 中带 token 测试
- 测试预约、归还、续借等状态流转接口时，注意先确认图书库存、借阅状态、预约状态
- 如果接口返回 `1001`，先检查 token 是否缺失或过期

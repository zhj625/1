# 公共JS模块说明

## 文件位置
`js/common.js`

## 功能说明
该模块抽取了 `admin.html` 和 `user.html` 中的公共代码,包括:

### 1. 全局配置
- `API_BASE_URL` - 后端API地址
- `PLACEHOLDER_COVER` - 图书封面占位图
- `PLACEHOLDER_BANNER` - Banner占位图
- `PLACEHOLDER_AVATAR` - 用户头像占位图

### 2. 工具函数
- `handleImgError(e, type)` - 图片加载失败处理
- `formatTime(dateStr)` - 时间格式化 (刚刚/X分钟前/X小时前/X天前)

### 3. HTTP客户端工厂
- `createHttpClient(tokenKey, userInfoKey)` - 创建HTTP客户端
  - 自动处理Token认证
  - 自动处理401登录过期
  - 统一错误处理
  - 支持 GET/POST/PUT/DELETE/uploadFile

## 使用方法

### 在HTML中引入
```html
<script src="./js/common.js"></script>
```

### 在JavaScript中使用
```javascript
// 解构导入需要的功能
const { API_BASE_URL, PLACEHOLDER_COVER, handleImgError, formatTime, createHttpClient } = window.LibraryCommon;

// 创建HTTP客户端 (管理后台)
const http = createHttpClient('admin_token_pro', 'admin_info_pro');

// 创建HTTP客户端 (用户前台)
const http = createHttpClient('user_token_pro', 'user_info_pro');

// 使用HTTP客户端
const data = await http.get('/books', { page: 1, size: 10 });
await http.post('/auth/login', { username, password });
await http.uploadFile('/upload/image', file);

// 使用工具函数
const timeStr = formatTime('2026-01-13T10:30:00');
```

## 重构效果

### 代码减少统计
- **admin.html**: 减少 ~94 行重复代码
- **user.html**: 减少 ~82 行重复代码
- **总计**: 减少 ~176 行重复代码

### 维护性提升
- ✅ 统一的HTTP请求处理逻辑
- ✅ 统一的错误处理
- ✅ 统一的工具函数
- ✅ 修改一处,两个页面同步更新
- ✅ 更容易添加新功能 (如请求拦截器、日志等)

## 注意事项
1. `common.js` 必须在 Vue 等框架库之后引入
2. 所有导出的功能都挂载在 `window.LibraryCommon` 对象上
3. HTTP客户端会自动处理Token过期,无需手动处理

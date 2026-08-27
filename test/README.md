# 测试说明

本目录集中保存智慧图书馆管理系统的接口自动化测试、测试用例和执行报告。

## 目录结构

```text
test/
├── README.md
├── postman/
│   ├── smart-library-main.json
│   ├── smart-library-regression.json
│   └── smart-library-cleanup.json
├── testcases/
│   └── testcase.xlsx
└── reports/
    └── test-report.md
```

## Collection 说明

- `smart-library-main.json`：48 个请求组成的完整接口测试，覆盖认证、权限、管理查询和核心业务闭环。
- `smart-library-regression.json`：11 个非破坏性快速回归请求，用于检查关键接口契约。
- `smart-library-cleanup.json`：删除测试收藏、评论并验证图书和分类的引用保护；执行前必须填写本轮测试产生的精确 ID。

## Postman 运行

1. 启动 MySQL 和 Spring Boot 后端。
2. 将所需 JSON 文件导入 Postman。
3. 在 Collection Variables 中填写密码变量；仓库不保存明文密码。
4. 运行整个 Collection。

## Newman 运行

安装 Node.js 后，可使用 Newman 执行主集合：

```powershell
npx -y newman run test\postman\smart-library-main.json
```

若要生成本地 JSON 报告：

```powershell
npx -y newman run test\postman\smart-library-main.json `
  --reporters cli,json `
  --reporter-json-export test\reports\newman-report.json
```

`test/reports/*.json` 已被 Git 忽略，不会将可重复生成的大型报告提交到仓库。

## 当前验证结果

- Postman/Newman：48 个请求，50 个断言，失败 0。
- 快速回归：11 个请求，11 个断言，失败 0。
- Maven 单元测试：33 条，失败 0，错误 0，跳过 0。

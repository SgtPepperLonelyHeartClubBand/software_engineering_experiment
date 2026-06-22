# 校园二手集市

本项目是一个面向校内学生的校园二手物品交易 Web 平台，用于支持闲置物品发布、浏览搜索、商品详情查看、收藏、私信沟通、商品预定、订单状态流转和系统通知等核心流程。

项目采用前后端分离架构：前端负责移动端风格的交互页面，后端提供 REST API、认证、商品、交易、私信和通知等业务能力。默认开发环境使用 H2 内存数据库，便于本地快速启动和课程演示；如需连接 MySQL，可使用后端的 `mysql` profile。

## 功能概览

- 用户登录：使用一卡通号和验证码登录，开发演示验证码为 `123456`。
- 资料完善：首次登录后填写昵称、宿舍地点和微信号。
- 商品浏览：支持商品列表、分类筛选、关键词搜索和详情查看。
- 商品发布：支持上传图片、填写标题、分类、成色、价格、地点和描述。
- 收藏与私信：买家可以收藏商品，也可以围绕商品与卖家建立私信会话。
- 预定交易：买家预定商品后，商品状态变为“被预定”，系统创建订单和会话。
- 状态流转：支持取消预定、卖家确认完成交易。
- 系统通知：预定、取消、完成等事件会生成系统通知。
- 测试验证：包含后端接口测试、异常场景测试、并发预定测试和性能压测脚本。

## 技术栈

前端：

- Vue 3
- Vite
- Vant 4
- Tailwind CSS
- Vue Router

后端：

- Spring Boot
- Spring Data JPA
- Spring Security
- JWT
- H2 / MySQL
- Maven

## 目录结构

```text
software_engineering_experiment
├── backend                 # Spring Boot 后端项目
│   ├── src/main/java       # 后端业务代码
│   ├── src/main/resources  # 配置文件与数据库结构说明
│   └── src/test/java       # 后端自动化测试
├── frontend                # Vue/Vite 前端项目
│   ├── src/components      # 页面组件
│   ├── src/api             # 前端 API 请求封装
│   └── src/stores          # 消息等前端状态逻辑
├── docs                    # 用户手册、测试报告、设计文档和报告素材
└── scripts                 # 压测脚本与报告资源生成脚本
```

## 本地运行

### 1. 启动后端

进入后端目录：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认运行在：

```text
http://localhost:8080
```

默认配置使用 H2 内存数据库，启动时会初始化地点树和开发演示用户数据。

### 2. 启动前端

进入前端目录并安装依赖：

```powershell
cd frontend
npm ci
```

启动开发服务器：

```powershell
npm run dev
```

前端默认运行在：

```text
http://localhost:5173
```

前端的 `/api` 和 `/uploads` 请求会通过 Vite 代理转发到后端 `http://localhost:8080`。

### 3. 登录演示

打开前端页面后：

1. 输入 9 位一卡通号。
2. 点击“获取验证码”。
3. 输入开发演示验证码 `123456`。
4. 首次登录会进入资料完善页，填写后进入集市首页。

## MySQL 配置

默认开发环境不需要手动创建数据库。如果需要使用 MySQL，可参考：

```text
backend/src/main/resources/application-mysql.yml
backend/src/main/resources/db/schema.sql
```

启动时可指定 `mysql` profile，并根据本机数据库修改用户名、密码和数据库地址。

## 测试与构建

后端自动化测试：

```powershell
cd backend
.\mvnw.cmd test
```

前端生产构建：

```powershell
cd frontend
npm run build
```

性能压测脚本：

```powershell
node scripts/qa-load-test.js
```

压测前需要先启动后端服务。脚本主要覆盖商品列表、搜索、详情和并发预定接口。

## 文档

主要文档位于 `docs/`：

- `USER_MANUAL_AND_MAINTENANCE.md`：用户手册与维护说明。
- `QA_TEST_PLAN.md`：测试计划。
- `QA_TEST_REPORT.md`：测试报告。
- `BUYER_DESIGN_DOC.md`：买家端设计文档。
- `BACKEND_A_FINAL_REPORT.md`：后端基础能力报告章节。
- `BACKEND_B_FINAL_REPORT_PRINT.md`：交易、消息、通知能力报告章节。

## 当前状态

当前版本已经完成校园二手交易的核心闭环，适合课程演示和基础验收。后续可以继续扩展管理员后台、举报审核、预定超时自动释放、浏览器端 E2E 测试和生产环境部署监控。

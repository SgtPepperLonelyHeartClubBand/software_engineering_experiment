# 后端 A API 测试清单

本文档依据 `docs/BACKEND_A_DEVELOPMENT_SPEC.md` 编写，用于验收后端 A 的认证、用户资料、地点树、商品 CRUD 和图片上传能力。

后端默认地址：`http://localhost:8080`  
前端代理地址：`http://localhost:5173/api/...`

统一响应格式：

```json
{ "code": 0, "message": "ok", "data": ... }
```

正式认证请求头：

```http
Authorization: Bearer <token>
```

开发期认证请求头：

```http
X-Dev-User-Id: 1
```

`X-Dev-User-Id` 只在 `app.security.dev-auth-enabled=true` 时生效；`mysql` profile 默认关闭该能力。

## 1. 自动化测试

| # | 命令 | 期望 |
|---|---|---|
| 1.1 | `cd backend && .\mvnw.cmd test` | 所有 MockMvc 测试通过 |
| 1.2 | `cd frontend && npm run build` | 前端生产构建通过 |

自动化测试至少覆盖：

- 未登录访问受保护 API 返回 401。
- JWT 可访问受保护 API。
- 开发期开关开启时 `X-Dev-User-Id` 可访问受保护 API。
- 开发期开关关闭时 `X-Dev-User-Id` 不生效。
- 地点树、用户资料、商品 CRUD、权限控制、图片上传均可用。

## 2. 健康检查

| # | 方法 | 路径 | 鉴权 | 验证点 | 通过 |
|---|---|---|---|---|---|
| 2.1 | GET | `/api/health` | 否 | `code=0`，`data="backend is running"` | [ ] |
| 2.2 | GET | `http://localhost:5173/api/health` | 否 | Vite 代理能转发到后端 | [ ] |

## 3. 认证 Auth

| # | 方法 | 路径 | Body | 验证点 | 通过 |
|---|---|---|---|---|---|
| 3.1 | POST | `/api/auth/send-code` | `{ "studentId": "220000002" }` | 返回 `code=0` | [ ] |
| 3.2 | POST | `/api/auth/login` | `{ "studentId": "220000002", "verifyCode": "123456" }` | 返回 `token`、`userId`、`isNewUser` | [ ] |
| 3.3 | POST | `/api/auth/login` | `{ "studentId": "220000002", "verifyCode": "000000" }` | 返回业务错误 `code=400` | [ ] |
| 3.4 | GET | `/api/users/me` | 无认证头 | HTTP 401，body 中 `code=401` | [ ] |
| 3.5 | GET | `/api/users/me` | `Authorization: Bearer <token>` | 可识别当前用户 | [ ] |
| 3.6 | GET | `/api/users/me` | `X-Dev-User-Id: 1` | 开发期开启时可识别当前用户 | [ ] |

## 4. 用户资料与地点树

| # | 方法 | 路径 | 请求 | 验证点 | 通过 |
|---|---|---|---|---|---|
| 4.1 | GET | `/api/locations/tree` | 登录态 | 返回校区/园区/楼栋三级结构，叶子节点无空 `children` | [ ] |
| 4.2 | GET | `/api/users/me` | 登录态 | 返回一卡通号、邮箱、昵称、头像、地点、资料完成状态 | [ ] |
| 4.3 | PUT | `/api/users/me` | `{ "nickname": "后端A", "wechat": "backend_a", "locationCode": "JLH-MY-02" }` | `isProfileComplete=true`，未传头像时不清空原头像 | [ ] |
| 4.4 | PUT | `/api/users/me` | 缺少 `locationCode` | 返回参数错误 `code=400` | [ ] |

## 5. 商品与权限

| # | 方法 | 路径 | 请求 | 验证点 | 通过 |
|---|---|---|---|---|---|
| 5.1 | POST | `/api/items` | 未完善资料用户 | 返回 `code=403` | [ ] |
| 5.2 | POST | `/api/items` | 完整商品请求 | 发布成功，返回商品详情和 `id` | [ ] |
| 5.3 | GET | `/api/items?category=专业书籍&keyword=数据库` | 登录态 | 支持分类和关键词筛选，默认只返回 `在售` 且未删除商品 | [ ] |
| 5.4 | GET | `/api/items/{id}` | 登录态 | 返回多图、卖家、地点、浏览量；浏览量加 1 | [ ] |
| 5.5 | GET | `/api/items/mine` | 登录态 | 返回当前用户发布的未删除商品 | [ ] |
| 5.6 | PUT | `/api/items/{id}` | 发布者 | 修改成功 | [ ] |
| 5.7 | PUT | `/api/items/{id}` | 非发布者 | 返回 `code=403` | [ ] |
| 5.8 | DELETE | `/api/items/{id}` | 非发布者 | 返回 `code=403` | [ ] |
| 5.9 | DELETE | `/api/items/{id}` | 发布者 | 软删除成功 | [ ] |
| 5.10 | GET | `/api/items/{id}` | 已删除商品 | 返回 `code=404` | [ ] |

商品发布 Body 示例：

```json
{
  "title": "数据库原理教材",
  "category": "专业书籍",
  "condition": "9成新",
  "price": 25.00,
  "locationCode": "JLH-MY-01",
  "description": "课程用书，少量划线",
  "imageUrls": ["/uploads/db-book.png"]
}
```

## 6. 图片上传

| # | 方法 | 路径 | 请求 | 验证点 | 通过 |
|---|---|---|---|---|---|
| 6.1 | POST | `/api/upload/image` | multipart 字段 `file`，图片文件 | 返回 `/uploads/...` URL | [ ] |
| 6.2 | POST | `/api/upload/image` | 文本文件 | 返回业务错误 `code=400` | [ ] |
| 6.3 | POST | `/api/upload/image` | 非允许扩展名 | 返回业务错误 `code=400` | [ ] |
| 6.4 | GET | `/uploads/{filename}` | 无认证 | 可直接访问静态资源 | [ ] |

## 7. 后端 B 占位接口

以下接口不属于后端 A，本阶段不验收：

| 功能 | 说明 |
|---|---|
| 商品预定/取消预定 | 后端 B 负责订单状态与并发锁定 |
| 收藏/取消收藏 | 后端 B 负责 |
| 私信会话/消息 | 后端 B 负责 |
| 系统通知 | 后端 B 负责 |

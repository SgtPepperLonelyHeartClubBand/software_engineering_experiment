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

## 7. 后端 B 交易闭环接口

后端 B 已补齐交易状态、收藏、私信和通知能力，仍沿用统一响应格式与登录态。

| # | 方法 | 路径 | 请求 | 验证点 | 通过 |
|---|---|---|---|---|---|
| 7.1 | POST | `/api/items/{id}/reserve` | 买家登录态 | 商品从 `在售` 变为 `被预定`，返回 `orderId`、`conversationId`，卖家收到通知 | [ ] |
| 7.2 | POST | `/api/items/{id}/reserve` | 卖家本人 | 返回 `code=400`，不能预定自己发布的商品 | [ ] |
| 7.3 | POST | `/api/items/{id}/reserve` | 第二个买家 | 已被预定商品返回 `code=400`，避免超卖 | [ ] |
| 7.4 | POST | `/api/orders/{id}/cancel` | 买家或卖家 | 订单变 `CANCELLED`，商品恢复 `在售` | [ ] |
| 7.5 | POST | `/api/orders/{id}/complete` | 卖家 | 订单变 `COMPLETED`，商品变 `已完成`，买家收到通知 | [ ] |
| 7.6 | POST | `/api/orders/{id}/complete` | 买家 | 返回 `code=403`，买家不能确认完成 | [ ] |
| 7.7 | GET | `/api/orders/reserved` | 买家登录态 | 返回“我的预定”商品列表 | [ ] |
| 7.8 | GET | `/api/orders/bought` | 买家登录态 | 返回“我买到的”商品列表 | [ ] |

## 8. 后端 B 收藏、私信与通知

| # | 方法 | 路径 | 请求 | 验证点 | 通过 |
|---|---|---|---|---|---|
| 8.1 | POST | `/api/items/{id}/favorite` | 买家登录态 | 收藏成功，商品详情 `favorited=true` | [ ] |
| 8.2 | DELETE | `/api/items/{id}/favorite` | 买家登录态 | 取消收藏成功 | [ ] |
| 8.3 | GET | `/api/favorites` | 买家登录态 | 返回“我的收藏”商品列表 | [ ] |
| 8.4 | POST | `/api/conversations` | `{ "itemId": 1 }` | 创建或复用买家与卖家的商品会话 | [ ] |
| 8.5 | GET | `/api/conversations` | 登录态 | 私信列表按未读优先展示，包含关联商品信息 | [ ] |
| 8.6 | POST | `/api/conversations/{id}/messages` | `{ "content": "还在吗" }` | 发送消息，接收方未读数加 1 | [ ] |
| 8.7 | POST | `/api/conversations/{id}/read` | 登录态 | 当前用户该会话未读数清零 | [ ] |
| 8.8 | POST | `/api/messages/{id}/recall` | 发送者 | 2 分钟内可撤回，非发送者返回 `code=403` | [ ] |
| 8.9 | GET | `/api/notifications` | 登录态 | 返回系统通知列表 | [ ] |
| 8.10 | POST | `/api/notifications/{id}/read` | 登录态 | 单条通知已读 | [ ] |
| 8.11 | POST | `/api/notifications/read-all` | 登录态 | 当前用户通知全部已读 | [ ] |
| 8.12 | GET | `/api/messages/unread-summary` | 登录态 | 返回私信、通知和总未读数 | [ ] |

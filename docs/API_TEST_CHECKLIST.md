# API 测试清单

后端默认：`http://localhost:8080`  
前端代理：`http://localhost:5173/api/...`（需 `npm run dev` + Vite proxy）

统一响应格式：

```json
{ "code": 0, "message": "ok", "data": ... }
```

---

## 阶段 0 / 已有接口

| # | 方法 | 路径 | 鉴权 | 验证方式 | 通过 |
|---|------|------|------|----------|------|
| 0.1 | GET | `/api/health` | 否 | 浏览器直接访问 | [ ] |
| 0.2 | GET | `/api/users/me` | 开发期 Header | `X-Dev-User-Id: 1` 或前端 `getCurrentUser()` | [ ] |
| 0.3 | GET | `http://localhost:5173/api/health` | 否 | 前端代理连通 | [ ] |
| 0.4 | GET | `http://localhost:5173/api/users/me` | 开发期 | 浏览器 / 控制台调 `user.js` | [ ] |

### 浏览器控制台快速测（在前端页面 F12）

```js
import { checkHealth, getCurrentUser } from './src/api/user.js' // 需在模块环境
// 或：
fetch('/api/health').then(r => r.json()).then(console.log)
fetch('/api/users/me', { headers: { 'X-Dev-User-Id': '1' } }).then(r => r.json()).then(console.log)
```

---

## 模块 1：登录 Auth（后端 A 已完成）

| # | 方法 | 路径 | Body 示例 | 通过 |
|---|------|------|-----------|------|
| 1.1 | POST | `/api/auth/send-code` | `{ "studentId": "220000002" }` | [ ] |
| 1.2 | POST | `/api/auth/login` | `{ "studentId": "220000002", "verifyCode": "123456" }` | [ ] |

---

## 模块 2：资料 + 宿舍树（后端 A 已完成）

| # | 方法 | 路径 | Body / Header 示例 | 通过 |
|---|------|------|--------------------|------|
| 2.1 | GET | `/api/locations/tree` | `X-Dev-User-Id: 1` | [ ] |
| 2.2 | PUT | `/api/users/me` | `{ "nickname": "后端A", "wechat": "backend_a", "locationCode": "JLH-MY-02" }` | [ ] |

---

## 模块 3：商品与上传（后端 A 已完成）

| # | 方法 | 路径 | Body / 参数示例 | 通过 |
|---|------|------|------------------|------|
| 3.1 | GET | `/api/items?category=专业书籍&keyword=数据库` | `X-Dev-User-Id: 1` | [ ] |
| 3.2 | GET | `/api/items/{id}` | `X-Dev-User-Id: 1` | [ ] |
| 3.3 | POST | `/api/items` | `title, category, condition, price, locationCode, description, imageUrls` | [ ] |
| 3.4 | PUT | `/api/items/{id}` | 仅发布者可修改 | [ ] |
| 3.5 | DELETE | `/api/items/{id}` | 仅发布者可下架/软删除 | [ ] |
| 3.6 | POST | `/api/upload/image` | multipart 字段 `file` | [ ] |

---

## 模块 4：预定（后端 B 待接入）

| # | 方法 | 路径 | 通过 |
|---|------|------|------|
| 4.1 | POST | `/api/items/{id}/reserve` | [ ] |

---

## 模块 5：消息（后端 B 待接入）

| # | 方法 | 路径 | 通过 |
|---|------|------|------|
| 5.1 | GET | `/api/conversations` | [ ] |
| 5.2 | GET | `/api/conversations/{id}/messages` | [ ] |
| 5.3 | POST | `/api/conversations/{id}/messages` | [ ] |
| 5.4 | POST | `/api/conversations/{id}/read` | [ ] |
| 5.5 | POST | `/api/messages/{id}/recall` | [ ] |

---

## 模块 6：通知 + 收藏（后端 B 待接入）

| # | 方法 | 路径 | 通过 |
|---|------|------|------|
| 6.1 | GET | `/api/notifications` | [ ] |
| 6.2 | POST | `/api/notifications/read-all` | [ ] |
| 6.3 | POST | `/api/items/{id}/favorite` | [ ] |
| 6.4 | DELETE | `/api/items/{id}/favorite` | [ ] |

---

## Postman 环境变量建议

| 变量 | 值 |
|------|-----|
| `baseUrl` | `http://localhost:8080` |
| `token` | 登录后填入 |
| `devUserId` | `1`（开发期） |

登录前请求头（开发期）：

```
X-Dev-User-Id: 1
```

登录后请求头：

```
Authorization: Bearer {{token}}
```

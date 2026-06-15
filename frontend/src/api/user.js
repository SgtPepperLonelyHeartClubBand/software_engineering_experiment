import { get } from './request'

/** 健康检查 */
export function checkHealth() {
  return get('/health', { skipAuth: true })
}

/** 当前用户资料（开发期默认 userId=1，或由 X-Dev-User-Id / token 决定） */
export function getCurrentUser() {
  return get('/users/me')
}

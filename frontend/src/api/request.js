const TOKEN_KEY = 'market_token'
const DEV_USER_ID_KEY = 'market_dev_user_id'

const BASE_URL = '/api'

export class ApiError extends Error {
  constructor(code, message) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getDevUserId() {
  return localStorage.getItem(DEV_USER_ID_KEY)
}

export function setDevUserId(userId) {
  if (userId) {
    localStorage.setItem(DEV_USER_ID_KEY, String(userId))
  } else {
    localStorage.removeItem(DEV_USER_ID_KEY)
  }
}

export function clearDevUserId() {
  localStorage.removeItem(DEV_USER_ID_KEY)
}

export function clearAuth() {
  clearToken()
  clearDevUserId()
}

/**
 * 统一请求封装：走 Vite 代理 /api -> http://localhost:8080
 * @param {string} path 例如 '/users/me'（不要重复加 /api 前缀）
 */
export async function request(path, options = {}) {
  const {
    method = 'GET',
    body,
    headers = {},
    skipAuth = false,
    useDevUserId = true
  } = options

  const finalHeaders = {
    Accept: 'application/json',
    ...headers
  }

  if (body !== undefined && !(body instanceof FormData)) {
    finalHeaders['Content-Type'] = 'application/json'
  }

  if (!skipAuth) {
    const token = getToken()
    if (token) {
      finalHeaders.Authorization = `Bearer ${token}`
    } else if (useDevUserId) {
      const devUserId = getDevUserId() || '1'
      finalHeaders['X-Dev-User-Id'] = devUserId
    }
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: finalHeaders,
    body: body instanceof FormData ? body : body !== undefined ? JSON.stringify(body) : undefined
  })

  let payload
  try {
    payload = await response.json()
  } catch {
    throw new ApiError(response.status, `请求失败 (${response.status})`)
  }

  if (!response.ok) {
    throw new ApiError(
      payload?.code ?? response.status,
      payload?.message ?? `请求失败 (${response.status})`
    )
  }

  if (payload.code !== 0) {
    throw new ApiError(payload.code, payload.message || '业务处理失败')
  }

  return payload.data
}

export function get(path, options) {
  return request(path, { ...options, method: 'GET' })
}

export function post(path, body, options) {
  return request(path, { ...options, method: 'POST', body })
}

export function put(path, body, options) {
  return request(path, { ...options, method: 'PUT', body })
}

export function del(path, options) {
  return request(path, { ...options, method: 'DELETE' })
}

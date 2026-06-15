import { post } from './request'

export function sendCode(studentId) {
  return post('/auth/send-code', { studentId }, { skipAuth: true })
}

export function login(studentId, verifyCode) {
  return post('/auth/login', { studentId, verifyCode }, { skipAuth: true })
}

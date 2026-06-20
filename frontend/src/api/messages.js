import { del, get, post } from './request'

export function ensureConversation(itemId) {
  return post('/conversations', { itemId })
}

export function listConversations() {
  return get('/conversations')
}

export function deleteConversation(id) {
  return del(`/conversations/${id}`)
}

export function listMessages(conversationId) {
  return get(`/conversations/${conversationId}/messages`)
}

export function sendConversationMessage(conversationId, payload) {
  return post(`/conversations/${conversationId}/messages`, payload)
}

export function markConversationRead(conversationId) {
  return post(`/conversations/${conversationId}/read`)
}

export function recallMessage(messageId) {
  return post(`/messages/${messageId}/recall`)
}

export function getUnreadSummary() {
  return get('/messages/unread-summary')
}

export function listNotifications() {
  return get('/notifications')
}

export function markNotificationRead(id) {
  return post(`/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return post('/notifications/read-all')
}

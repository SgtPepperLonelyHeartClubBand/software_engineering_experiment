import { computed, reactive, ref } from 'vue'
import {
  deleteConversation as apiDeleteConversation,
  ensureConversation as apiEnsureConversation,
  getUnreadSummary,
  listConversations,
  listMessages,
  listNotifications,
  markAllNotificationsRead,
  markConversationRead,
  markNotificationRead,
  recallMessage as apiRecallMessage,
  sendConversationMessage
} from '@/api/messages'

const RECALL_WINDOW_MS = 2 * 60 * 1000

export const chats = ref([])
export const notices = ref([])
export const chatMeta = reactive({})
export const chatMessages = reactive({})

const summary = reactive({
  chatUnread: 0,
  notificationUnread: 0
})

export const totalChatUnread = computed(() =>
  chats.value.length
    ? chats.value.reduce((sum, chat) => sum + (chat.unread || 0), 0)
    : summary.chatUnread
)

export const totalNoticeUnread = computed(() =>
  notices.value.length
    ? notices.value.filter((n) => n.unread).length
    : summary.notificationUnread
)

export const totalUnread = computed(() =>
  totalChatUnread.value + totalNoticeUnread.value
)

export const unreadChatsCount = computed(() =>
  chats.value.filter((c) => c.unread > 0).length
)

export function formatBadge(count) {
  if (!count || count <= 0) return undefined
  return count > 99 ? '99+' : count
}

export async function refreshUnreadSummary() {
  const data = await getUnreadSummary()
  summary.chatUnread = data.chatUnread || 0
  summary.notificationUnread = data.notificationUnread || 0
}

export async function refreshChats() {
  chats.value = await listConversations()
  syncSummaryFromLoadedData()
  chats.value.forEach(cacheChatMeta)
  return chats.value
}

export async function refreshNotices() {
  notices.value = await listNotifications()
  syncSummaryFromLoadedData()
  return notices.value
}

export async function ensureChatForItem(itemId) {
  const chat = await apiEnsureConversation(itemId)
  upsertChat(chat)
  cacheChatMeta(chat)
  syncSummaryFromLoadedData()
  return chat
}

export function getChatMeta(chatId) {
  return chatMeta[chatId] || {
    id: Number(chatId),
    name: '私信',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemId: chatId,
    itemTitle: '关联商品',
    itemPrice: '--',
    itemImage: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  }
}

export function getMessages(chatId) {
  if (!chatMessages[chatId]) {
    chatMessages[chatId] = []
  }
  return chatMessages[chatId]
}

export async function loadMessages(chatId) {
  chatMessages[chatId] = await listMessages(chatId)
  return chatMessages[chatId]
}

export async function markChatAsRead(chatId) {
  await markConversationRead(chatId)
  const chat = chats.value.find((c) => c.id === Number(chatId))
  if (chat) chat.unread = 0
  syncSummaryFromLoadedData()
}

export async function markAllChatsAsRead() {
  const unreadChats = chats.value.filter((c) => c.unread > 0)
  for (const chat of unreadChats) {
    await markConversationRead(chat.id)
    chat.unread = 0
  }
  syncSummaryFromLoadedData()
}

export async function markNoticesAsRead() {
  if (!notices.value.some((n) => n.unread)) return
  await markAllNotificationsRead()
  notices.value.forEach((n) => { n.unread = false })
  syncSummaryFromLoadedData()
}

export async function markNoticeAsRead(noticeId) {
  await markNotificationRead(noticeId)
  const notice = notices.value.find((n) => n.id === noticeId)
  if (notice) notice.unread = false
  syncSummaryFromLoadedData()
}

export async function deleteChat(chatId) {
  await apiDeleteConversation(chatId)
  chats.value = chats.value.filter((c) => c.id !== chatId)
  delete chatMessages[chatId]
  delete chatMeta[chatId]
  syncSummaryFromLoadedData()
}

export function canRecall(msg) {
  if (!msg?.isSelf || msg.recalled) return false
  const timestamp = new Date(msg.timestamp).getTime()
  return Number.isFinite(timestamp) && Date.now() - timestamp <= RECALL_WINDOW_MS
}

export async function recallMessage(chatId, msgId) {
  const recalled = await apiRecallMessage(msgId)
  const list = getMessages(chatId)
  const index = list.findIndex((m) => m.id === msgId)
  if (index >= 0) {
    list[index] = recalled
  }
  const chat = chats.value.find((c) => c.id === Number(chatId))
  if (chat) chat.lastMessage = '一条消息被撤回'
  return recalled
}

export async function sendMessage(chatId, content, quote = null) {
  const payload = {
    content,
    quoteMessageId: quote?.id
  }
  const msg = await sendConversationMessage(chatId, payload)
  getMessages(chatId).push(msg)
  const chat = chats.value.find((c) => c.id === Number(chatId))
  if (chat) {
    chat.lastMessage = content
    chat.time = new Date().toISOString()
  }
  return msg
}

export function getRecallText(msg) {
  return msg.isSelf ? '你撤回了一条消息' : '对方撤回了一条消息'
}

function upsertChat(chat) {
  const index = chats.value.findIndex((item) => item.id === chat.id)
  if (index >= 0) {
    chats.value[index] = chat
  } else {
    chats.value.unshift(chat)
  }
}

function cacheChatMeta(chat) {
  chatMeta[chat.id] = {
    id: chat.id,
    name: chat.name,
    avatar: chat.avatar,
    itemId: chat.itemId,
    itemTitle: chat.itemTitle,
    itemPrice: chat.itemPrice,
    itemImage: chat.itemImage
  }
}

function syncSummaryFromLoadedData() {
  if (chats.value.length) {
    summary.chatUnread = chats.value.reduce((sum, chat) => sum + (chat.unread || 0), 0)
  }
  if (notices.value.length) {
    summary.notificationUnread = notices.value.filter((n) => n.unread).length
  }
}

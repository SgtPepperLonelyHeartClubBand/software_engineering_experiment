import { computed, reactive, ref } from 'vue'

const RECALL_WINDOW_MS = 2 * 60 * 1000

export const chats = ref([])

export const notices = ref([])

export const chatMeta = reactive({})

export const chatMessages = reactive({})

export const totalChatUnread = computed(() =>
  chats.value.reduce((sum, chat) => sum + chat.unread, 0)
)

export const totalNoticeUnread = computed(() =>
  notices.value.filter((n) => n.unread).length
)

export const totalUnread = computed(() =>
  totalChatUnread.value + totalNoticeUnread.value
)

export function formatBadge(count) {
  if (!count || count <= 0) return undefined
  return count > 99 ? '99+' : count
}

export function getChatMeta(chatId) {
  return chatMeta[chatId] || {
    name: '私信',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemId: chatId,
    itemTitle: '私信功能待后端B接入',
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

export function markChatAsRead(chatId) {
  const chat = chats.value.find((c) => c.id === Number(chatId))
  if (chat) chat.unread = 0
}

export function markAllChatsAsRead() {
  chats.value.forEach((c) => { c.unread = 0 })
}

export const unreadChatsCount = computed(() =>
  chats.value.filter((c) => c.unread > 0).length
)

export function markNoticesAsRead() {
  notices.value.forEach((n) => { n.unread = false })
}

export function markNoticeAsRead(noticeId) {
  const notice = notices.value.find((n) => n.id === noticeId)
  if (notice) notice.unread = false
}

export function updateChatPreview(chatId, lastMessage) {
  const chat = chats.value.find((c) => c.id === Number(chatId))
  if (chat) {
    chat.lastMessage = lastMessage
    chat.time = '刚刚'
  }
}

export function deleteChat(chatId) {
  chats.value = chats.value.filter((c) => c.id !== chatId)
  delete chatMessages[chatId]
}

export function canRecall(msg) {
  if (!msg.isSelf || msg.recalled) return false
  return Date.now() - msg.timestamp <= RECALL_WINDOW_MS
}

export function recallMessage(chatId, msgId) {
  const list = getMessages(chatId)
  const msg = list.find((m) => m.id === msgId)
  if (!msg || !canRecall(msg)) return false

  msg.recalled = true
  msg.content = ''
  updateChatPreview(chatId, '你撤回了一条消息')
  return true
}

export function sendMessage(chatId, content, quote = null) {
  const list = getMessages(chatId)
  const msg = {
    id: Date.now(),
    content,
    isSelf: true,
    timestamp: Date.now(),
    recalled: false,
    quote: quote
      ? {
          id: quote.id,
          content: quote.content,
          isSelf: quote.isSelf,
          senderName: quote.isSelf ? '我' : getChatMeta(chatId).name
        }
      : null
  }
  list.push(msg)
  updateChatPreview(chatId, content)
  return msg
}

export function receiveMessage(chatId, content, { incrementUnread = true } = {}) {
  const list = getMessages(chatId)
  const msg = {
    id: Date.now(),
    content,
    isSelf: false,
    timestamp: Date.now(),
    recalled: false,
    quote: null
  }
  list.push(msg)

  const chat = chats.value.find((c) => c.id === Number(chatId))
  if (chat) {
    chat.lastMessage = content
    chat.time = '刚刚'
    if (incrementUnread) chat.unread += 1
  }
  return msg
}

export function getRecallText(msg) {
  return msg.isSelf ? '你撤回了一条消息' : '对方撤回了一条消息'
}

import { computed, reactive, ref } from 'vue'

const RECALL_WINDOW_MS = 2 * 60 * 1000

export const chats = ref([
  {
    id: 1,
    name: 'JLH刘同学',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemTitle: '数据库概论',
    lastMessage: '好的，明天下午梅园1栋楼下见？',
    time: '14:20',
    unread: 2
  },
  {
    id: 2,
    name: '成贤院学姐',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemTitle: 'AirPods Pro',
    lastMessage: '左耳杂音不影响使用的话 320 可以出',
    time: '昨天',
    unread: 0
  },
  {
    id: 3,
    name: '梅园彭于晏',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemTitle: '桌面小风扇',
    lastMessage: '[图片]',
    time: '周二',
    unread: 1
  }
])

export const notices = ref([
  {
    id: 1,
    icon: 'passed',
    title: '预定成功提醒',
    content: '您已成功预定《数据库系统概论》，请尽快与卖家私信确认面交细节。',
    time: '今天 10:30',
    unread: true
  },
  {
    id: 2,
    icon: 'info-o',
    title: '交易安全提示',
    content: '请勿脱离平台进行转账，建议在校内公共区域面交，保护个人财物安全。',
    time: '5月26日',
    unread: false
  },
  {
    id: 3,
    icon: 'gift-o',
    title: '欢迎加入校园二手集市',
    content: '完善个人资料后可获得更高曝光，祝您交易愉快！',
    time: '5月25日',
    unread: false
  }
])

export const chatMeta = reactive({
  1: {
    name: 'JLH刘同学',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemId: 1,
    itemTitle: '99新《数据库系统概论》王珊版',
    itemPrice: '25.00',
    itemImage: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=200'
  },
  2: {
    name: '成贤院学姐',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemId: 2,
    itemTitle: '二手 AirPods Pro',
    itemPrice: '350.00',
    itemImage: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?q=80&w=200'
  },
  3: {
    name: '梅园彭于晏',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    itemId: 3,
    itemTitle: '全新桌面小风扇',
    itemPrice: '15.00',
    itemImage: 'https://images.unsplash.com/photo-1618220179428-22790b461013?q=80&w=200'
  }
})

export const chatMessages = reactive({
  1: [
    { id: 101, content: '你好，这本书还在吗？', isSelf: true, timestamp: Date.now() - 3600000, recalled: false, quote: null },
    { id: 102, content: '在的，9成新，笔记很少，25 出', isSelf: false, timestamp: Date.now() - 3500000, recalled: false, quote: null },
    { id: 103, content: '可以面交吗？我在梅园', isSelf: true, timestamp: Date.now() - 3400000, recalled: false, quote: null },
    { id: 104, content: '可以的，梅园1栋楼下方便', isSelf: false, timestamp: Date.now() - 3300000, recalled: false, quote: null },
    { id: 105, content: '好的，明天下午梅园1栋楼下见？', isSelf: false, timestamp: Date.now() - 60000, recalled: false, quote: null }
  ],
  2: [
    { id: 201, content: '耳机还能再便宜点吗？', isSelf: true, timestamp: Date.now() - 86400000, recalled: false, quote: null },
    { id: 202, content: '左耳杂音不影响使用的话 320 可以出', isSelf: false, timestamp: Date.now() - 86000000, recalled: false, quote: null }
  ],
  3: [
    { id: 301, content: '风扇还有吗', isSelf: true, timestamp: Date.now() - 172800000, recalled: false, quote: null },
    { id: 302, content: '[图片]', isSelf: false, timestamp: Date.now() - 172000000, recalled: false, quote: null }
  ]
})

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
  return chatMeta[chatId] || chatMeta[1]
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

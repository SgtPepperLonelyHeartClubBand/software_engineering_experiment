<template>
  <div class="min-h-screen bg-[#F7F8FA] flex flex-col">
    <van-nav-bar
      :title="chatInfo.name"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    >
      <template #right>
        <van-icon name="ellipsis" size="20" @click="showNavActions = true" />
      </template>
    </van-nav-bar>

    <!-- 关联商品条 -->
    <div
      @click="goToItem"
      class="mx-3 mt-2 px-3 py-2.5 bg-white rounded-xl flex items-center gap-3 shadow-sm active:scale-[0.99] transition-transform"
    >
      <img :src="chatInfo.itemImage" class="w-12 h-12 rounded-lg object-cover shrink-0" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-bold text-gray-800 truncate">{{ chatInfo.itemTitle }}</p>
        <p class="text-red-500 font-bold text-sm mt-0.5">￥{{ chatInfo.itemPrice }}</p>
      </div>
      <van-icon name="arrow" class="text-gray-400" />
    </div>

    <!-- 消息列表 -->
    <div ref="messageListRef" class="flex-1 overflow-y-auto px-4 py-4 space-y-4">
      <div v-for="msg in messages" :key="msg.id">
        <!-- 撤回提示 -->
        <div v-if="msg.recalled" class="flex justify-center">
          <span class="text-xs text-gray-400 bg-black/5 px-3 py-1 rounded-full">
            {{ getRecallText(msg) }}
          </span>
        </div>

        <!-- 正常消息 -->
        <div
          v-else
          :class="msg.isSelf ? 'flex justify-end' : 'flex justify-start'"
        >
          <div
            v-if="!msg.isSelf"
            class="flex items-end gap-2 max-w-[78%]"
            @touchstart="onTouchStart($event, msg)"
            @touchend="onTouchEnd"
            @touchmove="onTouchMove"
            @contextmenu.prevent="openMsgMenu(msg, $event)"
          >
            <img :src="chatInfo.avatar" class="w-8 h-8 rounded-full shrink-0" />
            <div class="bg-white px-3.5 py-2.5 rounded-2xl rounded-bl-sm shadow-sm select-none">
              <div v-if="msg.quote" class="mb-2 pl-2 border-l-2 border-[#005A3C]/40">
                <p class="text-[10px] text-[#005A3C] font-medium">{{ msg.quote.senderName }}</p>
                <p class="text-xs text-gray-500 line-clamp-2 mt-0.5">{{ msg.quote.content }}</p>
              </div>
              <p class="text-sm text-gray-800 leading-relaxed">{{ msg.content }}</p>
            </div>
          </div>

          <div
            v-else
            class="flex items-end gap-2 max-w-[78%] flex-row-reverse"
            @touchstart="onTouchStart($event, msg)"
            @touchend="onTouchEnd"
            @touchmove="onTouchMove"
            @contextmenu.prevent="openMsgMenu(msg, $event)"
          >
            <img :src="myAvatar" class="w-8 h-8 rounded-full shrink-0" />
            <div class="bg-[#005A3C] px-3.5 py-2.5 rounded-2xl rounded-br-sm shadow-sm select-none">
              <div v-if="msg.quote" class="mb-2 pl-2 border-l-2 border-white/40">
                <p class="text-[10px] text-white/80 font-medium">{{ msg.quote.senderName }}</p>
                <p class="text-xs text-white/70 line-clamp-2 mt-0.5">{{ msg.quote.content }}</p>
              </div>
              <p class="text-sm text-white leading-relaxed">{{ msg.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 引用回复条 -->
    <div
      v-if="quoteTarget"
      class="mx-3 mb-1 px-3 py-2 bg-white rounded-xl flex items-center gap-2 border-l-4 border-[#005A3C] shadow-sm"
    >
      <div class="flex-1 min-w-0">
        <p class="text-xs text-[#005A3C] font-medium">引用 {{ quoteTarget.isSelf ? '我' : chatInfo.name }}</p>
        <p class="text-sm text-gray-600 truncate">{{ quoteTarget.content }}</p>
      </div>
      <van-icon name="cross" class="text-gray-400 shrink-0" @click="quoteTarget = null" />
    </div>

    <!-- 输入栏 -->
    <div class="sticky bottom-0 bg-white border-t border-gray-100 px-3 py-2.5">
      <div class="flex items-end gap-2">
        <van-field
          v-model="inputText"
          rows="1"
          autosize
          type="textarea"
          maxlength="200"
          :placeholder="quoteTarget ? '回复引用消息...' : '输入消息...'"
          :border="false"
          class="flex-1 !bg-gray-50 !rounded-xl"
          @keyup.enter="handleSend"
        />
        <button
          @click="handleSend"
          :disabled="!inputText.trim()"
          class="shrink-0 px-4 py-2.5 bg-[#005A3C] text-white text-sm font-bold rounded-xl disabled:opacity-50 active:scale-95 transition-all"
        >
          发送
        </button>
      </div>
    </div>

    <!-- 消息操作菜单 -->
    <van-action-sheet
      v-model:show="showMsgMenu"
      :actions="msgActions"
      cancel-text="取消"
      close-on-click-action
      @select="onMsgAction"
    />

    <van-action-sheet
      v-model:show="showNavActions"
      :actions="navActions"
      cancel-text="取消"
      close-on-click-action
      @select="onNavAction"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import {
  canRecall,
  getChatMeta,
  getMessages,
  getRecallText,
  loadMessages,
  markChatAsRead,
  recallMessage,
  refreshChats,
  sendMessage as storeSendMessage
} from '../stores/messages'

const router = useRouter()
const route = useRoute()
const chatId = computed(() => Number(route.params.id))

const messageListRef = ref(null)
const inputText = ref('')
const quoteTarget = ref(null)
const showMsgMenu = ref(false)
const showNavActions = ref(false)
const activeMsg = ref(null)
const myAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

let longPressTimer = null
let touchMoved = false

const chatInfo = computed(() => getChatMeta(chatId.value))
const messages = computed(() => getMessages(chatId.value))

const navActions = [
  { name: '查看商品详情' },
  { name: '举报该用户' }
]

const msgActions = computed(() => {
  if (!activeMsg.value) return []
  const msg = activeMsg.value
  const actions = [
    { name: '引用', color: '#005A3C' },
    { name: '复制' }
  ]
  if (canRecall(msg)) {
    actions.unshift({ name: '撤回', color: '#ee0a24' })
  }
  return actions
})

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

onMounted(async () => {
  try {
    await refreshChats()
    await loadMessages(chatId.value)
    await markChatAsRead(chatId.value)
    await scrollToBottom()
  } catch (error) {
    showFailToast(error.message || '聊天记录加载失败')
  }
})

onUnmounted(() => {
  clearLongPressTimer()
})

const clearLongPressTimer = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

const onTouchStart = (event, msg) => {
  if (msg.recalled) return
  touchMoved = false
  clearLongPressTimer()
  longPressTimer = setTimeout(() => {
    if (!touchMoved) {
      openMsgMenu(msg)
      if (navigator.vibrate) navigator.vibrate(30)
    }
  }, 500)
}

const onTouchMove = () => {
  touchMoved = true
  clearLongPressTimer()
}

const onTouchEnd = () => {
  clearLongPressTimer()
}

const openMsgMenu = (msg, event) => {
  if (msg.recalled) return
  activeMsg.value = msg
  showMsgMenu.value = true
  if (event) event.preventDefault()
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text) return

  try {
    await storeSendMessage(chatId.value, text, quoteTarget.value)
    inputText.value = ''
    quoteTarget.value = null
    await scrollToBottom()
  } catch (error) {
    showFailToast(error.message || '发送失败')
  }
}

const onMsgAction = async (action) => {
  const msg = activeMsg.value
  if (!msg) return

  if (action.name === '撤回') {
    try {
      await recallMessage(chatId.value, msg.id)
      showSuccessToast('已撤回')
    } catch (error) {
      showToast(error.message || '超过 2 分钟，无法撤回')
    }
  } else if (action.name === '引用') {
    quoteTarget.value = { ...msg }
    showToast('已选择引用')
  } else if (action.name === '复制') {
    navigator.clipboard?.writeText(msg.content)
    showToast('已复制')
  }
  activeMsg.value = null
}

const goBack = () => router.back()
const goToItem = () => router.push(`/item/${chatInfo.value.itemId}`)

const onNavAction = (action) => {
  if (action.name === '查看商品详情') {
    goToItem()
  } else {
    showToast('已进入举报流程')
  }
}
</script>

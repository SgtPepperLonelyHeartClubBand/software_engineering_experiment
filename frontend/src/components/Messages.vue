<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    <van-nav-bar title="消息" fixed placeholder />

    <van-tabs
      v-model:active="activeTab"
      color="#005A3C"
      title-active-color="#005A3C"
      sticky
      offset-top="46"
      @change="onTabChange"
    >
      <van-tab>
        <template #title>
          <span class="relative inline-flex items-center">
            私信
            <span
              v-if="chatUnreadBadge"
              class="absolute -top-1.5 -right-3 min-w-[16px] h-4 px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center leading-none"
            >
              {{ chatUnreadBadge }}
            </span>
          </span>
        </template>

        <van-swipe-cell v-for="chat in chats" :key="chat.id">
          <div
            @click="goToChat(chat.id)"
            class="flex items-center gap-3 px-4 py-3.5 bg-white active:bg-gray-50 transition-colors border-b border-gray-50"
          >
            <div class="relative shrink-0">
              <img :src="chat.avatar" class="w-12 h-12 rounded-full object-cover" />
              <span
                v-if="chat.unread > 0"
                class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white"
              >
                {{ chat.unread > 99 ? '99+' : chat.unread }}
              </span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <span class="font-bold text-sm text-gray-800">{{ chat.name }}</span>
                <span class="text-xs text-gray-400 shrink-0 ml-2">{{ chat.time }}</span>
              </div>
              <div class="flex items-center gap-2">
                <van-tag v-if="chat.itemTitle" color="#005A3C" plain class="!text-[10px] shrink-0">
                  {{ chat.itemTitle }}
                </van-tag>
                <p
                  class="text-sm truncate flex-1"
                  :class="chat.unread > 0 ? 'text-gray-800 font-medium' : 'text-gray-500'"
                >
                  {{ chat.lastMessage }}
                </p>
              </div>
            </div>
          </div>
          <template #right>
            <van-button square type="danger" text="删除" class="!h-full" @click="handleDelete(chat.id)" />
          </template>
        </van-swipe-cell>

        <van-empty v-if="!chats.length" description="暂无私信，去集市逛逛吧" />
      </van-tab>

      <van-tab>
        <template #title>
          <span class="relative inline-flex items-center">
            系统通知
            <span
              v-if="noticeUnreadDot"
              class="absolute -top-0.5 -right-2.5 w-2 h-2 bg-red-500 rounded-full"
            />
          </span>
        </template>

        <div
          v-for="notice in notices"
          :key="notice.id"
          @click="readNotice(notice.id)"
          class="px-4 py-4 bg-white border-b border-gray-50 active:bg-gray-50 transition-colors"
        >
          <div class="flex items-start gap-3">
            <div class="relative shrink-0">
              <div class="w-10 h-10 rounded-full bg-[#005A3C]/10 flex items-center justify-center">
                <van-icon :name="notice.icon" color="#005A3C" size="20" />
              </div>
              <span
                v-if="notice.unread"
                class="absolute -top-0.5 -right-0.5 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-white"
              />
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between mb-1">
                <span class="font-bold text-sm" :class="notice.unread ? 'text-gray-900' : 'text-gray-800'">
                  {{ notice.title }}
                </span>
                <span class="text-xs text-gray-400">{{ notice.time }}</span>
              </div>
              <p class="text-sm text-gray-500 leading-relaxed">{{ notice.content }}</p>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>

    <AppTabbar />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppTabbar from './AppTabbar.vue'
import {
  chats,
  notices,
  deleteChat,
  formatBadge,
  markChatAsRead,
  markNoticesAsRead,
  markNoticeAsRead,
  totalChatUnread,
  totalNoticeUnread
} from '../stores/messages'

const router = useRouter()
const activeTab = ref(0)

const chatUnreadBadge = computed(() => formatBadge(totalChatUnread.value))
const noticeUnreadDot = computed(() => totalNoticeUnread.value > 0)

const goToChat = (id) => {
  markChatAsRead(id)
  router.push(`/chat/${id}`)
}

const readNotice = (id) => {
  markNoticeAsRead(id)
}

const onTabChange = (index) => {
  if (index === 1) {
    markNoticesAsRead()
  }
}

const handleDelete = (id) => {
  deleteChat(id)
  showToast('已删除会话')
}
</script>

<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px] safe-area-bottom">
    <!-- 1. 顶部导航栏 -->
    <van-nav-bar
      title="商品详情"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    >
      <template #right>
        <van-icon name="share-o" size="20" @click="handleShare" />
      </template>
    </van-nav-bar>

    <!-- 加载骨架 -->
    <div v-if="loading" class="p-4 space-y-4">
      <div class="h-80 bg-gray-200 rounded-xl animate-pulse"></div>
      <div class="bg-white rounded-xl p-4 space-y-3">
        <div class="h-6 bg-gray-200 rounded animate-pulse w-1/3"></div>
        <div class="h-5 bg-gray-200 rounded animate-pulse w-full"></div>
        <div class="h-4 bg-gray-200 rounded animate-pulse w-3/4"></div>
        <div class="h-16 bg-gray-200 rounded animate-pulse w-full"></div>
      </div>
    </div>

    <!-- 内容区 -->
    <template v-else>
      <!-- 2. 图片轮播（点击进入预览） -->
      <van-swipe
        class="h-80 bg-white"
        :autoplay="3000"
        indicator-color="#005A3C"
        :loop="true"
        @click="showImagePreview = true"
      >
        <van-swipe-item v-for="(img, index) in itemImages" :key="index">
          <div class="w-full h-full flex items-center justify-center bg-gray-100">
            <img :src="img" class="w-full h-full object-contain" />
          </div>
        </van-swipe-item>
      </van-swipe>

      <!-- 3. 商品核心信息 -->
      <div class="p-4 bg-white mt-2">
        <div class="flex justify-between items-center mb-2">
          <span class="text-2xl font-bold text-red-500">
            <span class="text-sm">￥</span>{{ itemData.price }}
          </span>
          <van-tag color="#005A3C" plain>{{ itemData.condition }}</van-tag>
        </div>
        <h1 class="text-lg font-bold text-gray-800 leading-snug">{{ itemData.title }}</h1>
        <p class="text-gray-600 mt-3 text-sm leading-relaxed whitespace-pre-wrap">
          {{ itemData.description }}
        </p>
        <div class="flex items-center gap-2 mt-3 text-xs text-gray-400">
          <span>浏览 {{ viewCount }}</span>
          <span>·</span>
          <span>{{ wantCount }} 人想要</span>
        </div>
      </div>

      <!-- 4. 卖家信息卡片 -->
      <div
        class="p-4 bg-white mt-2 flex items-center justify-between active:bg-gray-50 transition-colors"
        @click="goToChat"
      >
        <div class="flex items-center gap-3">
          <img :src="itemData.sellerAvatar" class="w-10 h-10 rounded-full border border-gray-100" />
          <div>
            <div class="font-bold text-sm text-gray-800">{{ itemData.sellerName }}</div>
            <div class="text-xs text-gray-400 mt-0.5">发布于 {{ itemData.location }}</div>
          </div>
        </div>
        <van-icon name="arrow" class="text-gray-400" />
      </div>

      <!-- 猜你喜欢 -->
      <div class="mt-2 bg-white p-4">
        <h3 class="text-sm font-bold text-gray-800 mb-3">猜你喜欢</h3>
        <div class="grid grid-cols-2 gap-2">
          <div
            v-for="rec in recommendList"
            :key="rec.id"
            @click="goToDetail(rec.id)"
            class="bg-gray-50 rounded-xl overflow-hidden active:scale-[0.98] transition-transform cursor-pointer"
          >
            <div class="w-full h-28 bg-gray-100 flex items-center justify-center">
              <img :src="rec.image" class="w-full h-full object-contain" loading="lazy" />
            </div>
            <div class="p-2">
              <p class="text-xs text-gray-800 line-clamp-1">{{ rec.title }}</p>
              <span class="text-red-500 font-bold text-xs">￥{{ rec.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 5. 底部固定操作栏 -->
    <van-action-bar>
      <van-action-bar-icon icon="chat-o" text="私信留言" @click="goToChat" />
      <van-action-bar-icon :icon="isFav ? 'star' : 'star-o'" text="收藏" :color="isFav ? '#005A3C' : ''" @click="toggleFav" />
      <van-action-bar-button
        type="danger"
        color="#005A3C"
        :text="reserveButtonText"
        :disabled="!canReserve"
        @click="showConfirm = true"
      />
    </van-action-bar>

    <!-- 6. 图片预览 -->
    <van-image-preview v-model:show="showImagePreview" :images="itemImages" :closeable="true" :show-indicator="true" />  

    <!-- 7. 预定确认 -->
    <van-action-sheet v-model:show="showConfirm" title="确认预定此商品？">
      <div class="p-6 text-center">
        <p class="text-gray-600 mb-6 leading-relaxed">
          预定后商品会锁定为“被预定”，并自动进入与卖家的私信会话。
        </p>
        <button
          @click="confirmLock"
          class="w-full py-3.5 bg-[#005A3C] text-white font-bold rounded-xl active:scale-[0.98] transition-transform shadow-lg shadow-[#005A3C]/30"
        >
          确认预定
        </button>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { favoriteItem, unfavoriteItem } from '@/api/favorites'
import { getItemDetail, listItems } from '@/api/items'
import { reserveItem } from '@/api/trade'
import { ensureChatForItem } from '@/stores/messages'

const router = useRouter()
const route = useRoute()
const showConfirm = ref(false)
const showImagePreview = ref(false)
const isFav = ref(false)
const loading = ref(true)
const viewCount = ref(0)
const wantCount = ref(0)

const itemId = computed(() => Number(route.params.id))
const itemData = ref({})
const recommendList = ref([])

async function loadItem(id) {
  loading.value = true
  try {
    const found = await getItemDetail(id)
    itemData.value = found
    isFav.value = Boolean(found.favorited)
    viewCount.value = found.viewCount || 0
    wantCount.value = found.wantCount || 0
    const items = await listItems({ category: found.category })
    recommendList.value = items
      .filter(item => item.id !== found.id)
      .slice(0, 2)
  } catch (error) {
    itemData.value = {}
    recommendList.value = []
    showFailToast(error.message || '商品详情加载失败')
  } finally {
    loading.value = false
  }
}

watch(itemId, (id) => { loadItem(id) }, { immediate: true })

const itemImages = computed(() => {
  const images = itemData.value.images
  return images && images.length ? images : ['https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg']
})

const canReserve = computed(() => itemData.value.status === '在售')
const reserveButtonText = computed(() => {
  if (itemData.value.reservedByMe) return '已预定'
  if (itemData.value.status === '被预定') return '已被预定'
  if (itemData.value.status === '已完成') return '已完成'
  return '我想要'
})

const goBack = () => router.back()
const goToDetail = (id) => router.push(`/item/${id}`)
const goToChat = async () => {
  if (!itemData.value.id) return
  try {
    const chat = await ensureChatForItem(itemData.value.id)
    router.push(`/chat/${chat.id}`)
  } catch (error) {
    showFailToast(error.message || '私信会话创建失败')
  }
}

const handleShare = async () => {
  const d = itemData.value
  if (navigator.share) {
    try {
      await navigator.share({
        title: d.title,
        text: `${d.title} - 仅￥${d.price}`,
        url: window.location.href,
      })
    } catch {}
  } else {
    await navigator.clipboard?.writeText(window.location.href)
    showToast({ message: '链接已复制，可分享给好友', position: 'top' })
  }
}

const toggleFav = async () => {
  if (!itemData.value.id) return
  try {
    if (isFav.value) {
      await unfavoriteItem(itemData.value.id)
      isFav.value = false
      showToast({ message: '已取消收藏', position: 'top' })
    } else {
      await favoriteItem(itemData.value.id)
      isFav.value = true
      showSuccessToast('已收藏')
    }
  } catch (error) {
    showFailToast(error.message || '收藏操作失败')
  }
}

const confirmLock = async () => {
  showConfirm.value = false
  if (!itemData.value.id || !canReserve.value) return
  try {
    const order = await reserveItem(itemData.value.id)
    itemData.value.status = order.itemStatus
    itemData.value.reservedByMe = true
    itemData.value.activeOrderId = order.id
    wantCount.value += 1
    showSuccessToast('预定成功')
    router.push(`/chat/${order.conversationId}`)
  } catch (error) {
    showFailToast(error.message || '预定失败')
  }
}
</script>

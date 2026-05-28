<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    <!-- 1. 顶部导航栏 (固定在顶部) -->
    <van-nav-bar 
      title="商品详情" 
      left-arrow 
      fixed 
      placeholder
      @click-left="goBack" 
    />

    <!-- 2. 图片轮播 -->
    <van-swipe class="h-80 bg-white" :autoplay="3000" indicator-color="#005A3C">
      <van-swipe-item v-for="(img, index) in itemImages" :key="index">
        <img :src="img" class="w-full h-full object-cover" />
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
    </div>

    <!-- 4. 卖家信息卡片 -->
    <div class="p-4 bg-white mt-2 flex items-center justify-between active:bg-gray-50 transition-colors">
      <div class="flex items-center gap-3">
        <img :src="itemData.sellerAvatar" class="w-10 h-10 rounded-full border border-gray-100" />
        <div>
          <div class="font-bold text-sm text-gray-800">{{ itemData.sellerName }}</div>
          <div class="text-xs text-gray-400 mt-0.5">发布于 {{ itemData.location }}</div>
        </div>
      </div>
      <van-icon name="arrow" class="text-gray-400" />
    </div>

    <!-- 5. 底部固定操作栏 -->
    <van-action-bar>
      <van-action-bar-icon icon="chat-o" text="私信留言" @click="goToChat" />
      <van-action-bar-icon icon="warning-o" text="举报" @click="reportItem" />
      <van-action-bar-button 
        type="danger" 
        color="#005A3C" 
        text="我想要" 
        @click="showConfirm = true" 
      />
    </van-action-bar>

    <!-- 6. 核心逻辑：防误触与 FSM 锁单确认弹窗 (Action Sheet) -->
    <van-action-sheet v-model:show="showConfirm" title="确认预定此商品？">
      <div class="p-6 text-center">
        <p class="text-gray-600 mb-6 leading-relaxed">
          点击确认后，该商品将被标记为<span class="text-[#005A3C] font-bold">【被预定】</span>状态。<br>
          为防止恶意锁单，请尽快与卖家私信协商线下交收细节！
        </p>
        <button 
          @click="confirmLock"
          class="w-full py-3.5 bg-[#005A3C] text-white font-bold rounded-xl active:scale-[0.98] transition-transform shadow-lg shadow-[#005A3C]/30"
        >
          确认锁定库存
        </button>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const showConfirm = ref(false)

const itemData = reactive({
  id: route.params.id ?? '1',
  title: '99新《数据库系统概论》王珊版，笔记很少',
  price: '25.00',
  condition: '9成新',
  description: '上学期刚用完的教材，保护得很好，里面只有极少量的铅笔勾画痕迹，不影响阅读。\n\n因为快毕业了，清理宿舍物品，随便出。九龙湖校区梅园可以面交。',
  images: [
    'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600',
    'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600'
  ],
  sellerName: 'JLH刘同学',
  sellerAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
  location: '九龙湖校区 / 梅园'
})

const itemImages = computed(() =>
  itemData.images.length
    ? itemData.images
    : ['https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg']
)

const goBack = () => router.back()
const goToChat = () => router.push('/chat/1')
const reportItem = () => showToast('已进入违规举报流程')

const confirmLock = () => {
  showConfirm.value = false
  showSuccessToast('预定成功，快去私信卖家吧！')
  setTimeout(goToChat, 1500)
}
</script>
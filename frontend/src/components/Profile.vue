<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    <!-- 顶部用户信息 -->
    <div class="bg-[#005A3C] pt-12 pb-16 px-5 relative">
      <div class="flex items-center gap-4">
        <img :src="user.avatar" class="w-16 h-16 rounded-full border-2 border-white/30 object-cover" />
        <div class="flex-1 text-white">
          <h2 class="text-xl font-bold">{{ user.nickname }}</h2>
          <p class="text-sm text-white/70 mt-1">{{ user.location }}</p>
          <p class="text-xs text-white/50 mt-0.5 font-mono">{{ user.studentId }}@seu.edu.cn</p>
        </div>
        <van-icon name="edit" size="20" color="#fff" @click="showEdit = true" />
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="mx-4 mt-4 bg-white rounded-xl overflow-hidden">
      <div class="flex border-b border-gray-50">
        <button
          v-for="tab in menuTabs" :key="tab.key"
          @click="switchTab(tab.key)"
          class="flex-1 py-3 text-sm font-medium text-center transition-colors relative"
          :class="tab.key === activeListTab ? 'text-[#005A3C]' : 'text-gray-500'"
        >
          <van-icon :name="tab.icon" class="mr-1" />
          {{ tab.label }}
          <span v-if="tab.badge && myItems[tab.key]?.length" class="ml-0.5 text-[10px] text-red-500">({{ myItems[tab.key].length }})</span>
          <div
            v-if="tab.key === activeListTab"
            class="absolute bottom-0 left-1/4 right-1/4 h-0.5 bg-[#005A3C] rounded-full"
          />
        </button>
      </div>

      <div v-if="currentList.length" class="divide-y divide-gray-50">
        <div
          v-for="item in currentList" :key="item.id"
          @click="goToDetail(item.id)"
          class="flex items-center gap-3 px-4 py-3.5 active:bg-gray-50 transition-colors cursor-pointer"
        >
          <img :src="item.image" class="w-16 h-16 rounded-lg object-cover shrink-0 bg-gray-100" />
          <div class="flex-1 min-w-0">
            <h4 class="text-sm font-bold text-gray-800 line-clamp-2 leading-snug">{{ item.title }}</h4>
            <div class="flex items-center justify-between mt-1.5">
              <span class="text-red-500 font-bold text-sm">￥{{ item.price }}</span>
              <van-tag :color="statusColor(item.status)" plain size="small">{{ item.status }}</van-tag>
            </div>
          </div>
          <van-icon name="arrow" class="text-gray-300 shrink-0" />
        </div>
      </div>

      <div v-else class="py-12 text-center">
        <van-icon :name="emptyIcon" size="40" class="text-gray-200" />
        <p class="text-sm text-gray-400 mt-2">{{ emptyText }}</p>
      </div>
    </div>

    <!-- 设置与退出 -->
    <div class="mx-4 mt-4 bg-white rounded-xl overflow-hidden mb-4">
      <van-cell title="账号与安全" icon="shield-o" is-link @click="showDeveloping" />
      <van-cell title="帮助与反馈" icon="question-o" is-link @click="showDeveloping" />
      <van-cell title="关于集市" icon="info-o" is-link value="v0.1.0" />
      <van-cell title="退出登录" icon="revoke" is-link @click="handleLogout" />
    </div>

    <!-- 编辑资料弹窗 -->
    <van-popup v-model:show="showEdit" round position="bottom" :style="{ height: '45%' }">
      <div class="p-5">
        <h3 class="text-lg font-bold text-gray-800 mb-4">编辑资料</h3>
        <van-field v-model="editForm.nickname" label="昵称" placeholder="集市昵称" />
        <van-field v-model="editForm.wechat" label="微信号" placeholder="方便买家联系" />
        <button @click="saveProfile" class="w-full mt-6 py-3 bg-[#005A3C] text-white font-bold rounded-xl active:scale-[0.98] transition-transform">
          保存
        </button>
      </div>
    </van-popup>

    <AppTabbar />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showFailToast, showSuccessToast, showToast, showConfirmDialog } from 'vant'
import { listMyItems } from '@/api/items'
import { getCurrentUser, updateCurrentUser } from '@/api/user'
import { clearAuth } from '@/api/request'
import AppTabbar from './AppTabbar.vue'

const router = useRouter()
const route = useRoute()
const showEdit = ref(false)

const activeListTab = ref(route.query.tab || 'selling')

watch(() => route.query.tab, (tab) => {
  if (tab) activeListTab.value = tab
})

const user = reactive({
  nickname: '',
  avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
  location: '',
  locationCode: '',
  studentId: '',
  wechat: ''
})

const editForm = reactive({
  nickname: user.nickname,
  wechat: user.wechat
})

const menuTabs = [
  { key: 'selling', label: '我发布的', icon: 'bag-o' },
  { key: 'bought', label: '我买到的', icon: 'shopping-cart-o' },
  { key: 'reserved', label: '我的预定', icon: 'clock-o', badge: 1 },
  { key: 'favorites', label: '我的收藏', icon: 'star-o' }
]

const myItems = reactive({
  selling: [],
  bought: [],
  reserved: [],
  favorites: []
})

const tabKeys = Object.keys(myItems)
const listTitle = computed(() => menuTabs.find(t => t.key === activeListTab.value)?.label || '我的商品')
const currentList = computed(() => myItems[activeListTab.value] || [])

const emptyIcon = computed(() => {
  const map = { selling: 'bag-o', bought: 'cart-o', reserved: 'clock-o', favorites: 'star-o' }
  return map[activeListTab.value] || 'goods-o'
})
const emptyText = computed(() => {
  const map = {
    selling: '暂无在售商品',
    bought: '购买记录由后端B接入',
    reserved: '预定记录由后端B接入',
    favorites: '收藏夹由后端B接入'
  }
  return map[activeListTab.value] || '暂无数据'
})

const statusColor = (status) => {
  const map = { '在售': '#005A3C', '被预定': '#ed6a0c', '已完成': '#969799' }
  return map[status] || '#005A3C'
}

function switchTab(key) {
  activeListTab.value = key
  router.replace({ query: { ...route.query, tab: key } })
}

const goToDetail = (id) => router.push(`/item/${id}`)

const showDeveloping = () => showToast('功能开发中')

const loadProfile = async () => {
  try {
    const profile = await getCurrentUser()
    user.nickname = profile.nickname || profile.studentId || '未设置昵称'
    user.avatar = profile.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
    user.location = profile.locationText || '未设置常驻地点'
    user.locationCode = profile.locationCode || ''
    user.studentId = profile.studentId
    user.wechat = profile.wechat || ''
    editForm.nickname = user.nickname
    editForm.wechat = user.wechat
  } catch (error) {
    showFailToast(error.message || '资料加载失败')
  }
}

const loadMySellingItems = async () => {
  try {
    myItems.selling = await listMyItems()
  } catch (error) {
    showFailToast(error.message || '我发布的商品加载失败')
  }
}

const saveProfile = async () => {
  if (!user.locationCode) {
    showToast({ message: '请先在新用户引导页完善宿舍信息', position: 'top' })
    return
  }
  try {
    const profile = await updateCurrentUser({
      nickname: editForm.nickname.trim(),
      wechat: editForm.wechat.trim(),
      locationCode: user.locationCode
    })
    user.nickname = profile.nickname || profile.studentId
    user.wechat = profile.wechat || ''
    showEdit.value = false
    showSuccessToast('资料已更新')
  } catch (error) {
    showFailToast(error.message || '保存失败')
  }
}

const handleLogout = async () => {
  try {
    await showConfirmDialog({ title: '确认退出', message: '退出后需重新验证登录' })
    clearAuth()
    router.push('/login')
  } catch {}
}

onMounted(() => {
  loadProfile()
  loadMySellingItems()
})
</script>

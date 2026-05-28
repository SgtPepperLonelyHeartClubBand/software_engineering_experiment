<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    <!-- 顶部用户信息 -->
    <div class="bg-[#005A3C] pt-12 pb-16 px-5 relative">
      <div class="flex items-center gap-4">
        <img
          :src="user.avatar"
          class="w-16 h-16 rounded-full border-2 border-white/30 object-cover"
        />
        <div class="flex-1 text-white">
          <h2 class="text-xl font-bold">{{ user.nickname }}</h2>
          <p class="text-sm text-white/70 mt-1">{{ user.location }}</p>
          <p class="text-xs text-white/50 mt-0.5 font-mono">{{ user.studentId }}@seu.edu.cn</p>
        </div>
        <van-icon name="edit" size="20" color="#fff" @click="showEdit = true" />
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="mx-4 -mt-10 bg-white rounded-xl shadow-sm grid grid-cols-3 divide-x divide-gray-100">
      <div
        v-for="stat in stats"
        :key="stat.label"
        @click="activeListTab = stat.key"
        class="py-4 text-center active:bg-gray-50 transition-colors cursor-pointer"
      >
        <div class="text-xl font-bold text-gray-800">{{ stat.count }}</div>
        <div class="text-xs text-gray-500 mt-1">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="mx-4 mt-4 bg-white rounded-xl overflow-hidden">
      <van-cell title="我发布的" icon="bag-o" is-link @click="activeListTab = 'selling'" />
      <van-cell title="我买到的" icon="shopping-cart-o" is-link @click="activeListTab = 'bought'" />
      <van-cell title="我的预定" icon="clock-o" is-link badge="1" @click="activeListTab = 'reserved'" />
      <van-cell title="我的收藏" icon="star-o" is-link @click="activeListTab = 'favorites'" />
    </div>

    <!-- 商品列表 -->
    <div class="mx-4 mt-4">
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-bold text-gray-800">{{ listTitle }}</h3>
      </div>

      <div v-if="currentList.length" class="space-y-2">
        <div
          v-for="item in currentList"
          :key="item.id"
          @click="goToDetail(item.id)"
          class="bg-white rounded-xl p-3 flex gap-3 active:scale-[0.99] transition-transform cursor-pointer"
        >
          <img :src="item.image" class="w-20 h-20 rounded-lg object-cover shrink-0" />
          <div class="flex-1 min-w-0 flex flex-col justify-between py-0.5">
            <h4 class="text-sm font-bold text-gray-800 line-clamp-2">{{ item.title }}</h4>
            <div class="flex items-center justify-between mt-1">
              <span class="text-red-500 font-bold text-sm">￥{{ item.price }}</span>
              <van-tag :color="statusColor(item.status)" plain>{{ item.status }}</van-tag>
            </div>
          </div>
        </div>
      </div>

      <van-empty v-else :description="emptyText" />
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
        <button
          @click="saveProfile"
          class="w-full mt-6 py-3 bg-[#005A3C] text-white font-bold rounded-xl active:scale-[0.98] transition-transform"
        >
          保存
        </button>
      </div>
    </van-popup>

    <AppTabbar />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast, showConfirmDialog } from 'vant'
import AppTabbar from './AppTabbar.vue'

const router = useRouter()
const showEdit = ref(false)
const activeListTab = ref('selling')

const user = reactive({
  nickname: '东大淘货王',
  avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
  location: '九龙湖校区 / 梅园 / 梅园1栋',
  studentId: '220000001',
  wechat: 'seu_market_01'
})

const editForm = reactive({
  nickname: user.nickname,
  wechat: user.wechat
})

const stats = [
  { key: 'selling', label: '在售', count: 2 },
  { key: 'sold', label: '已售', count: 5 },
  { key: 'favorites', label: '收藏', count: 3 }
]

const myItems = {
  selling: [
    {
      id: 101,
      title: '高等数学同济第七版，几乎全新',
      price: '18.00',
      status: '在售',
      image: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=200'
    },
    {
      id: 102,
      title: '罗技 G304 无线鼠标',
      price: '80.00',
      status: '在售',
      image: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?q=80&w=200'
    }
  ],
  bought: [
    {
      id: 201,
      title: '宿舍收纳箱 大号',
      price: '12.00',
      status: '已完成',
      image: 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=200'
    }
  ],
  reserved: [
    {
      id: 1,
      title: '99新《数据库系统概论》王珊版',
      price: '25.00',
      status: '被预定',
      image: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=200'
    }
  ],
  favorites: [
    {
      id: 2,
      title: '二手 AirPods Pro',
      price: '350.00',
      status: '在售',
      image: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?q=80&w=200'
    },
    {
      id: 4,
      title: '出电吉他，带音箱',
      price: '400.00',
      status: '在售',
      image: 'https://images.unsplash.com/photo-1514649923863-ceaf75b770ab?q=80&w=200'
    }
  ]
}

const tabTitles = {
  selling: '我发布的',
  bought: '我买到的',
  reserved: '我的预定',
  favorites: '我的收藏',
  sold: '已售出'
}

const listTitle = computed(() => tabTitles[activeListTab.value] || '我的商品')
const currentList = computed(() => myItems[activeListTab.value] || [])
const emptyText = computed(() => `暂无${listTitle.value}的商品`)

const statusColor = (status) => {
  const map = { '在售': '#005A3C', '被预定': '#ed6a0c', '已完成': '#969799' }
  return map[status] || '#005A3C'
}

const goToDetail = (id) => router.push(`/item/${id}`)

const showDeveloping = () => showToast('功能开发中')

const saveProfile = () => {
  user.nickname = editForm.nickname
  user.wechat = editForm.wechat
  showEdit.value = false
  showSuccessToast('资料已更新')
}

const handleLogout = async () => {
  try {
    await showConfirmDialog({ title: '确认退出', message: '退出后需重新验证登录' })
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

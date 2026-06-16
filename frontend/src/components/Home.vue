<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    
    <!-- 1. 顶部吸顶搜索栏与分类 -->
    <div class="sticky top-0 z-50 bg-white shadow-sm">
      <div class="relative">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索闲置：例如 数据库原理"
          shape="round"
          background="#fff"
          clearable
          @clear="handleSearchClear"
          @search="handleSearch"
        />
      </div>
      <van-tabs
        v-model:active="activeCategory"
        color="#005A3C"
        title-active-color="#005A3C"
        :swipe-threshold="4"
        animated
        @change="handleCategoryChange"
      >
        <van-tab title="全部"></van-tab>
        <van-tab title="专业书籍"></van-tab>
        <van-tab title="电子数码"></van-tab>
        <van-tab title="宿舍日用"></van-tab>
      </van-tabs>
    </div>

    <!-- 2. 商品列表区域 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" success-text="刷新成功" pull-distance="80">
      <!-- 加载骨架屏 -->
      <div v-if="loading" class="p-2">
        <div class="columns-2 gap-2">
          <div v-for="n in 4" :key="n" class="break-inside-avoid mb-2 bg-white rounded-xl overflow-hidden shadow-sm">
            <div class="bg-gray-200 animate-pulse" :style="{ height: (n % 2 === 0 ? 200 : 160) + 'px' }"></div>
            <div class="p-2.5 space-y-2">
              <div class="h-3 bg-gray-200 rounded animate-pulse w-full"></div>
              <div class="h-3 bg-gray-200 rounded animate-pulse w-2/3"></div>
              <div class="flex justify-between items-center">
                <div class="h-4 bg-gray-200 rounded animate-pulse w-16"></div>
                <div class="h-3 bg-gray-200 rounded animate-pulse w-12"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!filteredList.length" class="pt-20">
        <van-empty
          :image="searchKeyword || activeCategory !== 0 ? 'search' : 'goods'"
          :description="emptyText"
        >
          <template #image v-if="searchKeyword">
            <van-icon name="search" size="60" color="#c8c9cc" />
          </template>
          <van-button
            v-if="searchKeyword || activeCategory !== 0"
            round
            size="small"
            color="#005A3C"
            @click="resetFilters"
          >
            查看全部商品
          </van-button>
        </van-empty>
      </div>

      <!-- 商品瀑布流 -->
      <div v-else class="p-2">
        <div class="columns-2 gap-2">
          <div
            v-for="item in filteredList"
            :key="item.id"
            @click="goToDetail(item.id)"
            class="break-inside-avoid mb-2 bg-white rounded-xl overflow-hidden shadow-sm active:scale-[0.98] transition-transform cursor-pointer"
          >
            <div class="relative">
              <img
                :src="item.image"
                class="w-full h-auto object-cover block"
                loading="lazy"
              />
              <div class="absolute top-2 left-2 bg-black/60 text-white text-[10px] px-1.5 py-0.5 rounded backdrop-blur-sm">
                {{ item.condition }}
              </div>
            </div>
            <div class="p-2.5">
              <h3 class="text-sm font-bold text-gray-800 leading-snug line-clamp-2">
                {{ item.title }}
              </h3>
              <div class="flex items-center justify-between mt-2">
                <span class="text-red-500 font-bold text-sm">
                  <span class="text-xs">￥</span>{{ item.price }}
                </span>
                <span class="text-xs text-gray-400">{{ item.wantCount }}人想要</span>
              </div>
              <div class="flex items-center gap-1.5 mt-2 pt-2 border-t border-gray-50">
                <img class="w-4 h-4 rounded-full" :src="item.sellerAvatar" />
                <span class="text-xs text-gray-500 truncate">{{ item.sellerName }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="text-center text-gray-400 text-xs py-4">
          — 共 {{ filteredList.length }} 件商品 —
        </div>
      </div>
    </van-pull-refresh>

    <AppTabbar />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { allItems } from '../stores/items'
import AppTabbar from './AppTabbar.vue'

const router = useRouter()
const searchKeyword = ref('')
const activeCategory = ref(0)
const refreshing = ref(false)
const loading = ref(true)

const categoryMap = ['全部', '专业书籍', '电子数码', '宿舍日用']

const filteredList = computed(() => {
  let list = allItems

  const cat = categoryMap[activeCategory.value]
  if (cat && cat !== '全部') {
    list = list.filter(item => item.category === cat)
  }

  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    list = list.filter(item =>
      item.title.toLowerCase().includes(kw) ||
      item.sellerName.toLowerCase().includes(kw)
    )
  }

  return list
})

const emptyText = computed(() => {
  if (searchKeyword.value.trim()) {
    return `未找到与"${searchKeyword.value.trim()}"相关的商品`
  }
  if (activeCategory.value !== 0) {
    return `暂无${categoryMap[activeCategory.value]}类商品`
  }
  return '暂无上架商品，去看看别的吧'
})

const goToDetail = (id) => router.push(`/item/${id}`)

const handleSearch = () => {
  if (searchKeyword.value.trim() && filteredList.value.length === 0) {
    showToast({ message: '未找到相关商品', position: 'top' })
  }
}

const handleSearchClear = () => {
  searchKeyword.value = ''
}

const handleCategoryChange = () => {
  if (filteredList.value.length === 0) {
    showToast({ message: emptyText.value, position: 'top' })
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  activeCategory.value = 0
}

const onRefresh = async () => {
  await new Promise(resolve => setTimeout(resolve, 800))
  refreshing.value = false
  showToast({ message: '已更新商品列表', position: 'top' })
}

onMounted(() => {
  setTimeout(() => { loading.value = false }, 600)
})
</script>

<style scoped>
::-webkit-scrollbar {
  display: none;
}
</style>
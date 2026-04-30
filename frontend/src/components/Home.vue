<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    
    <!-- 1. 顶部吸顶搜索栏与分类 -->
    <div class="sticky top-0 z-50 bg-white shadow-sm">
      <van-search 
        v-model="searchKeyword" 
        placeholder="搜索闲置：例如 数据库原理" 
        shape="round"
        background="#fff"
      />
      <van-tabs v-model:active="activeCategory" color="#005A3C" title-active-color="#005A3C" :swipe-threshold="4">
        <van-tab title="全部"></van-tab>
        <van-tab title="专业书籍"></van-tab>
        <van-tab title="电子数码"></van-tab>
        <van-tab title="宿舍日用"></van-tab>
      </van-tabs>
    </div>

    <!-- 2. 瀑布流列表 -->
    <div class="p-2">
      <!-- CSS 双列瀑布流布局 (Tailwind columns 语法) -->
      <div class="columns-2 gap-2">
        <!-- 商品卡片 -->
        <div 
          v-for="item in goodsList" 
          :key="item.id" 
          @click="goToDetail(item.id)"
          class="break-inside-avoid mb-2 bg-white rounded-xl overflow-hidden shadow-sm active:scale-[0.98] transition-transform cursor-pointer"
        >
          <!-- 图片区域 -->
          <div class="relative">
            <img 
              :src="item.image" 
              class="w-full h-auto object-cover block"
            />
            <!-- 售卖状态标签 -->
            <div class="absolute top-2 left-2 bg-black/60 text-white text-[10px] px-1.5 py-0.5 rounded backdrop-blur-sm">
              {{ item.condition }}
            </div>
          </div>
          
          <!-- 商品信息 -->
          <div class="p-2.5">
            <h3 class="text-sm font-bold text-gray-800 leading-snug line-clamp-2">
              {{ item.title }}
            </h3>
            <div class="flex items-center justify-between mt-2">
              <span class="text-red-500 font-bold text-sm">
                <span class="text-xs">￥</span>{{ item.price }}
              </span>
              <span class="text-xs text-gray-400">12人想要</span>
            </div>
            <!-- 卖家信息 -->
            <div class="flex items-center gap-1.5 mt-2 pt-2 border-t border-gray-50">
              <img class="w-4 h-4 rounded-full" :src="item.sellerAvatar" />
              <span class="text-xs text-gray-500 truncate">{{ item.sellerName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部加载提示 -->
      <div class="text-center text-gray-400 text-xs py-4">
        没有更多闲置啦
      </div>
    </div>

    <!-- 3. 底部导航栏 -->
    <van-tabbar v-model="activeTab" active-color="#005A3C" inactive-color="#94a3b8" route border>
      <van-tabbar-item icon="shop-o" to="/home">集市</van-tabbar-item>
      <!-- 强调发布按钮：为了美观，使用了品牌色 -->
      <van-tabbar-item icon="plus" class="font-bold text-[#005A3C]">发布</van-tabbar-item>
      <van-tabbar-item icon="chat-o" badge="2">消息</van-tabbar-item>
      <van-tabbar-item icon="user-o">我的</van-tabbar-item>
    </van-tabbar>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchKeyword = ref('')
const activeCategory = ref(0)
const activeTab = ref(0)

// 模拟【在售】商品数据
const goodsList = ref([
  {
    id: 1,
    title: '99新《数据库系统概论》王珊版，笔记很少',
    price: '25.00',
    condition: '9成新',
    image: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop',
    sellerName: 'JLH刘同学',
    sellerAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  },
  {
    id: 2,
    title: '毕业出个二手 AirPods Pro，左耳有点杂音，便宜出',
    price: '350.00',
    condition: '有瑕疵',
    image: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?q=80&w=600&auto=format&fit=crop',
    sellerName: '成贤院学姐',
    sellerAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  },
  {
    id: 3,
    title: '宿舍神器：全新桌面小风扇，买多了',
    price: '15.00',
    condition: '全新',
    image: 'https://images.unsplash.com/photo-1618220179428-22790b461013?q=80&w=600&auto=format&fit=crop',
    sellerName: '梅园彭于晏',
    sellerAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  },
  {
    id: 4,
    title: '出电吉他，带音箱',
    price: '400.00',
    condition: '8成新',
    image: 'https://images.unsplash.com/photo-1514649923863-ceaf75b770ab?q=80&w=600&auto=format&fit=crop',
    sellerName: '音乐社老张',
    sellerAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  }
])
</script>

<style scoped>
/* 隐藏浏览器原生滚动条，让移动端体验更清爽 */
::-webkit-scrollbar {
  display: none;
}
</style>
<template>
  <van-tabbar route active-color="#005A3C" inactive-color="#94a3b8" border>
    <van-tabbar-item icon="shop-o" to="/home">集市</van-tabbar-item>
    <van-tabbar-item icon="plus" to="/publish">发布</van-tabbar-item>
    <van-tabbar-item icon="chat-o" :badge="messageBadge" to="/messages">消息</van-tabbar-item>
    <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { formatBadge, refreshUnreadSummary, totalUnread } from '../stores/messages'

const messageBadge = computed(() => formatBadge(totalUnread.value))

onMounted(() => {
  refreshUnreadSummary().catch(() => {})
})
</script>

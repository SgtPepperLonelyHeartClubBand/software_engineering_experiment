import { createRouter, createWebHistory } from 'vue-router'
import Login from './components/Login.vue'
import Onboarding from './components/Onboarding.vue'
import Home from './components/Home.vue'
import Detail from './components/Detail.vue' // 1. 引入详情页

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/onboarding', component: Onboarding },
  { path: '/home', component: Home },
  { path: '/item/:id', component: Detail }   // 2. 注册带参数的详情页路由
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
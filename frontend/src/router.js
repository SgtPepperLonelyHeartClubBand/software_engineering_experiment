import { createRouter, createWebHistory } from 'vue-router'
import Login from './components/Login.vue'
import Onboarding from './components/Onboarding.vue'
import Home from './components/Home.vue'
import Detail from './components/Detail.vue'
import Publish from './components/Publish.vue'
import Messages from './components/Messages.vue'
import Chat from './components/Chat.vue'
import Profile from './components/Profile.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/onboarding', component: Onboarding },
  { path: '/home', component: Home },
  { path: '/publish', component: Publish },
  { path: '/messages', component: Messages },
  { path: '/chat/:id', component: Chat },
  { path: '/profile', component: Profile },
  { path: '/item/:id', component: Detail }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

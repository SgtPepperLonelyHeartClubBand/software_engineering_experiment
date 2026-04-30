import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router' // 你的 router 引入

import Vant from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)

app.use(router)
app.use(Vant)
app.mount('#app')
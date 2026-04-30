<template>
  <div class="min-h-screen flex items-center justify-center bg-[#F7F8FA] px-4">
    <!-- 主体卡片 -->
    <div class="max-w-md w-full p-8 bg-white rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
      
      <!-- 顶部 Logo 与 标题区域 -->
      <div class="flex flex-col items-center mb-10">
        <!-- 修改点1：默认正的，鼠标悬浮时倾斜 (hover:-rotate-6) -->
        <div class="w-16 h-16 bg-[#005A3C] shadow-lg shadow-[#005A3C]/30 text-white flex items-center justify-center rounded-2xl text-2xl font-bold mb-4 transform hover:-rotate-6 transition-transform duration-300">
          SEU
        </div>
        <h2 class="text-2xl font-extrabold text-gray-800 tracking-wider">校园二手集市</h2>
        <p class="text-sm text-gray-500 mt-2 font-medium">安全 · 真实 · 便捷的校内闲置平台</p>
      </div>

      <!-- 登录表单 -->
      <div class="space-y-5">
        <!-- 一卡通号输入 -->
        <div>
          <!-- 修改点2：文案改为纯“一卡通号” -->
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">一卡通号</label>
          <div class="flex items-center border-2 border-gray-100 rounded-xl overflow-hidden focus-within:border-[#005A3C] transition-colors bg-gray-50 focus-within:bg-white">
            <input
              type="text"
              v-model="studentId"
              placeholder="请输入 9 位一卡通号"
              maxlength="9"
              class="w-full px-4 py-3.5 outline-none bg-transparent font-medium placeholder-gray-400"
            />
            <span class="px-4 py-3.5 text-gray-500 bg-gray-100/50 border-l-2 border-gray-100 font-mono text-sm select-none">
              @seu.edu.cn
            </span>
          </div>
        </div>

        <!-- 验证码输入 -->
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">邮箱验证码</label>
          <div class="flex gap-3">
            <input
              type="text"
              v-model="verifyCode"
              placeholder="6位验证码"
              maxlength="6"
              class="flex-1 px-4 py-3.5 border-2 border-gray-100 rounded-xl focus:outline-none focus:border-[#005A3C] transition-colors bg-gray-50 focus:bg-white font-medium tracking-widest text-center"
            />
            <button
              @click="handleSendCode"
              :disabled="countdown > 0 || isSending"
              class="px-5 py-3.5 bg-[#005A3C]/10 text-[#005A3C] font-bold rounded-xl hover:bg-[#005A3C]/20 disabled:text-gray-400 disabled:bg-gray-100 transition-colors whitespace-nowrap active:scale-95 flex items-center justify-center min-w-[120px]"
            >
              <span v-if="isSending" class="flex items-center gap-2">
                <van-loading type="spinner" size="16px" color="#005A3C" /> 发送中
              </span>
              <span v-else>{{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}</span>
            </button>
          </div>
          
          <!-- 容错设计：高校邮件网关拦截/延迟提示 -->
          <div class="mt-2.5 flex items-start gap-1.5 px-1">
            <van-icon name="warning-o" class="text-amber-500 mt-0.5" />
            <p class="text-xs text-gray-500 leading-relaxed">
              校园邮件网关可能存在延迟，若未收到请注意查收 <span class="text-amber-600 font-bold">垃圾邮件箱</span>。
            </p>
          </div>
        </div>

        <!-- 登录按钮 -->
        <div class="pt-4">
          <button
            @click="handleLogin"
            :disabled="isLoggingIn"
            class="w-full py-3.5 px-4 bg-[#005A3C] text-white font-bold text-lg rounded-xl shadow-lg shadow-[#005A3C]/30 hover:bg-[#00422c] disabled:opacity-70 disabled:cursor-not-allowed transition-all active:scale-[0.98] flex items-center justify-center"
          >
            <van-loading v-if="isLoggingIn" type="spinner" size="20px" color="#fff" class="mr-2" />
            {{ isLoggingIn ? '验证中...' : '验证并登录' }}
          </button>
        </div>
      </div>
      
      <!-- 底部校训 -->
      <div class="mt-12 text-center">
        <div class="inline-block border-b border-gray-300 pb-1 px-4 text-xs tracking-[0.3em] text-gray-400 font-serif">
          止于至善
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant' 

const router = useRouter()
const studentId = ref('')
const verifyCode = ref('')
const countdown = ref(0)
const isSending = ref(false)
const isLoggingIn = ref(false)
let timer = null

// 组件销毁时清除定时器，防止内存泄漏
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// 发送验证码逻辑
const handleSendCode = async () => {
  // 正则校验：9位数字 (一卡通号规则)
  const seuIdRegex = /^[0-9]{9}$/
  if (!seuIdRegex.test(studentId.value)) {
    showToast({ message: '请输入有效的 9 位一卡通号', position: 'top' })
    return
  }

  const email = `${studentId.value}@seu.edu.cn`
  isSending.value = true
  
  try {
    // 模拟 API 请求
    await new Promise(resolve => setTimeout(resolve, 800)) 

    showSuccessToast('验证码已发送')
    
    // 开启 60s 倒计时
    countdown.value = 60
    timer = setInterval(() => {
      if (countdown.value <= 1) {
        clearInterval(timer)
        countdown.value = 0
      } else {
        countdown.value--
      }
    }, 1000)
  } catch (error) {
    showFailToast('发送失败，请稍后重试')
  } finally {
    isSending.value = false
  }
}

// 登录逻辑
const handleLogin = async () => {
  if (!studentId.value) {
    showToast({ message: '请输入一卡通号', position: 'top' })
    return
  }
  if (!verifyCode.value || verifyCode.value.length < 4) {
    showToast({ message: '请输入完整的验证码', position: 'top' })
    return
  }

  isLoggingIn.value = true

  try {
    // 模拟登录 API 请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    showSuccessToast('登录成功')
    
    // 路由跳转判断逻辑
    const isNewUser = true 
    if (isNewUser) {
      router.push('/onboarding') 
    } else {
      router.push('/home') 
    }
  } catch (error) {
    showFailToast('验证码错误或已过期')
  } finally {
    isLoggingIn.value = false
  }
}
</script>

<style scoped>
/* 隐藏输入框的原生高亮边框 */
input:-webkit-autofill {
  -webkit-box-shadow: 0 0 0 1000px #F9FAFB inset !important;
  -webkit-text-fill-color: #374151 !important;
}
</style>
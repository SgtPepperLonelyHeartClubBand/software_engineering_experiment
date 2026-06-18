<template>
  <div class="min-h-screen flex items-center justify-center bg-[#F7F8FA] px-4 pb-10">
    <!-- 主体卡片 -->
    <div class="max-w-md w-full p-8 bg-white rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
      
      <!-- 顶部欢迎区域 -->
      <div class="mb-8">
        <h2 class="text-3xl font-extrabold text-gray-800 tracking-wide mb-2">欢迎来到集市 👋</h2>
        <p class="text-sm text-gray-500 font-medium leading-relaxed">
          为了保障校内交易的安全与便捷，请花 1 分钟完善您的基础交易信息。
        </p>
      </div>

      <!-- 信息完善表单 -->
      <div class="space-y-5">
        
        <!-- 昵称设置 -->
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">集市昵称</label>
          <div class="flex items-center border-2 border-gray-100 rounded-xl overflow-hidden focus-within:border-[#005A3C] transition-colors bg-gray-50 focus-within:bg-white">
            <input
              type="text"
              v-model="formData.nickname"
              placeholder="起个好听的名字吧"
              maxlength="12"
              class="w-full px-4 py-3.5 outline-none bg-transparent font-medium placeholder-gray-400"
            />
          </div>
        </div>

        <!-- 结构化地址采集 (核心亮点) -->
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">常驻校区/宿舍 <span class="text-red-500">*</span></label>
          <div 
            @click="showCascader = true"
            class="flex items-center justify-between border-2 border-gray-100 rounded-xl px-4 py-3.5 bg-gray-50 cursor-pointer hover:bg-gray-100 transition-colors"
            :class="{'border-[#005A3C] bg-white': showCascader}"
          >
            <span :class="formData.locationText ? 'text-gray-800 font-medium' : 'text-gray-400 font-medium'">
              {{ formData.locationText || '请选择校区 -> 苑区 -> 楼栋' }}
            </span>
            <van-icon name="arrow" class="text-gray-400" />
          </div>
          <p class="text-xs text-gray-400 mt-1.5 ml-1">
            * 仅用于同城面交时的距离参考，不会泄露具体房号
          </p>
        </div>

        <!-- 微信号 (选填，方便私下联系) -->
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">微信号 <span class="text-gray-400 font-normal text-xs">(选填)</span></label>
          <div class="flex items-center border-2 border-gray-100 rounded-xl overflow-hidden focus-within:border-[#005A3C] transition-colors bg-gray-50 focus-within:bg-white">
            <input
              type="text"
              v-model="formData.wechat"
              placeholder="方便买卖双方达成意向后联系"
              class="w-full px-4 py-3.5 outline-none bg-transparent font-medium placeholder-gray-400"
            />
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="pt-6">
          <button
            @click="handleSubmit"
            :disabled="isSubmitting"
            class="w-full py-3.5 px-4 bg-[#005A3C] text-white font-bold text-lg rounded-xl shadow-lg shadow-[#005A3C]/30 hover:bg-[#00422c] disabled:opacity-70 disabled:cursor-not-allowed transition-all active:scale-[0.98] flex items-center justify-center"
          >
            <van-loading v-if="isSubmitting" type="spinner" size="20px" color="#fff" class="mr-2" />
            {{ isSubmitting ? '保存中...' : '开启集市之旅' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Vant 级联选择器 (底部弹出) -->
    <van-popup v-model:show="showCascader" round position="bottom">
      <van-cascader
        v-model="cascaderValue"
        title="选择所在宿舍区"
        :options="locationOptions"
        active-color="#005A3C"
        @close="showCascader = false"
        @finish="onCascaderFinish"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showToast, showSuccessToast } from 'vant'
import { getLocationTree } from '@/api/locations'
import { updateCurrentUser } from '@/api/user'

const router = useRouter()

// 表单数据
const formData = reactive({
  nickname: '',
  locationText: '', // 用于页面展示的文字 (如: 九龙湖校区/梅园/梅园1栋)
  locationCode: '', // 最终提交给后端的 code (如: JLH-MY-01)
  wechat: ''
})

const isSubmitting = ref(false)

// --- 级联选择器逻辑 ---
const showCascader = ref(false)
const cascaderValue = ref('')

const locationOptions = ref([])

const fetchLocationOptions = async () => {
  try {
    locationOptions.value = await getLocationTree()
  } catch (error) {
    showFailToast(error.message || '宿舍区加载失败')
  }
}

// 选择完毕后的回调
const onCascaderFinish = ({ selectedOptions }) => {
  showCascader.value = false
  // 拼接展示文字
  formData.locationText = selectedOptions.map((option) => option.text).join(' / ')
  // 提取最终一级的 code 发给后端
  formData.locationCode = selectedOptions[selectedOptions.length - 1].value
}

// --- 提交逻辑 ---
const handleSubmit = async () => {
  if (!formData.locationCode) {
    showToast({ message: '请选择常驻校区/宿舍', position: 'top' })
    return
  }

  isSubmitting.value = true

  try {
    await updateCurrentUser({
      nickname: formData.nickname.trim(),
      wechat: formData.wechat.trim(),
      locationCode: formData.locationCode
    })
    
    showSuccessToast('信息设置成功！')
    
    // 跳转到集市首页
    router.push('/home')
  } catch (error) {
    showToast('保存失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchLocationOptions)
</script>

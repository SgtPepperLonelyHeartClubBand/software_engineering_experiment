<template>
  <div class="min-h-screen bg-[#F7F8FA] pb-[60px]">
    <van-nav-bar title="发布闲置" fixed placeholder />

    <div class="p-4 space-y-3">
      <!-- 图片上传 -->
      <div class="bg-white rounded-xl p-4">
        <div class="text-sm font-semibold text-gray-700 mb-3">商品图片 <span class="text-red-500">*</span></div>
        <van-uploader
          v-model="fileList"
          multiple
          :max-count="6"
          :after-read="afterRead"
          preview-size="80"
        />
        <p class="text-xs text-gray-400 mt-2">最多上传 6 张，首张将作为封面展示</p>
      </div>

      <!-- 标题 -->
      <div class="bg-white rounded-xl p-4">
        <div class="text-sm font-semibold text-gray-700 mb-2">商品标题 <span class="text-red-500">*</span></div>
        <van-field
          v-model="form.title"
          rows="2"
          autosize
          type="textarea"
          maxlength="40"
          placeholder="描述品牌、型号、入手渠道等，吸引买家"
          show-word-limit
          :border="false"
          class="!p-0"
        />
      </div>

      <!-- 分类与成色 -->
      <div class="bg-white rounded-xl overflow-hidden">
        <van-cell title="商品分类" is-link :value="form.category || '请选择'" @click="showCategoryPicker = true" />
        <van-cell title="成色描述" is-link :value="form.condition || '请选择'" @click="showConditionPicker = true" />
      </div>

      <!-- 价格 -->
      <div class="bg-white rounded-xl p-4">
        <div class="text-sm font-semibold text-gray-700 mb-2">出售价格 <span class="text-red-500">*</span></div>
        <div class="flex items-center border-2 border-gray-100 rounded-xl overflow-hidden focus-within:border-[#005A3C] bg-gray-50 focus-within:bg-white transition-colors">
          <span class="px-4 text-red-500 font-bold text-lg">￥</span>
          <input
            v-model="form.price"
            type="number"
            placeholder="0.00"
            class="flex-1 py-3.5 pr-4 outline-none bg-transparent font-bold text-lg text-gray-800"
          />
        </div>
      </div>

      <!-- 面交地点 -->
      <div class="bg-white rounded-xl overflow-hidden">
        <van-cell
          title="面交地点"
          is-link
          :value="form.locationText || '请选择'"
          @click="showCascader = true"
        />
      </div>

      <!-- 详细描述 -->
      <div class="bg-white rounded-xl p-4">
        <div class="text-sm font-semibold text-gray-700 mb-2">详细描述</div>
        <van-field
          v-model="form.description"
          rows="4"
          autosize
          type="textarea"
          maxlength="500"
          placeholder="补充商品细节、使用情况、是否可小刀等"
          show-word-limit
          :border="false"
          class="!p-0"
        />
      </div>

      <!-- 提交 -->
      <button
        @click="handleSubmit"
        :disabled="isSubmitting"
        class="w-full py-3.5 bg-[#005A3C] text-white font-bold text-lg rounded-xl shadow-lg shadow-[#005A3C]/30 hover:bg-[#00422c] disabled:opacity-70 transition-all active:scale-[0.98] flex items-center justify-center"
      >
        <van-loading v-if="isSubmitting" type="spinner" size="20px" color="#fff" class="mr-2" />
        {{ isSubmitting ? '发布中...' : '立即发布' }}
      </button>
    </div>

    <!-- 分类选择 -->
    <van-popup v-model:show="showCategoryPicker" round position="bottom">
      <van-picker
        title="选择分类"
        :columns="categoryOptions"
        @confirm="onCategoryConfirm"
        @cancel="showCategoryPicker = false"
      />
    </van-popup>

    <!-- 成色选择 -->
    <van-popup v-model:show="showConditionPicker" round position="bottom">
      <van-picker
        title="选择成色"
        :columns="conditionOptions"
        @confirm="onConditionConfirm"
        @cancel="showConditionPicker = false"
      />
    </van-popup>

    <!-- 地点级联 -->
    <van-popup v-model:show="showCascader" round position="bottom">
      <van-cascader
        v-model="cascaderValue"
        title="选择面交地点"
        :options="locationOptions"
        active-color="#005A3C"
        @close="showCascader = false"
        @finish="onCascaderFinish"
      />
    </van-popup>

    <AppTabbar />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { getLocationTree } from '@/api/locations'
import { createItem } from '@/api/items'
import { uploadImage } from '@/api/upload'
import AppTabbar from './AppTabbar.vue'

const router = useRouter()

const fileList = ref([])
const isSubmitting = ref(false)
const showCategoryPicker = ref(false)
const showConditionPicker = ref(false)
const showCascader = ref(false)
const cascaderValue = ref('')

const form = reactive({
  title: '',
  category: '',
  condition: '',
  price: '',
  locationText: '',
  locationCode: '',
  description: ''
})

const categoryOptions = [
  { text: '专业书籍', value: '专业书籍' },
  { text: '电子数码', value: '电子数码' },
  { text: '宿舍日用', value: '宿舍日用' },
  { text: '运动户外', value: '运动户外' },
  { text: '其他闲置', value: '其他闲置' }
]
const conditionOptions = [
  { text: '全新', value: '全新' },
  { text: '9成新', value: '9成新' },
  { text: '8成新', value: '8成新' },
  { text: '有瑕疵', value: '有瑕疵' }
]

const locationOptions = ref([])

const fetchLocationOptions = async () => {
  try {
    locationOptions.value = await getLocationTree()
  } catch (error) {
    showFailToast(error.message || '地点加载失败')
  }
}

const uploadOne = async (item) => {
  item.status = 'uploading'
  item.message = '上传中...'
  try {
    const result = await uploadImage(item.file)
    item.url = result.url
    item.status = 'done'
    item.message = ''
  } catch (error) {
    item.status = 'failed'
    item.message = '上传失败'
    showFailToast(error.message || '图片上传失败')
  }
}

const afterRead = async (file) => {
  if (Array.isArray(file)) {
    for (const item of file) {
      await uploadOne(item)
    }
  } else {
    await uploadOne(file)
  }
}

const onCategoryConfirm = ({ selectedValues }) => {
  form.category = selectedValues[0]
  showCategoryPicker.value = false
}

const onConditionConfirm = ({ selectedValues }) => {
  form.condition = selectedValues[0]
  showConditionPicker.value = false
}

const onCascaderFinish = ({ selectedOptions }) => {
  showCascader.value = false
  form.locationText = selectedOptions.map((o) => o.text).join(' / ')
  form.locationCode = selectedOptions[selectedOptions.length - 1].value
}

const handleSubmit = async () => {
  if (!fileList.value.length) {
    showToast({ message: '请至少上传一张商品图片', position: 'top' })
    return
  }
  if (!form.title.trim()) {
    showToast({ message: '请填写商品标题', position: 'top' })
    return
  }
  if (!form.category) {
    showToast({ message: '请选择商品分类', position: 'top' })
    return
  }
  if (!form.condition) {
    showToast({ message: '请选择成色描述', position: 'top' })
    return
  }
  if (!form.price || Number(form.price) <= 0) {
    showToast({ message: '请输入有效的出售价格', position: 'top' })
    return
  }
  if (!form.locationCode) {
    showToast({ message: '请选择面交地点', position: 'top' })
    return
  }
  const imageUrls = fileList.value.map((item) => item.url).filter(Boolean)
  if (imageUrls.length !== fileList.value.length) {
    showToast({ message: '请等待图片上传完成', position: 'top' })
    return
  }

  isSubmitting.value = true
  try {
    const created = await createItem({
      title: form.title.trim(),
      category: form.category,
      condition: form.condition,
      price: Number(form.price),
      locationCode: form.locationCode,
      description: form.description.trim(),
      imageUrls
    })
    showSuccessToast('发布成功！')
    router.push(`/item/${created.id}`)
  } catch (error) {
    showFailToast(error.message || '发布失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchLocationOptions)
</script>

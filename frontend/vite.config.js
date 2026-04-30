import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite' // 增加这一行

// https://vitejs.dev/config/
export default defineConfig({
  plugins:[
    vue(),
    tailwindcss(), // 增加这一行
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
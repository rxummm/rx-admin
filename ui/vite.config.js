import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { visualizer } from 'rollup-plugin-visualizer'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    }),
    process.env.ANALYZE === 'true' && visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true
    })
  ].filter(Boolean),
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler'
      }
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://localhost:8088',
        changeOrigin: true
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('echarts')) return 'echarts'
            if (id.includes('element-plus')) return 'element-plus'
            if (id.includes('@antv/x6') || id.includes('@logicflow') || id.includes('@vue-flow')) return 'flowchart'
            if (id.includes('@wangeditor') || id.includes('md-editor')) return 'editor'
            if (id.includes('exceljs') || id.includes('jspdf') || id.includes('html2canvas')) return 'export'
            if (id.includes('@fortawesome')) return 'icons'
            if (id.includes('fontsource')) return 'fonts'
            if (id.includes('highlight.js') || id.includes('github-markdown-css') || id.includes('marked')) return 'markdown'
            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router') || id.includes('vue-i18n') || id.includes('axios')) return 'vendor'
          }
        }
      }
    }
  }
})

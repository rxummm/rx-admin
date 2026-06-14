import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import 'nprogress/nprogress.css'

// Element Plus 图标全局注册（按需导入配置下，确保所有图标可用）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router, { generateDynamicRoutes } from './router'
import i18n from './i18n'
import { useStorage, STORAGE_KEYS } from './composables/useStorage'
import './styles/variables.scss'
import './styles/global.scss'
import './styles/themes.scss'

const app = createApp(App)

// 全局注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册 Font Awesome 图标
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faGlobe, faExpand, faCompress, faShuffle, faRepeat, faList } from '@fortawesome/free-solid-svg-icons'
library.add(faGlobe, faExpand, faCompress, faShuffle, faRepeat, faList)
app.component('FontAwesomeIcon', FontAwesomeIcon)

// Pinia 必须在 router 之前注册，因为 userStore 在 router 守卫中被使用
const pinia = createPinia()
app.use(pinia)

// i18n 在 router 之前注册
app.use(i18n)

// 在 router install 之前，先用 localStorage 缓存的 menus 注册动态路由
// 避免 install 时首次导航找不到路由
const menusStore = useStorage(STORAGE_KEYS.MENUS, [])
const cachedMenus = menusStore.get()
if (cachedMenus && cachedMenus.length) {
  generateDynamicRoutes(cachedMenus)
}

app.use(router)

// Element Plus 根据 i18n locale 动态设置语言
const elLocaleMap = { 'zh-CN': zhCn, 'en-US': en }
const localeStore = useStorage(STORAGE_KEYS.LOCALE, 'zh-CN')
app.use(ElementPlus, { locale: elLocaleMap[localeStore.get()] || zhCn })

// 生产环境：初始化 Sentry（如果配置了 DSN）
if (import.meta.env.PROD && import.meta.env.VITE_SENTRY_DSN) {
  import('./utils/sentry').then(({ initSentry }) => {
    initSentry(app, router)
  })
}

// 开发环境：启动性能监控
if (import.meta.env.DEV) {
  import('./composables/usePerformanceMonitor').then(({ usePerformanceMonitor }) => {
    const { start } = usePerformanceMonitor()
    start()
    console.log('📊 性能监控已启动')
  })
}

// 全局错误处理（所有环境）
import('./utils/globalErrorHandler').then(({ initGlobalErrorHandler }) => {
  initGlobalErrorHandler()
})

app.mount('#app')

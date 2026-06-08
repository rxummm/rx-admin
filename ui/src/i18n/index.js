import { createI18n } from 'vue-i18n'
import zhCN from './lang/zh-CN'
import enUS from './lang/en-US'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

// 从 localStorage 获取用户语言偏好
const localeStore = useStorage(STORAGE_KEYS.LOCALE, 'zh-CN')

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: localeStore.get(),
  fallbackLocale: 'zh-CN',
  globalInjection: true, // 全局注入 $t
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
})

export default i18n

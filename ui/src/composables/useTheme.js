import { ref } from 'vue'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

const themeStore = useStorage(STORAGE_KEYS.THEME, 'light')
const isDark = ref(themeStore.get() === 'dark')

function applyTheme() {
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
  themeStore.set(isDark.value ? 'dark' : 'light')
  applyTheme()
}

// 初始化时应用主题
applyTheme()

// 对外暴露（作为 composable 使用）
export function useTheme() {
  return { isDark, toggleTheme }
}

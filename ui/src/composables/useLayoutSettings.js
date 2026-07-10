import { ref } from 'vue'
import { useStorage } from './useStorage'
import { COLORS } from '@/config/colors'
import { invalidateCyberTheme } from '@/utils/echartsTheme'

const theme = useStorage('layout_theme', 'default')
const sidebarStyle = useStorage('layout_sidebar_style', 'default')

// 应用主题
function applyTheme(themeName) {
  document.documentElement.setAttribute('data-theme', themeName === 'default' ? '' : themeName)
  invalidateCyberTheme()
}

// 初始化
applyTheme(theme.value)

export function useLayoutSettings() {
  const currentTheme = ref(theme.value)
  const currentSidebarStyle = ref(sidebarStyle.value)

  const setTheme = (name) => {
    currentTheme.value = name
    theme.value = name
    applyTheme(name)
  }

  const setSidebarStyle = (style) => {
    currentSidebarStyle.value = style
    sidebarStyle.value = style
  }

  const themeOptions = COLORS.THEME_OPTIONS

  return {
    currentTheme,
    setTheme,
    themeOptions,
    currentSidebarStyle,
    setSidebarStyle
  }
}

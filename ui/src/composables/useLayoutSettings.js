import { ref, watch } from 'vue'
import { useStorage } from './useStorage'

const theme = useStorage('layout_theme', 'default')
const sidebarStyle = useStorage('layout_sidebar_style', 'default')

// 应用主题
function applyTheme(themeName) {
  document.documentElement.setAttribute('data-theme', themeName === 'default' ? '' : themeName)
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

  const themeOptions = [
    { name: 'default', label: '默认蓝', color: '#409EFF' },
    { name: 'green', label: '翡翠绿', color: '#67C23A' },
    { name: 'purple', label: '深紫', color: '#9C27B0' },
    { name: 'orange', label: '暖橙', color: '#E6A23C' },
    { name: 'cyan', label: '青色', color: '#00BCD4' },
  ]

  return {
    currentTheme, setTheme, themeOptions,
    currentSidebarStyle, setSidebarStyle
  }
}

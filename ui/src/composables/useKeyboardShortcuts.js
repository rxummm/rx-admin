/**
 * 全局快捷键管理服务
 * 提供统一的键盘快捷键注册、注销和执行功能
 */

import { ref, onMounted, onUnmounted } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'

// 快捷键映射表
const shortcuts = ref(new Map())

// 是否启用快捷键
const enabled = ref(true)

// 防止重复触发的标志
let isProcessing = false

/**
 * 注册快捷键
 * @param {string} key - 快捷键组合（如 'Ctrl+B', 'Alt+Left'）
 * @param {Function} handler - 执行函数
 * @param {string} description - 描述信息
 */
export function registerShortcut(key, handler, description = '') {
  const normalizedKey = normalizeKey(key)
  shortcuts.value.set(normalizedKey, { handler, description })
}

/**
 * 注销快捷键
 * @param {string} key - 快捷键组合
 */
export function unregisterShortcut(key) {
  const normalizedKey = normalizeKey(key)
  shortcuts.value.delete(normalizedKey)
}

/**
 * 清空所有快捷键
 */
export function clearShortcuts() {
  shortcuts.value.clear()
}

/**
 * 启用/禁用快捷键
 */
export function toggleShortcuts(enable) {
  enabled.value = enable !== undefined ? enable : !enabled.value
}

/**
 * 标准化快捷键格式
 */
function normalizeKey(key) {
  return key
    .toLowerCase()
    .replace(/\s+/g, '')
    .replace('ctrl', 'control')
    .replace('left', 'arrowleft')
    .replace('right', 'arrowright')
    .replace('up', 'arrowup')
    .replace('down', 'arrowdown')
}

/**
 * 解析键盘事件为快捷键字符串
 */
function parseKeyEvent(e) {
  const keys = []

  if (e.ctrlKey || e.metaKey) keys.push('control')
  if (e.altKey) keys.push('alt')
  if (e.shiftKey) keys.push('shift')

  // 特殊键处理
  if (e.key === ' ') keys.push('space')
  else if (e.key.startsWith('Arrow')) keys.push(e.key.toLowerCase())
  else if (e.key.length === 1) keys.push(e.key.toLowerCase())
  else keys.push(e.key.toLowerCase())

  return keys.join('+')
}

/**
 * 键盘事件处理器
 */
function handleKeyDown(e) {
  // 如果禁用或正在处理，跳过
  if (!enabled.value || isProcessing) return

  // 忽略输入框内的快捷键（除非是 Escape）
  const target = e.target
  const isInput = target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable

  if (isInput && e.key !== 'Escape') return

  const keyCombo = parseKeyEvent(e)
  const shortcut = shortcuts.value.get(keyCombo)

  if (shortcut) {
    e.preventDefault()
    e.stopPropagation()

    isProcessing = true
    try {
      shortcut.handler(e)
    } catch (error) {
      console.error(`快捷键执行失败 [${keyCombo}]:`, error)
    } finally {
      // 延迟重置，防止快速按键导致重复触发
      setTimeout(() => {
        isProcessing = false
      }, 100)
    }
  }
}

/**
 * 初始化全局快捷键监听
 */
export function initGlobalShortcuts() {
  document.addEventListener('keydown', handleKeyDown, true)
}

/**
 * 销毁全局快捷键监听
 */
export function destroyGlobalShortcuts() {
  document.removeEventListener('keydown', handleKeyDown, true)
}

/**
 * Vue Composition API - 在组件中使用快捷键
 */
export function useKeyboardShortcuts(shortcutConfigs = []) {
  const { isDark, toggleTheme } = useTheme()

  onMounted(() => {
    // 注册默认快捷键
    registerDefaultShortcuts()

    // 注册自定义快捷键
    shortcutConfigs.forEach(({ key, handler, description }) => {
      registerShortcut(key, handler, description)
    })

    // 初始化监听
    initGlobalShortcuts()
  })

  onUnmounted(() => {
    // 清理自定义快捷键
    shortcutConfigs.forEach(({ key }) => {
      unregisterShortcut(key)
    })
  })

  /**
   * 获取所有已注册的快捷键列表（用于显示帮助）
   */
  function getRegisteredShortcuts() {
    const list = []
    shortcuts.value.forEach((value, key) => {
      list.push({
        key: formatKeyDisplay(key),
        description: value.description
      })
    })
    return list.sort((a, b) => a.key.localeCompare(b.key))
  }

  return {
    registerShortcut,
    unregisterShortcut,
    getRegisteredShortcuts,
    toggleShortcuts
  }
}

/**
 * 注册默认快捷键
 */
function registerDefaultShortcuts() {
  // Ctrl+B: 切换侧边栏
  registerShortcut(
    'Ctrl+B',
    () => {
      try {
        const collapseBtn = document.querySelector('.collapse-btn')
        if (collapseBtn) {
          collapseBtn.click()
          ElMessage.success('侧边栏已切换')
        } else {
          ElMessage.warning('未找到侧边栏切换按钮')
        }
      } catch (error) {
        console.error('切换侧边栏失败:', error)
        ElMessage.error('切换侧边栏失败')
      }
    },
    '切换侧边栏'
  )

  // Ctrl+D: 切换主题（动态获取 useTheme）
  registerShortcut(
    'Ctrl+D',
    () => {
      try {
        const { toggleTheme } = useTheme()
        toggleTheme()
        // 延迟获取 isDark 值，确保主题已切换
        setTimeout(() => {
          const currentIsDark = document.documentElement.classList.contains('dark')
          ElMessage.success(`已切换到${currentIsDark ? '暗色' : '亮色'}主题`)
        }, 100)
      } catch (error) {
        console.error('切换主题失败:', error)
        ElMessage.error('切换主题失败')
      }
    },
    '切换暗色/亮色主题'
  )

  // Ctrl+R: 刷新当前页面数据
  registerShortcut(
    'Ctrl+R',
    () => {
      try {
        // 触发自定义事件，页面可以监听此事件来刷新数据
        window.dispatchEvent(new CustomEvent('refresh-page-data'))
        ElMessage.success('页面数据已刷新')
      } catch (error) {
        console.error('刷新数据失败:', error)
        ElMessage.error('刷新数据失败')
      }
    },
    '刷新当前页面'
  )

  // Ctrl+F: 页面内搜索
  registerShortcut(
    'Ctrl+F',
    () => {
      try {
        // 优先查找命令面板的搜索框
        let searchInput = document.querySelector('.cp-input')

        // 如果命令面板未打开，查找页面内的搜索框
        if (!searchInput) {
          searchInput = document.querySelector('.search-bar input[type="text"], .search-bar .el-input__inner')
        }

        // 最后尝试通用输入框
        if (!searchInput) {
          searchInput = document.querySelector('input[type="text"]:not([disabled]), .el-input__inner:not([disabled])')
        }

        if (searchInput) {
          searchInput.focus()
          searchInput.select()
          ElMessage.success('已聚焦到搜索框')
        } else {
          ElMessage.info('提示：按 Ctrl+K 打开全局搜索')
        }
      } catch (error) {
        console.error('搜索框聚焦失败:', error)
      }
    },
    '页面内搜索'
  )

  // Esc: 关闭弹窗/菜单
  registerShortcut(
    'Escape',
    () => {
      try {
        // 查找当前打开的 Element Plus 弹窗
        const overlays = document.querySelectorAll('.el-overlay')
        let closed = false

        // 从后往前关闭（优先关闭最上层的弹窗）
        for (let i = overlays.length - 1; i >= 0; i--) {
          const overlay = overlays[i]
          // 检查是否可见
          if (overlay.style.display !== 'none' && overlay.offsetParent !== null) {
            // 触发遮罩层的点击事件来关闭弹窗
            const event = new MouseEvent('click', {
              bubbles: true,
              cancelable: true,
              view: window
            })
            overlay.dispatchEvent(event)
            closed = true
            break // 只关闭一个弹窗
          }
        }

        // 如果没有找到弹窗，尝试关闭右键菜单
        if (!closed) {
          const contextMenus = document.querySelectorAll('.tags-context-menu, .context-menu')
          contextMenus.forEach((menu) => {
            if (menu.style.display !== 'none') {
              menu.style.display = 'none'
              closed = true
            }
          })
        }

        if (closed) {
          ElMessage.success('已关闭弹窗/菜单')
        }
      } catch (error) {
        console.error('关闭弹窗失败:', error)
      }
    },
    '关闭弹窗/菜单'
  )

  // Alt+Left: 后退
  registerShortcut(
    'Alt+ArrowLeft',
    () => {
      window.history.back()
    },
    '浏览器后退'
  )

  // Alt+Right: 前进
  registerShortcut(
    'Alt+ArrowRight',
    () => {
      window.history.forward()
    },
    '浏览器前进'
  )
}

/**
 * 格式化快捷键显示
 */
function formatKeyDisplay(key) {
  return key
    .replace('control', 'Ctrl')
    .replace('alt', 'Alt')
    .replace('shift', 'Shift')
    .replace('arrowleft', '←')
    .replace('arrowright', '→')
    .replace('arrowup', '↑')
    .replace('arrowdown', '↓')
    .replace('space', 'Space')
    .split('+')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' + ')
}

/**
 * 显示快捷键帮助对话框
 */
export function showShortcutsHelp() {
  // 触发自定义事件，由 layout 组件监听并打开对话框
  window.dispatchEvent(new CustomEvent('show-shortcuts-help'))
}

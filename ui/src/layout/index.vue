<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-container" @click="goHome">
        <img src="@/assets/logo.svg" class="logo-img" />
        <span v-show="!isCollapse" class="logo-title">RX Admin</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          @select="handleMenuSelect"
        >
          <SubMenu v-for="menu in userStore.menus" :key="menu.id" :menu="menu" />
        </el-menu>
      </el-scrollbar>
      <!-- 快捷收藏 -->
      <FavoritesPanel />
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="layout-right-container">
      <!-- 顶栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">{{ $t('layout.home') }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ tMenu(route.meta.title) }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 全局搜索 -->
          <SearchBox ref="searchBoxRef" @open-command="commandPaletteRef?.open()" />
          <el-tooltip content="Ctrl+K 命令面板" placement="bottom">
            <div class="header-action-btn header-search-global" @click="commandPaletteRef?.open()">
              <el-icon><Search /></el-icon>
              <kbd>Ctrl+K</kbd>
            </div>
          </el-tooltip>
          <!-- 暗黑/明亮切换 -->
          <el-tooltip :content="isDark ? $t('layout.switchLight') : $t('layout.switchDark')" placement="bottom">
            <div class="header-action-btn" @click="toggleTheme">
              <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
            </div>
          </el-tooltip>
          <!-- 语言切换 -->
          <el-tooltip :content="$t('layout.switchLanguage')" placement="bottom">
            <div class="header-action-btn" @click="handleToggleLocale">
              <FontAwesomeIcon icon="globe" />
            </div>
          </el-tooltip>
          <!-- 通知公告 / 待办事项 -->
          <NoticePopover ref="noticePopoverRef" />
          <!-- 全屏 -->
          <el-tooltip
            :content="isFullscreen ? $t('layout.exitFullscreen') : $t('layout.fullscreen')"
            placement="bottom"
          >
            <div class="header-action-btn" @click="toggleFullscreen">
              <FontAwesomeIcon :icon="isFullscreen ? 'compress' : 'expand'" />
            </div>
          </el-tooltip>
          <!-- 快捷键帮助 -->
          <el-tooltip content="快捷键帮助 (?)" placement="bottom">
            <div class="header-action-btn" @click="shortcutsHelpRef?.open()">
              <el-icon><QuestionFilled /></el-icon>
            </div>
          </el-tooltip>
          <!-- 性能监控（仅开发环境） -->
          <el-tooltip v-if="isDev" content="性能监控" placement="bottom">
            <div class="header-action-btn" @click="performancePanelRef?.open()">
              <el-icon><Monitor /></el-icon>
            </div>
          </el-tooltip>
          <!-- 主题色切换 -->
          <el-tooltip content="主题色切换" placement="bottom">
            <el-dropdown trigger="click" @command="handleThemeChange">
              <div class="header-action-btn">
                <FontAwesomeIcon icon="palette" />
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="opt in themeOptions" :key="opt.name" :command="opt.name">
                    <span class="theme-color-dot" :style="{ background: opt.color }"></span>
                    <span :class="{ 'theme-active': currentTheme === opt.name }">{{ opt.label }}</span>
                    <el-icon v-if="currentTheme === opt.name" class="theme-checked"><Check /></el-icon>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-tooltip>
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">
                  <el-icon><User /></el-icon> {{ $t('layout.personalInfo') }}
                </el-dropdown-item>
                <el-dropdown-item v-if="!userStore.hasRole('admin')" @click="router.push('/permission/request')">
                  <el-icon><Key /></el-icon> {{ $t('layout.permissionRequest') }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> {{ $t('layout.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 标签页 -->
      <TagsView />

      <!-- 主内容 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component, route: compRoute }">
          <keep-alive :include="tagsStore.cachedViews">
            <component :is="Component" :key="compRoute.name + '-' + (tagsStore.refreshKeys[compRoute.name] || 0)" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>

    <!-- 全局命令面板 (Ctrl+K) -->
    <CommandPalette ref="commandPaletteRef" />
    <!-- 系统公告弹窗 -->
    <AnnouncementPopup />
    <!-- 快捷键帮助 -->
    <ShortcutsHelp ref="shortcutsHelpRef" />
    <!-- 性能监控面板（仅开发环境） -->
    <PerformancePanel v-if="isDev" ref="performancePanelRef" />
  </el-container>
</template>

<script setup>
defineOptions({ name: 'Layout' })
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resetDynamicRoutes } from '@/router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useTagsStore } from '@/stores/tags'
import { useTheme } from '@/composables/useTheme'
import { useMenuI18n } from '@/composables/useMenuI18n'
import { useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'
import { logoutApi } from '@/api/auth'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
import { useLayoutSettings } from '@/composables/useLayoutSettings'
import { useNotificationWebSocket } from '@/composables/useNotificationWebSocket'
import { ElMessage, ElMessageBox } from 'element-plus'
import TagsView from './TagsView.vue'
import SubMenu from './SubMenu.vue'
import SearchBox from './SearchBox.vue'
import NoticePopover from './NoticePopover.vue'
import CommandPalette from '@/components/CommandPalette.vue'
import AnnouncementPopup from '@/components/AnnouncementPopup.vue'
import FavoritesPanel from '@/components/FavoritesPanel.vue'
import ShortcutsHelp from '@/components/ShortcutsHelp.vue'
import PerformancePanel from '@/components/PerformancePanel.vue'
import { QuestionFilled, Monitor, Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tagsStore = useTagsStore()
const { isDark, toggleTheme } = useTheme()
const { currentTheme, setTheme, themeOptions } = useLayoutSettings()
const notificationWs = useNotificationWebSocket()

const { tMenu } = useMenuI18n()
const { locale, t } = useI18n()
const localeStore = useStorage(STORAGE_KEYS.LOCALE, 'zh-CN')

// 初始化全局快捷键
useKeyboardShortcuts([
  {
    key: '?',
    handler: () => shortcutsHelpRef.value?.open(),
    description: '显示快捷键帮助'
  }
])

const isCollapse = ref(false)
const isFullscreen = ref(false)
const isDev = import.meta.env.DEV // 开发环境标志
const searchBoxRef = ref(null)
const noticePopoverRef = ref(null)
const commandPaletteRef = ref(null)
const shortcutsHelpRef = ref(null)
const performancePanelRef = ref(null)

const activeMenu = computed(() => route.path)

// 防止快速点击菜单导致重复导航
let navigating = false

function handleThemeChange(name) {
  setTheme(name)
}

function handleToggleLocale() {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localeStore.set(newLocale)
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement
      .requestFullscreen()
      .then(() => (isFullscreen.value = true))
      .catch(() => {})
  } else {
    document
      .exitFullscreen()
      .then(() => (isFullscreen.value = false))
      .catch(() => {})
  }
}

function handleMenuSelect(index) {
  // 避免重复导航到当前页面
  if (index === route.path) return
  // 防止快速点击造成路由竞争导致标签异常
  if (navigating) return
  navigating = true
  router
    .push(index)
    .catch((err) => {
      // 动态 import 失败 / 路由不存在时，打印但不抛出，避免 unhandledrejection 把整个 SPA 弄退化
      // （退化表现：keep-alive 卸载、标签页看似丢失）
      console.warn('[router.push failed]', index, err)
      ElMessage.error(`页面加载失败: ${err?.message || err}`)
    })
    .finally(() => {
      navigating = false
    })
}

onMounted(() => {
  tagsStore.addView({
    path: '/dashboard',
    name: 'Dashboard',
    meta: { title: t('layout.dashboard'), affix: true, icon: 'DataAnalysis' }
  })

  // 监听快捷键帮助事件
  window.addEventListener('show-shortcuts-help', () => {
    shortcutsHelpRef.value?.open()
  })

  // 建立统一通知 WebSocket 连接
  notificationWs.connect()
})

onUnmounted(() => {
  notificationWs.disconnect()
})

watch(
  () => route.path,
  (path) => {
    // 跳过登录页和根路径（Layout自身），只处理业务页面
    if (path === '/login' || path === '/') return
    const title = route.meta?.title || ''
    const icon = route.meta?.icon
    if (title) {
      tagsStore.addView({
        path,
        name: route.name || path,
        meta: { title, affix: path === '/dashboard', icon }
      })
    }
    tagsStore.setActivePath(path)
  },
  { immediate: true }
)

function goHome() {
  router.push('/dashboard')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm(t('layout.logoutConfirm'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    try {
      await logoutApi()
    } catch {}
    resetDynamicRoutes()
    userStore.logout()
    tagsStore.removeAllViews()
    await nextTick()
    router.push('/login')
  } catch {
    // 用户取消对话框，不做处理
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100%;
  overflow: hidden;
  display: flex;

  // ====== 侧边栏 ======
  .layout-aside {
    background-color: var(--sidebar-bg);
    overflow: hidden;
    transition: width 0.3s;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    border-right: 1px solid var(--border-light);

    .logo-container {
      height: var(--header-height);
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 0 12px;
      border-bottom: 1px solid var(--sidebar-logo-border);
      background: var(--sidebar-logo-bg);
      transition: padding var(--transition-slow);

      .logo-img {
        width: 32px;
        height: 32px;
        flex-shrink: 0;
        transition: transform var(--transition-base);
        &:hover {
          transform: scale(1.1);
        }
      }

      .logo-title {
        margin-left: 10px;
        color: var(--sidebar-logo-color);
        font-size: 18px;
        font-weight: 700;
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .el-scrollbar {
      flex: 1;
      min-height: 0;
      overflow: hidden;
    }

    // 使用 CSS 变量覆盖 el-menu 内部色值，替代废弃的 background-color/text-color 属性
    :deep(.el-menu) {
      --el-menu-bg-color: var(--sidebar-bg);
      --el-menu-text-color: var(--sidebar-text);
      --el-menu-active-color: var(--sidebar-text-active);
    }
    :deep(.el-sub-menu .el-menu) {
      background-color: var(--sidebar-submenu-bg) !important;
    }
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      transition:
        background var(--transition-fast),
        color var(--transition-fast) !important;
      margin: 2px 8px;
      border-radius: var(--radius-sm);

      &:hover {
        background-color: var(--sidebar-item-hover-bg) !important;
      }
    }
    // 激活项：背景色 + 左侧指示条
    :deep(.el-menu-item.is-active) {
      background-color: var(--sidebar-item-active-bg) !important;
      color: var(--sidebar-text-active) !important;
      position: relative;
      font-weight: 500;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        border-radius: 0 var(--radius-xs) var(--radius-xs) 0;
        background: var(--sidebar-active-indicator);
      }
    }
  }

  // ====== 顶栏（玻璃态） ======
  .layout-header {
    background: var(--header-bg);
    backdrop-filter: var(--header-backdrop);
    -webkit-backdrop-filter: var(--header-backdrop);
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: var(--shadow-header);
    padding: 0 20px;
    height: 50px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        color: var(--text-regular);
        width: 34px;
        height: 34px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: var(--radius-sm);
        transition: all var(--transition-fast);

        &:hover {
          background: var(--bg-hover);
          color: var(--color-primary);
        }
      }

      // 面包屑
      :deep(.el-breadcrumb) {
        font-size: 13px;

        .el-breadcrumb__inner {
          color: var(--text-regular);
          font-weight: 400;
          transition: color var(--transition-fast);

          &:hover {
            color: var(--color-primary);
          }
        }
        .el-breadcrumb__item:last-child .el-breadcrumb__inner {
          color: var(--text-primary);
          font-weight: 500;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 4px;

      .header-search-box {
        width: 200px;
      }

      .header-action-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 34px;
        height: 34px;
        border-radius: var(--radius-sm);
        cursor: pointer;
        color: var(--text-regular);
        font-size: 17px;
        transition: all var(--transition-fast);
        position: relative;

        &:hover {
          background: var(--bg-hover);
          color: var(--color-primary);
        }
        &:active {
          transform: scale(0.94);
        }
      }

      .header-search-global {
        width: auto;
        gap: 4px;
        padding: 0 8px;
        font-size: 14px;
        color: var(--text-secondary);
        border: 1px solid var(--border-light);
        border-radius: var(--radius-full);

        &:hover {
          border-color: var(--color-primary);
          color: var(--color-primary);
          background: var(--bg-active);
        }

        kbd {
          font-size: 10px;
          padding: 1px 5px;
          line-height: 1.5;
          color: var(--text-secondary);
          background: var(--bg-hover);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-xs);
          user-select: none;
        }
      }

      // 用户下拉触发区
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 10px;
        margin-left: 4px;
        border-radius: var(--radius-sm);
        transition: background var(--transition-fast);

        &:hover {
          background: var(--bg-hover);
        }

        .username {
          font-size: 14px;
          color: var(--text-regular);
          max-width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .el-icon {
          font-size: 12px;
          color: var(--text-secondary);
          transition: transform var(--transition-fast);
        }
        &:hover .el-icon {
          transform: rotate(180deg);
        }
      }
    }
  }

  // ====== 右侧容器：让 el-header / TagsView / el-main 纵向 flex 分配 ======
  .layout-right-container {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    min-height: 0;
    overflow: hidden;
  }

  .layout-header {
    flex-shrink: 0;
  }

  // ====== 主内容：flex自动填满剩余高度，内容溢出时内部滚动 ======
  .layout-main {
    background: var(--bg-page);
    background-image: radial-gradient(circle, var(--border-light) 1px, transparent 1px);
    background-size: 24px 24px;
    padding: 10px;
    flex: 1;
    min-height: 0;
    overflow: auto;
  }
}

// ====== 动画 ======
.search-dropdown-fade-enter-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}
.search-dropdown-fade-leave-active {
  transition: opacity 0.1s ease;
}
.search-dropdown-fade-enter-from,
.search-dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.notice-badge {
  position: relative;
  display: inline-flex;

  :deep(.el-badge__content) {
    border: 2px solid var(--badge-border);
  }

  :deep(.el-badge) {
    display: flex;
    align-items: center;
  }
}

.route-loading-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: var(--text-secondary, #909399);
  font-size: 14px;
  gap: 8px;

  .el-icon {
    font-size: 32px;
  }
}

// ====== 响应式断点（全局：平板、手机端） ======
// ≤ 1024px 平板：收紧侧边栏、压缩搜索框
@media (max-width: 1024px) {
  .layout-container .layout-aside {
    width: var(--sidebar-collapse-width) !important;

    .el-menu--vertical {
      .el-sub-menu__title span,
      .el-menu-item span {
        display: none;
      }
    }

    .logo-text {
      display: none;
    }
  }

  .layout-container .layout-header {
    .header-search-box {
      width: 160px;
    }
  }
}

// ≤ 768px 手机：隐藏标签栏、紧凑布局
@media (max-width: 768px) {
  .layout-container {
    .layout-aside {
      position: fixed;
      z-index: var(--z-content, 10);
      transform: translateX(-100%);
      transition: transform var(--transition-base);

      &.is-mobile-open {
        transform: translateX(0);
      }
    }

    .layout-header {
      .header-search-box {
        width: 120px;
      }
    }
  }
}

// ====== 主题色切换下拉 ======
.theme-color-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: middle;
  border: 1px solid var(--border-color);
}

.theme-active {
  font-weight: 600;
}

.theme-checked {
  margin-left: auto;
  font-size: 14px;
  color: var(--color-primary);
}
</style>

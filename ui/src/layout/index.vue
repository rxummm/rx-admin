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
          :background-color="sidebarBgColor"
          :text-color="sidebarTextColor"
          :active-text-color="sidebarActiveColor"
          @select="handleMenuSelect"
        >
          <SubMenu v-for="menu in userStore.menus" :key="menu.id" :menu="menu" />
        </el-menu>
      </el-scrollbar>
      <!-- 快捷收藏 -->
      <FavoritesPanel />
    </el-aside>

    <!-- 右侧主体 -->
    <el-container>
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
          <el-tooltip :content="isFullscreen ? $t('layout.exitFullscreen') : $t('layout.fullscreen')" placement="bottom">
            <div class="header-action-btn" @click="toggleFullscreen">
              <FontAwesomeIcon :icon="isFullscreen ? 'compress' : 'expand'" />
            </div>
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
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resetDynamicRoutes } from '@/router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useTagsStore } from '@/stores/tags'
import { useTheme } from '@/composables/useTheme'
import { useMenuI18n } from '@/composables/useMenuI18n'
import { logoutApi } from '@/api/auth'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
import { ElMessageBox } from 'element-plus'
import TagsView from './TagsView.vue'
import SubMenu from './SubMenu.vue'
import SearchBox from './SearchBox.vue'
import NoticePopover from './NoticePopover.vue'
import CommandPalette from '@/components/CommandPalette.vue'
import AnnouncementPopup from '@/components/AnnouncementPopup.vue'
import FavoritesPanel from '@/components/FavoritesPanel.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tagsStore = useTagsStore()
const { isDark, toggleTheme } = useTheme()
const { tMenu } = useMenuI18n()
const { locale, t } = useI18n()
const localeStore = useStorage(STORAGE_KEYS.LOCALE, 'zh-CN')

const isCollapse = ref(false)
const isFullscreen = ref(false)
const searchBoxRef = ref(null)
const noticePopoverRef = ref(null)
const commandPaletteRef = ref(null)

const activeMenu = computed(() => route.path)

// 防止快速点击菜单导致重复导航
let navigating = false

function handleToggleLocale() {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localeStore.set(newLocale)
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().then(() => isFullscreen.value = true).catch(() => {})
  } else {
    document.exitFullscreen().then(() => isFullscreen.value = false).catch(() => {})
  }
}

function handleMenuSelect(index) {
  // 避免重复导航到当前页面
  if (index === route.path) return
  // 防止快速点击造成路由竞争导致标签异常
  if (navigating) return
  navigating = true
  router.push(index).finally(() => {
    navigating = false
  })
}

const sidebarBgColor = computed(() => isDark.value ? '#1d1e1f' : '#ffffff')
const sidebarTextColor = computed(() => isDark.value ? '#a3a6ad' : '#606266')
const sidebarActiveColor = computed(() => '#409eff')

onMounted(() => {
  tagsStore.addView({
    path: '/dashboard',
    name: 'Dashboard',
    meta: { title: t('layout.dashboard'), affix: true, icon: 'DataAnalysis' }
  })
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
    try { await logoutApi() } catch (e) {}
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

  .layout-aside {
    background-color: var(--sidebar-bg);
    overflow: hidden;
    transition: width 0.3s;
    display: flex;
    flex-direction: column;

    .logo-container {
      height: 50px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 0 12px;
      border-bottom: 1px solid var(--sidebar-logo-border);

      .logo-img {
        width: 32px;
        height: 32px;
        flex-shrink: 0;
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

    .el-menu {
      border-right: none;
    }

    :deep(.el-sub-menu .el-menu) {
      background-color: var(--sidebar-submenu-bg) !important;
    }
    :deep(.el-menu-item:hover),
    :deep(.el-sub-menu__title:hover) {
      background-color: var(--sidebar-item-hover-bg) !important;
    }
  }

  .layout-header {
    background: var(--header-bg);
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: var(--shadow-header);
    padding: 0 20px;
    height: 50px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        color: var(--text-regular);
        &:hover { color: var(--color-primary); }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .header-search-box {
        width: 200px;
      }

      .header-action-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 34px;
        height: 34px;
        border-radius: 6px;
        cursor: pointer;
        color: var(--text-regular);
        font-size: 18px;
        transition: all 0.2s;

        &:hover {
          background: var(--bg-active);
          color: var(--color-primary);
        }
      }

      .header-search-global {
        width: auto;
        gap: 4px;
        padding: 0 8px;

        kbd {
          font-size: 11px;
          padding: 1px 5px;
          line-height: 1.4;
          color: var(--text-secondary, #909399);
          background: var(--el-fill-color-light, #f0f2f5);
          border: 1px solid var(--el-border-color-lighter, #dcdfe6);
          border-radius: 4px;
          user-select: none;
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;

        &:hover { background: var(--bg-hover); }

        .username {
          font-size: 14px;
          color: var(--text-regular);
          max-width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .layout-main {
    background: var(--bg-page);
    padding: 10px;
    overflow-y: auto;
    height: calc(100vh - 50px - 37px);
  }
}

.search-dropdown-fade-enter-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
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
</style>

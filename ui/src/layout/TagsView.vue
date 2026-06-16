<template>
  <div class="tags-view-container" v-if="visitedViews.length">
    <el-scrollbar class="tags-scrollbar" @wheel.prevent="handleWheel">
      <div class="tags-wrapper" ref="tagsWrapperRef">
        <router-link
          v-for="tag in visitedViews"
          :key="tag.path"
          :to="tag.path"
          class="tags-item"
          :class="{ active: isActive(tag) }"
          @contextmenu.prevent="openContextMenu($event, tag)"
        >
          <el-icon class="tags-item-icon"><component :is="tagIcon(tag)" /></el-icon>
          <span class="tags-item-title">{{ tMenu(tag.meta?.title || tag.name) }}</span>
          <el-icon
            v-if="!isAffix(tag)"
            class="tags-item-close"
            @click.prevent.stop="handleClose(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>

    <!-- 右键菜单 -->
    <Teleport to="body">
      <ul
        v-show="contextMenuVisible"
        class="tags-context-menu"
        :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }"
      >
        <li @click="handleRefresh">
          <el-icon><Refresh /></el-icon> {{ $t('layout.tags.refresh') }}
        </li>
        <li @click="handleCloseCurrent" :class="{ disabled: isAffix(selectedTag) }">
          <el-icon><Close /></el-icon> {{ $t('layout.tags.closeCurrent') }}
        </li>
        <li @click="handleCloseOthers">
          <el-icon><CircleClose /></el-icon> {{ $t('layout.tags.closeOthers') }}
        </li>
        <li @click="handleCloseAll">
          <el-icon><Remove /></el-icon> {{ $t('layout.tags.closeAll') }}
        </li>
        <li class="context-menu-divider" />
        <li @click="handleToggleFavorite">
          <el-icon><StarFilled v-if="isTagFav" color="#F56C6C" /><Star v-else /></el-icon>
          {{ isTagFav ? $t('layout.tags.removeFav') : $t('layout.tags.addFav') }}
        </li>
      </ul>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTagsStore } from '@/stores/tags'
import { useMenuI18n } from '@/composables/useMenuI18n'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { toggleFavoriteApi } from '@/api/favorite'
import { useFavEvents } from '@/composables/useFavEvents'
import { useStorage, STORAGE_KEYS, useNamespacedKey, getKeysByPrefix, removeKeysByPrefix } from '@/composables/useStorage'

const { t } = useI18n()
const { tMenu } = useMenuI18n()

const route = useRoute()
const router = useRouter()
const tagsStore = useTagsStore()

const visitedViews = computed(() => tagsStore.visitedViews)
const tagsWrapperRef = ref(null)

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const selectedTag = ref({})

function isActive(tag) {
  return tag.path === route.path
}

function isAffix(tag) {
  return tag.meta?.affix
}

// 获取标签对应的菜单图标，优先从 meta.icon 取，否则回退默认
function tagIcon(tag) {
  return tag.meta?.icon || 'Menu'
}

// 关闭标签
async function handleClose(tag) {
  const nextPath = tagsStore.closeView(tag)
  if (nextPath) {
    await router.push(nextPath)
  }
}

// 右键菜单
function openContextMenu(e, tag) {
  contextMenuVisible.value = true
  contextMenuLeft.value = e.clientX
  contextMenuTop.value = e.clientY
  selectedTag.value = tag
}

function closeContextMenu() {
  contextMenuVisible.value = false
}

function handleRefresh() {
  tagsStore.refreshView(selectedTag.value)
  ElMessage.success(t('layout.tags.refreshDone'))
  closeContextMenu()
}

async function handleCloseCurrent() {
  if (isAffix(selectedTag.value)) {
    ElMessage.warning(t('layout.tags.affixWarning'))
    closeContextMenu()
    return
  }
  const nextPath = tagsStore.closeView(selectedTag.value)
  if (nextPath) {
    await router.push(nextPath)
  }
  closeContextMenu()
}

function handleCloseOthers() {
  tagsStore.removeOtherViews(selectedTag.value)
  closeContextMenu()
}

async function handleCloseAll() {
  tagsStore.removeAllViews()
  // 如果当前标签被关闭，跳转第一个剩余标签或dashboard
  const stillExists = visitedViews.value.some(v => v.path === route.path)
  if (!stillExists) {
    const target = visitedViews.value.length > 0 ? visitedViews.value[0].path : '/dashboard'
    await router.push(target)
  }
  closeContextMenu()
}

// 收藏夹切换
const { refreshTick, triggerRefresh } = useFavEvents()
const favSet = ref(new Set())

// 从统一命名空间 key 读取已收藏路径
// 新格式：rx_admin_favorite_star:/path  （由 useNamespacedKey 生成）
// 兼容旧格式：fav_/path  （逐步淘汰）
const FAV_PREFIX = `${STORAGE_KEYS.FAVORITE_STAR}:`
const LEGACY_PREFIX = 'fav_'

const syncFavSet = () => {
  const set = new Set()

  // 优先读取集中式收藏列表（单 key，JSON 数组）
  const favStore = useStorage(STORAGE_KEYS.FAVORITES)
  const centralized = favStore.get()
  if (Array.isArray(centralized) && centralized.length > 0) {
    centralized.forEach(p => set.add(p))
    favSet.value = set
    return
  }

  // 集中式不存在时，从旧格式迁移
  const legacyKeys = getKeysByPrefix(LEGACY_PREFIX)
  for (const k of legacyKeys) {
    const path = k.slice(LEGACY_PREFIX.length)
    set.add(path)
  }

  // 遍历命名空间 key
  const favKeys = getKeysByPrefix(FAV_PREFIX)
  for (const k of favKeys) {
    set.add(k.slice(FAV_PREFIX.length))
  }

  // 迁移到集中式存储后清除旧 key
  if (set.size > 0) {
    favStore.set([...set])
    removeKeysByPrefix(FAV_PREFIX)
    removeKeysByPrefix(LEGACY_PREFIX)
  }

  favSet.value = set
}
syncFavSet()
// 其他组件取消收藏后，通过 refreshTick 通知 TagsView 同步
watch(refreshTick, syncFavSet)

const isTagFav = computed(() => favSet.value.has(selectedTag.value.path))

async function handleToggleFavorite() {
  const tag = selectedTag.value
  const favKey = useNamespacedKey(STORAGE_KEYS.FAVORITE_STAR, tag.path)
  const favStore = useStorage(favKey)
  try {
    const res = await toggleFavoriteApi({
      name: tag.meta?.title || tag.name,
      path: tag.path,
      icon: tag.meta?.icon || '',
      menuId: tag.meta?.id || null
    })
    if (res.data?.collected) {
      favStore.set(res.data?.id || '1')
    } else {
      favStore.remove()
    }
    syncFavSet()
    triggerRefresh()
  } catch (e) { console.error('[ToggleFavorite]', e) }
  closeContextMenu()
}

// 鼠标滚轮水平滚动（支持 Shift+滚轮 或直接滚轮）
function handleWheel(e) {
  // el-scrollbar 的实际滚动容器是内部的 .el-scrollbar__wrap，不是 tags-wrapper
  const wrap = tagsWrapperRef.value?.closest('.el-scrollbar')?.querySelector('.el-scrollbar__wrap')
  if (wrap) {
    // deltaY > 0 = 向下滚动 → 向右；deltaY < 0 = 向上 → 向左
    wrap.scrollLeft += e.deltaY + (e.deltaX || 0)
    // 同时更新 el-scrollbar 水平滚动条的显示位置
    const bar = wrap.closest('.el-scrollbar')?.querySelector('.el-scrollbar__bar.is-horizontal')
    if (bar) {
      const thumb = bar.querySelector('.el-scrollbar__thumb')
      if (thumb) {
        const maxScroll = wrap.scrollWidth - wrap.clientWidth
        const ratio = maxScroll > 0 ? wrap.scrollLeft / maxScroll : 0
        thumb.style.transform = `translateX(${ratio * (bar.clientWidth - thumb.offsetWidth)}px)`
      }
    }
  }
}

// 点击其他地方关闭右键菜单
function handleClickOutside(e) {
  if (!e.target.closest('.tags-context-menu')) {
    closeContextMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
.tags-view-container {
  flex-shrink: 0;
  background: var(--tags-bg);
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  user-select: none;

  .tags-scrollbar {
    height: 36px;
    white-space: nowrap;

    :deep(.el-scrollbar__view) {
      height: 36px;
    }
  }

  .tags-wrapper {
    display: inline-flex;
    align-items: center;
    height: 36px;
    padding: 0 4px;
  }

  .tags-item {
    display: inline-flex;
    align-items: center;
    height: 28px;
    padding: 0 10px;
    margin: 0 2px;
    font-size: 12px;
    color: var(--tags-item-color);
    background: var(--tags-item-bg);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    cursor: pointer;
    text-decoration: none;
    transition: all var(--transition-fast);
    white-space: nowrap;

    &:hover {
      color: var(--tags-item-hover-color);
      background: var(--tags-item-hover-bg);
      border-color: var(--tags-item-hover-border);
    }

    &.active {
      color: var(--tags-item-active-color);
      background: var(--tags-item-active-bg);
      border-color: var(--tags-item-active-bg);

      .tags-item-close {
        color: var(--tags-item-active-color);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }

    .tags-item-title {
      margin-right: 6px;
    }

    .tags-item-icon {
      margin-right: 4px;
      font-size: 13px;
    }

    .tags-item-close {
      font-size: 12px;
      border-radius: 50%;
      padding: 1px;
      transition: background var(--transition-fast);

      &:hover {
        background: var(--tag-close-hover-bg);
      }
    }
  }
}
</style>

<!-- 全局样式：右键菜单不 scoped -->
<style lang="scss">
.tags-context-menu {
  position: fixed;
  // 必须高于 Element Plus 浮层（el-index-popper=2000）才能压住列标题/下拉等
  // ⚠️ 硬编码兜底 99999 在前：即使 --z-teleport 变量未定义（CSS 回退成 auto）也依然生效。
  // 不要用 var() 单独一行写，曾经因此被列标题/tab 下划线盖住。
  z-index: 99999;
  z-index: var(--z-teleport, 99999);
  isolation: isolate;  // 创建独立 stacking context，避免被父级 transform 干扰
  min-width: 140px;
  background: var(--context-menu-bg);
  border-radius: var(--radius-sm);
  box-shadow: var(--context-menu-shadow);
  padding: 4px 0;
  margin: 0;
  list-style: none;

  li {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    font-size: 13px;
    color: var(--text-regular);
    cursor: pointer;
    transition: all var(--transition-fast);

    &:hover {
      background: var(--bg-active);
      color: var(--color-primary);
    }

    &.disabled {
      color: var(--text-placeholder);
      cursor: not-allowed;

      &:hover {
        background: transparent;
        color: var(--text-placeholder);
      }
    }
  }

  .context-menu-divider {
    height: 1px;
    margin: 4px 8px;
    background: var(--border-color);
    padding: 0 !important;
    cursor: default !important;
    &:hover {
      background: var(--border-color) !important;
    }
  }
}
</style>
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
const { triggerRefresh } = useFavEvents()
const favSet = ref(new Set())
onMounted(() => {
  // 从 localStorage 恢复已收藏路径
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i)
    if (k?.startsWith('fav_')) favSet.value.add(k.slice(4))
  }
})
const isTagFav = computed(() => favSet.value.has(selectedTag.value.path))

async function handleToggleFavorite() {
  const tag = selectedTag.value
  try {
    const res = await toggleFavoriteApi({
      name: tag.meta?.title || tag.name,
      path: tag.path,
      icon: tag.meta?.icon || '',
      menuId: tag.meta?.id || null
    })
    if (res.data?.collected) {
      localStorage.setItem(`fav_${tag.path}`, res.data?.id || '1')
      favSet.value.add(tag.path)
    } else {
      localStorage.removeItem(`fav_${tag.path}`)
      favSet.value.delete(tag.path)
    }
    triggerRefresh()
  } catch { /* ignore */ }
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
  background: var(--tags-bg);
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
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
    border-radius: 4px;
    cursor: pointer;
    text-decoration: none;
    transition: all 0.2s;
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
      transition: background 0.2s;

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
  z-index: 3000;
  min-width: 140px;
  background: var(--context-menu-bg);
  border-radius: 6px;
  box-shadow: 0 4px 16px var(--context-menu-shadow);
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
    transition: all 0.2s;

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

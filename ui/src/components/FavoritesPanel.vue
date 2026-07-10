<template>
  <div class="favorites-panel" v-if="favorites.length > 0">
    <div class="fav-header">
      <span style="font-size: 12px; color: #909399">快捷收藏</span>
    </div>
    <div
      v-for="fav in favorites"
      :key="fav.id"
      class="fav-item"
      @click="goTo(fav.path)"
      @contextmenu.prevent="(e) => showMenu(e, fav)"
      :title="fav.name"
    >
      <div class="fav-icon star-btn" @click.stop="toggleFav(fav)" :class="{ active: true }">⭐</div>
      <span class="fav-name">{{ fav.name }}</span>
    </div>

    <!-- 右键菜单 -->
    <teleport to="body">
      <ul class="context-menu" v-show="visible" :style="{ left: x + 'px', top: y + 'px' }" @click.self="closeMenu">
        <li @click="removeFav">取消收藏</li>
      </ul>
    </teleport>
  </div>
</template>

<script setup>
defineOptions({ name: 'FavoritesPanel' })
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoritesApi, toggleFavoriteApi } from '@/api/favorite'
import { useFavEvents } from '@/composables/useFavEvents'
import { useStorage, STORAGE_KEYS, useNamespacedKey } from '@/composables/useStorage'

const router = useRouter()
const favorites = ref([])
const { refreshTick, triggerRefresh } = useFavEvents()

// 右键菜单状态
const visible = ref(false)
const x = ref(0)
const y = ref(0)
let currentFav = null

const fetchFavorites = async () => {
  try {
    const res = await getFavoritesApi()
    favorites.value = res.data || []
  } catch {
    /* */
  }
}

const goTo = (path) => router.push(path)

// 点击星星取消收藏（乐观更新：先移除UI，API后台确认）
const toggleFav = async (fav) => {
  favorites.value = favorites.value.filter((f) => f.id !== fav.id)
  // 用统一命名空间 key 删除本地收藏标记
  const favKey = useNamespacedKey(STORAGE_KEYS.FAVORITE_STAR, fav.path)
  const favStore = useStorage(favKey)
  favStore.remove()
  try {
    await toggleFavoriteApi({ name: fav.name, path: fav.path, icon: fav.icon || '', menuId: fav.menuId })
    triggerRefresh() // API成功后刷新，通知 TagsView 同步状态
  } catch (e) {
    console.error('[FavoritesPanel] toggleFav API失败，重新拉取', e)
    fetchFavorites()
  }
}

// 显示右键菜单
const showMenu = (e, fav) => {
  currentFav = fav
  x.value = e.clientX
  y.value = e.clientY
  visible.value = true
}

// 取消收藏（右键菜单）
const removeFav = async () => {
  if (!currentFav) return
  await toggleFav(currentFav)
  closeMenu()
}

const closeMenu = () => {
  visible.value = false
  currentFav = null
}

// 点击外部关闭菜单
onMounted(() => document.addEventListener('click', closeMenu))
onUnmounted(() => document.removeEventListener('click', closeMenu))

onMounted(fetchFavorites)
watch(refreshTick, fetchFavorites)
</script>

<style scoped>
.favorites-panel {
  padding: 4px 8px;
  border-top: 1px solid var(--el-border-color-light);
}
.fav-header {
  padding: 8px 12px 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.fav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  transition: background 0.2s;
}
.fav-item:hover {
  background: var(--el-fill-color-light);
}
.fav-icon {
  font-size: 14px;
  flex-shrink: 0;
}
.fav-icon.star-btn {
  cursor: pointer;
  transition: opacity 0.15s;
}
.fav-icon.star-btn:hover {
  opacity: 0.6;
}
.fav-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: var(--z-favorites, 9999);
  list-style: none;
  margin: 0;
  padding: 4px 0;
  min-width: 120px;
  border-radius: 6px;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid var(--el-border-color-lighter);
}
.context-menu li {
  padding: 8px 16px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.context-menu li:hover {
  background: var(--el-fill-color-light);
  color: var(--el-color-danger);
}
</style>

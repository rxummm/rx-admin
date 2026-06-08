<template>
  <div class="favorites-panel" v-if="favorites.length > 0">
    <div class="fav-header">
      <span style="font-size: 12px; color: #909399;">快捷收藏</span>
    </div>
    <div v-for="fav in favorites" :key="fav.id" class="fav-item" @click="goTo(fav.path)" :title="fav.name">
      <div class="fav-icon">⭐</div>
      <span class="fav-name">{{ fav.name }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoritesApi, deleteFavoriteApi } from '@/api/favorite'

const router = useRouter()
const favorites = ref([])

const fetchFavorites = async () => {
  try {
    const res = await getFavoritesApi()
    favorites.value = res.data || []
  } catch (e) { /* */ }
}

const goTo = (path) => router.push(path)

onMounted(fetchFavorites)
</script>

<style scoped>
.favorites-panel {
  padding: 4px 8px;
  border-top: 1px solid var(--el-border-color-light);
}
.fav-header {
  padding: 8px 12px 4px;
  display: flex; align-items: center; justify-content: space-between;
}
.fav-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; cursor: pointer; border-radius: 4px;
  font-size: 13px; transition: background 0.2s;
}
.fav-item:hover { background: var(--el-fill-color-light); }
.fav-icon { font-size: 14px; flex-shrink: 0; }
.fav-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>

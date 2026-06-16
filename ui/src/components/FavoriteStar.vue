<template>
  <span style="cursor: pointer; display: inline-flex; align-items: center;" @click.stop="toggle">
    <el-icon :size="18" :color="isFavorited ? '#F56C6C' : '#909399'">
      <StarFilled v-if="isFavorited" />
      <Star v-else />
    </el-icon>
    <span v-if="showText" style="margin-left: 4px; font-size: 12px; color: #909399;">
      {{ isFavorited ? '已收藏' : '收藏' }}
    </span>
  </span>
</template>

<script setup>
defineOptions({ name: 'FavoriteStar' })
import { ref, watch, onMounted } from 'vue'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { toggleFavoriteApi } from '@/api/favorite'
import { useStorage } from '@/composables/useStorage'
import { STORAGE_KEYS } from '@/composables/useStorage'
import { useNamespacedKey } from '@/composables/useStorage'
import { useFavEvents } from '@/composables/useFavEvents'

const props = defineProps({
  name: { type: String, required: true },
  path: { type: String, required: true },
  icon: { type: String, default: '' },
  menuId: { type: [Number, String], default: null },
  showText: { type: Boolean, default: false }
})

const { triggerRefresh } = useFavEvents()

const isFavorited = ref(false)
const favoriteId = ref(null)

// 统一用命名空间 key 替代散落的 fav_${path}
const favKey = useNamespacedKey(STORAGE_KEYS.FAVORITE_STAR, props.path)
const favStore = useStorage(favKey)

// 初始化时从 useStorage 恢复状态
onMounted(() => {
  const saved = favStore.get()
  if (saved) {
    isFavorited.value = true
    favoriteId.value = saved
  }
})

const toggle = async () => {
  try {
    const res = await toggleFavoriteApi({
      name: props.name, path: props.path, icon: props.icon, menuId: props.menuId
    })
    isFavorited.value = res.data?.collected
    if (isFavorited.value) {
      favStore.set(res.data?.id || '1')
      favoriteId.value = res.data?.id
    } else {
      favStore.remove()
      favoriteId.value = null
    }
    triggerRefresh()
  } catch (e) { /* ignore */ }
}
</script>

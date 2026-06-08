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
import { ref, watch } from 'vue'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { toggleFavoriteApi } from '@/api/favorite'

const props = defineProps({
  name: { type: String, required: true },
  path: { type: String, required: true },
  icon: { type: String, default: '' },
  menuId: { type: [Number, String], default: null },
  showText: { type: Boolean, default: false }
})

const isFavorited = ref(false)
const favoriteId = ref(null)

// 恢复状态
const key = `fav_${props.path}`
const saved = localStorage.getItem(key)
if (saved) {
  isFavorited.value = true
  favoriteId.value = saved
}

const toggle = async () => {
  try {
    const res = await toggleFavoriteApi({
      name: props.name, path: props.path, icon: props.icon, menuId: props.menuId
    })
    isFavorited.value = res.data?.collected
    if (isFavorited.value) {
      localStorage.setItem(key, res.data?.id || '1')
      favoriteId.value = res.data?.id
    } else {
      localStorage.removeItem(key)
      favoriteId.value = null
    }
  } catch (e) { /* ignore */ }
}
</script>

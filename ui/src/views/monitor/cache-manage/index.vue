<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600">缓存管理 (Caffeine)</span>
      <div style="flex: 1" />
      <el-button type="danger" @click="handleClearAll">清除全部缓存</el-button>
      <el-button @click="fetchCaches">刷新</el-button>
    </div>
    <el-table :data="caches" v-loading="loading" stripe border>
      <el-table-column prop="name" label="缓存名称" width="200" />
      <el-table-column prop="nativeType" label="缓存类型" width="150" />
      <el-table-column prop="hitCount" label="命中次数" width="120" />
      <el-table-column prop="missCount" label="未命中次数" width="120" />
      <el-table-column prop="hitRate" label="命中率" width="100" />
      <el-table-column prop="evictionCount" label="淘汰次数" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="clearCache(row.name)">清除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && caches.length === 0" />
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorCacheManage' })
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCacheListApi, clearCacheApi, clearAllCacheApi } from '@/api/cacheManage'

const caches = ref([])
const loading = ref(false)
async function fetchCaches() {
  loading.value = true
  try {
    const res = await getCacheListApi()
    caches.value = res.data || []
  } catch {
    ElMessage.error('获取缓存信息失败')
  } finally {
    loading.value = false
  }
}
async function clearCache(name) {
  try {
    await ElMessageBox.confirm(`确认清除缓存 "${name}"？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await clearCacheApi(name)
  ElMessage.success(`缓存 ${name} 已清除`)
  fetchCaches()
}
async function handleClearAll() {
  try {
    await ElMessageBox.confirm('确认清除所有缓存？', '警告', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  await clearAllCacheApi()
  ElMessage.success('所有缓存已清除')
  fetchCaches()
}
onMounted(fetchCaches)
</script>

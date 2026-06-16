<template>
  <el-dropdown
    v-if="visible"
    trigger="click"
    :disabled="exporting || loading"
    @command="handleExport"
  >
    <el-button :loading="exporting" size="default">
      <el-icon><Download /></el-icon>
      {{ exporting ? $t('common.exporting') : $t('common.exportFile') }}
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="t in availableTypes"
          :key="t.value"
          :command="t.value"
          :icon="t.icon"
        >
          {{ t.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
defineOptions({ name: 'ExportButton' })
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, ArrowDown, Document, Tickets } from '@element-plus/icons-vue'
import { getExportConfigApi, exportExcelApi, exportPdfApi } from '@/api/export'
import { exportExcelClient, exportPdfClient } from '@/utils/exportClient'

const { t } = useI18n()

// 模块级缓存：跨组件实例共享（keep-alive 重建组件时复用）
const configCache = new Map() // path → exportTypes[]
let fetchingPath = ''        // 当前正在请求的 path（简单去重）

const props = defineProps({
  /** 表格数据（响应式数组） */
  data: { type: Array, default: () => [] },
  /** 列定义 [{ field: 'username', label: '用户名' }] */
  columns: { type: Array, default: () => [] },
  /** 导出标题（可选，默认取路由 meta.title） */
  title: { type: String, default: '' },
  /** 导出模式: 'client' 纯前端导出(默认) | 'server' 后端导出 */
  mode: { type: String, default: 'client' }
})

const route = useRoute()

const visible = ref(false)
const loading = ref(true)
const exporting = ref(false)
const exportTypes = ref([])

const typeMap = {
  excel: { label: t('common.exportToExcel'), icon: Document, mime: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', ext: '.xlsx' },
  pdf:    { label: t('common.exportToPdf'),   icon: Tickets,  mime: 'application/pdf', ext: '.pdf' }
}

const availableTypes = computed(() =>
  exportTypes.value.map(t => ({ value: t, ...typeMap[t] })).filter(Boolean)
)

// 首次挂载查询配置（仅一次，keep-alive 缓存后切换标签不重复触发）
onMounted(fetchConfig)

async function fetchConfig() {
  const path = route.path
  if (!path || path === '/') {
    visible.value = false
    return
  }

  // 命中缓存直接返回
  if (configCache.has(path)) {
    exportTypes.value = configCache.get(path)
    visible.value = exportTypes.value.length > 0
    loading.value = false
    return
  }

  // 同一路径正在请求中，跳过重复请求
  if (fetchingPath === path) return

  loading.value = true
  fetchingPath = path
  try {
    const res = await getExportConfigApi(path)
    if (res?.data?.enabled) {
      exportTypes.value = res.data.exportTypes || []
      configCache.set(path, exportTypes.value)
      visible.value = exportTypes.value.length > 0
    } else {
      configCache.set(path, [])
      visible.value = false
    }
  } catch {
    visible.value = false
  } finally {
    loading.value = false
    fetchingPath = ''
  }
}

async function handleExport(type) {
  const data = toRawData(props.data)
  const columns = toRawData(props.columns)

  if (!columns.length) {
    ElMessage.warning(t('common.noExportColumns'))
    return
  }
  if (!data.length) {
    ElMessage.warning(t('common.noDataToExport'))
    return
  }

  const title = props.title || route.meta?.title || document.title || t('common.dataExport')
  exporting.value = true

  try {
    if (props.mode === 'server') {
      // 后端导出模式（保留）
      await exportViaServer(type, title, columns, data)
    } else {
      // 前端导出模式（默认）
      await exportViaClient(type, title, columns, data)
    }
    ElMessage.success(t('common.exportSuccess') + `：${title}${typeMap[type].ext}`)
  } catch (e) {
    ElMessage.error(t('common.exportFail') + ': ' + (e?.message || ''))
  } finally {
    exporting.value = false
  }
}

/** 前端导出（默认） */
async function exportViaClient(type, title, columns, data) {
  if (type === 'pdf') {
    await exportPdfClient({ title, columns, data })
  } else {
    await exportExcelClient({ title, columns, data })
  }
}

/** 后端导出（保留，通过 mode="server" 启用） */
async function exportViaServer(type, title, columns, data) {
  const api = type === 'pdf' ? exportPdfApi : exportExcelApi
  const info = typeMap[type]

  const res = await api({ title, columns, data })
  const blob = res instanceof Blob ? res : new Blob([res], { type: info.mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = title + '_' + formatDate() + info.ext
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 深度解包 ref/reactive 为纯 JSON 对象
function toRawData(arr) {
  if (!arr) return []
  return JSON.parse(JSON.stringify(arr))
}

function formatDate() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
}
</script>

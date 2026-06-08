<template>
  <div class="page-container">
    <div class="search-bar">
      <div style="flex: 1" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Refresh /></el-icon> {{ $t('common.refresh') }}
      </el-button>
      <el-dropdown trigger="click" @command="toggleColumn">
        <el-button>
          <el-icon><Setting /></el-icon> {{ $t('common.columns') }}
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="col in columnOptions" :key="col.key" :command="col.key">
              <el-icon v-if="visibleColumns.includes(col.key)"><Check /></el-icon>
              <span :style="{ opacity: visibleColumns.includes(col.key) ? 1 : 0.4 }">{{ col.label }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="vtable-container">
      <div class="vtable-header">
        <div class="vc vc-token" v-if="visibleColumns.includes('tokenId')" @click="toggleSort('tokenId')">
          {{ $t('monitor.online.sessionId') }}
          <span class="sort-arrow" v-if="sortField === 'tokenId'">{{ sortOrder === 'ascending' ? '▲' : '▼' }}</span>
        </div>
        <div class="vc vc-login" v-if="visibleColumns.includes('loginId')" @click="toggleSort('loginId')">
          {{ $t('monitor.online.userId') }}
          <span class="sort-arrow" v-if="sortField === 'loginId'">{{ sortOrder === 'ascending' ? '▲' : '▼' }}</span>
        </div>
        <div class="vc vc-user" v-if="visibleColumns.includes('username')" @click="toggleSort('username')">
          {{ $t('system.user.username') }}
          <span class="sort-arrow" v-if="sortField === 'username'">{{ sortOrder === 'ascending' ? '▲' : '▼' }}</span>
        </div>
        <div class="vc vc-nick" v-if="visibleColumns.includes('nickname')" @click="toggleSort('nickname')">
          {{ $t('system.user.nickname') }}
          <span class="sort-arrow" v-if="sortField === 'nickname'">{{ sortOrder === 'ascending' ? '▲' : '▼' }}</span>
        </div>
        <div class="vc vc-time" v-if="visibleColumns.includes('loginTime')" @click="toggleSort('loginTime')">
          {{ $t('monitor.online.loginTime') }}
          <span class="sort-arrow" v-if="sortField === 'loginTime'">{{ sortOrder === 'ascending' ? '▲' : '▼' }}</span>
        </div>
        <div class="vc vc-op">{{ $t('common.operation') }}</div>
      </div>

      <div v-loading="loading" class="vscroller-wrapper">
        <RecycleScroller v-if="sortedTableData.length > 0" class="vscroller" :items="sortedTableData" :item-size="44" key-field="tokenId" v-slot="{ item, index }">
          <div class="vrow" :class="{ even: index % 2 === 1 }">
            <div class="vc vc-token" v-if="visibleColumns.includes('tokenId')" :title="item.tokenId">{{ item.tokenId }}</div>
            <div class="vc vc-login" v-if="visibleColumns.includes('loginId')">{{ item.loginId }}</div>
            <div class="vc vc-user" v-if="visibleColumns.includes('username')">{{ item.username }}</div>
            <div class="vc vc-nick" v-if="visibleColumns.includes('nickname')">{{ item.nickname }}</div>
            <div class="vc vc-time" v-if="visibleColumns.includes('loginTime')">{{ item.loginTime }}</div>
            <div class="vc vc-op">
              <el-button link type="danger" size="small" @click.stop="handleKickOut(item)">
                <el-icon><SwitchButton /></el-icon> {{ $t('monitor.online.kickout') }}
              </el-button>
            </div>
          </div>
        </RecycleScroller>
        <el-empty v-if="!loading && sortedTableData.length === 0" :description="$t('monitor.online.noOnline')" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorOnline' })
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RecycleScroller } from 'vue-virtual-scroller'
import { getOnlineListApi, kickOutApi } from '@/api/online'

const { t } = useI18n()

const tableData = ref([])
const loading = ref(false)
const sortField = ref('')
const sortOrder = ref('')

const columnOptions = [
  { key: 'tokenId', label: t('monitor.online.sessionId') },
  { key: 'loginId', label: t('monitor.online.userId') },
  { key: 'username', label: t('system.user.username') },
  { key: 'nickname', label: t('system.user.nickname') },
  { key: 'loginTime', label: t('monitor.online.loginTime') }
]
const visibleColumns = ref(columnOptions.map(c => c.key))

function toggleColumn(key) {
  const idx = visibleColumns.value.indexOf(key)
  if (idx > -1) {
    visibleColumns.value.splice(idx, 1)
  } else {
    visibleColumns.value.push(key)
  }
}

function toggleSort(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'ascending' ? 'descending' : 'ascending'
  } else {
    sortField.value = field
    sortOrder.value = 'ascending'
  }
}

const sortedTableData = computed(() => {
  const data = [...tableData.value]
  if (!sortField.value || !sortOrder.value) return data
  return data.sort((a, b) => {
    let valA = a[sortField.value]
    let valB = b[sortField.value]
    if (valA == null) valA = ''
    if (valB == null) valB = ''
    if (typeof valA === 'string') valA = valA.toLowerCase()
    if (typeof valB === 'string') valB = valB.toLowerCase()
    if (valA < valB) return sortOrder.value === 'ascending' ? -1 : 1
    if (valA > valB) return sortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getOnlineListApi()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleKickOut(row) {
  try {
    await ElMessageBox.confirm(t('monitor.online.kickoutConfirm'), t('common.tip'), { type: 'warning' })
    await kickOutApi(row.tokenId)
    ElMessage.success(t('common.operateSuccess'))
    fetchData()
  } catch {}
}
</script>

<style scoped>
/* 页面特有样式 - .page-container/.search-bar 在 global.scss 中统一定义 */

.vtable-container { border: 1px solid var(--border-light); border-radius: 4px; width: 100%; overflow-x: auto; }
.vtable-header { display: flex; align-items: center; background: var(--el-fill-color-light); font-weight: 600; font-size: 13px; color: var(--text-regular); border-bottom: 1px solid var(--border-light); cursor: pointer; min-width: fit-content; }
.vscroller-wrapper { position: relative; min-height: 200px; width: 100%; overflow: hidden; }
.vscroller { height: 500px; width: 100%; }
.sort-arrow { font-size: 10px; margin-left: 2px; }
.vrow { display: flex; align-items: center; font-size: 13px; border-bottom: 1px solid var(--border-lighter); min-width: fit-content; }
.vrow.even { background: var(--el-fill-color-lighter); }
.vrow:hover { background: var(--bg-hover); }
.vc { padding: 4px 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; box-sizing: border-box; }
.vc-token { flex: 1 1 auto; min-width: 200px; }
.vc-login { width: 80px; text-align: center; flex-shrink: 0; }
.vc-user { width: 120px; flex-shrink: 0; }
.vc-nick { width: 140px; flex-shrink: 0; }
.vc-time { width: 200px; flex-shrink: 0; }
.vc-op { width: 120px; text-align: center; flex-shrink: 0; }
</style>

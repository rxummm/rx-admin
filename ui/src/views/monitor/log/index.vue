<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('monitor.log.operator') + '/' + $t('monitor.log.action') + '/' + $t('monitor.log.module')" clearable style="width: 200px" @keyup.enter="fetchData" />
      <el-select v-model="statusFilter" :placeholder="$t('common.status')" clearable style="width: 120px" @change="fetchData">
        <el-option :label="$t('monitor.log.statusOptions.success')" :value="1" />
        <el-option :label="$t('monitor.log.statusOptions.fail')" :value="0" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="-" :start-placeholder="$t('common.startDate')" :end-placeholder="$t('common.endDate')" value-format="YYYY-MM-DD HH:mm:ss" style="width: 240px" @change="fetchData" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> {{ $t('common.batchDelete') }}({{ selectedIds.length }})
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

    <div class="log-table-wrapper">
      <div class="vtable-header">
        <div class="vc vc-chk"><el-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="toggleAll" /></div>
        <div class="vc vc-id" v-if="visibleColumns.includes('id')" @click="toggleSort('id')">ID<span class="sort-arrow" v-if="sortField==='id'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-user" v-if="visibleColumns.includes('username')" @click="toggleSort('username')">{{ $t('monitor.log.operator') }}<span class="sort-arrow" v-if="sortField==='username'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-mod" v-if="visibleColumns.includes('module')" @click="toggleSort('module')">{{ $t('monitor.log.module') }}<span class="sort-arrow" v-if="sortField==='module'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-act" v-if="visibleColumns.includes('operation')" @click="toggleSort('operation')">{{ $t('monitor.log.action') }}<span class="sort-arrow" v-if="sortField==='operation'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-mth" v-if="visibleColumns.includes('method')">{{ $t('monitor.log.method') }}</div>
        <div class="vc vc-ip" v-if="visibleColumns.includes('ip')">{{ $t('monitor.log.ip') }}</div>
        <div class="vc vc-sts" v-if="visibleColumns.includes('status')" @click="toggleSort('status')">{{ $t('common.status') }}<span class="sort-arrow" v-if="sortField==='status'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-cst" v-if="visibleColumns.includes('costTime')" @click="toggleSort('costTime')">{{ $t('monitor.log.duration') }}<span class="sort-arrow" v-if="sortField==='costTime'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-tim" v-if="visibleColumns.includes('createTime')" @click="toggleSort('createTime')">{{ $t('monitor.log.operateTime') }}<span class="sort-arrow" v-if="sortField==='createTime'">{{ sortOrder==='ascending'?'\u25b2':'\u25bc' }}</span></div>
        <div class="vc vc-op">{{ $t('common.actions') }}</div>
      </div>
      <div v-loading="loading" class="vscroller-wrapper">
        <RecycleScroller v-if="sortedTableData.length > 0" class="vscroller" :style="{ height: scrollerHeight + 'px' }" :items="sortedTableData" :item-size="44" key-field="id" v-slot="{ item, index }">
          <div class="vrow" :class="{ even: index % 2 === 1 }">
            <div class="vc vc-chk"><el-checkbox :model-value="selectedIds.includes(item.id)" @change="toggleSelect(item)" /></div>
            <div class="vc vc-id" v-if="visibleColumns.includes('id')">{{ item.id }}</div>
            <div class="vc vc-user" v-if="visibleColumns.includes('username')">{{ item.username }}</div>
            <div class="vc vc-mod" v-if="visibleColumns.includes('module')">{{ item.module }}</div>
            <div class="vc vc-act" v-if="visibleColumns.includes('operation')">{{ item.operation }}</div>
            <div class="vc vc-mth" v-if="visibleColumns.includes('method')" :title="item.method">{{ item.method }}</div>
            <div class="vc vc-ip" v-if="visibleColumns.includes('ip')">{{ item.ip }}</div>
            <div class="vc vc-sts" v-if="visibleColumns.includes('status')">
              <el-tag :type="item.status === 1 ? 'success' : 'danger'" size="small">{{ item.status === 1 ? $t('monitor.log.statusOptions.success') : $t('monitor.log.statusOptions.fail') }}</el-tag>
            </div>
            <div class="vc vc-cst" v-if="visibleColumns.includes('costTime')">{{ item.costTime }}</div>
            <div class="vc vc-tim" v-if="visibleColumns.includes('createTime')">{{ item.createTime }}</div>
            <div class="vc vc-op">
              <el-button link type="primary" size="small" @click.stop="showDetail(item)">{{ $t('common.detail') }}</el-button>
              <el-popconfirm :title="$t('common.deleteConfirm')" @confirm="handleDelete(item.id)">
                <template #reference><el-button link type="danger" size="small" @click.stop>{{ $t('common.delete') }}</el-button></template>
              </el-popconfirm>
            </div>
          </div>
        </RecycleScroller>
        <el-empty v-if="!loading && sortedTableData.length === 0" :description="$t('common.noData')" />
    </div>
    </div>

    <el-pagination
      v-model:current-page="page" v-model:page-size="size" :total="total"
      :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
      class="page-pagination"
      @size-change="fetchData" @current-change="fetchData"
    />
    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="$t('monitor.log.title') + $t('common.detail')" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="$t('monitor.log.params')">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap; word-break: break-all;">
            {{ currentLog.params || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('monitor.log.result')">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap; word-break: break-all;">
            {{ currentLog.result || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('monitor.log.errorMsg')" v-if="currentLog.errorMsg">
          <span style="color: var(--el-color-danger)">{{ currentLog.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorLog' })
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { RecycleScroller } from 'vue-virtual-scroller'
import { getLogPageApi, deleteLogApi, deleteLogBatchApi } from '@/api/log'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(null)
const dateRange = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const sortField = ref('')
const sortOrder = ref('')
const selectedIds = ref([])
const isAllSelected = computed(() => tableData.value.length > 0 && selectedIds.value.length === tableData.value.length)
const isIndeterminate = computed(() => selectedIds.value.length > 0 && selectedIds.value.length < tableData.value.length)

function toggleSelect(item) {
  const idx = selectedIds.value.indexOf(item.id)
  idx > -1 ? selectedIds.value.splice(idx, 1) : selectedIds.value.push(item.id)
}
function toggleAll(val) {
  selectedIds.value = val ? tableData.value.map(i => i.id) : []
}

const detailVisible = ref(false)
const currentLog = ref({})

// 列显示配置
const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: t('monitor.log.operator') },
  { key: 'module', label: t('monitor.log.module') },
  { key: 'operation', label: t('monitor.log.action') },
  { key: 'method', label: t('monitor.log.method') },
  { key: 'ip', label: t('monitor.log.ip') },
  { key: 'status', label: t('common.status') },
  { key: 'costTime', label: t('monitor.log.duration') },
  { key: 'createTime', label: t('monitor.log.operateTime') }
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

// 前端排序
const scrollerHeight = computed(() => {
  const count = sortedTableData.value.length
  if (count === 0) return 200
  const contentH = count * 44 + 8
  return Math.max(200, Math.min(contentH, 500))
})

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

function toggleSort(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'ascending' ? 'descending' : 'ascending'
  } else {
    sortField.value = field
    sortOrder.value = 'ascending'
  }
}

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
        const res = await getLogPageApi({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value, startTime: dateRange.value ? dateRange.value[0] : null, endTime: dateRange.value ? dateRange.value[1] : null })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
keyword.value = ''
  page.value = 1
  statusFilter.value = null
  dateRange.value = null
  fetchData()
}

function showDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}



async function handleDelete(id) {
  try {
    await deleteLogApi(id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
  } catch {
    ElMessage.error(t('common.deleteFailed'))
  }
}

async function handleBatchDelete() {
  try {
    await deleteLogBatchApi(selectedIds.value)
    ElMessage.success(t('common.deleteSuccess'))
    selectedIds.value = []
    fetchData()
  } catch {
    ElMessage.error(t('common.deleteFailed'))
  }
}
</script>

<style scoped>
/* 页面特有样式 - .page-container/.search-bar 在 global.scss 中统一定义 */

.log-table-wrapper {
  border: 1px solid var(--border-light);
  border-radius: 4px;
  overflow: hidden;
}

.vtable-header {
  display: flex;
  align-items: center;
  background: var(--el-fill-color-light);
  font-weight: 600;
  font-size: 13px;
  color: var(--text-regular);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
}

.vscroller-wrapper {
  position: relative;
  min-height: 200px;
  width: 100%;
}

.vscroller {
  width: 100%;
}

.sort-arrow { font-size: 10px; margin-left: 2px; color: var(--color-primary); }
.vrow { display: flex; align-items: center; font-size: 13px; border-bottom: 1px solid var(--border-lighter); width: 100%; }
.vrow.even { background: var(--el-fill-color-lighter); }
.vrow:hover { background: var(--bg-hover); }
.vc { padding: 4px 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; box-sizing: border-box; }
.vc-chk { width: 45px; text-align: center; flex-shrink: 0; }
.vc-id { width: 70px; flex-shrink: 0; }
.vc-user { width: 120px; flex-shrink: 0; }
.vc-mod { width: 120px; flex-shrink: 0; }
.vc-act { width: 100px; text-align: center; flex-shrink: 0; }
.vc-mth { flex: 1; min-width: 150px; }
.vc-ip { width: 140px; flex-shrink: 0; }
.vc-sts { width: 80px; text-align: center; flex-shrink: 0; }
.vc-cst { width: 100px; text-align: right; padding-right: 12px; flex-shrink: 0; }
.vc-tim { width: 170px; flex-shrink: 0; }
.vc-op { width: 140px; text-align: center; flex-shrink: 0; }
</style>

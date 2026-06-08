<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('monitor.slowQuery.sql')" clearable style="width:240px" @keyup.enter="handleSearch" />
      <el-select v-model="queryTypeFilter" :placeholder="$t('file.type')" clearable style="width:130px" @change="handleSearch">
        <el-option label="SELECT" value="SELECT" />
        <el-option label="INSERT" value="INSERT" />
        <el-option label="UPDATE" value="UPDATE" />
        <el-option label="DELETE" value="DELETE" />
      </el-select>
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex:1" />
      <el-button type="danger" @click="handleClearAll">
        <el-icon><Delete /></el-icon> {{ $t('common.clear') }}
      </el-button>
      <el-button type="danger" plain :disabled="selectedIds.length===0" @click="handleBatchDelete">
        {{ $t('common.batchDelete') }}
      </el-button>
    </div>

    <div class="virtual-table-wrapper">
      <div class="vtable-header">
        <div class="vc vc-chk"><el-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="toggleAll" /></div>
        <div class="vc vc-id">ID</div>
        <div class="vc vc-sql">{{ $t('monitor.slowQuery.sql') }}</div>
        <div class="vc vc-cost">{{ $t('monitor.slowQuery.executeTime') }}</div>
        <div class="vc vc-type">{{ $t('file.type') }}</div>
        <div class="vc vc-method">Mapper</div>
        <div class="vc vc-time">{{ $t('monitor.slowQuery.queryTime') }}</div>
        <div class="vc vc-op">{{ $t('common.actions') }}</div>
      </div>
      <RecycleScroller class="vscroller" :items="tableData" :item-size="44" key-field="id" v-slot="{ item }">
        <div class="vrow" :class="{ even: tableData.indexOf(item) % 2 === 1 }">
          <div class="vc vc-chk"><el-checkbox :model-value="selectedIds.includes(item.id)" @change="toggleSelect(item)" /></div>
          <div class="vc vc-id">{{ item.id }}</div>
          <div class="vc vc-sql" :title="item.sqlText">{{ item.sqlText }}</div>
          <div class="vc vc-cost">
            <el-tag :type="item.costTimeMs>5000 ? 'danger' : item.costTimeMs>3000 ? 'warning' : 'info'" size="small">{{ item.costTimeMs }}</el-tag>
          </div>
          <div class="vc vc-type">
            <el-tag :type="item.queryType==='SELECT'?'primary':item.queryType==='INSERT'?'success':item.queryType==='UPDATE'?'warning':'danger'" size="small">{{ item.queryType }}</el-tag>
          </div>
          <div class="vc vc-method" :title="item.mapperMethod">{{ item.mapperMethod }}</div>
          <div class="vc vc-time">{{ item.createTime }}</div>
          <div class="vc vc-op"><el-button type="danger" link size="small" @click.stop="handleDelete(item)">{{ $t('common.delete') }}</el-button></div>
        </div>
      </RecycleScroller>
    </div>

    <el-pagination v-model:page="queryParams.page" v-model:limit="queryParams.size" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper"
      @size-change="fetchData" @current-change="fetchData" />
  </div>
</template>

<script setup>
defineOptions({ name: "MonitorSlowQuery" })
import { ref, reactive, computed, onMounted } from "vue"
import { useI18n } from "vue-i18n"
import { ElMessage, ElMessageBox } from "element-plus"
import { RecycleScroller } from "vue-virtual-scroller"
import { getSlowQueryPageApi, deleteSlowQueryApi, batchDeleteSlowQueryApi, clearSlowQueryApi } from "@/api/slowQuery"

const { t } = useI18n()

const queryParams = reactive({ page: 1, size: 20 })
const keyword = ref("")
const queryTypeFilter = ref("")
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])

const isAllSelected = computed(() => tableData.value.length > 0 && selectedIds.value.length === tableData.value.length)
const isIndeterminate = computed(() => selectedIds.value.length > 0 && selectedIds.value.length < tableData.value.length)

async function fetchData() {
  loading.value = true
  try {
    const res = await getSlowQueryPageApi({
      page: queryParams.page, size: queryParams.size,
      keyword: keyword.value || undefined,
      queryType: queryTypeFilter.value || undefined
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {} finally { loading.value = false }
}

function handleSearch() { queryParams.page = 1; fetchData() }
function resetSearch() { keyword.value = ""; queryTypeFilter.value = ""; queryParams.page = 1; fetchData() }
function toggleSelect(item) {
  const idx = selectedIds.value.indexOf(item.id)
  idx > -1 ? selectedIds.value.splice(idx, 1) : selectedIds.value.push(item.id)
}
function toggleAll(val) { selectedIds.value = val ? tableData.value.map(i => i.id) : [] }
function handleDelete(row) {
  ElMessageBox.confirm(t('common.deleteConfirm'), t('common.tip'), { type: "warning" }).then(async () => {
    await deleteSlowQueryApi(row.id)
    ElMessage.success(t('common.deleteSuccess')); fetchData()
  }).catch(() => {})
}
function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  ElMessageBox.confirm(t('common.confirmBatchDelete', { count: selectedIds.value.length }), t('common.tip'), { type: "warning" }).then(async () => {
    await batchDeleteSlowQueryApi(selectedIds.value)
    ElMessage.success(t('common.deleteSuccess')); selectedIds.value = []; fetchData()
  }).catch(() => {})
}
function handleClearAll() {
  ElMessageBox.confirm(t('common.clear') + '?', t('common.tip'), { type: "warning" }).then(async () => {
    await clearSlowQueryApi()
    ElMessage.success(t('common.deleteSuccess')); fetchData()
  }).catch(() => {})
}
onMounted(() => { fetchData() })
</script>

<style scoped>
/* 页面特有样式 - .page-container/.search-bar 在 global.scss 中统一定义 */

.virtual-table-wrapper { border: 1px solid var(--border-light); border-radius: 4px; margin-bottom: 12px; }
.vtable-header { display: flex; align-items: center; background: var(--el-fill-color-light); font-weight: 600; font-size: 13px; color: var(--text-regular); border-bottom: 1px solid var(--border-light); }
.vscroller { height: 500px; }
.vrow { display: flex; align-items: center; font-size: 13px; border-bottom: 1px solid var(--border-lighter); }
.vrow.even { background: var(--el-fill-color-lighter); }
.vrow:hover { background: var(--bg-hover); }
.vc { padding: 8px 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; }
.vc-chk { width: 45px; text-align: center; }
.vc-id { width: 60px; }
.vc-sql { flex: 1; min-width: 200px; }
.vc-cost { width: 90px; text-align: right; padding-right: 12px; }
.vc-type { width: 80px; text-align: center; }
.vc-method { width: 220px; }
.vc-time { width: 160px; }
.vc-op { width: 60px; text-align: center; }
</style>

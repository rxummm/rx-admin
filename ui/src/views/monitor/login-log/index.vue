<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.username" :placeholder="$t('monitor.loginLog.username')" clearable style="width:160px" />
      <el-select v-model="searchForm.status" :placeholder="$t('monitor.loginLog.status')" clearable style="width:130px;margin-left:8px">
        <el-option :label="$t('monitor.loginLog.success')" :value="1" />
        <el-option :label="$t('monitor.loginLog.fail')" :value="0" />
      </el-select>
      <el-date-picker v-model="searchForm.timeRange" type="datetimerange" :range-separator="$t('monitor.loginLog.to')"
        :start-placeholder="$t('monitor.loginLog.start')" :end-placeholder="$t('monitor.loginLog.end')" value-format="YYYY-MM-DD HH:mm:ss" style="margin-left:8px" />
      <el-button type="primary" @click="handleSearch" style="margin-left:8px">{{ $t('common.search') }}</el-button>
      <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
      <div style="flex:1" />
      <el-button type="danger" :disabled="selectedIds.length===0" @click="handleBatchDelete">{{ $t('common.batchDelete') }}</el-button>
    </div>
    <div class="login-log-table-wrapper">
      <el-skeleton v-if="initialLoading" :rows="5" animated />
      <template v-if="!initialLoading">
        <el-table v-if="!errorState" :data="tableData" :max-height="tableMaxHeight" @selection-change="handleSelectionChange" v-loading="loading" stripe border style="width: 100%">
          <el-table-column type="selection" width="45" />
          <el-table-column prop="id" :label="$t('monitor.loginLog.id')" width="70" />
          <el-table-column prop="username" :label="$t('monitor.loginLog.username')" width="120" />
          <el-table-column prop="ip" :label="$t('monitor.loginLog.ip')" width="150" />
          <el-table-column prop="browser" :label="$t('monitor.loginLog.browser')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="os" :label="$t('monitor.loginLog.os')" width="200" show-overflow-tooltip />
          <el-table-column prop="status" :label="$t('monitor.loginLog.status')" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?$t('monitor.loginLog.success'):$t('monitor.loginLog.fail') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="failReason" :label="$t('monitor.loginLog.failReason')" min-width="160" show-overflow-tooltip />
          <el-table-column prop="loginTime" :label="$t('monitor.loginLog.loginTime')" width="170" />
          <el-table-column :label="$t('common.operation')" width="70" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" size="small" link @click="handleDelete(row.id)">{{ $t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && tableData.length === 0 && !errorState" :description="$t('common.noData')" />
        <el-result v-if="errorState" icon="error" :title="$t('common.loadFail')" :sub-title="errorMsg">
          <template #extra>
            <el-button type="primary" @click="fetchData">{{ $t('common.retry') }}</el-button>
          </template>
        </el-result>
        <el-pagination
          v-if="!errorState"
          class="page-pagination"
          v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData" @current-change="fetchData" />
      </template>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorLoginLog' })
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getLoginLogPageApi, deleteLoginLogApi, deleteLoginLogBatchApi } from '@/api/loginLog'
import { useTableHeight } from '@/composables/useTableHeight'

const { t } = useI18n()

const initialLoading = ref(true)
const loading = ref(false)
const errorState = ref(false)
const errorMsg = ref('')
const tableData = ref([])
const selectedIds = ref([])

const searchForm = reactive({ username: '', status: null, timeRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.login-log-table-wrapper')

function buildParams() {
  const p = { page: pagination.page, size: pagination.size, username: searchForm.username || undefined, status: searchForm.status }
  if (searchForm.timeRange && searchForm.timeRange.length === 2) {
    p.startTime = searchForm.timeRange[0]; p.endTime = searchForm.timeRange[1]
  }
  return p
}

async function fetchData() {
  loading.value = true
  errorState.value = false
  errorMsg.value = ''
  try {
    const res = await getLoginLogPageApi(buildParams())
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    errorState.value = true
    errorMsg.value = e.message || t('monitor.loginLog.loadError')
    ElMessage.error(t('common.loadFail'))
  } finally {
    loading.value = false
    initialLoading.value = false
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.username = ''; searchForm.status = null; searchForm.timeRange = null; handleSearch() }
function handleSelectionChange(vals) { selectedIds.value = vals.map(r => r.id) }
async function handleDelete(id) {
  await ElMessageBox.confirm(t('common.confirmDelete'), t('common.tip'), { type: 'warning' })
  await deleteLoginLogApi(id); ElMessage.success(t('common.deleteSuccess')); fetchData()
}
async function handleBatchDelete() {
  await ElMessageBox.confirm(t('common.confirmBatchDelete', { count: selectedIds.value.length }), t('common.tip'), { type: 'warning' })
  await deleteLoginLogBatchApi(selectedIds.value); ElMessage.success(t('common.batchDeleteSuccess')); fetchData()
}
onMounted(() => { fetchData(); calcTableMaxHeight() })
</script>


<style scoped>
.login-log-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.login-log-table-wrapper :deep(.page-pagination) {
  margin-top: 12px;
  flex-shrink: 0;
}
</style>
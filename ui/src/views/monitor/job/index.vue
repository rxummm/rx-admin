<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('common.input')" clearable style="width: 240px" @keyup.enter="handleSearch" />
      <el-select v-model="statusFilter" :placeholder="$t('common.status')" clearable style="width: 120px" @change="handleSearch">
        <el-option :label="$t('common.all')" value="" />
        <el-option :label="$t('common.enable')" :value="1" />
        <el-option :label="$t('common.disable')" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> {{ $t('common.add') }}
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> {{ $t('common.batchDelete') }}
      </el-button>
    </div>

    <div class="job-table-wrapper">
      <el-table :data="tableData" :max-height="tableMaxHeight" border stripe v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="jobName" :label="$t('job.jobName')" min-width="150" />
        <el-table-column prop="beanName" :label="$t('job.beanName')" width="180" />
        <el-table-column prop="cronExpression" :label="$t('job.cronExpression')" width="160" />
        <el-table-column prop="status" :label="$t('job.status')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('job.statusNormal') : $t('job.statusPaused') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" :label="$t('job.remark')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="170" />
        <el-table-column :label="$t('common.operate')" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="primary" link size="small" @click="handleRunOnce(row)">{{ $t('job.runOnce') }}</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? $t('common.disable') : $t('common.enable') }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="page-pagination"
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="isEdit ? $t('common.edit') : $t('common.add')" v-model="dialogVisible" :width="520">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" label-position="right">
        <el-form-item :label="$t('job.jobName')" prop="jobName">
          <el-input v-model="form.jobName" :placeholder="$t('common.input')" />
        </el-form-item>
        <el-form-item :label="$t('job.beanName')" prop="beanName">
          <el-input v-model="form.beanName" :placeholder="$t('common.input')" />
        </el-form-item>
        <el-form-item :label="$t('job.cronExpression')" prop="cronExpression">
          <el-input v-model="form.cronExpression" :placeholder="$t('common.input')" />
        </el-form-item>
        <el-form-item :label="$t('job.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="$t('common.input')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobPageApi, addJobApi, updateJobApi, deleteJobApi, runOnceApi, toggleJobStatusApi } from '@/api/job'
import { useTableHeight } from '@/composables/useTableHeight'

const { t } = useI18n()

const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.job-table-wrapper')

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10
})
const keyword = ref('')
const statusFilter = ref('')
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)

const form = reactive({
  id: null,
  jobName: '',
  beanName: '',
  cronExpression: '',
  remark: '',
  status: 1
})

const rules = {
  jobName: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  beanName: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  cronExpression: [{ required: true, message: () => t('common.required'), trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      keyword: keyword.value || undefined,
      status: statusFilter.value !== '' ? Number(statusFilter.value) : undefined
    }
    const res = await getJobPageApi(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {} finally { loading.value = false }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function resetSearch() {
  keyword.value = ''
  statusFilter.value = ''
  queryParams.page = 1
  fetchData()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(s => s.id)
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.jobName = ''
  form.beanName = ''
  form.cronExpression = ''
  form.remark = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.jobName = row.jobName
  form.beanName = row.beanName
  form.cronExpression = row.cronExpression
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateJobApi(form)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addJobApi(form)
      ElMessage.success(t('common.addSuccess'))
    }
    dialogVisible.value = false
    fetchData()
  } catch {} finally { submitLoading.value = false }
}

async function handleRunOnce(row) {
  try {
    await runOnceApi(row.id)
    ElMessage.success(t('job.runOnce') + ' ' + t('common.success'))
  } catch {}
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await toggleJobStatusApi(row.id, newStatus)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.tip'), { type: 'warning' })
    await deleteJobApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(t('common.batchDeleteConfirm'), t('common.tip'), { type: 'warning' })
    for (const id of selectedIds.value) { await deleteJobApi(id) }
    ElMessage.success(t('common.batchDeleteSuccess'))
    fetchData()
  } catch {}
}

// 模板引用
const formRef = ref(null)

onMounted(() => { fetchData(); calcTableMaxHeight() })
</script>

<style scoped>
.job-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.job-table-wrapper :deep(.page-pagination) {
  margin-top: 12px;
  flex-shrink: 0;
}
</style>
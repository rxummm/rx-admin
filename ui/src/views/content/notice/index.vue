<template>
  <div class="notice-page">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('common.search') + $t('content.notice.noticeTitle')" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('content:notice:add')">
        <el-icon><Plus /></el-icon> {{ $t('common.add') + $t('content.notice.title') }}
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('content:notice:delete')" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> {{ $t('common.batchDelete') }}
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

    <div class="notice-table-wrapper">
      <el-table :data="sortedTableData" border stripe v-loading="loading" :max-height="tableMaxHeight" style="width: 100%" @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('id')" prop="id" label="ID" width="70" sortable />
        <el-table-column v-if="visibleColumns.includes('title')" prop="title" :label="$t('content.notice.noticeTitle')" min-width="240" show-overflow-tooltip sortable />
        <el-table-column v-if="visibleColumns.includes('noticeType')" prop="noticeType" :label="$t('content.notice.type')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === '1' ? 'primary' : 'warning'" size="small">
              {{ row.noticeType === '1' ? $t('content.notice.typeOptions.notice') : $t('content.notice.typeOptions.announcement') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('status')" prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('content.notice.statusOptions.normal') : $t('content.notice.statusOptions.closed') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('createByName')" prop="createByName" :label="$t('content.notice.creator')" width="100" />
        <el-table-column v-if="visibleColumns.includes('createTime')" prop="createTime" :label="$t('common.createTime')" width="170" sortable />
        <el-table-column :label="$t('common.operation')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">{{ $t('common.detail') }}</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('content:notice:edit')">{{ $t('common.edit') }}</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('content:notice:delete')">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && sortedTableData.length === 0" />

      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
        class="notice-pagination"
        @size-change="fetchData" @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item :label="$t('content.notice.noticeTitle')" prop="title">
          <el-input v-model="form.title" :placeholder="$t('common.input') + $t('content.notice.noticeTitle')" />
        </el-form-item>
        <el-form-item :label="$t('content.notice.type')">
          <el-radio-group v-model="form.noticeType">
            <el-radio value="1">{{ $t('content.notice.typeOptions.notice') }}</el-radio>
            <el-radio value="2">{{ $t('content.notice.typeOptions.announcement') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="$t('common.remark')" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" :placeholder="$t('common.input') + $t('common.remark')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" :title="$t('content.notice.title') + $t('common.detail')" width="700px">
      <h3 style="margin-bottom: 16px;">{{ viewData.title }}</h3>
      <div style="margin-bottom: 12px; color: var(--text-secondary); font-size: 13px;">
        <span>{{ $t('content.notice.type') }}: <el-tag :type="viewData.noticeType === '1' ? 'primary' : 'warning'" size="small">{{ viewData.noticeType === '1' ? $t('content.notice.typeOptions.notice') : $t('content.notice.typeOptions.announcement') }}</el-tag></span>
        <span style="margin-left: 16px;">{{ $t('content.notice.creator') }}: {{ viewData.createByName }}</span>
        <span style="margin-left: 16px;">{{ $t('common.createTime') }}: {{ viewData.createTime }}</span>
      </div>
      <el-divider />
      <div style="line-height: 1.8; min-height: 120px;" v-html="sanitizeHtml(viewData.content)"></div>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ContentNotice' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sanitizeHtml } from '@/utils/sanitize'
import { getNoticePageApi, addNoticeApi, updateNoticeApi, deleteNoticeApi } from '@/api/notice'

const { t } = useI18n()
const userStore = useUserStore()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('') // 'ascending' | 'descending' | ''

// 列显示配置
const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: t('content.notice.noticeTitle') },
  { key: 'noticeType', label: t('content.notice.type') },
  { key: 'status', label: t('common.status') },
  { key: 'createByName', label: t('content.notice.creator') },
  { key: 'createTime', label: t('common.createTime') }
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

// 前端排序：计算属性根据sortField/sortOrder对tableData排序
const sortedTableData = computed(() => {
  const data = [...tableData.value]
  if (!sortField.value || !sortOrder.value) return data
  return data.sort((a, b) => {
    let valA = a[sortField.value]
    let valB = b[sortField.value]
    // 处理 null/undefined
    if (valA == null) valA = ''
    if (valB == null) valB = ''
    // 字符串排序
    if (typeof valA === 'string') valA = valA.toLowerCase()
    if (typeof valB === 'string') valB = valB.toLowerCase()
    if (valA < valB) return sortOrder.value === 'ascending' ? -1 : 1
    if (valA > valB) return sortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

function handleSortChange({ prop, order }) {
  sortField.value = prop || ''
  sortOrder.value = order || ''
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

// 动态计算表格最大高度：约20行数据 + 表头
// el-table 行高约 44px，20行 + 表头约40px = 920px
const TABLE_ROW_HEIGHT = 44
const TABLE_HEADER_HEIGHT = 40
const MAX_VISIBLE_ROWS = 20
const tableMaxHeight = ref(0)

function calcTableMaxHeight() {
  // 20行数据 + 表头
  const contentHeight = MAX_VISIBLE_ROWS * TABLE_ROW_HEIGHT + TABLE_HEADER_HEIGHT
  const wrapper = document.querySelector('.notice-table-wrapper')
  if (wrapper) {
    const availableHeight = wrapper.clientHeight - 44 // 预留分页条
    // 取可用高度和16行高度中的较小值
    tableMaxHeight.value = Math.max(200, Math.min(availableHeight, contentHeight))
  } else {
    tableMaxHeight.value = contentHeight
  }
}

const dialogVisible = ref(false)
const dialogTitle = ref(t('common.add') + t('content.notice.title'))
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  content: '',
  noticeType: '1',
  status: 1
})

const formRules = {
  title: [{ required: true, message: t('common.input') + t('content.notice.noticeTitle'), trigger: 'blur' }],
  content: [{ required: true, message: t('common.input') + t('common.remark'), trigger: 'blur' }]
}

// 查看
const viewVisible = ref(false)
const viewData = reactive({ title: '', content: '', noticeType: '1', createByName: '', createTime: '' })

onMounted(() => {
  calcTableMaxHeight()
  window.addEventListener('resize', calcTableMaxHeight)
  fetchData()
})

onUnmounted(() => {
  window.removeEventListener('resize', calcTableMaxHeight)
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, keyword: keyword.value }
    const res = await getNoticePageApi(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = t('common.add') + t('content.notice.title')
  resetForm()
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(viewData, row)
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = t('common.edit') + t('content.notice.title')
  Object.assign(form, {
    id: row.id,
    title: row.title,
    content: row.content,
    noticeType: row.noticeType,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.confirmDeleteItem', { name: row.title }), t('common.tip'), { type: 'warning' })
    await deleteNoticeApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(t('common.confirmBatchDelete', { count: selectedIds.value.length }), t('common.batchDelete'), { type: 'warning' })
    await Promise.all(selectedIds.value.map(id => deleteNoticeApi(id)))
    ElMessage.success(t('common.batchDeleteSuccess'))
    selectedIds.value = []
    fetchData()
  } catch {}
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateNoticeApi({ ...form })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addNoticeApi({ ...form })
      ElMessage.success(t('common.addSuccess'))
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  form.id = null
  form.title = ''
  form.content = ''
  form.noticeType = '1'
  form.status = 1
}
</script>

<style scoped>
.notice-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin: -4px;
  padding: 4px;
}

/* 减少搜索栏与 tags 的距离、搜索栏自身高度、与表格间距 */
.notice-page :deep(.search-bar) {
  padding: 6px 10px;
  margin-bottom: 6px;
  gap: 6px;
}

/* 搜索输入框高度缩减 */
.notice-page :deep(.search-bar .el-input) {
  height: 28px;
  font-size: 13px;
}

.notice-page :deep(.search-bar .el-input__wrapper) {
  min-height: 28px;
}

.notice-page :deep(.search-bar .el-button) {
  height: 28px;
  padding: 4px 10px;
  font-size: 13px;
}

.notice-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.notice-table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.notice-pagination {
  margin-top: 12px;
  justify-content: flex-end;
  flex-shrink: 0;
}
</style>
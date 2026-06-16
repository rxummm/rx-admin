<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索标题 / 作者 / 内容" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-select v-model="filterPoemType" placeholder="诗词类型" clearable style="width: 130px" @change="fetchData">
        <el-option v-for="t in poemTypes" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('classics:shuihu:poem:add')">
        <el-icon><Plus /></el-icon> 新增诗词
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('classics:shuihu:poem:delete')" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
      <el-dropdown trigger="click" @command="toggleColumn">
        <el-button>
          <el-icon><Setting /></el-icon> 列设置
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

    <div class="classics-table-wrapper">
      <el-table :data="sortedTableData" border stripe v-loading="loading" :max-height="tableMaxHeight" style="width: 100%"
        @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('id')" prop="id" label="ID" width="70" sortable />
        <el-table-column v-if="visibleColumns.includes('title')" prop="title" label="标题" min-width="200" show-overflow-tooltip sortable>
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('author')" prop="author" label="作者" width="120" show-overflow-tooltip sortable />
        <el-table-column v-if="visibleColumns.includes('poemType')" prop="poemType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.poemType" size="small">{{ row.poemType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('relatedScene')" prop="relatedScene" label="场景/回目" min-width="160" show-overflow-tooltip sortable />
        <el-table-column v-if="visibleColumns.includes('relatedCharacter')" prop="relatedCharacter" label="相关人物" width="130" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('createdTime')" prop="createdTime" label="创建时间" width="170" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('classics:shuihu:poem:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('classics:shuihu:poem:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
        class="page-pagination"
        @size-change="fetchData" @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="诗词标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入诗词标题" />
        </el-form-item>
        <el-form-item label="作者/出处" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者或出处" />
        </el-form-item>
        <el-form-item label="诗词类型" prop="poemType">
          <el-select v-model="form.poemType" placeholder="请选择类型" style="width: 100%">
            <el-option label="诗" value="诗" />
            <el-option label="词" value="词" />
            <el-option label="曲" value="曲" />
            <el-option label="偈语" value="偈语" />
            <el-option label="赞" value="赞" />
          </el-select>
        </el-form-item>
        <el-form-item label="场景/回目">
          <el-input v-model="form.relatedScene" placeholder="如：第十回 林教头风雪山神庙" />
        </el-form-item>
        <el-form-item label="相关人物">
          <el-input v-model="form.relatedCharacter" placeholder="如：宋江、武松" />
        </el-form-item>
        <el-form-item label="诗词内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入诗词正文" />
        </el-form-item>
        <el-form-item label="诗词赏析">
          <el-input v-model="form.appreciation" type="textarea" :rows="4" placeholder="请输入赏析内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="诗词详情" width="700px">
      <div v-if="viewData" class="poem-detail">
        <h3 class="poem-title">{{ viewData.title }}</h3>
        <div class="poem-meta">
          <el-tag type="info">{{ viewData.author || '佚名' }}</el-tag>
          <el-tag v-if="viewData.poemType" size="small">{{ viewData.poemType }}</el-tag>
          <span v-if="viewData.relatedScene">{{ viewData.relatedScene }}</span>
          <span v-if="viewData.relatedCharacter">相关人物：{{ viewData.relatedCharacter }}</span>
        </div>
        <div class="poem-content">
          <pre>{{ viewData.content }}</pre>
        </div>
        <div v-if="viewData.appreciation" class="poem-extra">
          <el-divider content-position="left">赏析</el-divider>
          <p>{{ viewData.appreciation }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsShuihuPoems' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getShuihuPoemPageApi,
  getShuihuPoemDetailApi,
  addShuihuPoemApi,
  updateShuihuPoemApi,
  deleteShuihuPoemApi,
  batchDeleteShuihuPoemApi
} from '@/api/shuihu'

const userStore = useUserStore()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const filterPoemType = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('')

const poemTypes = ref([])

const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: '标题' },
  { key: 'author', label: '作者' },
  { key: 'poemType', label: '类型' },
  { key: 'relatedScene', label: '场景/回目' },
  { key: 'relatedCharacter', label: '相关人物' },
  { key: 'createdTime', label: '创建时间' }
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

function handleSortChange({ prop, order }) {
  sortField.value = prop || ''
  sortOrder.value = order || ''
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

// 动态表格高度（通过 useTableHeight 共享模块，.env 可配置行高/表头/最大行数）
const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.classics-table-wrapper')

const dialogVisible = ref(false)
const dialogTitle = ref('新增诗词')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  author: '',
  poemType: '',
  relatedScene: '',
  relatedCharacter: '',
  content: '',
  appreciation: ''
})

const formRules = {
  title: [{ required: true, message: '请输入诗词标题', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者/出处', trigger: 'blur' }],
  poemType: [{ required: true, message: '请选择诗词类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入诗词内容', trigger: 'blur' }]
}

const viewVisible = ref(false)
const viewData = ref(null)

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
    const res = await getShuihuPoemPageApi(params)
    let records = res.data.records
    if (filterPoemType.value) {
      records = records.filter(r => r.poemType === filterPoemType.value)
    }
    tableData.value = records
    total.value = res.data.total
    const types = new Set(records.map(r => r.poemType).filter(Boolean))
    poemTypes.value = [...types]
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  filterPoemType.value = ''
  page.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增诗词'
  resetForm()
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = row
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑诗词'
  Object.assign(form, {
    id: row.id,
    title: row.title,
    author: row.author,
    poemType: row.poemType,
    relatedScene: row.relatedScene,
    relatedCharacter: row.relatedCharacter,
    content: row.content,
    appreciation: row.appreciation
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除诗词 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteShuihuPoemApi(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条诗词吗？`, '批量删除', { type: 'warning' })
    await batchDeleteShuihuPoemApi(selectedIds.value)
    ElMessage.success('批量删除成功')
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
      await updateShuihuPoemApi({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addShuihuPoemApi({ ...form })
      ElMessage.success('新增成功')
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
  form.author = ''
  form.poemType = ''
  form.relatedScene = ''
  form.relatedCharacter = ''
  form.content = ''
  form.appreciation = ''
}
</script>

<style scoped>
.classics-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.classics-table-wrapper :deep(.page-pagination) {
  margin-top: 12px;
  flex-shrink: 0;
}
.classics-table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}
.poem-detail .poem-title {
  text-align: center;
  margin-bottom: 12px;
}
.poem-detail .poem-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--text-color-secondary);
  margin-bottom: 16px;
}
.poem-detail .poem-content pre {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 15px;
  padding: 16px;
  background: var(--bg-color-page, #f5f7fa);
  border-radius: 6px;
  max-height: 400px;
  overflow-y: auto;
}
.poem-detail .poem-extra p {
  line-height: 1.8;
  color: var(--text-color-regular);
}
</style>
<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索标题 / 内容 / 人物" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-select v-model="filterDifficulty" placeholder="阅读难度" clearable style="width: 130px" @change="fetchData">
        <el-option label="简单" value="简单" />
        <el-option label="一般" value="一般" />
        <el-option label="困难" value="困难" />
      </el-select>
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('classics:shuihu:chapter:add')">
        <el-icon><Plus /></el-icon> 新增章节
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('classics:shuihu:chapter:delete')" :disabled="selectedIds.length === 0">
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
        <el-table-column v-if="visibleColumns.includes('chapterNumber')" prop="chapterNumber" label="回" width="60" sortable>
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.chapterNumber }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('chapterTitle')" prop="chapterTitle" label="章节标题" min-width="220" show-overflow-tooltip sortable>
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">{{ row.chapterTitle }}</el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('characters')" prop="characters" label="出场人物" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('locations')" prop="locations" label="涉及地点" width="120" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('themes')" prop="themes" label="主题" width="120" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('readingDifficulty')" prop="readingDifficulty" label="阅读难度" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.readingDifficulty" size="small"
              :type="row.readingDifficulty === '困难' ? 'danger' : row.readingDifficulty === '一般' ? 'warning' : 'success'">
              {{ row.readingDifficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('estimatedReadingTime')" prop="estimatedReadingTime" label="预计阅读" width="100">
          <template #default="{ row }">
            {{ row.estimatedReadingTime }}分钟
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('createdTime')" prop="createdTime" label="创建时间" width="170" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('classics:shuihu:chapter:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('classics:shuihu:chapter:delete')">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="750px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="章节编号" prop="chapterNumber">
          <el-input-number v-model="form.chapterNumber" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="章节标题" prop="chapterTitle">
          <el-input v-model="form.chapterTitle" placeholder="如：张天师祈禳瘟疫 洪太尉误走妖魔" />
        </el-form-item>
        <el-form-item label="章节副标题">
          <el-input v-model="form.chapterSubtitle" placeholder="如：第一回" />
        </el-form-item>
        <el-form-item label="章节内容">
          <el-input v-model="form.chapterContent" type="textarea" :rows="12" placeholder="请输入章节正文内容" />
        </el-form-item>
        <el-form-item label="精彩看点">
          <el-input v-model="form.highlights" type="textarea" :rows="3" placeholder="如：武松景阳冈打虎" />
        </el-form-item>
        <el-form-item label="出场人物">
          <el-input v-model="form.characters" placeholder="如：宋江、武松、林冲" />
        </el-form-item>
        <el-form-item label="涉及地点">
          <el-input v-model="form.locations" placeholder="如：景阳冈、梁山泊" />
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="form.themes" placeholder="如：兄弟义气、官逼民反" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="form.keywords" placeholder="如：打虎、英雄" />
        </el-form-item>
        <el-form-item label="阅读难度">
          <el-select v-model="form.readingDifficulty" placeholder="请选择" style="width: 100%">
            <el-option label="简单" value="简单" />
            <el-option label="一般" value="一般" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计阅读时间(分)">
          <el-input-number v-model="form.estimatedReadingTime" :min="1" :max="120" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="章节详情" width="750px">
      <div v-if="viewData" class="chapter-detail">
        <h3 class="chapter-title">第{{ viewData.chapterNumber }}回 {{ viewData.chapterTitle }}</h3>
        <div class="chapter-meta">
          <el-tag v-if="viewData.readingDifficulty" size="small"
            :type="viewData.readingDifficulty === '困难' ? 'danger' : viewData.readingDifficulty === '一般' ? 'warning' : 'success'">
            {{ viewData.readingDifficulty }}
          </el-tag>
          <span v-if="viewData.estimatedReadingTime">预计阅读：{{ viewData.estimatedReadingTime }}分钟</span>
        </div>
        <div class="chapter-info" v-if="viewData.characters || viewData.locations || viewData.themes">
          <p v-if="viewData.characters"><strong>出场人物：</strong>{{ viewData.characters }}</p>
          <p v-if="viewData.locations"><strong>涉及地点：</strong>{{ viewData.locations }}</p>
          <p v-if="viewData.themes"><strong>主题：</strong>{{ viewData.themes }}</p>
        </div>
        <div class="chapter-section" v-if="viewData.highlights">
          <el-divider content-position="left">精彩看点</el-divider>
          <p>{{ viewData.highlights }}</p>
        </div>
        <div class="chapter-section" v-if="viewData.chapterContent">
          <el-divider content-position="left">章节内容</el-divider>
          <p class="chapter-content">{{ viewData.chapterContent }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsShuihuChapters' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getShuihuChapterPageApi,
  getShuihuChapterDetailApi,
  addShuihuChapterApi,
  updateShuihuChapterApi,
  deleteShuihuChapterApi,
  batchDeleteShuihuChapterApi
} from '@/api/shuihu'

const userStore = useUserStore()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const filterDifficulty = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('')

const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'chapterNumber', label: '回' },
  { key: 'chapterTitle', label: '章节标题' },
  { key: 'characters', label: '出场人物' },
  { key: 'locations', label: '涉及地点' },
  { key: 'themes', label: '主题' },
  { key: 'readingDifficulty', label: '阅读难度' },
  { key: 'estimatedReadingTime', label: '预计阅读' },
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
const dialogTitle = ref('新增章节')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  chapterNumber: null,
  chapterTitle: '',
  chapterSubtitle: '',
  chapterContent: '',
  highlights: '',
  characters: '',
  locations: '',
  themes: '',
  keywords: '',
  readingDifficulty: '一般',
  estimatedReadingTime: 5
})

const formRules = {
  chapterNumber: [{ required: true, message: '请输入章节编号', trigger: 'blur' }],
  chapterTitle: [{ required: true, message: '请输入章节标题', trigger: 'blur' }]
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
    const res = await getShuihuChapterPageApi(params)
    let records = res.data.records
    if (filterDifficulty.value) {
      records = records.filter(r => r.readingDifficulty === filterDifficulty.value)
    }
    tableData.value = records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  filterDifficulty.value = ''
  page.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增章节'
  resetForm()
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = row
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑章节'
  Object.assign(form, {
    id: row.id,
    chapterNumber: row.chapterNumber,
    chapterTitle: row.chapterTitle,
    chapterSubtitle: row.chapterSubtitle,
    chapterContent: row.chapterContent,
    highlights: row.highlights,
    characters: row.characters,
    locations: row.locations,
    themes: row.themes,
    keywords: row.keywords,
    readingDifficulty: row.readingDifficulty || '一般',
    estimatedReadingTime: row.estimatedReadingTime || 5
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除第${row.chapterNumber}回 "${row.chapterTitle}" 吗？`, '提示', { type: 'warning' })
    await deleteShuihuChapterApi(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条章节吗？`, '批量删除', { type: 'warning' })
    await batchDeleteShuihuChapterApi(selectedIds.value)
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
      await updateShuihuChapterApi({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addShuihuChapterApi({ ...form })
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
  form.chapterNumber = null
  form.chapterTitle = ''
  form.chapterSubtitle = ''
  form.chapterContent = ''
  form.highlights = ''
  form.characters = ''
  form.locations = ''
  form.themes = ''
  form.keywords = ''
  form.readingDifficulty = '一般'
  form.estimatedReadingTime = 5
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
.chapter-detail .chapter-title {
  text-align: center;
  margin-bottom: 12px;
}
.chapter-detail .chapter-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--text-color-secondary);
  margin-bottom: 16px;
}
.chapter-detail .chapter-info p {
  margin: 4px 0;
  color: var(--text-color-secondary);
}
.chapter-detail .chapter-section p {
  line-height: 1.8;
  color: var(--text-color-regular);
}
.chapter-detail .chapter-content {
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: var(--bg-color-page, #f5f7fa);
  border-radius: 6px;
}
</style>
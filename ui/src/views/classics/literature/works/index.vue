<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索作品标题 / 关键词" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-select v-model="filterDynastyId" placeholder="朝代" clearable style="width: 140px" @change="fetchData">
        <el-option v-for="d in dynastyList" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="filterGenreId" placeholder="体裁" clearable style="width: 140px" @change="fetchData">
        <el-option v-for="g in genreList" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
      <el-select v-model="filterAuthorId" placeholder="作者" clearable filterable style="width: 160px" @change="fetchData">
        <el-option v-for="a in authorList" :key="a.id" :label="a.name" :value="a.id" />
      </el-select>
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('classics:literature:work:add')">
        <el-icon><Plus /></el-icon> 新增作品
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('classics:literature:work:delete')" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <el-table :data="tableData" border stripe v-loading="loading" :max-height="tableMaxHeight" style="width: 100%"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="作品名称" min-width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="dynastyName" label="朝代" width="90" />
        <el-table-column prop="genreName" label="体裁" width="80" />
        <el-table-column prop="wordCount" label="字数" width="80">
          <template #default="{ row }">{{ row.wordCount ? row.wordCount.toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="difficultyLevel" label="难度" width="70">
          <template #default="{ row }">
            <el-rate v-if="row.difficultyLevel" v-model="row.difficultyLevel" disabled show-score :max="5" style="height:22px" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ (row.summary || '-').slice(0, 60) }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('classics:literature:work:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('classics:literature:work:delete')">删除</el-button>
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
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="作品名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入作品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="副标题">
              <el-input v-model="form.subtitle" placeholder="副标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="作者">
              <el-select v-model="form.authorId" filterable placeholder="选择作者" style="width:100%">
                <el-option v-for="a in authorList" :key="a.id" :label="a.name" :value="a.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="朝代">
              <el-select v-model="form.dynastyId" filterable placeholder="选择朝代" style="width:100%">
                <el-option v-for="d in dynastyList" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="体裁">
              <el-select v-model="form.genreId" filterable placeholder="选择体裁" style="width:100%">
                <el-option v-for="g in genreList" :key="g.id" :label="g.name" :value="g.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="字数">
              <el-input-number v-model="form.wordCount" :min="0" style="width:100%" disabled />
              <div style="font-size:12px;color:#909399;margin-top:2px">保存时根据正文自动统计</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-rate v-model="form.difficultyLevel" :max="5" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关键词">
          <el-input v-model="form.keywords" placeholder="多个关键词用逗号分隔" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="标签" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="form.source" placeholder="作品出处" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="form.coverUrl" placeholder="封面图片地址" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="作品摘要简介" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="作品正文内容" />
        </el-form-item>
        <el-form-item label="译文">
          <el-input v-model="form.translation" type="textarea" :rows="4" placeholder="白话译文" />
        </el-form-item>
        <el-form-item label="赏析">
          <el-input v-model="form.appreciation" type="textarea" :rows="4" placeholder="作品赏析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="作品详情" width="800px">
      <template v-if="viewData">
        <h3 class="detail-name">{{ viewData.title }}</h3>
        <p v-if="viewData.subtitle" class="detail-subtitle">{{ viewData.subtitle }}</p>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="作者">{{ viewData.authorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="朝代">{{ viewData.dynastyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="体裁">{{ viewData.genreName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="字数">{{ viewData.wordCount ? viewData.wordCount.toLocaleString() : '-' }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ viewData.difficultyLevel ? '★'.repeat(viewData.difficultyLevel) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ viewData.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="关键词">{{ viewData.keywords || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ viewData.source || '-' }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ viewData.sortOrder }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(viewData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(viewData.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="3">{{ viewData.tags || '-' }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="3">{{ viewData.summary || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="viewData.content" class="detail-content">
          <h4>正文</h4>
          <div class="content-text" v-html="sanitizeHtml(viewData.contentHtml || viewData.content)"></div>
        </div>
        <div v-if="viewData.translation" class="detail-content">
          <h4>译文</h4>
          <p>{{ viewData.translation }}</p>
        </div>
        <div v-if="viewData.appreciation" class="detail-content">
          <h4>赏析</h4>
          <p>{{ viewData.appreciation }}</p>
        </div>
      </template>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsLiteratureWorks' })
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sanitizeHtml } from '@/utils/sanitize'
import { getAllAuthorsApi, getAllDynastiesApi, getAllGenresApi } from '@/api/literature'
import {
  getWorkPageApi, getWorkDetailApi, addWorkApi, updateWorkApi, deleteWorkApi, batchDeleteWorkApi
} from '@/api/literature'

const userStore = useUserStore()

// 下拉选项数据
const dynastyList = ref([])
const genreList = ref([])
const authorList = ref([])

// 筛选条件
const keyword = ref('')
const filterDynastyId = ref(null)
const filterGenreId = ref(null)
const filterAuthorId = ref(null)

// 表格数据
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 表格高度
// 动态表格高度（通过 useTableHeight 共享模块，.env 可配置行高/表头/最大行数）
const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.table-wrapper')

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增作品')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, title: '', subtitle: '', authorId: null, dynastyId: null,
  genreId: null, genreCode: '', content: '', contentHtml: '',
  preface: '', epilogue: '', annotations: '', appreciation: '', translation: '',
  keywords: '', tags: '', difficultyLevel: 3, wordCount: 0,
  source: '', coverUrl: '', summary: '', sortOrder: 0
})

const formRules = {
  title: [{ required: true, message: '请输入作品名称', trigger: 'blur' }]
}

// 查看弹窗
const viewVisible = ref(false)
const viewData = ref(null)

onMounted(async () => {
  calcTableMaxHeight()
  window.addEventListener('resize', calcTableMaxHeight)
  await loadOptions()
  fetchData()
})

onUnmounted(() => {
  window.removeEventListener('resize', calcTableMaxHeight)
})

async function loadOptions() {
  try {
    const [dRes, gRes, aRes] = await Promise.all([
      getAllDynastiesApi(), getAllGenresApi(), getAllAuthorsApi()
    ])
    dynastyList.value = dRes.data
    genreList.value = gRes.data
    authorList.value = aRes.data
  } catch { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: page.value, size: size.value, keyword: keyword.value,
      dynastyId: filterDynastyId.value, genreId: filterGenreId.value, authorId: filterAuthorId.value
    }
    const res = await getWorkPageApi(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return '-'
  // 将 T 替换为空格，截取前19位
  return String(time).replace('T', ' ').substring(0, 19)
}

function resetSearch() {
  keyword.value = ''
  filterDynastyId.value = null
  filterGenreId.value = null
  filterAuthorId.value = null
  page.value = 1
  fetchData()
}

function resetForm() {
  Object.assign(form, {
    id: null, title: '', subtitle: '', authorId: null, dynastyId: null,
    genreId: null, genreCode: '', content: '', contentHtml: '',
    preface: '', epilogue: '', annotations: '', appreciation: '', translation: '',
    keywords: '', tags: '', difficultyLevel: 3, wordCount: 0,
    source: '', coverUrl: '', summary: '', sortOrder: 0
  })
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增作品'
  resetForm()
  dialogVisible.value = true
}

async function handleView(row) {
  try {
    const res = await getWorkDetailApi(row.id)
    viewData.value = res.data
    viewVisible.value = true
  } catch { /* ignore */ }
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑作品'
  Object.assign(form, {
    id: row.id, title: row.title, subtitle: row.subtitle,
    authorId: row.authorId, dynastyId: row.dynastyId, genreId: row.genreId,
    genreCode: row.genreCode, content: row.content, contentHtml: row.contentHtml,
    preface: row.preface, epilogue: row.epilogue, annotations: row.annotations,
    appreciation: row.appreciation, translation: row.translation,
    keywords: row.keywords, tags: row.tags,
    difficultyLevel: row.difficultyLevel ?? 3, wordCount: row.wordCount ?? 0,
    source: row.source, coverUrl: row.coverUrl, summary: row.summary,
    sortOrder: row.sortOrder ?? 0
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除作品「${row.title}」吗？`, '提示', { type: 'warning' })
    await deleteWorkApi(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancel */ }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 部作品吗？`, '批量删除', { type: 'warning' })
    await batchDeleteWorkApi(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch { /* cancel */ }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateWorkApi({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addWorkApi({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.search-bar {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.table-wrapper :deep(.page-pagination) {
  margin-top: 12px;
  flex-shrink: 0;
}

.table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.detail-name {
  text-align: center;
  margin: 0 0 4px 0;
  font-size: 22px;
}

.detail-subtitle {
  text-align: center;
  margin: 0 0 16px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.detail-content {
  margin-top: 20px;
}

.detail-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: var(--text-primary);
  border-left: 3px solid var(--color-primary);
  padding-left: 8px;
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-primary);
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background: var(--bg-hover);
  border-radius: 4px;
  font-size: 14px;
}

.detail-content p {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-regular);
}
</style>
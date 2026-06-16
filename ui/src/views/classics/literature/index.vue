<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="作者" name="author" />
      <el-tab-pane label="朝代" name="dynasty" />
      <el-tab-pane label="体裁" name="genre" />
      <el-tab-pane label="内容分类" name="category" />
    </el-tabs>

    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="searchPlaceholder" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm(addPerm)">
        <el-icon><Plus /></el-icon> 新增
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm(deletePerm)" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" border stripe v-loading="loading" :max-height="tableMaxHeight" style="width: 100%"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />

        <!-- 作者列 -->
        <template v-if="activeTab === 'author'">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="作者姓名" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">{{ row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="courtesyName" label="字" width="100" show-overflow-tooltip />
          <el-table-column prop="pseudonym" label="号" width="100" show-overflow-tooltip />
          <el-table-column prop="dynastyId" label="朝代ID" width="80" />
          <el-table-column prop="authorType" label="类型" width="80" />
          <el-table-column prop="biography" label="简介" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ (row.biography || '-').slice(0, 50) }}</template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="70" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </template>

        <!-- 朝代列 -->
        <template v-if="activeTab === 'dynasty'">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="朝代名称" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">{{ row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="简介" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">{{ (row.description || '-').slice(0, 80) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </template>

        <!-- 体裁列 -->
        <template v-if="activeTab === 'genre'">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="体裁名称" width="160">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">{{ row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">{{ (row.description || '-').slice(0, 100) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </template>

        <!-- 内容分类列 -->
        <template v-if="activeTab === 'category'">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="分类名称" width="160">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">{{ row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">{{ (row.description || '-').slice(0, 80) }}</template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="70" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </template>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm(editPerm)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm(deletePerm)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <!-- 作者表单 -->
        <template v-if="activeTab === 'author'">
          <el-form-item label="作者姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入作者姓名" />
          </el-form-item>
          <el-form-item label="字">
            <el-input v-model="form.courtesyName" placeholder="如：太白、子美" />
          </el-form-item>
          <el-form-item label="号">
            <el-input v-model="form.pseudonym" placeholder="如：青莲居士、东坡" />
          </el-form-item>
          <el-form-item label="朝代ID">
            <el-input-number v-model="form.dynastyId" :min="1" />
          </el-form-item>
          <el-form-item label="出生年份">
            <el-input-number v-model="form.birthYear" />
          </el-form-item>
          <el-form-item label="卒年年份">
            <el-input-number v-model="form.deathYear" />
          </el-form-item>
          <el-form-item label="出生地">
            <el-input v-model="form.birthplace" placeholder="如：长安、洛阳" />
          </el-form-item>
          <el-form-item label="作者类型">
            <el-input v-model="form.authorType" placeholder="如：poet、writer" />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
          </el-form-item>
          <el-form-item label="代表作品">
            <el-input v-model="form.representativeWorks" type="textarea" :rows="2" placeholder="代表作品" />
          </el-form-item>
          <el-form-item label="成就">
            <el-input v-model="form.achievement" type="textarea" :rows="2" placeholder="主要成就" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="form.biography" type="textarea" :rows="4" placeholder="作者生平简介" />
          </el-form-item>
          <el-form-item label="头像URL">
            <el-input v-model="form.avatarUrl" placeholder="头像图片地址" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          </el-form-item>
        </template>

        <!-- 朝代表单 -->
        <template v-if="activeTab === 'dynasty'">
          <el-form-item label="朝代名称" prop="name">
            <el-input v-model="form.name" placeholder="如：唐、宋、元、明、清" />
          </el-form-item>
          <el-form-item label="编码">
            <el-input v-model="form.code" placeholder="如：tang、song" />
          </el-form-item>
          <el-form-item label="起始年份">
            <el-input-number v-model="form.startYear" />
          </el-form-item>
          <el-form-item label="结束年份">
            <el-input-number v-model="form.endYear" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="朝代简介" />
          </el-form-item>
        </template>

        <!-- 体裁表单 -->
        <template v-if="activeTab === 'genre'">
          <el-form-item label="体裁名称" prop="name">
            <el-input v-model="form.name" placeholder="如：五言绝句、七言律诗、词" />
          </el-form-item>
          <el-form-item label="编码">
            <el-input v-model="form.code" placeholder="如：shi、ci" />
          </el-form-item>
          <el-form-item label="父级ID">
            <el-input-number v-model="form.parentId" :min="0" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="体裁描述" />
          </el-form-item>
        </template>

        <!-- 内容分类表单 -->
        <template v-if="activeTab === 'category'">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="form.name" placeholder="如：诗、词、曲、赋、文言文" />
          </el-form-item>
          <el-form-item label="编码">
            <el-input v-model="form.code" placeholder="如：authors、poems" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="分类描述" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="详情" width="650px">
      <template v-if="viewData">
        <!-- 作者详情 -->
        <template v-if="activeTab === 'author'">
          <h3 class="detail-name">{{ viewData.name }}</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="字">{{ viewData.courtesyName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="号">{{ viewData.pseudonym || '-' }}</el-descriptions-item>
            <el-descriptions-item label="朝代ID">{{ viewData.dynastyId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出生年份">{{ viewData.birthYear || '-' }}</el-descriptions-item>
            <el-descriptions-item label="卒年年份">{{ viewData.deathYear || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出生地">{{ viewData.birthplace || '-' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ viewData.authorType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ viewData.sortOrder }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="标签" :span="2">{{ viewData.tags || '-' }}</el-descriptions-item>
            <el-descriptions-item label="代表作品" :span="2">{{ viewData.representativeWorks || '-' }}</el-descriptions-item>
            <el-descriptions-item label="成就" :span="2">{{ viewData.achievement || '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ viewData.biography || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <!-- 朝代详情 -->
        <template v-if="activeTab === 'dynasty'">
          <h3 class="detail-name">{{ viewData.name }}</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <!-- 体裁详情 -->
        <template v-if="activeTab === 'genre'">
          <h3 class="detail-name">{{ viewData.name }}</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <!-- 内容分类详情 -->
        <template v-if="activeTab === 'category'">
          <h3 class="detail-name">{{ viewData.name }}</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="编码">{{ viewData.code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ viewData.sortOrder }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </template>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsLiteratureIndex' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getAuthorPageApi, getAuthorDetailApi, addAuthorApi, updateAuthorApi, deleteAuthorApi, batchDeleteAuthorApi,
  getDynastyPageApi, getDynastyDetailApi, addDynastyApi, updateDynastyApi, deleteDynastyApi, batchDeleteDynastyApi,
  getGenrePageApi, getGenreDetailApi, addGenreApi, updateGenreApi, deleteGenreApi, batchDeleteGenreApi,
  getCategoryPageApi, getCategoryDetailApi, addCategoryApi, updateCategoryApi, deleteCategoryApi, batchDeleteCategoryApi
} from '@/api/literature'

const userStore = useUserStore()

// Tab 状态
const activeTab = ref('author')
const searchPlaceholder = computed(() => {
  const map = { author: '搜索作者姓名 / 字号 / 朝代', dynasty: '搜索朝代名称', genre: '搜索体裁名称', category: '搜索分类名称' }
  return map[activeTab.value] || '搜索'
})

// 权限计算
const prefix = computed(() => `classics:literature:${activeTab.value}`)
const addPerm = computed(() => `${prefix.value}:add`)
const editPerm = computed(() => `${prefix.value}:edit`)
const deletePerm = computed(() => `${prefix.value}:delete`)

// 表格数据
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 表格高度
const TABLE_ROW_HEIGHT = 44
const TABLE_HEADER_HEIGHT = 40
const MAX_VISIBLE_ROWS = 20
const tableMaxHeight = ref(0)

function calcTableMaxHeight() {
  const contentHeight = MAX_VISIBLE_ROWS * TABLE_ROW_HEIGHT + TABLE_HEADER_HEIGHT
  const wrapper = document.querySelector('.table-wrapper')
  if (wrapper) {
    tableMaxHeight.value = Math.max(200, Math.min(wrapper.clientHeight - 44, contentHeight))
  } else {
    tableMaxHeight.value = contentHeight
  }
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, name: '', courtesyName: '', pseudonym: '', dynastyId: null,
  birthYear: null, deathYear: null, birthplace: '', authorType: '', tags: '',
  representativeWorks: '', achievement: '', biography: '', avatarUrl: '',
  sortOrder: 0, description: '', code: '', startYear: null, endYear: null, parentId: 0
})

const formRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

// 查看弹窗
const viewVisible = ref(false)
const viewData = ref(null)

// 根据当前 Tab 获取对应的 API 函数
function getApis() {
  switch (activeTab.value) {
    case 'author': return { page: getAuthorPageApi, detail: getAuthorDetailApi, add: addAuthorApi, update: updateAuthorApi, del: deleteAuthorApi, batchDel: batchDeleteAuthorApi }
    case 'dynasty': return { page: getDynastyPageApi, detail: getDynastyDetailApi, add: addDynastyApi, update: updateDynastyApi, del: deleteDynastyApi, batchDel: batchDeleteDynastyApi }
    case 'genre': return { page: getGenrePageApi, detail: getGenreDetailApi, add: addGenreApi, update: updateGenreApi, del: deleteGenreApi, batchDel: batchDeleteGenreApi }
    case 'category': return { page: getCategoryPageApi, detail: getCategoryDetailApi, add: addCategoryApi, update: updateCategoryApi, del: deleteCategoryApi, batchDel: batchDeleteCategoryApi }
  }
}

function onTabChange() {
  resetSearch()
}

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
    const apis = getApis()
    const params = { page: page.value, size: size.value, keyword: keyword.value }
    const res = await apis.page(params)
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

function resetForm() {
  form.id = null
  form.name = ''
  form.courtesyName = ''
  form.pseudonym = ''
  form.dynastyId = null
  form.birthYear = null
  form.deathYear = null
  form.birthplace = ''
  form.authorType = ''
  form.tags = ''
  form.representativeWorks = ''
  form.achievement = ''
  form.biography = ''
  form.avatarUrl = ''
  form.sortOrder = 0
  form.description = ''
  form.code = ''
  form.startYear = null
  form.endYear = null
  form.parentId = 0
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增'
  resetForm()
  dialogVisible.value = true
}

async function handleView(row) {
  try {
    const apis = getApis()
    const res = await apis.detail(row.id)
    viewData.value = res.data
    viewVisible.value = true
  } catch { /* ignore */ }
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑'
  Object.assign(form, {
    id: row.id, name: row.name, courtesyName: row.courtesyName,
    pseudonym: row.pseudonym, dynastyId: row.dynastyId,
    birthYear: row.birthYear, deathYear: row.deathYear, birthplace: row.birthplace,
    authorType: row.authorType, tags: row.tags,
    representativeWorks: row.representativeWorks, achievement: row.achievement,
    biography: row.biography, avatarUrl: row.avatarUrl,
    sortOrder: row.sortOrder ?? 0, description: row.description,
    code: row.code, startYear: row.startYear, endYear: row.endYear,
    parentId: row.parentId ?? 0
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除这条记录吗？`, '提示', { type: 'warning' })
    const apis = getApis()
    await apis.del(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancel */ }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条记录吗？`, '批量删除', { type: 'warning' })
    const apis = getApis()
    await apis.batchDel(selectedIds.value)
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
    const apis = getApis()
    if (isEdit.value) {
      await apis.update({ ...form })
      ElMessage.success('修改成功')
    } else {
      await apis.add({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
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
  margin: 0 0 16px 0;
  font-size: 20px;
}
</style>
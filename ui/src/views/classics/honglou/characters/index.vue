<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索姓名 / 别称" clearable style="width: 220px" @keyup.enter="fetchData" />
      <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="主角" value="主角" />
        <el-option label="重要配角" value="重要配角" />
        <el-option label="一般角色" value="一般角色" />
      </el-select>
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('classics:honglou:character:add')">
        <el-icon><Plus /></el-icon> 新增人物
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('classics:honglou:character:delete')" :disabled="selectedIds.length === 0">
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

    <div class="honglou-table-wrapper">
      <el-table :data="sortedTableData" border stripe v-loading="loading" :max-height="tableMaxHeight" style="width: 100%"
        @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('id')" prop="id" label="ID" width="70" sortable />
        <el-table-column v-if="visibleColumns.includes('name')" prop="name" label="姓名" width="110" sortable>
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">{{ row.name }}</el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('nickname')" prop="nickname" label="别称" min-width="140" show-overflow-tooltip sortable />
        <el-table-column v-if="visibleColumns.includes('role')" prop="role" label="角色" width="100" sortable>
          <template #default="{ row }">
            <el-tag v-if="row.role" size="small" :type="row.role === '主角' ? 'danger' : row.role === '重要配角' ? 'warning' : 'info'">
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('appearanceDescription')" label="外貌描述" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ (row.appearanceDescription || '-').slice(0, 50) }}
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('personalityTraits')" label="性格特点" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ (row.personalityTraits || '-').slice(0, 40) }}
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('createdTime')" prop="createdTime" label="创建时间" width="170" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('classics:honglou:character:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('classics:honglou:character:delete')">删除</el-button>
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
        <el-form-item label="人物姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="别称/昵称">
          <el-input v-model="form.nickname" placeholder="如：颦儿、宝二哥" />
        </el-form-item>
        <el-form-item label="角色身份" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="主角" value="主角" />
            <el-option label="重要配角" value="重要配角" />
            <el-option label="一般角色" value="一般角色" />
          </el-select>
        </el-form-item>
        <el-form-item label="外貌描述">
          <el-input v-model="form.appearanceDescription" type="textarea" :rows="3" placeholder="如：面若中秋之月，色如春晓之花" />
        </el-form-item>
        <el-form-item label="性格特点">
          <el-input v-model="form.personalityTraits" type="textarea" :rows="3" placeholder="如：多愁善感、才华横溢" />
        </el-form-item>
        <el-form-item label="命运概述">
          <el-input v-model="form.fateSummary" type="textarea" :rows="3" placeholder="如：泪尽而逝，魂归离恨天" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="人物详情" width="700px">
      <div v-if="viewData" class="character-detail">
        <h3 class="character-name">{{ viewData.name }}
          <el-tag v-if="viewData.role" style="margin-left: 8px" size="small"
            :type="viewData.role === '主角' ? 'danger' : viewData.role === '重要配角' ? 'warning' : 'info'">
            {{ viewData.role }}
          </el-tag>
        </h3>
        <div class="character-meta" v-if="viewData.nickname">
          <p><strong>别称：</strong>{{ viewData.nickname }}</p>
        </div>
        <div class="character-section" v-if="viewData.appearanceDescription">
          <el-divider content-position="left">外貌描述</el-divider>
          <p>{{ viewData.appearanceDescription }}</p>
        </div>
        <div class="character-section" v-if="viewData.personalityTraits">
          <el-divider content-position="left">性格特点</el-divider>
          <p>{{ viewData.personalityTraits }}</p>
        </div>
        <div class="character-section" v-if="viewData.fateSummary">
          <el-divider content-position="left">命运概述</el-divider>
          <p>{{ viewData.fateSummary }}</p>
        </div>

        <!-- 人物关系 -->
        <div class="character-section" v-if="relations.length > 0">
          <el-divider content-position="left">人物关系</el-divider>
          <div class="relation-list">
            <div v-for="r in relations" :key="r.id" class="relation-item">
              <el-tag size="small" type="warning">{{ r.relationType }}</el-tag>
              <span class="relation-name" @click="viewRelation(r.toCharacterId)">{{ r.relationDesc }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsHonglouCharacters' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getHonglouCharacterPageApi,
  getHonglouCharacterDetailApi,
  addHonglouCharacterApi,
  updateHonglouCharacterApi,
  deleteHonglouCharacterApi,
  batchDeleteHonglouCharacterApi,
  getHonglouRelationApi
} from '@/api/honglou'

const userStore = useUserStore()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const filterRole = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('') // 'ascending' | 'descending' | ''

// 列显示配置
const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: '姓名' },
  { key: 'nickname', label: '别称' },
  { key: 'role', label: '角色' },
  { key: 'appearanceDescription', label: '外貌描述' },
  { key: 'personalityTraits', label: '性格特点' },
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

// 前端排序
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
const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.honglou-table-wrapper')

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增人物')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  nickname: '',
  role: '',
  appearanceDescription: '',
  personalityTraits: '',
  fateSummary: ''
})

const formRules = {
  name: [{ required: true, message: '请输入人物姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色身份', trigger: 'change' }]
}

// 查看弹窗
const viewVisible = ref(false)
const viewData = ref(null)
const relations = ref([])

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
    const res = await getHonglouCharacterPageApi(params)
    let records = res.data.records
    // 前端按角色筛选
    if (filterRole.value) {
      records = records.filter(r => r.role === filterRole.value)
    }
    tableData.value = records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  filterRole.value = ''
  page.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增人物'
  resetForm()
  dialogVisible.value = true
}

async function handleView(row) {
  try {
    const [detailRes, relationRes] = await Promise.all([
      getHonglouCharacterDetailApi(row.id),
      getHonglouRelationApi(row.id)
    ])
    viewData.value = detailRes.data
    relations.value = relationRes.data || []
    viewVisible.value = true
  } catch {}
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑人物'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    nickname: row.nickname,
    role: row.role,
    appearanceDescription: row.appearanceDescription,
    personalityTraits: row.personalityTraits,
    fateSummary: row.fateSummary
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除人物 "${row.name}" 吗？`, '提示', { type: 'warning' })
    await deleteHonglouCharacterApi(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条人物吗？`, '批量删除', { type: 'warning' })
    await batchDeleteHonglouCharacterApi(selectedIds.value)
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
      await updateHonglouCharacterApi({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addHonglouCharacterApi({ ...form })
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
  form.name = ''
  form.nickname = ''
  form.role = ''
  form.appearanceDescription = ''
  form.personalityTraits = ''
  form.fateSummary = ''
}

async function viewRelation(characterId) {
  try {
    const [detailRes, relationRes] = await Promise.all([
      getHonglouCharacterDetailApi(characterId),
      getHonglouRelationApi(characterId)
    ])
    viewData.value = detailRes.data
    relations.value = relationRes.data || []
  } catch {}
}
</script>

<style scoped>
.honglou-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.honglou-table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.character-detail .character-name {
  text-align: center;
  margin-bottom: 12px;
}
.character-detail .character-meta p {
  margin: 4px 0;
  color: var(--text-secondary);
}
.character-detail .character-section p {
  line-height: 1.8;
  color: var(--text-regular);
}
.character-detail .relation-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.character-detail .relation-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.character-detail .relation-name {
  cursor: pointer;
  color: var(--el-color-primary);
}
.character-detail .relation-name:hover {
  text-decoration: underline;
}
</style>

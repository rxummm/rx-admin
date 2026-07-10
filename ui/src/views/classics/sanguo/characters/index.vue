<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索姓名 / 字 / 绰号 / 事迹"
        clearable
        style="width: 240px"
        @keyup.enter="fetchData"
      />
      <el-select v-model="filterCountry" placeholder="国家筛选" clearable style="width: 140px" @change="fetchData">
        <el-option v-for="c in countryOptions" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('classics:sanguo:character:add')">
        <el-icon><Plus /></el-icon> 新增人物
      </el-button>
      <el-button
        type="danger"
        @click="handleBatchDelete"
        v-if="userStore.hasPerm('classics:sanguo:character:delete')"
        :disabled="selectedIds.length === 0"
      >
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
      <el-table
        :data="sortedTableData"
        border
        stripe
        v-loading="loading"
        :max-height="tableMaxHeight"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('id')" prop="id" label="ID" width="70" sortable />
        <el-table-column v-if="visibleColumns.includes('name')" prop="name" label="姓名" width="110" sortable>
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">{{ row.name }}</el-button>
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('courtesyName')"
          prop="courtesyName"
          label="字"
          width="100"
          show-overflow-tooltip
        />
        <el-table-column
          v-if="visibleColumns.includes('nickname')"
          prop="nickname"
          label="绰号"
          width="110"
          show-overflow-tooltip
        />
        <el-table-column v-if="visibleColumns.includes('country')" prop="country" label="国家" width="80">
          <template #default="{ row }">
            <el-tag
              v-if="row.country"
              size="small"
              :type="
                row.country === '蜀'
                  ? 'success'
                  : row.country === '魏'
                    ? 'danger'
                    : row.country === '吴'
                      ? 'warning'
                      : 'info'
              "
            >
              {{ row.country }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('role')" prop="role" label="角色" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.role" size="small" type="info">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('position')"
          prop="position"
          label="官职"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          v-if="visibleColumns.includes('weapon')"
          prop="weapon"
          label="武器"
          width="110"
          show-overflow-tooltip
        />
        <el-table-column
          v-if="visibleColumns.includes('notableEvents')"
          label="著名事迹"
          min-width="180"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ (row.notableEvents || '-').slice(0, 50) }}
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('createdTime')"
          prop="createdTime"
          label="创建时间"
          width="170"
          sortable
        />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEdit(row)"
              v-if="userStore.hasPerm('classics:sanguo:character:edit')"
              >编辑</el-button
            >
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="userStore.hasPerm('classics:sanguo:character:delete')"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="page-pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="人物姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="字">
          <el-input v-model="form.courtesyName" placeholder="如：孔明" />
        </el-form-item>
        <el-form-item label="号">
          <el-input v-model="form.styleName" placeholder="如：卧龙先生" />
        </el-form-item>
        <el-form-item label="绰号">
          <el-input v-model="form.nickname" placeholder="如：常胜将军" />
        </el-form-item>
        <el-form-item label="角色类型">
          <el-input v-model="form.role" placeholder="如：君主、武将、谋士、文官" />
        </el-form-item>
        <el-form-item label="所属国家">
          <el-select v-model="form.country" placeholder="请选择" style="width: 100%">
            <el-option label="魏" value="魏" />
            <el-option label="蜀" value="蜀" />
            <el-option label="吴" value="吴" />
            <el-option label="汉" value="汉" />
            <el-option label="晋" value="晋" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="官职/地位">
          <el-input v-model="form.position" placeholder="如：丞相、大将军" />
        </el-form-item>
        <el-form-item label="武器">
          <el-input v-model="form.weapon" placeholder="如：青龙偃月刀、丈八蛇矛" />
        </el-form-item>
        <el-form-item label="籍贯">
          <el-input v-model="form.hometown" placeholder="如：涿郡涿县" />
        </el-form-item>
        <el-form-item label="外貌描述">
          <el-input
            v-model="form.appearanceDescription"
            type="textarea"
            :rows="2"
            placeholder="如：身长八尺，面如冠玉"
          />
        </el-form-item>
        <el-form-item label="性格特点">
          <el-input v-model="form.personalityTraits" type="textarea" :rows="2" placeholder="如：忠义无双、智勇双全" />
        </el-form-item>
        <el-form-item label="命运概述">
          <el-input v-model="form.fateSummary" type="textarea" :rows="2" placeholder="如：败走麦城，被擒斩首" />
        </el-form-item>
        <el-form-item label="著名事迹">
          <el-input v-model="form.notableEvents" type="textarea" :rows="3" placeholder="如：过五关斩六将、单刀赴会" />
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
        <h3 class="character-name">
          {{ viewData.name }}
          <el-tag
            v-if="viewData.country"
            style="margin-left: 8px"
            size="small"
            :type="
              viewData.country === '蜀'
                ? 'success'
                : viewData.country === '魏'
                  ? 'danger'
                  : viewData.country === '吴'
                    ? 'warning'
                    : 'info'
            "
          >
            {{ viewData.country }}
          </el-tag>
        </h3>
        <div class="character-meta">
          <p v-if="viewData.courtesyName"><strong>字：</strong>{{ viewData.courtesyName }}</p>
          <p v-if="viewData.styleName"><strong>号：</strong>{{ viewData.styleName }}</p>
          <p v-if="viewData.nickname"><strong>绰号：</strong>{{ viewData.nickname }}</p>
          <p v-if="viewData.role"><strong>角色：</strong>{{ viewData.role }}</p>
          <p v-if="viewData.position"><strong>官职：</strong>{{ viewData.position }}</p>
          <p v-if="viewData.weapon"><strong>武器：</strong>{{ viewData.weapon }}</p>
          <p v-if="viewData.hometown"><strong>籍贯：</strong>{{ viewData.hometown }}</p>
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
        <div class="character-section" v-if="viewData.notableEvents">
          <el-divider content-position="left">著名事迹</el-divider>
          <p>{{ viewData.notableEvents }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsSanguoCharacters' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getSanguoCharacterPageApi,
  addSanguoCharacterApi,
  updateSanguoCharacterApi,
  deleteSanguoCharacterApi,
  batchDeleteSanguoCharacterApi
} from '@/api/sanguo'

const userStore = useUserStore()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const filterCountry = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('')

const countryOptions = ref([])

const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: '姓名' },
  { key: 'courtesyName', label: '字' },
  { key: 'nickname', label: '绰号' },
  { key: 'country', label: '国家' },
  { key: 'role', label: '角色' },
  { key: 'position', label: '官职' },
  { key: 'weapon', label: '武器' },
  { key: 'notableEvents', label: '著名事迹' },
  { key: 'createdTime', label: '创建时间' }
]
const visibleColumns = ref(columnOptions.map((c) => c.key))

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
  selectedIds.value = rows.map((r) => r.id)
}

// 动态表格高度（通过 useTableHeight 共享模块，.env 可配置行高/表头/最大行数）
const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.classics-table-wrapper')

const dialogVisible = ref(false)
const dialogTitle = ref('新增人物')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  courtesyName: '',
  styleName: '',
  nickname: '',
  role: '',
  country: '',
  position: '',
  weapon: '',
  hometown: '',
  appearanceDescription: '',
  personalityTraits: '',
  fateSummary: '',
  notableEvents: ''
})

const formRules = {
  name: [{ required: true, message: '请输入人物姓名', trigger: 'blur' }]
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
    const res = await getSanguoCharacterPageApi(params)
    let records = res.data.records
    if (filterCountry.value) {
      records = records.filter((r) => r.country === filterCountry.value)
    }
    tableData.value = records
    total.value = res.data.total
    const countries = new Set(records.map((r) => r.country).filter(Boolean))
    countryOptions.value = [...countries]
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  filterCountry.value = ''
  page.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增人物'
  resetForm()
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = row
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑人物'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    courtesyName: row.courtesyName,
    styleName: row.styleName,
    nickname: row.nickname,
    role: row.role,
    country: row.country,
    position: row.position,
    weapon: row.weapon,
    hometown: row.hometown,
    appearanceDescription: row.appearanceDescription,
    personalityTraits: row.personalityTraits,
    fateSummary: row.fateSummary,
    notableEvents: row.notableEvents
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除人物 "${row.name}" 吗？`, '提示', { type: 'warning' })
    await deleteSanguoCharacterApi(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条人物吗？`, '批量删除', { type: 'warning' })
    await batchDeleteSanguoCharacterApi(selectedIds.value)
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
      await updateSanguoCharacterApi({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addSanguoCharacterApi({ ...form })
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
  form.courtesyName = ''
  form.styleName = ''
  form.nickname = ''
  form.role = ''
  form.country = ''
  form.position = ''
  form.weapon = ''
  form.hometown = ''
  form.appearanceDescription = ''
  form.personalityTraits = ''
  form.fateSummary = ''
  form.notableEvents = ''
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
.character-detail .character-name {
  text-align: center;
  margin-bottom: 12px;
}
.character-detail .character-meta p {
  margin: 4px 0;
  color: var(--text-color-secondary);
}
.character-detail .character-section p {
  line-height: 1.8;
  color: var(--text-color-regular);
}
</style>

<template>
  <div class="page-container">
    <el-row :gutter="16" style="flex: 1; min-height: 0;">
      <!-- 左侧：字典类型 -->
      <el-col :span="10">
        <el-card shadow="never" header="字典类型" style="height: 100%; display: flex; flex-direction: column;">
          <div class="search-bar" style="padding: 0; margin-bottom: 6px; background: transparent;">
            <el-input v-model="typeKeyword" placeholder="搜索字典名称/类型" clearable size="small" style="width: 180px;" />
            <div style="flex: 1" />
            <el-button type="primary" size="small" @click="handleAddType" v-if="userStore.hasPerm('sys:dict:add')">
              <el-icon><Plus /></el-icon> 新增
            </el-button>
            <el-dropdown trigger="click" @command="toggleTypeColumn">
              <el-button size="small">
                <el-icon><Setting /></el-icon> 列设置
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="col in typeColumnOptions" :key="col.key" :command="col.key">
                    <el-icon v-if="visibleTypeColumns.includes(col.key)"><Check /></el-icon>
                    <span :style="{ opacity: visibleTypeColumns.includes(col.key) ? 1 : 0.4 }">{{ col.label }}</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <el-table :data="sortedTypeData" border stripe v-loading="typeLoading" highlight-current-row
            @row-click="onTypeClick" :row-class-name="typeRowClassName" @sort-change="handleTypeSortChange" style="flex: 1;">
            <el-table-column v-if="visibleTypeColumns.includes('dictName')" prop="dictName" label="字典名称" min-width="120" sortable />
            <el-table-column v-if="visibleTypeColumns.includes('dictType')" prop="dictType" label="字典类型" min-width="140" show-overflow-tooltip sortable />
            <el-table-column v-if="visibleTypeColumns.includes('status')" prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click.stop="handleEditType(row)" v-if="userStore.hasPerm('sys:dict:edit')">编辑</el-button>
                <el-button link type="danger" size="small" @click.stop="handleDeleteType(row)" v-if="userStore.hasPerm('sys:dict:delete')">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="typePage" v-model:page-size="typeSize" :total="typeTotal"
            :page-sizes="[5, 10, 20]" layout="total, prev, pager, next" size="small"
            style="margin-top: 8px; justify-content: flex-end; flex-shrink: 0;"
            @size-change="fetchTypeData" @current-change="fetchTypeData"
          />
        </el-card>
      </el-col>

      <!-- 右侧：字典数据 -->
      <el-col :span="14">
        <el-card shadow="never" :header="`字典数据${currentType ? ' - ' + currentType.dictName : ''}`" style="height: 100%; display: flex; flex-direction: column;">
          <div class="search-bar" style="padding: 0; margin-bottom: 6px; background: transparent;" v-if="currentType">
            <div style="flex: 1" />
            <el-button type="primary" size="small" @click="handleAddData" v-if="userStore.hasPerm('sys:dict:add')">
              <el-icon><Plus /></el-icon> 新增数据
            </el-button>
            <el-dropdown trigger="click" @command="toggleDataColumn">
              <el-button size="small">
                <el-icon><Setting /></el-icon> 列设置
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="col in dataColumnOptions" :key="col.key" :command="col.key">
                    <el-icon v-if="visibleDataColumns.includes(col.key)"><Check /></el-icon>
                    <span :style="{ opacity: visibleDataColumns.includes(col.key) ? 1 : 0.4 }">{{ col.label }}</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <el-table :data="sortedDataList" border stripe v-loading="dataLoading" style="flex: 1;" @sort-change="handleDataSortChange">
            <el-table-column v-if="visibleDataColumns.includes('dictLabel')" prop="dictLabel" label="字典标签" width="120" sortable />
            <el-table-column v-if="visibleDataColumns.includes('dictValue')" prop="dictValue" label="字典键值" width="120" sortable />
            <el-table-column v-if="visibleDataColumns.includes('sort')" prop="sort" label="排序" width="60" sortable />
            <el-table-column v-if="visibleDataColumns.includes('status')" prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="visibleDataColumns.includes('remark')" prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEditData(row)" v-if="userStore.hasPerm('sys:dict:edit')">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDeleteData(row)" v-if="userStore.hasPerm('sys:dict:delete')">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!currentType" description="请选择左侧字典类型" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeFormRules" label-width="80px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" :disabled="isTypeEdit" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="handleTypeSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataFormRules" label-width="80px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitLoading" @click="handleDataSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolDict' })
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getDictTypePageApi, addDictTypeApi, updateDictTypeApi, deleteDictTypeApi } from '@/api/dict'
import { getDictDataByTypeApi, addDictDataApi, updateDictDataApi, deleteDictDataApi } from '@/api/dict'

const userStore = useUserStore()

// 字典类型
const typeData = ref([])
const typeLoading = ref(false)
const typeKeyword = ref('')
const typePage = ref(1)
const typeSize = ref(10)
const typeTotal = ref(0)
const currentType = ref(null)
const typeSortField = ref('')
const typeSortOrder = ref('')

// 类型列设置
const typeColumnOptions = [
  { key: 'dictName', label: '字典名称' },
  { key: 'dictType', label: '字典类型' },
  { key: 'status', label: '状态' }
]
const visibleTypeColumns = ref(typeColumnOptions.map(c => c.key))

function toggleTypeColumn(key) {
  const idx = visibleTypeColumns.value.indexOf(key)
  if (idx > -1) {
    visibleTypeColumns.value.splice(idx, 1)
  } else {
    visibleTypeColumns.value.push(key)
  }
}

const sortedTypeData = computed(() => {
  const data = [...typeData.value]
  if (!typeSortField.value || !typeSortOrder.value) return data
  return data.sort((a, b) => {
    let valA = a[typeSortField.value]
    let valB = b[typeSortField.value]
    if (valA == null) valA = ''
    if (valB == null) valB = ''
    if (typeof valA === 'string') valA = valA.toLowerCase()
    if (typeof valB === 'string') valB = valB.toLowerCase()
    if (valA < valB) return typeSortOrder.value === 'ascending' ? -1 : 1
    if (valA > valB) return typeSortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

function handleTypeSortChange({ prop, order }) {
  typeSortField.value = prop || ''
  typeSortOrder.value = order || ''
}

// 字典类型弹窗
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增字典类型')
const isTypeEdit = ref(false)
const typeSubmitLoading = ref(false)
const typeFormRef = ref(null)
const typeForm = reactive({ id: null, dictName: '', dictType: '', remark: '', status: 1 })
const typeFormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

// 字典数据
const dataList = ref([])
const dataLoading = ref(false)
const dataSortField = ref('')
const dataSortOrder = ref('')

// 数据列设置
const dataColumnOptions = [
  { key: 'dictLabel', label: '字典标签' },
  { key: 'dictValue', label: '字典键值' },
  { key: 'sort', label: '排序' },
  { key: 'status', label: '状态' },
  { key: 'remark', label: '备注' }
]
const visibleDataColumns = ref(dataColumnOptions.map(c => c.key))

function toggleDataColumn(key) {
  const idx = visibleDataColumns.value.indexOf(key)
  if (idx > -1) {
    visibleDataColumns.value.splice(idx, 1)
  } else {
    visibleDataColumns.value.push(key)
  }
}

const sortedDataList = computed(() => {
  const data = [...dataList.value]
  if (!dataSortField.value || !dataSortOrder.value) return data
  return data.sort((a, b) => {
    let valA = a[dataSortField.value]
    let valB = b[dataSortField.value]
    if (valA == null) valA = ''
    if (valB == null) valB = ''
    if (typeof valA === 'string') valA = valA.toLowerCase()
    if (typeof valB === 'string') valB = valB.toLowerCase()
    if (valA < valB) return dataSortOrder.value === 'ascending' ? -1 : 1
    if (valA > valB) return dataSortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

function handleDataSortChange({ prop, order }) {
  dataSortField.value = prop || ''
  dataSortOrder.value = order || ''
}

// 字典数据弹窗
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('新增字典数据')
const isDataEdit = ref(false)
const dataSubmitLoading = ref(false)
const dataFormRef = ref(null)
const dataForm = reactive({ id: null, typeId: null, dictLabel: '', dictValue: '', sort: 0, remark: '', status: 1 })
const dataFormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

onMounted(() => fetchTypeData())

async function fetchTypeData() {
  typeLoading.value = true
  try {
    const res = await getDictTypePageApi({ page: typePage.value, size: typeSize.value, keyword: typeKeyword.value })
    typeData.value = res.data.records
    typeTotal.value = res.data.total
  } finally {
    typeLoading.value = false
  }
}

function typeRowClassName({ row }) {
  return currentType.value && row.id === currentType.value.id ? 'current-row' : ''
}

async function onTypeClick(row) {
  currentType.value = row
  dataLoading.value = true
  try {
    const res = await getDictDataByTypeApi(row.id)
    dataList.value = res.data || []
  } finally {
    dataLoading.value = false
  }
}

// 类型 CRUD
function handleAddType() {
  isTypeEdit.value = false
  typeDialogTitle.value = '新增字典类型'
  Object.assign(typeForm, { id: null, dictName: '', dictType: '', remark: '', status: 1 })
  typeDialogVisible.value = true
}

function handleEditType(row) {
  isTypeEdit.value = true
  typeDialogTitle.value = '编辑字典类型'
  Object.assign(typeForm, { id: row.id, dictName: row.dictName, dictType: row.dictType, remark: row.remark || '', status: row.status })
  typeDialogVisible.value = true
}

async function handleDeleteType(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典类型 "${row.dictName}" 吗？`, '提示', { type: 'warning' })
    await deleteDictTypeApi(row.id)
    ElMessage.success('删除成功')
    if (currentType.value?.id === row.id) currentType.value = null
    fetchTypeData()
  } catch {}
}

async function handleTypeSubmit() {
  const valid = await typeFormRef.value.validate().catch(() => false)
  if (!valid) return
  typeSubmitLoading.value = true
  try {
    if (isTypeEdit.value) {
      await updateDictTypeApi({ ...typeForm })
      ElMessage.success('修改成功')
    } else {
      await addDictTypeApi({ ...typeForm })
      ElMessage.success('新增成功')
    }
    typeDialogVisible.value = false
    fetchTypeData()
  } finally {
    typeSubmitLoading.value = false
  }
}

// 数据 CRUD
function handleAddData() {
  isDataEdit.value = false
  dataDialogTitle.value = '新增字典数据'
  Object.assign(dataForm, { id: null, typeId: currentType.value.id, dictLabel: '', dictValue: '', sort: 0, remark: '', status: 1 })
  dataDialogVisible.value = true
}

function handleEditData(row) {
  isDataEdit.value = true
  dataDialogTitle.value = '编辑字典数据'
  Object.assign(dataForm, { id: row.id, typeId: row.typeId, dictLabel: row.dictLabel, dictValue: row.dictValue, sort: row.sort, remark: row.remark || '', status: row.status })
  dataDialogVisible.value = true
}

async function handleDeleteData(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典数据 "${row.dictLabel}" 吗？`, '提示', { type: 'warning' })
    await deleteDictDataApi(row.id)
    ElMessage.success('删除成功')
    onTypeClick(currentType.value)
  } catch {}
}

async function handleDataSubmit() {
  const valid = await dataFormRef.value.validate().catch(() => false)
  if (!valid) return
  dataSubmitLoading.value = true
  try {
    if (isDataEdit.value) {
      await updateDictDataApi({ ...dataForm })
      ElMessage.success('修改成功')
    } else {
      await addDictDataApi({ ...dataForm })
      ElMessage.success('新增成功')
    }
    dataDialogVisible.value = false
    onTypeClick(currentType.value)
  } finally {
    dataSubmitLoading.value = false
  }
}
</script>

<style scoped lang="scss">
::deep(.current-row) {
  background-color: var(--dict-current-row-bg) !important;
}
</style>

<template>
  <div class="region-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="queryParams.level" placeholder="层级筛选" clearable style="width: 120px">
        <el-option :label="$t('tool.region.levelOptions.province')" :value="1" />
        <el-option :label="$t('tool.region.levelOptions.city')" :value="2" />
        <el-option :label="$t('tool.region.levelOptions.district')" :value="3" />
      </el-select>
      <el-input
        v-model="queryParams.parentCode"
        placeholder="上级代码"
        clearable
        style="width: 140px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
      <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('tool:region:add')">
        <el-icon><Plus /></el-icon> {{ $t('common.add') }}
      </el-button>
    </div>

    <!-- 表格 + 分页 包裹容器 -->
    <div class="region-table-wrapper">
      <el-table
        ref="tableRef"
        :data="tableData"
        border
        stripe
        v-loading="loading"
        :max-height="tableMaxHeight"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :load="loadChildren"
        lazy
        style="width: 100%"
      >
        <el-table-column prop="name" :label="$t('tool.region.name')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="code" :label="$t('tool.region.code')" width="130" />
        <el-table-column prop="level" :label="$t('tool.region.level')" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">
              {{ levelLabel(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" :label="$t('tool.region.parentCode')" width="130" />
        <el-table-column prop="abbreviation" :label="$t('tool.region.shortName')" width="80" align="center" />
        <el-table-column prop="pinyin" :label="$t('tool.region.pinyin')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sort" :label="$t('common.sort')" width="70" align="center" />
        <el-table-column prop="status" :label="$t('common.status')" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('common.enable') : $t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEdit(row)"
              v-if="userStore.hasPerm('tool:region:edit')"
              >{{ $t('common.edit') }}</el-button
            >
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="userStore.hasPerm('tool:region:delete')"
              >{{ $t('common.delete') }}</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 15, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="region-pagination"
        @size-change="fetchRootData"
        @current-change="fetchRootData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item :label="$t('tool.region.parentCode')" prop="parentCode" v-if="form.level !== 1">
          <el-cascader
            v-model="parentCascader"
            :options="cascaderOptions"
            :props="{ value: 'code', label: 'name', checkStrictly: true, lazy: true, lazyLoad: cascaderLazyLoad }"
            placeholder="请选择上级行政区划（可搜索）"
            filterable
            clearable
            style="width: 100%"
            @change="handleParentChange"
          />
        </el-form-item>
        <el-form-item :label="$t('tool.region.level')" prop="level">
          <el-radio-group v-model="form.level" @change="handleLevelChange">
            <el-radio :value="1">{{ $t('tool.region.levelOptions.province') }}</el-radio>
            <el-radio :value="2">{{ $t('tool.region.levelOptions.city') }}</el-radio>
            <el-radio :value="3">{{ $t('tool.region.levelOptions.district') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('tool.region.code')" prop="code">
          <el-input v-model="form.code" placeholder="如 440300" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('tool.region.name')" prop="name">
          <el-input v-model="form.name" placeholder="如 深圳市" />
        </el-form-item>
        <el-form-item :label="$t('tool.region.pinyin')">
          <el-input v-model="form.pinyin" placeholder="如 ShenZhen Shi" />
        </el-form-item>
        <el-form-item :label="$t('tool.region.shortName')">
          <el-input v-model="form.abbreviation" placeholder="如 深" />
        </el-form-item>
        <el-form-item :label="$t('common.sort')">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
defineOptions({ name: 'ToolRegion' })
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
import {
  getRegionPageApi,
  getRegionChildrenApi,
  getRegionByIdApi,
  addRegionApi,
  updateRegionApi,
  deleteRegionApi
} from '@/api/region'

const userStore = useUserStore()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 15,
  keyword: '',
  level: '',
  parentCode: ''
})

// 表格引用
const tableRef = ref(null)

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 已加载过的子节点集合（记录哪些节点已经加载过了）
const loadedNodeIds = ref(new Set())

// 表格最大高度
const tableMaxHeight = ref(400)

function calcTableMaxHeight() {
  const wrapper = document.querySelector('.region-table-wrapper')
  if (wrapper) {
    const availableHeight = wrapper.clientHeight - 44 // 预留分页条高度
    tableMaxHeight.value = Math.max(200, availableHeight)
  }
}

// 监听窗口 resize
let resizeObserver = null
function setupResizeObserver() {
  const wrapper = document.querySelector('.region-table-wrapper')
  if (wrapper && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      calcTableMaxHeight()
    })
    resizeObserver.observe(wrapper)
  } else {
    window.addEventListener('resize', calcTableMaxHeight)
  }
}

function cleanupResizeObserver() {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  window.removeEventListener('resize', calcTableMaxHeight)
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增行政区划')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  code: '',
  name: '',
  level: 1,
  parentCode: '',
  pinyin: '',
  abbreviation: '',
  sort: 0,
  status: 1
})
const parentCascader = ref([])

const formRules = {
  code: [{ required: true, message: '请输入行政区划代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择层级', trigger: 'change' }]
}

const cascaderOptions = ref([])

onMounted(async () => {
  await fetchRootData()
  await nextTick()
  calcTableMaxHeight()
  setupResizeObserver()
})

onBeforeUnmount(() => {
  cleanupResizeObserver()
})

// 分页查询顶层数据（搜索模式）或有筛选条件时
async function fetchRootData() {
  loading.value = true
  try {
    const hasFilter = queryParams.keyword || queryParams.level || queryParams.parentCode
    if (hasFilter) {
      // 有筛选条件时使用分页接口
      const params = {
        page: queryParams.page,
        size: queryParams.size,
        keyword: queryParams.keyword || undefined,
        level: queryParams.level || undefined,
        parentCode: queryParams.parentCode || undefined
      }
      const res = await getRegionPageApi(params)
      tableData.value = (res.data.records || []).map((item) => ({
        ...item,
        hasChildren: item.level < 3,
        children: undefined
      }))
      total.value = res.data.total || 0
      loadedNodeIds.value = new Set()
    } else {
      // 无筛选条件时，只加载顶层（parentCode为空）的数据，支持分页
      const res = await getRegionChildrenApi()
      const allRoot = res.data || []
      // 分页处理
      const start = (queryParams.page - 1) * queryParams.size
      const end = start + queryParams.size
      total.value = allRoot.length
      tableData.value = allRoot.slice(start, end).map((item) => ({
        ...item,
        hasChildren: item.level < 3,
        children: undefined
      }))
      loadedNodeIds.value = new Set()
    }
  } finally {
    loading.value = false
  }
}

// 懒加载子节点
async function loadChildren(row, treeNode, resolve) {
  // 已加载过直接返回缓存
  if (loadedNodeIds.value.has(row.id)) {
    resolve(row.children || [])
    return
  }
  try {
    const res = await getRegionChildrenApi(row.code)
    const children = (res.data || []).map((item) => ({
      ...item,
      hasChildren: item.level < 3,
      children: undefined
    }))
    row.children = children
    loadedNodeIds.value.add(row.id)
    resolve(children)
  } catch {
    resolve([])
  }
}

// 刷新表格中某条数据及其子树
function refreshRow(row) {
  // 如果该行已展开，重新加载其子节点
  if (loadedNodeIds.value.has(row.id)) {
    loadedNodeIds.value.delete(row.id)
    // 触发重新加载：先收起再展开
    tableRef.value?.toggleRowExpansion(row, false)
    nextTick(() => {
      tableRef.value?.toggleRowExpansion(row, true)
    })
  }
  // 刷新当前行的父级数据（如果有的话，让父级重新加载子树）
  // 简单处理：如果当前行不是顶层，刷新整个顶层数据
  if (row.parentCode) {
    // 找到表格中是否有父节点，刷新其子树
    findAndRefreshParent(tableData.value, row.parentCode)
  }
}

function findAndRefreshParent(nodes, parentCode) {
  for (const node of nodes) {
    if (node.code === parentCode) {
      if (loadedNodeIds.value.has(node.id)) {
        loadedNodeIds.value.delete(node.id)
        tableRef.value?.toggleRowExpansion(node, false)
        nextTick(() => {
          tableRef.value?.toggleRowExpansion(node, true)
        })
      }
      return true
    }
    if (node.children && node.children.length) {
      if (findAndRefreshParent(node.children, parentCode)) return true
    }
  }
  return false
}

// 搜索
function handleSearch() {
  queryParams.page = 1
  fetchRootData()
}

// 重置
function handleReset() {
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.level = ''
  queryParams.parentCode = ''
  fetchRootData()
}

// 级联选择器懒加载
async function cascaderLazyLoad(node, resolve) {
  const { level, data } = node
  if (level === 0) {
    try {
      const res = await getRegionChildrenApi()
      resolve((res.data || []).map((item) => ({ ...item, leaf: item.level >= 3 })))
    } catch {
      resolve([])
    }
  } else {
    try {
      const res = await getRegionChildrenApi(data.code)
      resolve((res.data || []).map((item) => ({ ...item, leaf: item.level >= 3 })))
    } catch {
      resolve([])
    }
  }
}

// 层级标签
function levelLabel(level) {
  const map = {
    1: t('tool.region.levelOptions.province'),
    2: t('tool.region.levelOptions.city'),
    3: t('tool.region.levelOptions.district')
  }
  return map[level] || ''
}

function levelTagType(level) {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[level] || 'info'
}

// 层级切换
function handleLevelChange(val) {
  if (val === 1) {
    form.parentCode = ''
    parentCascader.value = []
  }
}

// 上级选择变化
function handleParentChange(values) {
  form.parentCode = values && values.length > 0 ? values[values.length - 1] : ''
}

// 新增
function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增行政区划'
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    level: 1,
    parentCode: '',
    pinyin: '',
    abbreviation: '',
    sort: 0,
    status: 1
  })
  parentCascader.value = []
  dialogVisible.value = true
}

// 编辑
async function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑行政区划'
  try {
    const res = await getRegionByIdApi(row.id)
    const data = res.data
    Object.assign(form, {
      id: data.id,
      code: data.code,
      name: data.name,
      level: data.level,
      parentCode: data.parentCode || '',
      pinyin: data.pinyin || '',
      abbreviation: data.abbreviation || '',
      sort: data.sort || 0,
      status: data.status
    })
    parentCascader.value = data.parentCode ? [data.parentCode] : []
    dialogVisible.value = true
  } catch {
    Object.assign(form, {
      id: row.id,
      code: row.code,
      name: row.name,
      level: row.level,
      parentCode: row.parentCode || '',
      pinyin: row.pinyin || '',
      abbreviation: row.abbreviation || '',
      sort: row.sort || 0,
      status: row.status
    })
    parentCascader.value = row.parentCode ? [row.parentCode] : []
    dialogVisible.value = true
  }
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.tip'), { type: 'warning' })
    await deleteRegionApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    // 如果已加载子节点，清除缓存
    if (loadedNodeIds.value.has(row.id)) {
      loadedNodeIds.value.delete(row.id)
    }
    fetchRootData()
  } catch {
    /* 取消 */
  }
}

// 提交
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateRegionApi({ ...form })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addRegionApi({ ...form })
      ElMessage.success(t('common.addSuccess'))
    }
    dialogVisible.value = false
    // 增改后重新加载根数据，清除已加载缓存
    loadedNodeIds.value = new Set()
    fetchRootData()
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.region-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-bar {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.region-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.region-table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.region-pagination {
  margin-top: 12px;
  justify-content: flex-end;
  flex-shrink: 0;
}
</style>

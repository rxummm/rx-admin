<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('system.role.roleName') + '/' + $t('system.role.roleCode')" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <ExportButton :data="sortedTableData" :columns="exportColumns" title="角色管理" />
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('sys:role:add')">
        <el-icon><Plus /></el-icon> {{ $t('common.add') + $t('system.role.title') }}
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('sys:role:delete')" :disabled="selectedIds.length === 0">
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

    <div class="table-container">
      <!-- 骨架屏加载状态（仅在首次加载且无数据时显示） -->
      <SkeletonLoader v-if="loading && !tableData.length" type="table" :rows="10" :columns="visibleColumns.length + 2" />
      
      <!-- 正常表格（有数据后显示，刷新时保留旧数据并显示 loading 遮罩） -->
      <el-table v-else :data="sortedTableData" border stripe v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('id')" prop="id" label="ID" width="70" sortable />
        <el-table-column v-if="visibleColumns.includes('roleName')" prop="roleName" :label="$t('system.role.roleName')" width="150" sortable />
        <el-table-column v-if="visibleColumns.includes('roleCode')" prop="roleCode" :label="$t('system.role.roleCode')" width="150" sortable />
        <el-table-column v-if="visibleColumns.includes('description')" prop="description" :label="$t('system.role.description')" min-width="200" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('sort')" prop="sort" :label="$t('common.sort')" width="80" sortable />
        <el-table-column v-if="visibleColumns.includes('status')" prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('system.role.statusOptions.enable') : $t('system.role.statusOptions.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('createTime')" prop="createTime" :label="$t('common.createTime')" width="170" sortable />
        <el-table-column :label="$t('common.operation')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('sys:role:edit')">{{ $t('common.edit') }}</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('sys:role:delete')">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" :close-on-click-modal="false" draggable>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item :label="$t('system.role.roleName')" prop="roleName">
          <el-input v-model="form.roleName" :disabled="isEdit" :placeholder="$t('common.input') + $t('system.role.roleName')" />
        </el-form-item>
        <el-form-item :label="$t('system.role.roleCode')" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" :placeholder="$t('common.input') + $t('system.role.roleCode')" />
        </el-form-item>
        <el-form-item :label="$t('system.role.description')">
          <el-input v-model="form.description" :placeholder="$t('common.input') + $t('system.role.description')" />
        </el-form-item>
        <div style="display: flex; gap: 16px;">
          <el-form-item :label="$t('common.sort')" style="flex: 0 0 auto; margin-bottom: 22px;">
            <el-input-number v-model="form.sort" :min="0" />
          </el-form-item>
          <el-form-item :label="$t('common.status')" style="flex: 1; margin-bottom: 22px;">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        <el-form-item :label="$t('system.role.dataScope')">
          <el-radio-group v-model="form.dataScope">
            <el-radio v-for="opt in dataScopeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.dataScope === 5" :label="$t('system.role.customDept')">
          <el-tree-select
            v-model="dataScopeDeptIds"
            :data="deptTree"
            multiple
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            style="width:100%"
            :placeholder="$t('system.role.customDeptPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('system.role.menuPerms')">
          <div class="two-col-tree">
            <el-tree
              ref="leftTreeRef"
              :data="leftMenuTree"
              show-checkbox
              node-key="id"
              :props="{ label: 'menuName', children: 'children' }"
              :default-expand-all="false"
            />
            <el-tree
              ref="rightTreeRef"
              :data="rightMenuTree"
              show-checkbox
              node-key="id"
              :props="{ label: 'menuName', children: 'children' }"
              :default-expand-all="false"
            />
          </div>
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
defineOptions({ name: 'SystemRole' })
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getRoleListApi, addRoleApi, updateRoleApi, deleteRoleApi } from '@/api/role'
import { getMenuTreeApi } from '@/api/menu'
import { getDeptTreeApi } from '@/api/dept'
import ExportButton from '@/components/ExportButton/index.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'

const { t } = useI18n()
const userStore = useUserStore()
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('')
const menuTree = ref([])
const leftTreeRef = ref(null)
const rightTreeRef = ref(null)

// 将菜单树拆成左右两列
const leftMenuTree = computed(() => {
  const data = menuTree.value || []
  const mid = Math.ceil(data.length / 2)
  return data.slice(0, mid)
})
const rightMenuTree = computed(() => {
  const data = menuTree.value || []
  const mid = Math.ceil(data.length / 2)
  return data.slice(mid)
})

// 列显示配置
const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'roleName', label: t('system.role.roleName') },
  { key: 'roleCode', label: t('system.role.roleCode') },
  { key: 'description', label: t('system.role.description') },
  { key: 'sort', label: t('common.sort') },
  { key: 'status', label: t('common.status') },
  { key: 'createTime', label: t('common.createTime') }
]
const visibleColumns = ref(columnOptions.map(c => c.key))

// 导出列定义
const exportColumns = [
  { field: 'id', label: 'ID' },
  { field: 'roleName', label: '角色名称' },
  { field: 'roleCode', label: '角色编码' },
  { field: 'description', label: '描述' },
  { field: 'sort', label: '排序' },
  { field: 'status', label: '状态' },
  { field: 'createTime', label: '创建时间' }
]

function toggleColumn(key) {
  const idx = visibleColumns.value.indexOf(key)
  if (idx > -1) {
    visibleColumns.value.splice(idx, 1)
  } else {
    visibleColumns.value.push(key)
  }
}

// 前端搜索 + 排序
const filteredData = computed(() => {
  let data = tableData.value
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    data = data.filter(item =>
      (item.roleName && item.roleName.toLowerCase().includes(kw)) ||
      (item.roleCode && item.roleCode.toLowerCase().includes(kw))
    )
  }
  return data
})

const sortedTableData = computed(() => {
  const data = [...filteredData.value]
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

function resetSearch() {
  keyword.value = ''
}

const dialogVisible = ref(false)
const dialogTitle = ref(t('common.add') + t('system.role.title'))
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  sort: 0,
  status: 1,
  dataScope: 1,
  dataDeptIds: ''
})
const deptTree = ref([])
const dataScopeDeptIds = ref([]) // el-tree-select multiple 绑定数组

const dataScopeOptions = computed(() => [
  { value: 1, label: t('system.role.dataScopeOptions.all') },
  { value: 2, label: t('system.role.dataScopeOptions.dept') },
  { value: 3, label: t('system.role.dataScopeOptions.deptTree') },
  { value: 4, label: t('system.role.dataScopeOptions.self') },
  { value: 5, label: t('system.role.dataScopeOptions.custom') }
])

// 切换数据范围时清空自定义部门选择
watch(() => form.dataScope, (val) => {
  if (val !== 5) {
    dataScopeDeptIds.value = []
    form.dataDeptIds = ''
  }
})

const formRules = {
  roleName: [{ required: true, message: t('common.input') + t('system.role.roleName'), trigger: 'blur' }],
  roleCode: [{ required: true, message: t('common.input') + t('system.role.roleCode'), trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleListApi()
    // 确保骨架屏至少显示 300ms，避免闪烁
    await new Promise(resolve => setTimeout(resolve, 300))
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function fetchMenuTree() {
  const res = await getMenuTreeApi()
  menuTree.value = res.data || []
}

async function fetchDeptTree() {
  const res = await getDeptTreeApi()
  deptTree.value = res.data || []
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = t('common.add') + t('system.role.title')
  resetForm()
  fetchMenuTree()
  fetchDeptTree()
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = t('common.edit') + t('system.role.title')
  Object.assign(form, {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    description: row.description,
    sort: row.sort,
    status: row.status,
    dataScope: row.dataScope || 1,
    dataDeptIds: row.dataDeptIds || ''
  })
  // 自定义部门回显
  if (form.dataScope === 5 && form.dataDeptIds) {
    dataScopeDeptIds.value = form.dataDeptIds.split(',').map(Number).filter(n => n)
  } else {
    dataScopeDeptIds.value = []
  }
  // 先打开弹窗，让 el-tree 渲染出来
  dialogVisible.value = true
  await fetchMenuTree()
  await fetchDeptTree()
  await nextTick()
  const menuIds = row.menuIds || []
  if (menuIds.length > 0) {
    // 对左右两棵树分别设置勾选
    for (const treeRef of [leftTreeRef.value, rightTreeRef.value]) {
      if (!treeRef) continue
      const leafIds = menuIds.filter(id => {
        const node = treeRef.getNode(id)
        return node && (!node.childNodes || node.childNodes.length === 0)
      })
      if (leafIds.length > 0) {
        treeRef.setCheckedKeys(leafIds)
      }
    }
  }
}


async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.confirmDeleteItem', { name: row.roleName }), t('common.tip'), { type: 'warning' })
    await deleteRoleApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(t('common.confirmBatchDelete', { count: selectedIds.value.length }), t('common.batchDelete'), { type: 'warning' })
    await Promise.all(selectedIds.value.map(id => deleteRoleApi(id)))
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
    // 合并左右两棵树的选中节点
    const allCheckedKeys = [
      ...(leftTreeRef.value?.getCheckedKeys() || []),
      ...(rightTreeRef.value?.getCheckedKeys() || [])
    ]
    const leafKeys = []
    for (const id of allCheckedKeys) {
      let node
      node = leftTreeRef.value?.getNode(id)
      if (!node) node = rightTreeRef.value?.getNode(id)
      if (node && (!node.childNodes || node.childNodes.length === 0)) {
        leafKeys.push(id)
      }
    }
    const halfKeys = [
      ...(leftTreeRef.value?.getHalfCheckedKeys() || []),
      ...(rightTreeRef.value?.getHalfCheckedKeys() || [])
    ]
    const menuIds = [...new Set([...leafKeys, ...halfKeys])]

    if (isEdit.value) {
      form.dataDeptIds = dataScopeDeptIds.value.join(',')
      await updateRoleApi({ ...form }, menuIds)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      form.dataDeptIds = dataScopeDeptIds.value.join(',')
      await addRoleApi({ ...form }, menuIds)
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
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.sort = 0
  form.status = 1
  form.dataScope = 1
  form.dataDeptIds = ''
  dataScopeDeptIds.value = []
}
</script>

<style scoped>
/* 双排展示菜单权限树：左右两列独立，互不干扰 */
.two-col-tree {
  display: flex;
  gap: 32px;
}
.two-col-tree > .el-tree {
  flex: 1;
  min-width: 340px;
}
</style>

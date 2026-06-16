<template>
  <div class="page-container">
    <div class="search-bar">
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd({ parentId: 0 })" v-if="userStore.hasPerm('sys:dept:add')">
        <el-icon><Plus /></el-icon> {{ $t('common.add') + $t('system.dept.title') }}
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
      <el-table :data="deptTree" row-key="id" border stripe v-loading="loading" default-expand-all>
        <el-table-column v-if="visibleColumns.includes('deptName')" prop="deptName" :label="$t('system.dept.deptName')" min-width="200" />
        <el-table-column v-if="visibleColumns.includes('leader')" prop="leader" :label="$t('system.dept.leader')" width="120" />
        <el-table-column v-if="visibleColumns.includes('phone')" prop="phone" :label="$t('system.dept.phone')" width="140" />
        <el-table-column v-if="visibleColumns.includes('email')" prop="email" :label="$t('system.dept.email')" width="180" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('sort')" prop="sort" :label="$t('common.sort')" width="70" />
        <el-table-column v-if="visibleColumns.includes('status')" prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('system.dept.statusOptions.enable') : $t('system.dept.statusOptions.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('createTime')" prop="createTime" :label="$t('common.createTime')" width="170" />
        <el-table-column :label="$t('common.operation')" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdd(row)" v-if="userStore.hasPerm('sys:dept:add')">{{ $t('system.dept.addChild') }}</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('sys:dept:edit')">{{ $t('common.edit') }}</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('sys:dept:delete')">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item :label="$t('system.dept.parentDept')">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            :placeholder="$t('system.dept.selectParentPlaceholder')"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('system.dept.deptName')" prop="deptName">
          <el-input v-model="form.deptName" :placeholder="$t('common.input') + $t('system.dept.deptName')" />
        </el-form-item>
        <el-form-item :label="$t('system.dept.leader')">
          <el-input v-model="form.leader" :placeholder="$t('common.input') + $t('system.dept.leader')" />
        </el-form-item>
        <el-form-item :label="$t('system.dept.phone')">
          <el-input v-model="form.phone" :placeholder="$t('common.input') + $t('system.dept.phone')" />
        </el-form-item>
        <el-form-item :label="$t('system.dept.email')">
          <el-input v-model="form.email" :placeholder="$t('common.input') + $t('system.dept.email')" />
        </el-form-item>
        <el-form-item :label="$t('common.sort')">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
defineOptions({ name: 'SystemDept' })
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getDeptTreeApi, addDeptApi, updateDeptApi, deleteDeptApi } from '@/api/dept'

const { t } = useI18n()
const userStore = useUserStore()

const deptTree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('common.add') + t('system.dept.title'))
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 列显示配置
const columnOptions = [
  { key: 'deptName', label: t('system.dept.deptName') },
  { key: 'leader', label: t('system.dept.leader') },
  { key: 'phone', label: t('system.dept.phone') },
  { key: 'email', label: t('system.dept.email') },
  { key: 'sort', label: t('common.sort') },
  { key: 'status', label: t('common.status') },
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

const form = reactive({
  id: null,
  parentId: 0,
  deptName: '',
  leader: '',
  phone: '',
  email: '',
  sort: 0,
  status: 1
})

const formRules = {
  deptName: [{ required: true, message: t('common.input') + t('system.dept.deptName'), trigger: 'blur' }]
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getDeptTreeApi()
    deptTree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd(row) {
  isEdit.value = false
  dialogTitle.value = t('common.add') + t('system.dept.title')
  resetForm()
  form.parentId = row.id || 0
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = t('common.edit') + t('system.dept.title')
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId,
    deptName: row.deptName,
    leader: row.leader || '',
    phone: row.phone || '',
    email: row.email || '',
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.confirmDeleteItem', { name: row.deptName }), t('common.tip'), { type: 'warning' })
    await deleteDeptApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
  } catch {}
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await updateDeptApi(data)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addDeptApi(data)
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
  form.parentId = 0
  form.deptName = ''
  form.leader = ''
  form.phone = ''
  form.email = ''
  form.sort = 0
  form.status = 1
}
</script>

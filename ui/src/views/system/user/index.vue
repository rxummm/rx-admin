<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        :placeholder="
          $t('common.input') +
          ' ' +
          $t('system.user.username') +
          '/' +
          $t('system.user.nickname') +
          '/' +
          $t('system.user.phone')
        "
        clearable
        style="width: 240px"
        @keyup.enter="fetchData"
      />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <ExportButton :data="sortedTableData" :columns="exportColumns" :title="$t('system.user.title')" />
      <div style="flex: 1" />
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('sys:user:add')">
        <el-icon><Plus /></el-icon> {{ $t('common.add') + $t('system.user.title') }}
      </el-button>
      <el-button
        type="danger"
        @click="handleBatchDelete"
        v-if="userStore.hasPerm('sys:user:delete')"
        :disabled="selectedIds.length === 0"
      >
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
      <el-badge v-if="userStore.hasRole('admin')" :value="pendingCount" :hidden="pendingCount === 0" :max="99">
        <el-button @click="openApprovalDialog">
          <el-icon><Bell /></el-icon> {{ $t('permission.request.approvalTitle') }}
        </el-button>
      </el-badge>
    </div>

    <div class="table-container">
      <!-- 骨架屏加载状态（仅在首次加载且无数据时显示） -->
      <SkeletonLoader
        v-if="loading && !tableData.length"
        type="table"
        :rows="10"
        :columns="visibleColumns.length + 2"
      />

      <!-- 正常表格（有数据后显示，刷新时保留旧数据并显示 loading 遮罩） -->
      <el-table
        v-else
        :data="sortedTableData"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('id')" prop="id" label="ID" width="70" sortable />
        <el-table-column
          v-if="visibleColumns.includes('username')"
          prop="username"
          :label="$t('system.user.username')"
          width="120"
          sortable
        />
        <el-table-column
          v-if="visibleColumns.includes('nickname')"
          prop="nickname"
          :label="$t('system.user.nickname')"
          width="120"
          sortable
        />
        <el-table-column
          v-if="visibleColumns.includes('email')"
          prop="email"
          :label="$t('system.user.email')"
          min-width="180"
          show-overflow-tooltip
          sortable
        />
        <el-table-column
          v-if="visibleColumns.includes('phone')"
          prop="phone"
          :label="$t('system.user.phone')"
          width="130"
        />
        <el-table-column
          v-if="visibleColumns.includes('gender')"
          prop="gender"
          :label="$t('system.user.gender')"
          width="70"
        >
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'primary' : row.gender === 2 ? 'danger' : 'info'" size="small">
              {{
                row.gender === 1
                  ? $t('system.user.genderOptions.male')
                  : row.gender === 2
                    ? $t('system.user.genderOptions.female')
                    : $t('system.user.genderOptions.unknown')
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('status')" prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('system.user.statusOptions.enable') : $t('system.user.statusOptions.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('createTime')"
          prop="createTime"
          :label="$t('common.createTime')"
          width="170"
          sortable
        />
        <el-table-column :label="$t('common.operation')" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEdit(row)"
              v-if="userStore.hasPerm('sys:user:edit')"
              >{{ $t('common.edit') }}</el-button
            >
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="userStore.hasPerm('sys:user:delete')"
              >{{ $t('common.delete') }}</el-button
            >
            <el-button
              link
              type="warning"
              size="small"
              @click="openPermManage(row)"
              v-if="userStore.hasRole('admin')"
              >{{ $t('permission.manage.title') }}</el-button
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item :label="$t('system.user.username')" prop="username">
          <el-input
            v-model="form.username"
            :disabled="isEdit"
            :placeholder="$t('common.input') + $t('system.user.username')"
          />
        </el-form-item>
        <el-form-item :label="$t('system.user.password')" :prop="isEdit ? '' : 'password'">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="isEdit ? $t('common.leaveBlank') : $t('common.input') + $t('system.user.password')"
          />
          <PasswordStrength :password="form.password" />
        </el-form-item>
        <el-form-item :label="$t('system.user.nickname')" prop="nickname">
          <el-input v-model="form.nickname" :placeholder="$t('common.input') + $t('system.user.nickname')" />
        </el-form-item>
        <el-form-item :label="$t('system.user.email')" prop="email">
          <el-input v-model="form.email" :placeholder="$t('common.input') + $t('system.user.email')" />
        </el-form-item>
        <el-form-item :label="$t('system.user.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="$t('common.input') + $t('system.user.phone')" />
        </el-form-item>
        <el-form-item :label="$t('system.user.gender')">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">{{ $t('system.user.genderOptions.unknown') }}</el-radio>
            <el-radio :value="1">{{ $t('system.user.genderOptions.male') }}</el-radio>
            <el-radio :value="2">{{ $t('system.user.genderOptions.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="$t('system.user.role')">
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox v-for="role in roleList" :key="role.id" :value="role.id" :label="role.roleName" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 权限审批弹窗 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="$t('permission.request.approvalTitle')"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-table :data="pendingRequests" border stripe v-loading="approvalLoading" style="width: 100%" max-height="400">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" :label="$t('system.user.username')" width="120" />
        <el-table-column
          prop="menuNames"
          :label="$t('permission.request.appliedMenus')"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="160" />
        <el-table-column :label="$t('common.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleApprove(row)">{{
              $t('permission.request.approve')
            }}</el-button>
            <el-button link type="danger" size="small" @click="handleReject(row)">{{
              $t('permission.request.reject')
            }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="text-align: center; padding: 12px 0 0" v-if="pendingRequests.length === 0 && !approvalLoading">
        {{ $t('permission.request.noPending') }}
      </div>
    </el-dialog>

    <!-- 权限管理弹窗（admin 主动管理用户权限） -->
    <el-dialog
      v-model="permManageVisible"
      :title="$t('permission.manage.title') + ' - ' + permManageUser.username"
      width="750px"
      :close-on-click-modal="false"
      @opened="initPermManage"
      @closed="resetPermManage"
    >
      <el-tabs v-model="permManageTab">
        <!-- 标签1：用户已有权限 -->
        <el-tab-pane :label="$t('permission.manage.currentPerms')" name="current">
          <div style="margin-bottom: 12px">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>{{ $t('permission.manage.currentPermsHint') }}</template>
            </el-alert>
          </div>
          <el-scrollbar max-height="400px">
            <el-tree
              ref="currentPermTreeRef"
              :data="currentPermTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :check-strictly="false"
              :props="{ label: 'menuName', children: 'children' }"
              @check="handleCurrentPermCheck"
            >
              <template #default="{ node: _node, data }">
                <span style="display: flex; align-items: center; gap: 6px">
                  <el-tag
                    :type="data.menuType === 1 ? 'info' : data.menuType === 2 ? 'success' : 'warning'"
                    size="small"
                  >
                    {{
                      data.menuType === 1
                        ? $t('system.menu.typeOptions.dir')
                        : data.menuType === 2
                          ? $t('system.menu.typeOptions.menu')
                          : $t('system.menu.typeOptions.button')
                    }}
                  </el-tag>
                  <span :style="pendingRemoveIds.has(data.id) ? 'text-decoration:line-through;color:#f56c6c' : ''">{{
                    data.menuName
                  }}</span>
                  <span v-if="data.perms" style="color: #909399; font-size: 12px">({{ data.perms }})</span>
                </span>
              </template>
            </el-tree>
          </el-scrollbar>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px">
            <span style="color: #909399; font-size: 13px">
              {{ $t('permission.manage.pendingRemove', { count: pendingRemoveIds.size }) }}
            </span>
            <el-button
              type="danger"
              :loading="permManageLoading"
              :disabled="pendingRemoveIds.size === 0"
              @click="handleRemovePerms"
            >
              {{ $t('permission.manage.removeSelected') }}
            </el-button>
          </div>
        </el-tab-pane>

        <!-- 标签2：可分配权限 -->
        <el-tab-pane :label="$t('permission.manage.assignPerms')" name="assign">
          <div style="margin-bottom: 12px">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>{{ $t('permission.manage.assignPermsHint') }}</template>
            </el-alert>
          </div>
          <el-scrollbar max-height="400px">
            <el-tree
              ref="assignPermTreeRef"
              :data="assignPermTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :check-strictly="false"
              :props="{ label: 'menuName', children: 'children' }"
            >
              <template #default="{ node: _node, data }">
                <span style="display: flex; align-items: center; gap: 6px">
                  <el-tag
                    :type="data.menuType === 1 ? 'info' : data.menuType === 2 ? 'success' : 'warning'"
                    size="small"
                  >
                    {{
                      data.menuType === 1
                        ? $t('system.menu.typeOptions.dir')
                        : data.menuType === 2
                          ? $t('system.menu.typeOptions.menu')
                          : $t('system.menu.typeOptions.button')
                    }}
                  </el-tag>
                  <span>{{ data.menuName }}</span>
                  <span v-if="data.perms" style="color: #909399; font-size: 12px">({{ data.perms }})</span>
                </span>
              </template>
            </el-tree>
          </el-scrollbar>
          <div style="text-align: right; margin-top: 16px">
            <el-button
              type="primary"
              :loading="permManageLoading"
              :disabled="getCheckedAssignKeys().length === 0"
              @click="handleAddPerms"
            >
              {{ $t('permission.manage.assignSelected') }}
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemUser' })
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useNoticeBroadcast } from '@/composables/useNoticeBroadcast'
import { getUserPageApi, addUserApi, updateUserApi, deleteUserApi } from '@/api/user'
import { getRoleListApi } from '@/api/role'
import { getMenuTreeApi } from '@/api/menu'
import {
  getPendingRequestsApi,
  approveRequestApi,
  rejectRequestApi,
  getUserMenuIdsApi,
  getManageableMenuTreeApi,
  addUserMenusApi,
  removeUserMenusApi
} from '@/api/permission'
import PasswordStrength from '@/components/PasswordStrength.vue'
import ExportButton from '@/components/ExportButton/index.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'

const { t } = useI18n()
const userStore = useUserStore()

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('')

// 列显示配置
const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: t('system.user.username') },
  { key: 'nickname', label: t('system.user.nickname') },
  { key: 'email', label: t('system.user.email') },
  { key: 'phone', label: t('system.user.phone') },
  { key: 'gender', label: t('system.user.gender') },
  { key: 'status', label: t('common.status') },
  { key: 'createTime', label: t('common.createTime') }
]
const visibleColumns = ref(columnOptions.map((c) => c.key))

// 导出列定义
const exportColumns = [
  { field: 'id', label: 'ID' },
  { field: 'username', label: t('system.user.username') },
  { field: 'nickname', label: t('system.user.nickname') },
  { field: 'email', label: t('system.user.email') },
  { field: 'phone', label: t('system.user.phone') },
  { field: 'gender', label: t('system.user.gender') },
  { field: 'status', label: t('common.status') },
  { field: 'createTime', label: t('common.createTime') }
]

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
  selectedIds.value = rows.map((r) => r.id)
}

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref(t('common.add') + t('system.user.title'))
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const selectedRoleIds = ref([])
const roleList = ref([])

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  status: 1
})

const formRules = {
  username: [{ required: true, message: t('common.input') + t('system.user.username'), trigger: 'blur' }],
  password: [
    { required: true, message: t('common.input') + t('system.user.password'), trigger: 'blur', min: 6 },
    { pattern: /^[A-Za-z]/, message: '需以字母开头', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: t('common.input') + t('system.user.nickname'), trigger: 'blur' }]
}

// 权限审批相关
const approvalDialogVisible = ref(false)
const approvalLoading = ref(false)
const pendingRequests = ref([])
const pendingCount = ref(0)
const { triggerRefresh } = useNoticeBroadcast()

async function fetchPendingRequests() {
  approvalLoading.value = true
  try {
    const res = await getPendingRequestsApi({ page: 1, size: 100 })
    const records = res.data?.records || []
    records.forEach((r) => {
      try {
        const names = JSON.parse(r.menuNames)
        r.menuNames = Array.isArray(names) ? names.join('、') : r.menuNames
      } catch {}
    })
    pendingRequests.value = records
    pendingCount.value = res.data?.total || records.length
  } catch {
    // 静默
  } finally {
    approvalLoading.value = false
  }
}

function openApprovalDialog() {
  approvalDialogVisible.value = true
  fetchPendingRequests()
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(t('permission.request.approveConfirm', { name: row.username }), t('common.tip'), {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await approveRequestApi(row.id)
    ElMessage.success(t('permission.request.approveSuccess'))
    fetchPendingRequests()
    triggerRefresh() // 立即通知铃铛弹窗刷新待办
  } catch {
    ElMessage.error(t('common.operateFail'))
  }
}

async function handleReject(row) {
  try {
    const { value: remark } = await ElMessageBox.prompt(
      t('permission.request.rejectRemark'),
      t('permission.request.reject'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        inputPlaceholder: t('common.input') + t('common.remark')
      }
    ).catch(() => ({ value: null }))
    if (remark === null) return
    await rejectRequestApi(row.id, { remark })
    ElMessage.success(t('permission.request.rejectSuccess'))
    fetchPendingRequests()
    triggerRefresh() // 立即通知铃铛弹窗刷新待办
  } catch {
    ElMessage.error(t('common.operateFail'))
  }
}

// ====== 权限管理弹窗 ======
const permManageVisible = ref(false)
const permManageUser = ref({})
const permManageTab = ref('current')
const permManageLoading = ref(false)
const currentPermTreeRef = ref(null)
const assignPermTreeRef = ref(null)
const currentPermTree = ref([])
const assignPermTree = ref([])
// 方案B：记录原始已有权限ID集合 vs 待移除（取消勾选的）ID集合
const originalPermIds = ref(new Set())
const pendingRemoveIds = ref(new Set())

function openPermManage(row) {
  permManageUser.value = row
  permManageVisible.value = true
}

function resetPermManage() {
  originalPermIds.value = new Set()
  pendingRemoveIds.value = new Set()
}

async function initPermManage() {
  return refreshPermManageData('current')
}

async function refreshPermManageData(keepTab) {
  if (keepTab) {
    permManageTab.value = keepTab
  }
  permManageLoading.value = true
  try {
    const userId = permManageUser.value.id

    // 加载当前用户已有权限的菜单树
    const [menuIdsRes, fullTreeRes] = await Promise.all([getUserMenuIdsApi(userId), getMenuTreeApi()])
    const menuIds = new Set(menuIdsRes.data || [])
    const fullTree = fullTreeRes.data || []

    // 过滤掉管理类顶级菜单及其子孙（1=系统管理, 24=系统工具, 30=内容管理, 36=系统监控, 300=权限申请）
    const excludedTops = new Set([1, 24, 30, 36, 300])
    const excludedIds = new Set()
    function collectExcluded(nodes) {
      for (const n of nodes) {
        excludedIds.add(n.id)
        if (n.children) collectExcluded(n.children)
      }
    }
    for (const n of fullTree) {
      if (excludedTops.has(n.id)) collectExcluded([n])
    }

    // 构建"已有权限"树：只包含用户拥有的菜单
    function filterOwned(nodes) {
      const result = []
      for (const n of nodes) {
        if (excludedIds.has(n.id)) continue
        const ownedChildren = n.children ? filterOwned(n.children) : []
        if (menuIds.has(n.id) || ownedChildren.length > 0) {
          result.push({ ...n, children: ownedChildren.length > 0 ? ownedChildren : n.children ? [] : undefined })
        }
      }
      return result
    }
    currentPermTree.value = filterOwned(fullTree)

    // 记录原始权限ID集合
    const userPermIds = [...menuIds].filter((id) => !excludedIds.has(id))
    originalPermIds.value = new Set(userPermIds)
    pendingRemoveIds.value = new Set()

    // 默认全部勾选（check-strictly=false 时自动联动父子）
    await nextTick()
    if (currentPermTreeRef.value) {
      currentPermTreeRef.value.setCheckedKeys(userPermIds)
    }

    // 加载可分配权限树
    const assignTreeRes = await getManageableMenuTreeApi(userId)
    assignPermTree.value = assignTreeRes.data || []
  } finally {
    permManageLoading.value = false
  }
}

function _getCheckedCurrentKeys() {
  return currentPermTreeRef.value?.getCheckedKeys() || []
}

function getCheckedAssignKeys() {
  return assignPermTreeRef.value?.getCheckedKeys() || []
}

// 方案B核心：监听已有权限树的check事件，计算取消勾选的差异
function handleCurrentPermCheck(node, checkedInfo) {
  // checkedInfo.checkedKeys = 当前所有被勾选的节点ID
  // originalPermIds = 弹窗打开时用户已有的权限ID
  // pendingRemoveIds = 原始有但当前未勾选的 = 待移除
  const currentChecked = new Set(checkedInfo.checkedKeys)
  const toRemove = new Set()
  for (const id of originalPermIds.value) {
    if (!currentChecked.has(id)) {
      toRemove.add(id)
    }
  }
  pendingRemoveIds.value = toRemove
}

// 已有权限Tab：移除取消勾选的权限（差异删除）
async function handleRemovePerms() {
  const removeIds = [...pendingRemoveIds.value]
  if (removeIds.length === 0) return
  try {
    await ElMessageBox.confirm(t('permission.manage.removeConfirm', { count: removeIds.length }), t('common.tip'), {
      type: 'warning'
    })
  } catch {
    return
  }

  permManageLoading.value = true
  try {
    await removeUserMenusApi(permManageUser.value.id, removeIds)
    ElMessage.success(t('permission.manage.removeSuccess'))
    // 刷新数据，保持当前 tab 不变
    await refreshPermManageData(permManageTab.value)
    // 如果修改的是当前用户，刷新路由和权限
    if (permManageUser.value.id === userStore.userInfo?.id) {
      await userStore.refreshRouters()
    }
  } catch {
    ElMessage.error(t('common.operateFail'))
  } finally {
    permManageLoading.value = false
  }
}

async function handleAddPerms() {
  const keys = getCheckedAssignKeys()
  if (keys.length === 0) return
  try {
    await ElMessageBox.confirm(t('permission.manage.addConfirm'), t('common.tip'), { type: 'warning' })
  } catch {
    return
  }

  permManageLoading.value = true
  try {
    await addUserMenusApi(permManageUser.value.id, keys)
    ElMessage.success(t('permission.manage.addSuccess'))
    // 刷新数据，保持当前 tab 不变
    await refreshPermManageData(permManageTab.value)
    // 如果修改的是当前用户，刷新路由和权限
    if (permManageUser.value.id === userStore.userInfo?.id) {
      await userStore.refreshRouters()
    }
  } catch {
    ElMessage.error(t('common.operateFail'))
  } finally {
    permManageLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchRoles()
  if (userStore.hasRole('admin')) {
    fetchPendingRequests()
  }
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPageApi({ page: page.value, size: size.value, keyword: keyword.value })
    // 确保骨架屏至少显示 300ms，避免闪烁
    await new Promise((resolve) => setTimeout(resolve, 300))
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  const res = await getRoleListApi()
  roleList.value = res.data || []
}

function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = t('common.add') + t('system.user.title')
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = t('common.edit') + t('system.user.title')
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    gender: row.gender,
    status: row.status
  })
  selectedRoleIds.value = row.roleIds || []
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.confirmDeleteItem', { name: row.username }), t('common.tip'), {
      type: 'warning'
    })
    await deleteUserApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      t('common.confirmBatchDelete', { count: selectedIds.value.length }),
      t('common.batchDelete'),
      { type: 'warning' }
    )
    await Promise.all(selectedIds.value.map((id) => deleteUserApi(id)))
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
    if (isEdit.value) {
      const roleIdParam = selectedRoleIds.value.length > 0 ? selectedRoleIds.value : null
      await updateUserApi({ ...form }, roleIdParam)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addUserApi({ ...form }, selectedRoleIds.value)
      ElMessage.success(t('common.addSuccess'))
    }
    dialogVisible.value = false
    fetchData()
  } catch {
  } finally {
    submitLoading.value = false
  }
}
function resetForm() {
  form.id = null
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.gender = 0
  form.status = 1
  selectedRoleIds.value = []
}
</script>

<style scoped>
.page-pagination {
  margin-top: 12px;
}
</style>

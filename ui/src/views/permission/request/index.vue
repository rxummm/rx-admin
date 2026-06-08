<template>
  <div class="page-container">
    <!-- 权限申请页面 -->
    <el-row :gutter="20">
      <!-- 左侧：可选菜单树 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ $t('permission.request.selectMenus') }}</span>
              <el-tag size="small" type="info">{{ selectedMenuIds.length }} / {{ allMenuIds.length }}</el-tag>
            </div>
          </template>
          <el-scrollbar max-height="520px">
            <el-tree
              ref="treeRef"
              :data="menuTree"
              show-checkbox
              node-key="id"
              :props="{ label: 'menuName', children: 'children', disabled: () => false }"
              :default-expand-all="true"
              @check="handleTreeCheck"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <el-icon v-if="data.icon" style="margin-right:6px"><component :is="data.icon" /></el-icon>
                  <span>{{ data.menuName }}</span>
                  <el-tag size="small" :type="data.menuType === 1 ? 'info' : data.menuType === 2 ? 'success' : 'warning'" style="margin-left:8px">
                    {{ data.menuType === 1 ? $t('system.menu.typeOptions.dir') : data.menuType === 2 ? $t('system.menu.typeOptions.menu') : $t('system.menu.typeOptions.button') }}
                  </el-tag>
                  <span v-if="data.perms" style="color:#909399;font-size:11px;margin-left:4px">({{ data.perms }})</span>
                </span>
              </template>
            </el-tree>
          </el-scrollbar>
        </el-card>
      </el-col>

      <!-- 右侧：已选菜单 + 我的申请 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ $t('permission.request.selectedMenus') }}</span>
              <el-button type="primary" size="small" @click="handleSubmit" :loading="submitLoading" :disabled="selectedMenuIds.length === 0">
                <el-icon><Upload /></el-icon> {{ $t('permission.request.submitApply') }}
              </el-button>
            </div>
          </template>
          <el-scrollbar max-height="200px">
            <div v-if="selectedMenuIds.length === 0" class="empty-hint">{{ $t('permission.request.noSelectHint') }}</div>
            <el-tag
              v-for="menu in selectedMenuTags"
              :key="menu.id"
              closable
              :type="menu.menuType === 1 ? 'info' : menu.menuType === 2 ? 'success' : 'warning'"
              style="margin:4px"
              @close="removeMenu(menu)"
            >
              {{ menu.menuName }}
            </el-tag>
          </el-scrollbar>
        </el-card>

        <!-- 我的申请记录 -->
        <el-card shadow="never" style="margin-top:16px">
          <template #header>
            <div class="card-header">
              <span>{{ $t('permission.request.myRequests') }}</span>
              <el-button type="warning" size="small" plain @click="openEmailApply">
                <el-icon><Message /></el-icon> {{ $t('permission.request.emailApply') }}
              </el-button>
            </div>
          </template>
          <el-scrollbar max-height="280px">
            <div v-if="myRequests.length === 0" class="empty-hint">{{ $t('permission.request.noRequest') }}</div>
            <div v-for="req in myRequests" :key="req.id" class="request-item">
              <div class="request-header">
                <span class="request-menus">{{ req.menuNames }}</span>
                <el-tag :type="req.status === 0 ? 'warning' : req.status === 1 ? 'success' : 'danger'" size="small">
                  {{ req.status === 0 ? $t('permission.request.statusPending') : req.status === 1 ? $t('permission.request.statusApproved') : $t('permission.request.statusRejected') }}
                </el-tag>
              </div>
              <div class="request-time">{{ req.createTime }}</div>
              <div v-if="req.auditRemark" class="request-remark">{{ $t('permission.request.remark') }}：{{ req.auditRemark }}</div>
              <div v-if="req.status === 0 || req.status === 1" style="font-size:12px;color:#e6a23c;margin-top:4px">
                <el-icon><WarningFilled /></el-icon>
                {{ req.status === 1 ? $t('permission.request.reloginHintApproved') : $t('permission.request.reloginHint') }}
              </div>
            </div>
          </el-scrollbar>
        </el-card>

        <!-- 邮件申请弹窗 -->
        <el-dialog v-model="emailDialogVisible" :title="$t('permission.request.emailApplyTitle')" width="480px" :close-on-click-modal="false">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
            {{ $t('permission.request.emailApplyHint') }}
          </el-alert>
          <el-form ref="emailFormRef" :model="emailForm" :rules="emailFormRules" label-width="100px">
            <el-form-item :label="$t('permission.request.emailApplyMenus')" prop="menus">
              <el-input v-model="emailForm.menus" :placeholder="$t('permission.request.emailApplyMenusPlaceholder')" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item :label="$t('permission.request.emailApplyReason')" prop="description">
              <el-input v-model="emailForm.description" :placeholder="$t('permission.request.emailApplyReasonPlaceholder')" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="emailDialogVisible = false">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" :loading="emailLoading" @click="handleEmailSubmit">{{ $t('common.confirm') }}</el-button>
          </template>
        </el-dialog>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'PermissionRequest' })
import { ref, onMounted, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getRequestableMenusApi, submitPermissionRequestApi, getMyRequestsApi, emailPermissionRequestApi } from '@/api/permission'

const { t } = useI18n()
const userStore = useUserStore()

const treeRef = ref(null)
const menuTree = ref([])
const selectedMenuIds = ref([])
const submitLoading = ref(false)
const myRequests = ref([])

// 收集所有可申请菜单ID（用于总数显示）
const allMenuIds = computed(() => {
  const ids = []
  function collect(menus) {
    menus.forEach(m => {
      ids.push(m.id)
      if (m.children) collect(m.children)
    })
  }
  collect(menuTree.value)
  return ids
})

// 已选菜单标签（用于右侧显示）
const selectedMenuTags = computed(() => {
  const result = []
  function collect(menus) {
    menus.forEach(m => {
      if (selectedMenuIds.value.includes(m.id)) {
        result.push(m)
      }
      if (m.children) collect(m.children)
    })
  }
  collect(menuTree.value)
  return result
})

function handleTreeCheck(curNode, checkedState) {
  selectedMenuIds.value = checkedState.checkedKeys
}

function removeMenu(menu) {
  treeRef.value.setChecked(menu.id, false, true)
  selectedMenuIds.value = selectedMenuIds.value.filter(id => id !== menu.id)
}

async function fetchMenuTree() {
  try {
    const res = await getRequestableMenusApi()
    menuTree.value = res.data || []
  } catch {
    ElMessage.error(t('permission.request.loadMenuFail'))
  }
}

async function fetchMyRequests() {
  try {
    const res = await getMyRequestsApi({ page: 1, size: 50 })
    const records = res.data?.records || []
    // 尝试解析 JSON 格式的 menuNames
    records.forEach(r => {
      try {
        const names = JSON.parse(r.menuNames)
        r.menuNames = Array.isArray(names) ? names.join('、') : r.menuNames
      } catch {}
    })
    myRequests.value = records
  } catch {
    // 静默
  }
}

async function handleSubmit() {
  if (selectedMenuIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      t('permission.request.submitConfirm'),
      t('common.tip'),
      { type: 'warning' }
    )
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const menuNames = selectedMenuTags.value.map(m => m.menuName)
    await submitPermissionRequestApi({
      menuIds: selectedMenuIds.value,
      menuNames: menuNames
    })
    ElMessage.success(t('permission.request.submitSuccess'))
    selectedMenuIds.value = []
    treeRef.value.setCheckedKeys([])
    fetchMyRequests()
  } catch {
    ElMessage.error(t('permission.request.submitFail'))
  } finally {
    submitLoading.value = false
  }
}

// ====== 邮件申请 ======
const emailDialogVisible = ref(false)
const emailLoading = ref(false)
const emailFormRef = ref(null)
const emailForm = reactive({
  menus: '',
  description: ''
})
const emailFormRules = {
  menus: [{ required: true, message: '请输入申请菜单', trigger: 'blur' }],
  description: [{ required: true, message: '请输入申请理由', trigger: 'blur' }]
}

function openEmailApply() {
  emailDialogVisible.value = true
}

async function handleEmailSubmit() {
  const valid = await emailFormRef.value.validate().catch(() => false)
  if (!valid) return
  emailLoading.value = true
  try {
    await emailPermissionRequestApi({
      userName: userStore.userInfo?.username || userStore.userInfo?.nickname || '',
      menus: emailForm.menus,
      description: emailForm.description
    })
    ElMessage.success(t('permission.request.emailApplySuccess'))
    emailDialogVisible.value = false
    emailForm.menus = ''
    emailForm.description = ''
  } catch {
    ElMessage.error(t('permission.request.emailApplyFail'))
  } finally {
    emailLoading.value = false
  }
}

onMounted(() => {
  fetchMenuTree()
  fetchMyRequests()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.empty-hint {
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.request-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .request-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;

    .request-menus {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
      margin-right: 8px;
    }
  }

  .request-time {
    font-size: 11px;
    color: var(--text-secondary);
  }

  .request-remark {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 4px;
  }
}
</style>

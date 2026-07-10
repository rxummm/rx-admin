<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">{{ $t('profile.baseInfo') }}</span>
          </template>
          <div class="user-avatar-section">
            <el-avatar :size="80" :src="userStore.userInfo?.avatar">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
            </el-avatar>
            <h3 class="nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h3>
            <el-tag v-if="userStore.roles.includes('admin')" type="danger" effect="dark">{{
              $t('profile.roleOptions.admin')
            }}</el-tag>
            <el-tag v-else-if="userStore.roles.includes('operator')" type="warning" effect="dark">{{
              $t('profile.roleOptions.operator')
            }}</el-tag>
            <el-tag v-else type="info" effect="dark">{{ $t('profile.roleOptions.user') }}</el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span class="info-label">{{ $t('profile.username') }}</span>
              <span class="info-value">{{ userStore.userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="info-label">{{ $t('profile.email') }}</span>
              <span class="info-value">{{ userStore.userInfo?.email || $t('profile.notSet') }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span class="info-label">{{ $t('profile.phone') }}</span>
              <span class="info-value">{{ userStore.userInfo?.phone || $t('profile.notSet') }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span class="info-label">{{ $t('profile.createTime') }}</span>
              <span class="info-value">{{ userStore.userInfo?.createTime || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：修改资料 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">{{ $t('profile.updateInfo') }}</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="max-width: 480px">
            <el-form-item :label="$t('profile.nickname')" prop="nickname">
              <el-input v-model="form.nickname" :placeholder="$t('common.input') + $t('profile.nickname')" />
            </el-form-item>
            <el-form-item :label="$t('profile.email')" prop="email">
              <el-input v-model="form.email" :placeholder="$t('common.input') + $t('profile.email')" />
            </el-form-item>
            <el-form-item :label="$t('profile.phone')" prop="phone">
              <el-input v-model="form.phone" :placeholder="$t('common.input') + $t('profile.phone')" />
            </el-form-item>
            <el-form-item :label="$t('profile.gender')" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">{{ $t('profile.genderOptions.unknown') }}</el-radio>
                <el-radio :value="1">{{ $t('profile.genderOptions.male') }}</el-radio>
                <el-radio :value="2">{{ $t('profile.genderOptions.female') }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-divider />
            <!-- ⚠️ 安全要求：改密码时必须先输入旧密码。
                 防止 token 泄露后被攻击者直接改密码永久接管账号。 -->
            <el-form-item :label="$t('profile.oldPassword')" prop="oldPassword">
              <el-input
                v-model="form.oldPassword"
                type="password"
                :placeholder="$t('profile.oldPasswordChangePlaceholder')"
                show-password
              />
            </el-form-item>
            <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
              <el-input
                v-model="form.newPassword"
                type="password"
                :placeholder="$t('common.leaveBlank')"
                show-password
              />
            </el-form-item>
            <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                :placeholder="$t('profile.confirmPasswordPlaceholder')"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">{{ $t('common.save') }}</el-button>
              <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'Profile' })
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { updateProfileApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Message, Phone, Clock } from '@element-plus/icons-vue'

const { t } = useI18n()
const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)

const form = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (form.newPassword && !value) {
    callback(new Error(t('profile.confirmPasswordRequired')))
  } else if (value !== form.newPassword) {
    callback(new Error(t('profile.passwordMismatch')))
  } else {
    callback()
  }
}

// 邮箱格式校验（填写时触发）
const validateEmail = (_rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback()
    return
  }
  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailPattern.test(value)) {
    callback(new Error(t('profile.emailInvalid')))
  } else {
    callback()
  }
}

// 手机号格式校验（填写时触发）
const validatePhone = (_rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback()
    return
  }
  const phonePattern = /^1[3-9]\d{9}$/
  if (!phonePattern.test(value)) {
    callback(new Error(t('profile.phoneInvalid')))
  } else {
    callback()
  }
}

// 密码强度校验（填写时触发）
const validateNewPassword = (_rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const pwdPattern = /^[A-Za-z](?=.*\d).{5,}$/
  if (!pwdPattern.test(value)) {
    callback(new Error(t('profile.passwordPolicy')))
  } else if (value === userStore.userInfo?.username) {
    callback(new Error(t('profile.passwordSameAsUsername')))
  } else if (value === form.nickname) {
    callback(new Error(t('profile.passwordSameAsNickname')))
  } else {
    callback()
  }
}

// 旧密码校验：仅当用户要改密码时才校验（仅校验"是否填写"，不校验"是否正确"，
// 因为前端无法知道当前密码哈希；后端会再校验密码匹配）
const validateOldPassword = (_rule, value, callback) => {
  if (form.newPassword && !value) {
    callback(new Error(t('profile.oldPasswordRequired')))
  } else {
    callback()
  }
}

const rules = {
  nickname: [{ required: true, message: t('common.input') + t('profile.nickname'), trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  oldPassword: [{ validator: validateOldPassword, trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

function loadUserInfo() {
  const info = userStore.userInfo
  form.nickname = info?.nickname || ''
  form.email = info?.email || ''
  form.phone = info?.phone || ''
  form.gender = info?.gender || 0
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

function handleReset() {
  loadUserInfo()
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.resetFields()
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      id: userStore.userInfo.id,
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      gender: form.gender
    }
    if (form.newPassword) {
      // 改密码时同时传递旧密码，后端会先验证旧密码
      data.password = form.newPassword
      data.oldPassword = form.oldPassword
    }
    await updateProfileApi(data)
    await userStore.fetchUserInfo()
    ElMessage.success(t('profile.updateInfoSuccess'))
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } catch {
    // 错误已在拦截器中处理
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .user-avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .nickname {
      margin: 0;
      font-size: 18px;
      color: var(--text-primary);
    }
  }

  .info-list {
    .info-item {
      display: flex;
      align-items: center;
      padding: 10px 0;
      gap: 10px;
      color: var(--text-regular);
      font-size: 14px;

      .el-icon {
        color: var(--text-secondary);
        font-size: 16px;
      }

      .info-label {
        width: 60px;
        flex-shrink: 0;
        color: var(--text-secondary);
      }

      .info-value {
        color: var(--text-primary);
        margin-left: auto;
        text-align: right;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>

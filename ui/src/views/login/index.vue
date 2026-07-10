<template>
  <div class="login-page" :class="{ 'login-page--dark': isDark }">
    <!-- 粒子动画背景 -->
    <ParticleBackground />

    <div class="login-wrapper">
      <!-- 表单工具栏（暗黑/语言切换） -->
      <div class="form-toolbar">
        <el-tooltip :content="isDark ? $t('layout.switchLight') : $t('layout.switchDark')" placement="bottom">
          <div class="toolbar-btn" @click="toggleTheme">
            <el-icon :size="18"><Sunny v-if="isDark" /><Moon v-else /></el-icon>
          </div>
        </el-tooltip>
        <el-tooltip :content="$t('layout.switchLanguage')" placement="bottom">
          <div class="toolbar-btn" @click="handleToggleLocale">
            <FontAwesomeIcon icon="globe" style="font-size: 16px" />
          </div>
        </el-tooltip>
      </div>

      <div class="login-header">
        <h2 class="login-title">{{ isRegister ? $t('login.registerTitle') : $t('login.title') }}</h2>
      </div>

      <!-- 登录表单 -->
      <el-form
        v-if="!isRegister"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" :placeholder="$t('login.username')" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="$t('login.password')"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <!-- 验证码 -->
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaCode"
              :placeholder="$t('login.captchaPlaceholder')"
              size="large"
              maxlength="4"
              class="captcha-input"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" :alt="$t('login.captchaAlt')" />
              <span v-else class="captcha-loading">{{ $t('common.loading') }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loginLoading" @click="handleLogin">
            {{ loginLoading ? $t('login.logining') : $t('login.loginBtn') }}
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <span class="register-link" @click="switchToRegister"
            >{{ $t('login.noAccount') }}{{ $t('login.registerNow') }}</span
          >
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            :placeholder="$t('login.username')"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            :placeholder="$t('login.nickname')"
            size="large"
            :prefix-icon="Edit"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            :placeholder="$t('login.password')"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
          <PasswordStrength :password="registerForm.password" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            :placeholder="$t('login.confirmPassword')"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="registerLoading" @click="handleRegister">
            {{ registerLoading ? $t('login.registering') : $t('login.registerBtn') }}
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <span class="register-link" @click="switchToLogin"
            >{{ $t('login.hasAccount') }}{{ $t('login.backLogin') }}</span
          >
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'Login' })
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { generateDynamicRoutes } from '@/router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { User, Lock, Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCaptchaApi, registerApi } from '@/api/auth'
import { useTheme } from '@/composables/useTheme'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
import PasswordStrength from '@/components/PasswordStrength.vue'
import ParticleBackground from '@/components/ParticleBackground.vue'

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()
const { isDark, toggleTheme } = useTheme()
const localeStore = useStorage(STORAGE_KEYS.LOCALE, 'zh-CN')

const isRegister = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)

// 验证码
const captchaUuid = ref('')
const captchaImage = ref('')

// 登录表单
const loginFormRef = ref(null)
const loginForm = reactive({
  username: import.meta.env.DEV ? 'admin' : '',
  password: import.meta.env.DEV ? 'admin123' : '',
  captchaCode: import.meta.env.DEV ? 'dev000' : ''
})

const loginRules = {
  username: [{ required: true, message: () => t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('login.passwordRequired'), trigger: 'blur' }],
  captchaCode: [{ required: true, message: () => t('login.captchaRequired'), trigger: 'blur' }]
}

// 注册表单
const registerFormRef = ref(null)
const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
  username: [
    { required: true, message: () => t('login.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 20, message: () => t('login.usernameLength'), trigger: 'blur' }
  ],
  nickname: [{ required: true, message: () => t('login.nicknameRequired'), trigger: 'blur' }],
  password: [
    { required: true, message: () => t('login.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: () => t('login.passwordLength'), trigger: 'blur' },
    { pattern: /^[A-Za-z]/, message: () => t('login.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: () => t('login.confirmRequired'), trigger: 'blur' },
    {
      validator: (rule, value) =>
        value === registerForm.password || Promise.reject(new Error(t('login.passwordMismatch'))),
      trigger: 'blur'
    }
  ]
}

// 语言切换
function handleToggleLocale() {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localeStore.set(newLocale)
}

onMounted(() => {
  refreshCaptcha()
})

function refreshCaptcha() {
  getCaptchaApi()
    .then((res) => {
      captchaUuid.value = res.data.uuid
      captchaImage.value = res.data.image
    })
    .catch(() => {})
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    await userStore.login(loginForm.username, loginForm.password, captchaUuid.value, loginForm.captchaCode)
    generateDynamicRoutes(userStore.menus)
    ElMessage.success(t('login.loginSuccess'))
    router.push('/dashboard')
  } catch {
    refreshCaptcha()
    loginForm.captchaCode = ''
  } finally {
    loginLoading.value = false
  }
}

function switchToRegister() {
  isRegister.value = true
}

function switchToLogin() {
  isRegister.value = false
  refreshCaptcha()
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await registerApi(registerForm.username, registerForm.password, registerForm.nickname)
    ElMessage.success(t('login.registerSuccess'))
    switchToLogin()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--login-bg, #1a1a1a);
  position: relative;
  overflow: hidden;
}

.login-page--dark {
  background: var(--login-bg, #0a0a0a);
}

.login-wrapper {
  width: 400px;
  padding: 40px 36px;
  background: var(--login-card-bg, #fff);
  border-radius: var(--radius-sm, 4px);
  border: 1px solid var(--border-color);
  position: relative;
  z-index: var(--z-decor, 1);
}

.form-toolbar {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 6px;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-xs, 2px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition:
    color var(--transition-fast),
    background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    color: var(--text-primary);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.login-form {
  max-width: 328px;
  margin: 0 auto;
  width: 100%;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 110px;
  height: 40px;
  border-radius: var(--radius-xs, 2px);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border-color);
  flex-shrink: 0;
  transition: border-color var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
  }
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 12px;
  color: var(--text-secondary);
}

.login-btn {
  width: 100%;
  border-radius: var(--radius-sm) !important;
  font-weight: 500;
  letter-spacing: 0;
}

.form-footer {
  text-align: center;
  margin-top: 4px;
}

.register-link {
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-primary);
  }
}

// 登录页响应式
@media (max-width: 768px) {
  .login-wrapper {
    width: calc(100vw - 32px);
    padding: 28px 20px;
  }
  .login-title {
    font-size: 22px;
  }
}
@media (max-width: 480px) {
  .login-wrapper {
    padding: 20px 16px;
  }
}
</style>

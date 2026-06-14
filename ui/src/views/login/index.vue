<template>
  <div class="login-page" :class="{ 'login-page--dark': isDark }">
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
      <el-form v-if="!isRegister" ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" :placeholder="$t('login.username')" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" :placeholder="$t('login.password')" size="large" show-password :prefix-icon="Lock" />
        </el-form-item>
        <!-- 验证码 -->
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="loginForm.captchaCode" :placeholder="$t('login.captchaPlaceholder')" size="large" maxlength="4" class="captcha-input" />
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else class="captcha-loading">加载中...</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loginLoading" @click="handleLogin">
            {{ loginLoading ? $t('login.logining') : $t('login.loginBtn') }}
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <span class="register-link" @click="switchToRegister">{{ $t('login.noAccount') }}{{ $t('login.registerNow') }}</span>
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules" class="login-form" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" :placeholder="$t('login.username')" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="registerForm.nickname" :placeholder="$t('login.nickname')" size="large" :prefix-icon="Edit" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" :placeholder="$t('login.password')" size="large" show-password :prefix-icon="Lock" />
        <PasswordStrength :password="registerForm.password" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" :placeholder="$t('login.confirmPassword')" size="large" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="registerLoading" @click="handleRegister">
            {{ registerLoading ? $t('login.registering') : $t('login.registerBtn') }}
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <span class="register-link" @click="switchToLogin">{{ $t('login.hasAccount') }}{{ $t('login.backLogin') }}</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
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
      validator: (rule, value) => value === registerForm.password || Promise.reject(new Error(t('login.passwordMismatch'))),
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
  getCaptchaApi().then(res => {
    captchaUuid.value = res.data.uuid
    captchaImage.value = res.data.image
  }).catch(() => {})
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
  } catch (e) {
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
  background-color: #667eea;
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 300% 300%;
  animation: gradientShift 14s ease infinite;
  position: relative;
  overflow: hidden;

  // 装饰光斑
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse 60% 40% at 20% 80%, rgba(255,255,255,0.14), transparent),
      radial-gradient(ellipse 50% 30% at 75% 15%, rgba(255,255,255,0.10), transparent);
    pointer-events: none;
    z-index: 0;
  }
}

.login-page--dark {
  background-color: #0d1117;
  background-image: linear-gradient(135deg, #0d1117 0%, #161b22 100%);
  background-size: 300% 300%;
  animation: gradientShift 14s ease infinite;
}

.login-wrapper {
  width: 420px;
  padding: 48px 40px 40px;
  background: var(--login-card-bg, #fff);
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow-xl, 0 16px 48px rgba(0,0,0,0.12));
  position: relative;
  z-index: 1;
  transition: transform var(--transition-base);
}

.form-toolbar {
  position: absolute;
  top: 14px;
  right: 14px;
  display: flex;
  gap: 8px;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm, 6px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    color: var(--color-primary);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.login-form {
  max-width: 340px;
  margin: 0 auto;
  width: 100%;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 110px;
  height: 40px;
  border-radius: var(--radius-xs);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border-color);
  flex-shrink: 0;
  transition: border-color var(--transition-fast);

  &:hover { border-color: var(--color-primary); }
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
  letter-spacing: 1px;
  transition: all var(--transition-fast);
}

.form-footer {
  text-align: center;
  margin-top: 4px;
}

.register-link {
  color: var(--color-primary);
  cursor: pointer;
  font-size: 13px;
  transition: opacity var(--transition-fast);

  &:hover {
    opacity: 0.8;
    text-decoration: underline;
  }
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>

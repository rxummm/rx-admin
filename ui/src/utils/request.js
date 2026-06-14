import axios from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import router from '@/router'
import { formatResponseData } from './index'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
import { getActivePinia } from 'pinia'
import { API } from '@/api/routes'
import {
  performanceRequestInterceptor,
  performanceResponseSuccessInterceptor,
  performanceResponseErrorInterceptor
} from './performanceInterceptor'

const tokenStore = useStorage(STORAGE_KEYS.TOKEN)
const userInfoStore = useStorage(STORAGE_KEYS.USER_INFO)
const rolesStore = useStorage(STORAGE_KEYS.ROLES)
const permsStore = useStorage(STORAGE_KEYS.PERMS)
const menusStore = useStorage(STORAGE_KEYS.MENUS)

const request = axios.create({
  baseURL: '/api',
  timeout: Number(import.meta.env.VITE_API_REQUEST_TIMEOUT) || 15000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 性能监控：记录请求开始时间
    performanceRequestInterceptor(config)
    
    if (!config._skipNProgress) {
      NProgress.start()
    }
    const token = tokenStore.get()
    if (token) {
      config.headers['Authorization'] = token
    }
    // 非 GET 请求自动添加防重放头（X-Timestamp + X-Nonce）
    if (config.method && config.method.toLowerCase() !== 'get' && config.method.toLowerCase() !== 'head') {
      if (!config.headers['X-Timestamp']) {
        config.headers['X-Timestamp'] = String(Date.now())
      }
      if (!config.headers['X-Nonce']) {
        config.headers['X-Nonce'] = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 15)
      }
    }
    return config
  },
  error => {
    NProgress.done()
    return Promise.reject(error)
  }
)

function clearAuthData() {
  stopHeartbeat()
  tokenStore.remove()
  userInfoStore.remove()
  rolesStore.remove()
  permsStore.remove()
  menusStore.remove()
  // 同步清除 Pinia userStore 的响应式状态，确保路由守卫能正确判断
  try {
    const pinia = getActivePinia()
    if (pinia) {
      const userStore = pinia._s.get('user')
      if (userStore) {
        userStore.token = ''
        userStore.userInfo = null
        userStore.roles = []
        userStore.perms = []
        userStore.menus = []
      }
    }
  } catch (e) {
    // ignore
  }
}

/** 是否正在显示强制下线遮罩 */
let kickOutOverlayActive = false

/** 显示强制下线倒计时遮罩 */
function showKickOutOverlay() {
  if (kickOutOverlayActive) return
  kickOutOverlayActive = true

  // 清除旧遮罩
  const old = document.getElementById('rx-kickout-overlay')
  if (old) old.remove()

  // 创建遮罩层
  const overlay = document.createElement('div')
  overlay.id = 'rx-kickout-overlay'
  overlay.style.cssText = `
    position: fixed; inset: 0; z-index: 99999;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    background: rgba(0,0,0,0.6); backdrop-filter: blur(6px);
    color: #fff; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    user-select: none;
  `

  // 主标题
  const title = document.createElement('div')
  title.style.cssText = 'font-size: 28px; font-weight: 600; margin-bottom: 16px; letter-spacing: 2px;'
  title.textContent = '已被强制下线'

  // 倒计时
  const countdown = document.createElement('div')
  countdown.style.cssText = 'font-size: 18px; opacity: 0.85;'
  let seconds = Number(import.meta.env.VITE_KICKOUT_COUNTDOWN) || 5

  function updateText() {
    // 使用 i18n 检测，但优先用中文硬编码保证可靠
    countdown.textContent = seconds > 0
      ? `${seconds} 秒后返回登录页面`
      : '正在返回登录页面...'
  }
  updateText()

  overlay.appendChild(title)
  overlay.appendChild(countdown)
  document.body.appendChild(overlay)

  // 启动倒计时
  const timer = setInterval(() => {
    seconds--
    updateText()
    if (seconds <= 0) {
      clearInterval(timer)
      overlay.remove()
      kickOutOverlayActive = false
      clearAuthData()
      window.location.replace('/login')
    }
  }, 1000)
}

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 性能监控：记录成功响应
    performanceResponseSuccessInterceptor(response)
    
    NProgress.done()
    // blob 类型响应（文件下载、导出等）直接透传，不做 JSON 校验
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response.data
    }
    const res = response.data
    if (res.code === 401) {
      if (res.message === 'KICK_OUT') {
        showKickOutOverlay()
      } else {
        clearAuthData()
        window.location.replace('/login')
        ElMessage.error(res.message || '未登录或登录已过期')
      }
      return Promise.reject(new Error(res.message))
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    // 自动格式化时间字段（去掉 ISO 时间中的 T 分隔符）
    return formatResponseData(res)
  },
  error => {
    // 性能监控：记录失败响应
    performanceResponseErrorInterceptor(error)
    
    NProgress.done()
    const status = error.response?.status
    const data = error.response?.data
    if (status === 401) {
      if (data?.message === 'KICK_OUT') {
        showKickOutOverlay()
      } else {
        clearAuthData()
        window.location.replace('/login')
        ElMessage.error('登录已过期，请重新登录')
      }
    } else if (status === 403) {
      // 静默处理：普通用户访问受限资源时不弹错误提示
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)


/** 会话心跳定时器 */
let heartbeatTimer = null

/**
 * 启动会话心跳检测
 * 每 N 秒请求一次 /auth/ping，被踢出时心跳响应会被拦截器捕获，触发 KICK_OUT 遮罩
 * 使用 _skipNProgress=true 避免触发进度条
 * 间隔和超时通过 .env 文件配置：VITE_HEARTBEAT_INTERVAL / VITE_HEARTBEAT_TIMEOUT
 */
function startHeartbeat() {
  if (heartbeatTimer) return
  const interval = Number(import.meta.env.VITE_HEARTBEAT_INTERVAL) || 10000
  const timeout = Number(import.meta.env.VITE_HEARTBEAT_TIMEOUT) || 5000
  heartbeatTimer = setInterval(() => {
    const token = tokenStore.get()
    if (!token) {
      return
    }
    request({ url: API.AUTH.PING, method: 'get', _skipNProgress: true, timeout }).catch(() => {})
  }, interval)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

// 模块加载时自动启动心跳
startHeartbeat()

export default request



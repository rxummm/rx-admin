/**
 * 全局错误处理器
 * 捕获未处理的 Promise 错误、运行时错误和资源加载错误
 */

import { ElMessage, ElNotification } from 'element-plus'
import { captureException } from '@/utils/sentry'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

// 错误存储
const errorStore = useStorage(STORAGE_KEYS.APP_ERRORS)

// 错误计数器（防止短时间内重复弹出大量提示）
let errorCount = 0
const MAX_ERRORS = 3 // 最多显示 3 次错误提示
const RESET_TIME = 10000 // 10秒后重置计数器

// 上次错误时间
let lastErrorTime = 0
const ERROR_THROTTLE = 2000 // 2秒内相同错误只显示一次

// 错误消息缓存
const errorCache = new Map()

/**
 * 初始化全局错误处理
 */
export function initGlobalErrorHandler() {
  // 1. 捕获未处理的 Promise 拒绝
  window.addEventListener('unhandledrejection', handleUnhandledRejection)
  
  // 2. 捕获运行时错误
  window.addEventListener('error', handleRuntimeError, true)
  
  // 3. 捕获资源加载错误
  window.addEventListener('error', handleResourceError, true)
  
  console.log('✅ 全局错误处理器已启动')
}

/**
 * 处理未处理的 Promise 拒绝
 */
function handleUnhandledRejection(event) {
  event.preventDefault()
  
  const reason = event.reason
  console.error('❌ Unhandled Promise Rejection:', reason)
  
  // 上报错误
  reportError('UnhandledPromiseRejection', reason)
  
  // 显示用户友好的提示
  showUserFriendlyError(reason)
}

/**
 * 处理运行时错误
 */
function handleRuntimeError(event) {
  // 忽略资源加载错误（由 handleResourceError 处理）
  if (event.target && (event.target instanceof HTMLElement)) {
    return
  }
  
  const error = event.error || event.message
  
  // 忽略无害的浏览器警告
  if (shouldIgnoreHarmlessError(error)) {
    return
  }
  
  console.error('❌ Runtime Error:', error)
  
  // 上报错误
  reportError('RuntimeError', error, {
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno
  })
  
  // 显示用户友好的提示
  showUserFriendlyError(error)
}

/**
 * 处理资源加载错误
 */
function handleResourceError(event) {
  const target = event.target
  
  // 只处理元素节点的资源加载错误
  if (!target || !(target instanceof HTMLElement)) {
    return
  }
  
  const tagName = target.tagName.toLowerCase()
  const src = target.src || target.href || ''
  
  // 忽略常见的无害错误
  if (shouldIgnoreResourceError(tagName, src)) {
    return
  }
  
  console.warn('⚠️ Resource Load Error:', {
    tag: tagName,
    src,
    type: target.type
  })
  
  // 上报资源加载错误
  reportError('ResourceLoadError', null, {
    tag: tagName,
    src,
    type: target.type
  })
}

/**
 * 判断是否应该忽略该资源错误
 */
function shouldIgnoreResourceError(tagName, src) {
  // 忽略 favicon.ico 错误
  if (src.includes('favicon.ico')) {
    return true
  }
  
  // 忽略空 src
  if (!src) {
    return true
  }
  
  // 忽略 data: 和 blob: URL
  if (src.startsWith('data:') || src.startsWith('blob:')) {
    return true
  }

  // 忽略 track 元素（字幕轨道，ArtPlayer 等播放器自动创建）
  if (tagName === 'track') {
    return true
  }

  return false
}

/**
 * 判断是否应该忽略无害的浏览器警告
 */
function shouldIgnoreHarmlessError(error) {
  if (!error) return true
  
  const errMsg = typeof error === 'string' ? error : (error.message || '')
  
  // ResizeObserver 循环警告（Element Plus 等组件库常见）
  if (errMsg.includes('ResizeObserver loop')) {
    return true
  }
  
  // IntersectionObserver 相关警告
  if (errMsg.includes('IntersectionObserver loop')) {
    return true
  }
  
  // 第三方脚本错误（通常无法控制）
  if (errMsg.includes('Script error.') && !errMsg.includes('http')) {
    return true
  }
  
  // Chrome 扩展程序注入的错误
  if (errMsg.includes('chrome-extension://')) {
    return true
  }
  
  // Webpack HMR 热更新相关警告
  if (errMsg.includes('[HMR]') || errMsg.includes('Hot Module Replacement')) {
    return true
  }
  
  // Vite 开发服务器相关警告
  if (errMsg.includes('[vite]') && errMsg.includes('connected')) {
    return true
  }

  // 动态 import 失败：路由层 onError 已处理，这里只静默记录，不再弹窗打扰用户
  // 关键：不能让它继续向用户弹 Error / Notification，否则会和 ElMessage 重复弹两次
  if (errMsg.includes('Failed to fetch dynamically imported module')) {
    return true
  }

  return false
}

/**
 * 向用户显示友好的错误提示
 */
function showUserFriendlyError(error) {
  const now = Date.now()
  
  // 节流：2秒内相同错误只显示一次
  const errorMessage = getErrorMessage(error)
  const cachedTime = errorCache.get(errorMessage)
  
  if (cachedTime && (now - cachedTime) < ERROR_THROTTLE) {
    return
  }
  
  errorCache.set(errorMessage, now)
  
  // 限制错误提示次数
  if (errorCount >= MAX_ERRORS) {
    // 静默记录，不再弹出提示
    return
  }
  
  errorCount++
  
  // 重置计数器
  setTimeout(() => {
    errorCount = 0
    errorCache.clear()
  }, RESET_TIME)
  
  // 根据错误类型显示不同的提示
  const errorType = classifyError(error)
  
  switch (errorType) {
    case 'network':
      ElNotification({
        title: '网络错误',
        message: '网络连接失败，请检查您的网络设置',
        type: 'error',
        duration: 5000
      })
      break
      
    case 'timeout':
      ElNotification({
        title: '请求超时',
        message: '服务器响应超时，请稍后重试',
        type: 'warning',
        duration: 5000
      })
      break
      
    case 'permission':
      ElNotification({
        title: '权限不足',
        message: '您没有执行此操作的权限',
        type: 'warning',
        duration: 4000
      })
      break
      
    case 'validation':
      ElMessage.warning(errorMessage || '数据验证失败')
      break
      
    default:
      ElNotification({
        title: '发生错误',
        message: errorMessage || '系统遇到意外错误，请稍后重试',
        type: 'error',
        duration: 5000,
        showClose: true
      })
  }
}

/**
 * 获取错误消息
 */
function getErrorMessage(error) {
  if (!error) return ''
  
  if (typeof error === 'string') {
    return error
  }
  
  if (error.message) {
    return error.message
  }
  
  if (error.statusText) {
    return error.statusText
  }
  
  return String(error)
}

/**
 * 错误分类
 */
function classifyError(error) {
  if (!error) return 'unknown'
  
  const errMsg = getErrorMessage(error).toLowerCase()
  const statusCode = error.status || error.code
  
  // 网络错误
  if (errMsg.includes('network error') || 
      errMsg.includes('failed to fetch') ||
      errMsg.includes('networkrequesterror')) {
    return 'network'
  }
  
  // 超时错误
  if (errMsg.includes('timeout') || statusCode === 408) {
    return 'timeout'
  }
  
  // 权限错误
  if (statusCode === 401 || statusCode === 403 || 
      errMsg.includes('permission') || 
      errMsg.includes('unauthorized')) {
    return 'permission'
  }
  
  // 验证错误
  if (errMsg.includes('validation') || 
      errMsg.includes('invalid') ||
      statusCode === 422) {
    return 'validation'
  }
  
  return 'unknown'
}

/**
 * 上报错误到监控服务
 */
function reportError(type, error, context = {}) {
  const errorInfo = {
    type,
    message: getErrorMessage(error),
    timestamp: new Date().toISOString(),
    url: window.location.href,
    userAgent: navigator.userAgent,
    ...context
  }
  
  // 1. 控制台日志
  console.error('[Error Report]', errorInfo)
  
  // 2. 发送到 Sentry（如果已配置）
  try {
    captureException(error || new Error(type), {
      tags: {
        error_type: type,
        ...context
      },
      extra: errorInfo
    })
  } catch (e) {
    // Sentry 未初始化或上报失败，静默处理
    console.warn('Sentry 上报失败:', e)
  }
  
  // 3. 本地存储（用于后续分析）
  try {
    const errors = errorStore.get() || []
    errors.push(errorInfo)
    
    // 只保留最近 100 条错误
    if (errors.length > 100) {
      errors.splice(0, errors.length - 100)
    }
    
    errorStore.set(errors)
  } catch (e) {
    console.error('本地错误存储失败:', e)
  }
}

/**
 * 手动上报错误（供业务代码调用）
 */
export function reportManualError(error, context = {}) {
  reportError('ManualReport', error, context)
}

/**
 * 清除错误缓存
 */
export function clearErrorCache() {
  errorCache.clear()
  errorCount = 0
}

/**
 * 销毁全局错误处理
 */
export function destroyGlobalErrorHandler() {
  window.removeEventListener('unhandledrejection', handleUnhandledRejection)
  window.removeEventListener('error', handleRuntimeError, true)
  window.removeEventListener('error', handleResourceError, true)
  
  console.log('🛑 全局错误处理器已停止')
}

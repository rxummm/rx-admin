/**
 * Sentry 错误监控集成
 * 用于生产环境错误上报和性能监控
 */

import * as Sentry from '@sentry/vue'
import { BrowserTracing } from '@sentry/tracing'

// Sentry DSN（从环境变量读取）
const SENTRY_DSN = import.meta.env.VITE_SENTRY_DSN
const ENV = import.meta.env.MODE

/**
 * 初始化 Sentry
 */
export function initSentry(app, router) {
  // 仅在配置了 DSN 时启用
  if (!SENTRY_DSN) {
    console.warn('⚠️ Sentry DSN 未配置，错误监控已禁用')
    return
  }
  
  Sentry.init({
    app,
    dsn: SENTRY_DSN,
    environment: ENV,
    
    // 性能监控配置
    integrations: [
      new BrowserTracing({
        routingInstrumentation: Sentry.vueRouterInstrumentation(router),
        tracePropagationTargets: ['localhost', /^\//]
      })
    ],
    
    // 采样率
    tracesSampleRate: ENV === 'production' ? 0.1 : 1.0, // 生产环境 10%，开发环境 100%
    
    // 会话回放（可选）
    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    
    // 忽略的错误类型
    ignoreErrors: [
      'ResizeObserver loop limit exceeded',
      'Non-Error promise rejection captured with keys: code, message',
      'Loading chunk'
    ],
    
    //  beforeSend 钩子 - 过滤敏感信息
    beforeSend(event, hint) {
      // 移除可能包含敏感信息的字段
      if (event.request?.headers?.Authorization) {
        delete event.request.headers.Authorization
      }
      
      // 过滤掉一些常见的无意义错误
      if (event.exception?.values?.[0]?.value?.includes('Network Error')) {
        return null
      }
      
      return event
    },
    
    // 用户上下文
    initialScope: {
      tags: {
        app: 'rx-admin',
        version: import.meta.env.VITE_APP_VERSION || 'unknown'
      }
    }
  })
  
  console.log('✅ Sentry 错误监控已启动')
}

/**
 * 设置用户信息
 */
export function setSentryUser(userInfo) {
  if (!userInfo) return
  
  Sentry.setUser({
    id: String(userInfo.id),
    username: userInfo.username,
    email: userInfo.email
  })
}

/**
 * 清除用户信息
 */
export function clearSentryUser() {
  Sentry.setUser(null)
}

/**
 * 添加自定义标签
 */
export function addSentryTag(key, value) {
  Sentry.setTag(key, value)
}

/**
 * 添加自定义上下文
 */
export function addSentryContext(key, value) {
  Sentry.setContext(key, value)
}

/**
 * 手动捕获异常
 */
export function captureException(error, options = {}) {
  Sentry.captureException(error, options)
}

/**
 * 手动捕获消息
 */
export function captureMessage(message, level = 'info') {
  Sentry.captureMessage(message, level)
}

/**
 * 开始事务（性能追踪）
 */
export function startTransaction(name, op = 'custom') {
  return Sentry.startTransaction({ name, op })
}

import * as Sentry from '@sentry/vue'

const SENTRY_DSN = import.meta.env.VITE_SENTRY_DSN
const ENV = import.meta.env.MODE

export function initSentry(app, router) {
  if (!SENTRY_DSN) {
    console.warn('⚠️ Sentry DSN 未配置，错误监控已禁用')
    return
  }

  Sentry.init({
    app,
    dsn: SENTRY_DSN,
    environment: ENV,

    integrations: [
      Sentry.browserTracingIntegration({
        router,
        tracePropagationTargets: ['localhost', /^\//]
      })
    ],

    tracesSampleRate: ENV === 'production' ? 0.1 : 1.0,

    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,

    ignoreErrors: [
      'ResizeObserver loop limit exceeded',
      'Non-Error promise rejection captured with keys: code, message',
      'Loading chunk'
    ],

    beforeSend(event, hint) {
      if (event.request?.headers?.Authorization) {
        delete event.request.headers.Authorization
      }
      if (event.exception?.values?.[0]?.value?.includes('Network Error')) {
        return null
      }
      return event
    },

    initialScope: {
      tags: {
        app: 'rx-admin',
        version: import.meta.env.VITE_APP_VERSION || 'unknown'
      }
    }
  })

  console.log('✅ Sentry 错误监控已启动')
}

export function setSentryUser(userInfo) {
  if (!userInfo) return
  Sentry.setUser({
    id: String(userInfo.id),
    username: userInfo.username,
    email: userInfo.email
  })
}

export function clearSentryUser() {
  Sentry.setUser(null)
}

export function addSentryTag(key, value) {
  Sentry.setTag(key, value)
}

export function addSentryContext(key, value) {
  Sentry.setContext(key, value)
}

export function captureException(error, options = {}) {
  Sentry.captureException(error, options)
}

export function captureMessage(message, level = 'info') {
  Sentry.captureMessage(message, level)
}

export function startTransaction(name, op = 'custom') {
  return Sentry.startTransaction({ name, op })
}

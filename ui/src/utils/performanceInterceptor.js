/**
 * API 性能监控拦截器
 * 自动记录所有 API 请求的耗时
 */

import { recordAPIRequest } from '@/composables/usePerformanceMonitor'

// 存储请求开始时间
const requestStartTime = new Map()

/**
 * 请求拦截器
 */
export function performanceRequestInterceptor(config) {
  // 记录请求开始时间
  requestStartTime.set(config.url, Date.now())
  
  return config
}

/**
 * 响应拦截器 - 成功
 */
export function performanceResponseSuccessInterceptor(response) {
  const url = response.config.url
  const method = response.config.method?.toUpperCase() || 'GET'
  const startTime = requestStartTime.get(url)
  
  if (startTime) {
    const endTime = Date.now()
    recordAPIRequest(url, method, startTime, endTime, response.status)
    requestStartTime.delete(url)
  }
  
  return response
}

/**
 * 响应拦截器 - 失败
 */
export function performanceResponseErrorInterceptor(error) {
  const url = error.config?.url
  const method = error.config?.method?.toUpperCase() || 'GET'
  const startTime = requestStartTime.get(url)
  
  if (startTime) {
    const endTime = Date.now()
    const status = error.response?.status || 0
    recordAPIRequest(url, method, startTime, endTime, status, error.message)
    requestStartTime.delete(url)
  }
  
  return Promise.reject(error)
}

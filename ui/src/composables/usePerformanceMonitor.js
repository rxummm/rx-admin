/**
 * 性能监控服务
 * 提供 FPS、API 耗时、首屏加载时间等性能指标监控
 */

import { ref, computed } from 'vue'
import { COLORS } from '@/config/colors'

// ==================== FPS 监控 ====================

const fps = ref(60)
const frameCount = ref(0)
let lastTime = performance.now()
let fpsTimer = null

/**
 * 启动 FPS 监控
 */
export function startFPSMonitor() {
  let frames = 0
  let lastFrameTime = performance.now()
  
  function updateFPS(currentTime) {
    frames++
    
    if (currentTime - lastFrameTime >= 1000) {
      fps.value = frames
      frames = 0
      lastFrameTime = currentTime
    }
    
    fpsTimer = requestAnimationFrame(updateFPS)
  }
  
  fpsTimer = requestAnimationFrame(updateFPS)
}

/**
 * 停止 FPS 监控
 */
export function stopFPSMonitor() {
  if (fpsTimer) {
    cancelAnimationFrame(fpsTimer)
    fpsTimer = null
  }
}

/**
 * 获取当前 FPS
 */
export function getCurrentFPS() {
  return fps.value
}

// ==================== API 请求统计 ====================

const apiStats = ref({
  totalRequests: 0,
  failedRequests: 0,
  avgResponseTime: 0,
  slowestRequest: 0,
  requests: [] // 最近 50 个请求
})

const MAX_REQUESTS = 50

/**
 * 记录 API 请求
 */
export function recordAPIRequest(url, method, startTime, endTime, status, error = null) {
  const duration = endTime - startTime
  
  apiStats.value.totalRequests++
  
  if (status >= 400) {
    apiStats.value.failedRequests++
  }
  
  // 更新最慢请求
  if (duration > apiStats.value.slowestRequest) {
    apiStats.value.slowestRequest = duration
  }
  
  // 计算平均响应时间
  const totalTime = apiStats.value.requests.reduce((sum, req) => sum + req.duration, 0) + duration
  apiStats.value.avgResponseTime = Math.round(totalTime / apiStats.value.requests.length) || 0
  
  // 添加到请求列表（保留最近 50 个）
  apiStats.value.requests.unshift({
    url,
    method,
    duration: Math.round(duration),
    status,
    timestamp: new Date().toISOString(),
    error
  })
  
  if (apiStats.value.requests.length > MAX_REQUESTS) {
    apiStats.value.requests.pop()
  }
}

/**
 * 获取 API 统计数据
 */
export function getAPIStats() {
  return apiStats.value
}

/**
 * 重置 API 统计
 */
export function resetAPIStats() {
  apiStats.value = {
    totalRequests: 0,
    failedRequests: 0,
    avgResponseTime: 0,
    slowestRequest: 0,
    requests: []
  }
}

// ==================== 首屏加载时间分析 ====================

const pageLoadMetrics = ref({
  FCP: 0, // First Contentful Paint
  LCP: 0, // Largest Contentful Paint
  FID: 0, // First Input Delay
  CLS: 0, // Cumulative Layout Shift
  TTFB: 0, // Time to First Byte
  DOMContentLoaded: 0,
  LoadComplete: 0
})

/**
 * 收集页面加载指标
 */
export function collectPageLoadMetrics() {
  // 使用 Performance API
  if ('performance' in window) {
    const perfData = performance.getEntriesByType('navigation')[0]
    
    if (perfData) {
      pageLoadMetrics.value.TTFB = Math.round(perfData.responseStart - perfData.requestStart)
      pageLoadMetrics.value.DOMContentLoaded = Math.round(perfData.domContentLoadedEventEnd - perfData.startTime)
      pageLoadMetrics.value.LoadComplete = Math.round(perfData.loadEventEnd - perfData.startTime)
    }
    
    // 收集 Paint  timing
    const paintEntries = performance.getEntriesByType('paint')
    paintEntries.forEach(entry => {
      if (entry.name === 'first-contentful-paint') {
        pageLoadMetrics.value.FCP = Math.round(entry.startTime)
      }
    })
    
    // 收集 Largest Contentful Paint
    if ('PerformanceObserver' in window) {
      try {
        const lcpObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          const lastEntry = entries[entries.length - 1]
          pageLoadMetrics.value.LCP = Math.round(lastEntry.startTime)
        })
        
        lcpObserver.observe({ type: 'largest-contentful-paint', buffered: true })
      } catch (e) {
        console.warn('LCP observer not supported')
      }
      
      // 收集 First Input Delay
      try {
        const fidObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          entries.forEach(entry => {
            pageLoadMetrics.value.FID = Math.round(entry.processingStart - entry.startTime)
          })
        })
        
        fidObserver.observe({ type: 'first-input', buffered: true })
      } catch (e) {
        console.warn('FID observer not supported')
      }
      
      // 收集 Cumulative Layout Shift
      try {
        let clsValue = 0
        const clsObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          entries.forEach(entry => {
            if (!entry.hadRecentInput) {
              clsValue += entry.value
            }
          })
          pageLoadMetrics.value.CLS = parseFloat(clsValue.toFixed(4))
        })
        
        clsObserver.observe({ type: 'layout-shift', buffered: true })
      } catch (e) {
        console.warn('CLS observer not supported')
      }
    }
  }
}

/**
 * 获取页面加载指标
 */
export function getPageLoadMetrics() {
  return pageLoadMetrics.value
}

// ==================== 内存监控 ====================

const memoryUsage = ref({
  usedJSHeapSize: 0,
  totalJSHeapSize: 0,
  jsHeapSizeLimit: 0
})

/**
 * 更新内存使用情况
 */
export function updateMemoryUsage() {
  if ('memory' in performance) {
    const memory = performance.memory
    memoryUsage.value = {
      usedJSHeapSize: Math.round(memory.usedJSHeapSize / 1024 / 1024), // MB
      totalJSHeapSize: Math.round(memory.totalJSHeapSize / 1024 / 1024), // MB
      jsHeapSizeLimit: Math.round(memory.jsHeapSizeLimit / 1024 / 1024) // MB
    }
  }
}

/**
 * 获取内存使用情况
 */
export function getMemoryUsage() {
  return memoryUsage.value
}

// ==================== 综合性能评分 ====================

/**
 * 计算性能评分（0-100）
 */
export function calculatePerformanceScore() {
  let score = 100
  
  // FPS 扣分
  if (fps.value < 30) score -= 30
  else if (fps.value < 50) score -= 15
  else if (fps.value < 60) score -= 5
  
  // FCP 扣分
  if (pageLoadMetrics.value.FCP > 3000) score -= 20
  else if (pageLoadMetrics.value.FCP > 1500) score -= 10
  
  // API 失败率扣分
  const failRate = apiStats.value.totalRequests > 0 
    ? apiStats.value.failedRequests / apiStats.value.totalRequests 
    : 0
  
  if (failRate > 0.1) score -= 20
  else if (failRate > 0.05) score -= 10
  
  // 平均响应时间扣分
  if (apiStats.value.avgResponseTime > 2000) score -= 15
  else if (apiStats.value.avgResponseTime > 1000) score -= 8
  
  return Math.max(0, Math.min(100, score))
}

/**
 * 获取性能等级
 */
export function getPerformanceLevel() {
  const score = calculatePerformanceScore()
  
  if (score >= 90) return { level: '优秀', color: COLORS.STATUS.SUCCESS, emoji: '🚀' }
  if (score >= 70) return { level: '良好', color: COLORS.STATUS.PRIMARY, emoji: '✨' }
  if (score >= 50) return { level: '一般', color: COLORS.STATUS.WARNING, emoji: '⚠️' }
  return { level: '较差', color: COLORS.STATUS.DANGER, emoji: '❌' }
}

// ==================== Vue Composition API ====================

/**
 * 性能监控 Composable
 */
export function usePerformanceMonitor() {
  const isMonitoring = ref(false)
  
  /**
   * 启动监控
   */
  function start() {
    if (isMonitoring.value) return
    
    isMonitoring.value = true
    startFPSMonitor()
    collectPageLoadMetrics()
    
    // 每秒更新内存使用
    const memoryInterval = setInterval(updateMemoryUsage, 1000)
    
    return () => {
      clearInterval(memoryInterval)
      stop()
    }
  }
  
  /**
   * 停止监控
   */
  function stop() {
    isMonitoring.value = false
    stopFPSMonitor()
  }
  
  /**
   * 获取所有性能数据
   */
  function getAllMetrics() {
    return {
      fps: fps.value,
      apiStats: apiStats.value,
      pageLoad: pageLoadMetrics.value,
      memory: memoryUsage.value,
      score: calculatePerformanceScore(),
      level: getPerformanceLevel()
    }
  }
  
  return {
    isMonitoring,
    fps,
    apiStats,
    pageLoadMetrics,
    memoryUsage,
    start,
    stop,
    getAllMetrics,
    calculatePerformanceScore,
    getPerformanceLevel
  }
}

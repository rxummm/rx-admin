import request from '@/utils/request'

// 获取仪表盘统计数据
export function getDashboardStatsApi() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

// 获取登录统计（今日登录/失败、7天趋势）
export function getLoginStatsApi() {
  return request({
    url: '/dashboard/enhanced/login-stats',
    method: 'get'
  })
}

// 获取导出统计
export function getExportStatsApi() {
  return request({
    url: '/dashboard/enhanced/export-stats',
    method: 'get'
  })
}

// 获取操作日志Top10
export function getOperationTop10Api() {
  return request({
    url: '/dashboard/enhanced/operation-top10',
    method: 'get'
  })
}

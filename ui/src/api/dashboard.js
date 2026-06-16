import request from '@/utils/request'
import { API } from './routes'

// 获取仪表盘统计数据
export function getDashboardStatsApi() {
  return request({
    url: API.DASHBOARD.STATS,
    method: 'get'
  })
}

// 获取登录统计（今日登录/失败、7天趋势）
export function getLoginStatsApi() {
  return request({
    url: API.DASHBOARD.LOGIN_STATS,
    method: 'get'
  })
}

// 获取导出统计
export function getExportStatsApi() {
  return request({
    url: API.DASHBOARD.EXPORT_STATS,
    method: 'get'
  })
}

// 获取操作日志Top10
export function getOperationTop10Api() {
  return request({
    url: API.DASHBOARD.OPERATION_TOP10,
    method: 'get'
  })
}

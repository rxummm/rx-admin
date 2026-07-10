import request from '@/utils/request'

/**
 * 获取用户登录频率统计
 */
export function getLoginFrequency(days = 7) {
  return request({ url: '/monitor/user-behavior/login-frequency', method: 'get', params: { days } })
}

/**
 * 获取活跃时段分布
 */
export function getActiveTimeDistribution() {
  return request({ url: '/monitor/user-behavior/active-time', method: 'get' })
}

/**
 * 获取操作偏好统计
 */
export function getOperationPreference() {
  return request({ url: '/monitor/user-behavior/operation-preference', method: 'get' })
}

import request from '@/utils/request'
import { API } from './routes'

/**
 * 获取登录异常统计
 */
export function getAnomalyStats() {
  return request({ url: API.MONITOR.LOGIN_ANOMALY.STATS, method: 'get' })
}

/**
 * 获取按IP统计的失败登录
 */
export function getFailedLoginsByIp(days = 7) {
  return request({ url: API.MONITOR.LOGIN_ANOMALY.FAILED_BY_IP, method: 'get', params: { days } })
}

/**
 * 手动触发异常检测
 */
export function triggerDetection() {
  return request({ url: API.MONITOR.LOGIN_ANOMALY.DETECT, method: 'post' })
}

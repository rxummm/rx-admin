import request from '@/utils/request'
import { API } from './routes'

// 日志列表
export function getLogPageApi(params) {
  return request({
    url: API.MONITOR.LOG.PAGE,
    method: 'get',
    params
  })
}

// 删除单条日志
export function deleteLogApi(id) {
  return request({
    url: API.MONITOR.LOG.BY_ID(id),
    method: 'delete'
  })
}

// 批量删除日志
export function deleteLogBatchApi(ids) {
  return request({
    url: API.MONITOR.LOG.BATCH,
    method: 'delete',
    data: ids
  })
}

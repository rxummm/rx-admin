import request from '@/utils/request'
import { API } from './routes'

export function getLoginLogPageApi(params) {
  return request.get(API.MONITOR.LOGIN_LOG.PAGE, { params })
}
export function deleteLoginLogApi(id) {
  return request.delete(API.MONITOR.LOGIN_LOG.BY_ID(id))
}
export function deleteLoginLogBatchApi(ids) {
  return request.delete(API.MONITOR.LOGIN_LOG.BATCH, { data: ids })
}

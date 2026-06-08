import request from '@/utils/request'

export function getLoginLogPageApi(params) {
  return request.get('/monitor/login-log/page', { params })
}
export function deleteLoginLogApi(id) {
  return request.delete(`/monitor/login-log/${id}`)
}
export function deleteLoginLogBatchApi(ids) {
  return request.delete('/monitor/login-log/batch', { data: ids })
}

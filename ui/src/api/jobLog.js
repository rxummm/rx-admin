import request from '@/utils/request'

export function getJobLogPageApi(params) {
  return request.get('/monitor/job-log/page', { params })
}
export function deleteJobLogApi(id) {
  return request.delete(`/monitor/job-log/${id}`)
}
export function deleteJobLogBatchApi(ids) {
  return request.delete('/monitor/job-log/batch', { data: ids })
}

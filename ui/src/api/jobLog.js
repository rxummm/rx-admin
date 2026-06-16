import request from '@/utils/request'
import { API } from './routes'

export function getJobLogPageApi(params) {
  return request.get(API.MONITOR.JOB_LOG.PAGE, { params })
}
export function deleteJobLogApi(id) {
  return request.delete(API.MONITOR.JOB_LOG.BY_ID(id))
}
export function deleteJobLogBatchApi(ids) {
  return request.delete(API.MONITOR.JOB_LOG.BATCH, { data: ids })
}

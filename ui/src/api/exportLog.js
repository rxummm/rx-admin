import request from '@/utils/request'
import { API } from './routes'

export function getExportLogPageApi(params) {
  return request.get(API.MONITOR.EXPORT_LOG.PAGE, { params })
}

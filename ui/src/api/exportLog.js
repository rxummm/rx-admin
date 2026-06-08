import request from '@/utils/request'

export function getExportLogPageApi(params) {
  return request.get('/monitor/export-log/page', { params })
}

import request from '@/utils/request'

// 日志列表
export function getLogPageApi(params) {
  return request({
    url: '/monitor/log/page',
    method: 'get',
    params
  })
}

// 删除单条日志
export function deleteLogApi(id) {
  return request({
    url: `/monitor/log/${id}`,
    method: 'delete'
  })
}

// 批量删除日志
export function deleteLogBatchApi(ids) {
  return request({
    url: '/monitor/log/batch',
    method: 'delete',
    data: ids
  })
}

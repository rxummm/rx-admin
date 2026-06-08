import request from '@/utils/request'

export function getDeptTreeApi() {
  return request({ url: '/sys/dept/tree', method: 'get' })
}

export function addDeptApi(data) {
  return request({ url: '/sys/dept', method: 'post', data })
}

export function updateDeptApi(data) {
  return request({ url: '/sys/dept', method: 'put', data })
}

export function deleteDeptApi(id) {
  return request({ url: `/sys/dept/${id}`, method: 'delete' })
}

/**
 * 根据ID查询部门详情
 * @reserved 预留接口
 */
export function getDeptByIdApi(id) {
  return request({ url: `/sys/dept/${id}`, method: 'get' })
}

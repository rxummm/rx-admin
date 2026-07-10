import request from '@/utils/request'
import { API } from './routes'

export function getDeptTreeApi() {
  return request({ url: API.SYS.DEPT.TREE, method: 'get' })
}

export function addDeptApi(data) {
  return request({ url: API.SYS.DEPT.CRUD, method: 'post', data })
}

export function updateDeptApi(data) {
  return request({ url: API.SYS.DEPT.CRUD, method: 'put', data })
}

export function deleteDeptApi(id) {
  return request({ url: API.SYS.DEPT.BY_ID(id), method: 'delete' })
}

/**
 * 根据ID查询部门详情
 * @reserved 预留接口
 */
export function getDeptByIdApi(id) {
  return request({ url: API.SYS.DEPT.BY_ID(id), method: 'get' })
}

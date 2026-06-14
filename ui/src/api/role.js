import request from '@/utils/request'
import { API } from './routes'

// 角色列表
export function getRoleListApi() {
  return request({
    url: API.SYS.ROLE.LIST,
    method: 'get'
  })
}

// 新增角色
export function addRoleApi(data, menuIds) {
  return request({
    url: API.SYS.ROLE.CRUD,
    method: 'post',
    data,
    params: { menuIds: menuIds?.join(',') }
  })
}

// 修改角色
export function updateRoleApi(data, menuIds) {
  return request({
    url: API.SYS.ROLE.CRUD,
    method: 'put',
    data,
    params: { menuIds: menuIds?.join(',') }
  })
}

// 删除角色
export function deleteRoleApi(id) {
  return request({
    url: API.SYS.ROLE.BY_ID(id),
    method: 'delete'
  })
}

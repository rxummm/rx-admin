import request from '@/utils/request'
import { API } from './routes'

// 用户列表
export function getUserPageApi(params) {
  return request({
    url: API.SYS.USER.PAGE,
    method: 'get',
    params
  })
}

// 新增用户
export function addUserApi(data, roleIds) {
  return request({
    url: API.SYS.USER.CRUD,
    method: 'post',
    data,
    params: { roleIds: roleIds?.join(',') }
  })
}

// 修改用户
export function updateUserApi(data, roleIds) {
  return request({
    url: API.SYS.USER.CRUD,
    method: 'put',
    data,
    params: { roleIds: roleIds?.join(',') }
  })
}

// 删除用户
export function deleteUserApi(id) {
  return request({
    url: API.SYS.USER.BY_ID(id),
    method: 'delete'
  })
}

/**
 * 用户详情
 * @reserved 预留接口，编辑时通过列表数据回显
 */
export function getUserByIdApi(id) {
  return request({
    url: API.SYS.USER.BY_ID(id),
    method: 'get'
  })
}

import request from '@/utils/request'

// 角色列表
export function getRoleListApi() {
  return request({
    url: '/sys/role/list',
    method: 'get'
  })
}

// 新增角色
export function addRoleApi(data, menuIds) {
  return request({
    url: '/sys/role',
    method: 'post',
    data,
    params: { menuIds: menuIds?.join(',') }
  })
}

// 修改角色
export function updateRoleApi(data, menuIds) {
  return request({
    url: '/sys/role',
    method: 'put',
    data,
    params: { menuIds: menuIds?.join(',') }
  })
}

// 删除角色
export function deleteRoleApi(id) {
  return request({
    url: `/sys/role/${id}`,
    method: 'delete'
  })
}

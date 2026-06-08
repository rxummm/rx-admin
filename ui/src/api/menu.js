import request from '@/utils/request'

// 菜单树
export function getMenuTreeApi() {
  return request({
    url: '/sys/menu/tree',
    method: 'get'
  })
}

// 新增菜单
export function addMenuApi(data) {
  return request({
    url: '/sys/menu',
    method: 'post',
    data
  })
}

// 修改菜单
export function updateMenuApi(data) {
  return request({
    url: '/sys/menu',
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenuApi(id) {
  return request({
    url: `/sys/menu/${id}`,
    method: 'delete'
  })
}

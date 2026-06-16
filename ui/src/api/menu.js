import request from '@/utils/request'
import { API } from './routes'

// 菜单树
export function getMenuTreeApi() {
  return request({
    url: API.SYS.MENU.TREE,
    method: 'get'
  })
}

// 新增菜单
export function addMenuApi(data) {
  return request({
    url: API.SYS.MENU.CRUD,
    method: 'post',
    data
  })
}

// 修改菜单
export function updateMenuApi(data) {
  return request({
    url: API.SYS.MENU.CRUD,
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenuApi(id) {
  return request({
    url: API.SYS.MENU.BY_ID(id),
    method: 'delete'
  })
}

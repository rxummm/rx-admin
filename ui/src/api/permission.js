import request from '@/utils/request'
import { API } from './routes'

// 获取可申请的菜单树
export function getRequestableMenusApi() {
  return request({
    url: API.SYS.MENU.REQUESTABLE,
    method: 'get'
  })
}

// 提交权限申请
export function submitPermissionRequestApi(data) {
  return request({
    url: API.SYS.PERMISSION_REQUEST.CRUD,
    method: 'post',
    data
  })
}

// 获取待审批列表（admin）
export function getPendingRequestsApi(params) {
  return request({
    url: API.SYS.PERMISSION_REQUEST.PENDING,
    method: 'get',
    params
  })
}

// 获取我的申请列表
export function getMyRequestsApi(params) {
  return request({
    url: API.SYS.PERMISSION_REQUEST.MY,
    method: 'get',
    params
  })
}

// 审批通过
export function approveRequestApi(id) {
  return request({
    url: API.SYS.PERMISSION_REQUEST.APPROVE(id),
    method: 'put'
  })
}

// 审批拒绝
export function rejectRequestApi(id, data) {
  return request({
    url: API.SYS.PERMISSION_REQUEST.REJECT(id),
    method: 'put',
    data
  })
}

// ====== 权限管理 API（admin） ======

// 获取用户已有菜单权限ID列表
export function getUserMenuIdsApi(userId) {
  return request({
    url: API.SYS.PERMISSION_MANAGE.USER_MENUS(userId),
    method: 'get'
  })
}

// 获取可管理的菜单树（排除管理类菜单和已拥有菜单）
export function getManageableMenuTreeApi(userId) {
  return request({
    url: API.SYS.PERMISSION_MANAGE.MANAGEABLE_TREE(userId),
    method: 'get'
  })
}

// 给用户添加菜单权限
export function addUserMenusApi(userId, menuIds) {
  return request({
    url: API.SYS.PERMISSION_MANAGE.ADD(userId),
    method: 'post',
    data: { menuIds }
  })
}

// 移除用户菜单权限
export function removeUserMenusApi(userId, menuIds) {
  return request({
    url: API.SYS.PERMISSION_MANAGE.REMOVE(userId),
    method: 'post',
    data: { menuIds }
  })
}

// 设置用户菜单权限（替换模式：清空后写入勾选的，只保留选中权限）
export function setUserMenusApi(userId, menuIds) {
  return request({
    url: API.SYS.PERMISSION_MANAGE.SET(userId),
    method: 'post',
    data: { menuIds }
  })
}

// 邮件申请权限（申请角色范围外的菜单权限）
export function emailPermissionRequestApi(data) {
  return request({
    url: API.SYS.PERMISSION_REQUEST.EMAIL_REQUEST,
    method: 'post',
    data
  })
}

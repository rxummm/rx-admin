import request from '@/utils/request'
import { API } from './routes'

/**
 * 获取所有分类及其服务列表（树形结构，列表级别不包含子表）
 */
export function getCategoriesApi() {
  return request({
    url: API.ISERVICE.CATEGORIES,
    method: 'get'
  })
}

/**
 * 根据分类编码获取服务列表
 */
export function getCategoryByCodeApi(code) {
  return request({
    url: API.ISERVICE.CATEGORY_BY_CODE(code),
    method: 'get'
  })
}

/**
 * 获取单个服务的完整详情（含参数/列/示例/权限子表）
 */
export function getItemDetailApi(id) {
  return request({
    url: API.ISERVICE.ITEM_DETAIL(id),
    method: 'get'
  })
}

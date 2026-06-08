import request from '@/utils/request'

/**
 * 获取所有分类及其服务列表（树形结构，列表级别不包含子表）
 */
export function getCategoriesApi() {
  return request({
    url: '/iservice/categories',
    method: 'get'
  })
}

/**
 * 根据分类编码获取服务列表
 */
export function getCategoryByCodeApi(code) {
  return request({
    url: `/iservice/categories/${code}`,
    method: 'get'
  })
}

/**
 * 获取单个服务的完整详情（含参数/列/示例/权限子表）
 */
export function getItemDetailApi(id) {
  return request({
    url: `/iservice/items/${id}`,
    method: 'get'
  })
}

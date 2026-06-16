import request from '@/utils/request'
import { API } from './routes'

/**
 * 分页查询行政区划
 */
export function getRegionPageApi(params) {
  return request({
    url: API.TOOL.REGION.PAGE,
    method: 'get',
    params
  })
}

/**
 * 查询下级行政区划列表（级联选择器用）
 */
export function getRegionChildrenApi(parentCode) {
  return request({
    url: API.TOOL.REGION.CHILDREN,
    method: 'get',
    params: { parentCode }
  })
}

/**
 * 搜索行政区划（级联选择器搜索用）
 */
export function searchRegionApi(params) {
  return request({
    url: API.TOOL.REGION.SEARCH,
    method: 'get',
    params
  })
}

/**
 * 根据ID查询详情
 * @reserved 预留接口，编辑时通过列表数据回显
 */
export function getRegionByIdApi(id) {
  return request({
    url: API.TOOL.REGION.BY_ID(id),
    method: 'get'
  })
}

/**
 * 新增行政区划
 */
export function addRegionApi(data) {
  return request({
    url: API.TOOL.REGION.CRUD,
    method: 'post',
    data
  })
}

/**
 * 修改行政区划
 */
export function updateRegionApi(data) {
  return request({
    url: API.TOOL.REGION.CRUD,
    method: 'put',
    data
  })
}

/**
 * 删除行政区划
 */
export function deleteRegionApi(id) {
  return request({
    url: API.TOOL.REGION.BY_ID(id),
    method: 'delete'
  })
}

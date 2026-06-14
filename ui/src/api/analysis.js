import request from '@/utils/request'
import { API } from './routes'

/**
 * 获取所有可分析的菜单列表
 */
export function getAnalysisMenusApi() {
  return request({ url: API.TOOL.ANALYSIS.MENUS, method: 'get' })
}

/**
 * 分析指定菜单的完整交互链路
 * @param {string} menuName - 菜单名称，如"红楼人物"
 */
export function analyzeMenuApi(menuName) {
  return request({ url: API.TOOL.ANALYSIS.ANALYZE, method: 'get', params: { menuName } })
}

/**
 * 搜索菜单（模糊匹配）
 * @param {string} keyword - 搜索关键词
 */
export function searchMenuApi(keyword) {
  return request({ url: API.TOOL.ANALYSIS.SEARCH, method: 'get', params: { keyword } })
}

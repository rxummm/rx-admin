import request from '@/utils/request'
import { API } from './routes'

/**
 * 查询指定页面是否启用导出
 * @param {string} path 菜单路径，如 /system/user
 */
export function getExportConfigApi(path) {
  return request({ url: API.EXPORT.CONFIG, method: 'get', params: { path } })
}

/**
 * 导出 Excel
 * @param {{ title, columns, data }} payload
 */
export function exportExcelApi(payload) {
  return request({
    url: API.EXPORT.EXCEL,
    method: 'post',
    data: payload,
    responseType: 'blob',
    timeout: 120000 // 导出超时 2 分钟
  })
}

/**
 * 导出 PDF
 * @param {{ title, columns, data }} payload
 */
export function exportPdfApi(payload) {
  return request({
    url: API.EXPORT.PDF,
    method: 'post',
    data: payload,
    responseType: 'blob',
    timeout: 120000 // PDF 字体加载 + 渲染较慢，超时 2 分钟
  })
}

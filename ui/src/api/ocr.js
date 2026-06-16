import request from '@/utils/request'
import { API } from './routes'

// 上传文件并OCR识别
export function recognizeOcrApi(formData) {
  return request({
    url: API.OCR.RECOGNIZE,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 分页查询识别记录
export function getOcrPageApi(params) {
  return request({ url: API.OCR.PAGE, method: 'get', params })
}

// 获取识别详情
export function getOcrByIdApi(id) {
  return request({ url: API.OCR.BY_ID(id), method: 'get' })
}

// 删除识别记录
export function deleteOcrApi(id) {
  return request({ url: API.OCR.BY_ID(id), method: 'delete' })
}

// 批量删除识别记录
export function deleteOcrBatchApi(ids) {
  return request({ url: API.OCR.BATCH + '/' + ids, method: 'delete' })
}

// 全部删除识别记录
export async function deleteAllOcrApi() {
  const res = await request({ url: API.OCR.PAGE, method: 'get', params: { page: 1, size: 10000 } })
  const records = res.data?.records || []
  if (records.length === 0) return
  const ids = records.map(r => r.id).join(',')
  return deleteOcrBatchApi(ids)
}

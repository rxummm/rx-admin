import request from '@/utils/request'
import { API } from './routes'

// 模板
export function getTemplatePageApi(params) {
  return request.get(API.NOTIFY_CENTER.TEMPLATES.PAGE, { params })
}
export function addTemplateApi(data) {
  return request.post(API.NOTIFY_CENTER.TEMPLATES.CRUD, data)
}
export function updateTemplateApi(data) {
  return request.put(API.NOTIFY_CENTER.TEMPLATES.CRUD, data)
}
export function deleteTemplateApi(id) {
  return request.delete(API.NOTIFY_CENTER.TEMPLATES.BY_ID(id))
}
export function sendNotifyApi(data) {
  return request.post(API.NOTIFY_CENTER.SEND, data)
}
// 记录
export function getNotifyRecordPageApi(params) {
  return request.get(API.NOTIFY_CENTER.RECORDS.PAGE, { params })
}
export function deleteNotifyRecordApi(id) {
  return request.delete(API.NOTIFY_CENTER.RECORDS.BY_ID(id))
}
export function deleteNotifyRecordsBatchApi(ids) {
  return request.delete(API.NOTIFY_CENTER.RECORDS.BATCH, { data: ids })
}
export function retryNotifyApi(id) {
  return request.post(API.NOTIFY_CENTER.RECORDS.RETRY(id))
}

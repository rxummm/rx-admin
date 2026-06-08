import request from '@/utils/request'

// 模板
export function getTemplatePageApi(params) { return request.get('/notify-center/templates/page', { params }) }
export function addTemplateApi(data) { return request.post('/notify-center/templates', data) }
export function updateTemplateApi(data) { return request.put('/notify-center/templates', data) }
export function deleteTemplateApi(id) { return request.delete(`/notify-center/templates/${id}`) }
export function sendNotifyApi(data) { return request.post('/notify-center/send', data) }
// 记录
export function getNotifyRecordPageApi(params) { return request.get('/notify-center/records/page', { params }) }
export function deleteNotifyRecordApi(id) { return request.delete(`/notify-center/records/${id}`) }
export function deleteNotifyRecordsBatchApi(ids) { return request.delete('/notify-center/records/batch', { data: ids }) }
export function retryNotifyApi(id) { return request.post(`/notify-center/records/${id}/retry`) }

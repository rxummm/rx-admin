import request from '@/utils/request'

export function getFilePageApi(params) {
  return request({ url: '/sys/file/page', method: 'get', params })
}

export function uploadFileApi(formData) {
  return request({ url: '/sys/file/upload', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function deleteFileApi(id) {
  return request({ url: `/sys/file/${id}`, method: 'delete' })
}

export function deleteFileBatchApi(ids) {
  return request({ url: '/sys/file/batch', method: 'delete', data: ids })
}

export function getFileDownloadUrlApi(id) {
  return `/api/sys/file/download/${id}`
}

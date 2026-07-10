import request from '@/utils/request'
import { API } from './routes'

export function getFilePageApi(params) {
  return request({ url: API.SYS.FILE.PAGE, method: 'get', params })
}

export function uploadFileApi(formData) {
  return request({
    url: API.SYS.FILE.UPLOAD,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteFileApi(id) {
  return request({ url: API.SYS.FILE.BY_ID(id), method: 'delete' })
}

export function deleteFileBatchApi(ids) {
  return request({ url: API.SYS.FILE.BATCH, method: 'delete', data: ids })
}

export function getFileDownloadUrlApi(id) {
  return API.SYS.FILE.DOWNLOAD(id)
}

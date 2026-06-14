import request from '@/utils/request'
import { API } from './routes'

// Excel解析 - 上传并解析
export function parseExcelApi(formData) {
  return request({
    url: API.TOOL.COMMON.EXCEL_PARSE,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 文档上传
export function uploadDocumentApi(formData) {
  return request({
    url: API.TOOL.COMMON.DOCUMENT_UPLOAD,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 文档列表
export function getDocumentListApi(params) {
  return request({ url: API.TOOL.COMMON.DOCUMENT_LIST, method: 'get', params })
}

// 删除文档
export function deleteDocumentApi(id) {
  return request({ url: API.TOOL.COMMON.DOCUMENT_BY_ID(id), method: 'delete' })
}

// 获取默认存储路径
export function getDefaultDirApi() {
  return request({ url: API.TOOL.COMMON.DOCUMENT_DEFAULT_DIR, method: 'get' })
}

// PDF转Word
export function convertPdfToWordApi(formData) {
  return request({
    url: API.TOOL.COMMON.PDF_TO_WORD,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Word转PDF
export function convertWordToPdfApi(formData) {
  return request({
    url: API.TOOL.COMMON.WORD_TO_PDF,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 邮件发送
export function sendEmailApi(data) {
  const formData = new FormData()
  formData.append('to', data.to)
  if (data.cc) formData.append('cc', data.cc)
  if (data.bcc) formData.append('bcc', data.bcc)
  formData.append('subject', data.subject)
  formData.append('content', data.content)
  formData.append('isHtml', data.isHtml || false)
  if (data.attachmentPaths) formData.append('attachmentPaths', data.attachmentPaths)
  return request({
    url: API.TOOL.COMMON.EMAIL_SEND,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传邮件附件
export function uploadEmailAttachmentApi(formData) {
  return request({
    url: API.TOOL.COMMON.EMAIL_UPLOAD,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取邮件配置（发件人等）
export function getEmailConfigApi() {
  return request({ url: API.TOOL.COMMON.EMAIL_CONFIG, method: 'get' })
}

import request from '@/utils/request'
import { API } from './routes'

export function jsonFormatApi(data) {
  return request.post(API.TOOL.DEV.JSON_FORMAT, data)
}
export function generateUuidApi(count = 5) {
  return request.get(API.TOOL.DEV.UUID, { params: { count } })
}
export function timestampConvertApi(data) {
  return request.post(API.TOOL.DEV.TIMESTAMP, data)
}

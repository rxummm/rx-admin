import request from '@/utils/request'

export function jsonFormatApi(data) { return request.post('/tool/dev/json-format', data) }
export function generateUuidApi(count = 5) { return request.get('/tool/dev/uuid', { params: { count } }) }
export function timestampConvertApi(data) { return request.post('/tool/dev/timestamp', data) }

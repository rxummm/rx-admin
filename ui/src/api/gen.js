import request from '@/utils/request'

export const getTableListApi = () => request.get('/tool/gen/tables')
export const getTableColumnsApi = (table) => request.get('/tool/gen/columns', { params: { table } })
export const previewCodeApi = (data) => request.post('/tool/gen/preview', data)
export const generateCodeApi = (data) => request.post('/tool/gen/generate', data)

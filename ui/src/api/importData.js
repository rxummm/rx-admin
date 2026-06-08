import request from '@/utils/request'

export const analyzeFileApi = (formData) => request.post('/tool/import/analyze', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const executeImportApi = (data) => request.post('/tool/import/execute', data)

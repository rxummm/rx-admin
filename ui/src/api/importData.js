import request from '@/utils/request'
import { API } from './routes'

export const analyzeFileApi = (formData) => request.post(API.TOOL.IMPORT.ANALYZE, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const executeImportApi = (data) => request.post(API.TOOL.IMPORT.EXECUTE, data)

import request from '@/utils/request'
import { API } from './routes'

export const getTableListApi = () => request.get(API.TOOL.GEN.TABLES)
export const getTableColumnsApi = (table) => request.get(API.TOOL.GEN.COLUMNS, { params: { table } })
export const previewCodeApi = (data) => request.post(API.TOOL.GEN.PREVIEW, data)
export const generateCodeApi = (data) => request.post(API.TOOL.GEN.GENERATE, data)

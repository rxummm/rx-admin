import request from '@/utils/request'
import { API } from './routes'

export const getEndpointsApi = () => request.get(API.TOOL.API_DEBUG.ENDPOINTS)

import request from '@/utils/request'

export const getEndpointsApi = () => request.get('/tool/api-debug/endpoints')

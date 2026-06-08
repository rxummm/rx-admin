import request from '@/utils/request'

export const getSystemHealthApi = () => request.get('/monitor/health/system')
export const getGcStatsApi = () => request.get('/monitor/health/gc')

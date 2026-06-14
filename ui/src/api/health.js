import request from '@/utils/request'

export const getSystemHealthApi = () => request.get('/monitor/health/system', { _skipNProgress: true })
export const getGcStatsApi = () => request.get('/monitor/health/gc', { _skipNProgress: true })

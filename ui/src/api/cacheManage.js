import request from '@/utils/request'

export function getCacheListApi() { return request.get('/monitor/cache/list') }
export function clearCacheApi(name) { return request.delete(`/monitor/cache/clear/${encodeURIComponent(name)}`) }
export function clearAllCacheApi() { return request.delete('/monitor/cache/clear-all') }

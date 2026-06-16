import request from '@/utils/request'
import { API } from './routes'

export function getCacheListApi() { return request.get(API.MONITOR.CACHE.LIST) }
export function clearCacheApi(name) { return request.delete(API.MONITOR.CACHE.CLEAR(name)) }
export function clearAllCacheApi() { return request.delete(API.MONITOR.CACHE.CLEAR_ALL) }

import request from '@/utils/request'
import { API } from './routes'

export const getSystemHealthApi = () => request.get(API.MONITOR.HEALTH.SYSTEM, { _skipNProgress: true })
export const getGcStatsApi = () => request.get(API.MONITOR.HEALTH.GC, { _skipNProgress: true })

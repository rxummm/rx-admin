import request from '@/utils/request'
import { API } from './routes'

export const getLogSummaryApi = () => request.get(API.MONITOR.LOG_ANALYSIS.SUMMARY, { _skipNProgress: true })
export const getLogHourlyApi = () => request.get(API.MONITOR.LOG_ANALYSIS.HOURLY)
export const getLogTypeDistributionApi = () => request.get(API.MONITOR.LOG_ANALYSIS.TYPE_DISTRIBUTION)
export const getLogTrendApi = (days = 7) => request.get(API.MONITOR.LOG_ANALYSIS.TREND, { params: { days } })

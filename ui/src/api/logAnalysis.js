import request from '@/utils/request'

export const getLogSummaryApi = () => request.get('/monitor/log-analysis/summary', { _skipNProgress: true })
export const getLogHourlyApi = () => request.get('/monitor/log-analysis/hourly')
export const getLogTypeDistributionApi = () => request.get('/monitor/log-analysis/type-distribution')
export const getLogTrendApi = (days = 7) => request.get('/monitor/log-analysis/trend', { params: { days } })

import request from '@/utils/request'
import { API } from './routes'

export function getOnlineListApi() {
  return request({ url: API.MONITOR.ONLINE.LIST, method: 'get' })
}

export function kickOutApi(tokenValue) {
  return request({ url: API.MONITOR.ONLINE.KICK(tokenValue), method: 'delete' })
}

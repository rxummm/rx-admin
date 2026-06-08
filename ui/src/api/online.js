import request from '@/utils/request'

export function getOnlineListApi() {
  return request({ url: '/monitor/online/list', method: 'get' })
}

export function kickOutApi(tokenValue) {
  return request({ url: `/monitor/online/${tokenValue}`, method: 'delete' })
}

import request from "@/utils/request"
import { API } from './routes'

export function getSlowQueryPageApi(params) {
  return request({ url: API.MONITOR.SLOW_QUERY.PAGE, method: "get", params })
}
export function deleteSlowQueryApi(id) {
  return request({ url: API.MONITOR.SLOW_QUERY.BY_ID(id), method: "delete" })
}
export function batchDeleteSlowQueryApi(ids) {
  return request({ url: API.MONITOR.SLOW_QUERY.BATCH, method: "delete", data: ids })
}
export function clearSlowQueryApi() {
  return request({ url: API.MONITOR.SLOW_QUERY.CLEAR, method: "delete" })
}

import request from "@/utils/request"

export function getSlowQueryPageApi(params) {
  return request({ url: "/monitor/slow-query/page", method: "get", params })
}
export function deleteSlowQueryApi(id) {
  return request({ url: "/monitor/slow-query/" + id, method: "delete" })
}
export function batchDeleteSlowQueryApi(ids) {
  return request({ url: "/monitor/slow-query/batch", method: "delete", data: ids })
}
export function clearSlowQueryApi() {
  return request({ url: "/monitor/slow-query/clear", method: "delete" })
}

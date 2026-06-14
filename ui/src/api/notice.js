import request from '@/utils/request'
import { API } from './routes'

/**
 * @param {object} params 查询参数
 * @param {object} [options] axios 配置覆盖（如 { _skipNProgress: true } 定时轮询时跳过进度条）
 */
export function getNoticePageApi(params, options = {}) {
  return request({ url: API.CONTENT.NOTICE.PAGE, method: 'get', params, ...options })
}

/**
 * @param {object} [options] axios 配置覆盖（如 { _skipNProgress: true } 定时轮询时跳过进度条）
 */
export function getNoticeSummaryApi(options = {}) {
  return request({ url: API.CONTENT.NOTICE.SUMMARY, method: 'get', ...options })
}

/**
 * 获取待办数量
 * @reserved 预留接口，布局铃铛处使用 summary 接口统计
 */
export function getTodoCountApi() {
  return request({ url: API.CONTENT.NOTICE.TODO_COUNT, method: 'get' })
}

export function addNoticeApi(data) {
  return request({ url: API.CONTENT.NOTICE.CRUD, method: 'post', data })
}

export function updateNoticeApi(data) {
  return request({ url: API.CONTENT.NOTICE.CRUD, method: 'put', data })
}

export function deleteNoticeApi(id) {
  return request({ url: API.CONTENT.NOTICE.BY_ID(id), method: 'delete' })
}

/**
 * 根据ID查询单条通知详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getNoticeByIdApi(id) {
  return request({ url: API.CONTENT.NOTICE.BY_ID(id), method: 'get' })
}

/**
 * 获取当前用户已读的通知/公告ID列表（替代localStorage持久化）
 */
export function getReadNoticeIdsApi() {
  return request({ url: API.CONTENT.NOTICE.READ_IDS, method: 'get' })
}

/**
 * 标记单条通知/公告为已读（持久化到数据库）
 */
export function markNoticeReadApi(id) {
  return request({ url: API.CONTENT.NOTICE.READ(id), method: 'post' })
}

/**
 * 标记所有通知/公告为已读
 */
export function markAllNoticeReadApi() {
  return request({ url: API.CONTENT.NOTICE.READ_ALL, method: 'post' })
}

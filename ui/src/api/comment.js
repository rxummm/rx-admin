import request from '@/utils/request'
import { API } from './routes'

/**
 * 分页查询评论
 */
export function getCommentPage(params) {
  return request({ url: '/content/comment/page', method: 'get', params })
}

/**
 * 添加评论
 */
export function addComment(data) {
  return request({ url: '/content/comment', method: 'post', data })
}

/**
 * 删除评论
 */
export function deleteComment(id) {
  return request({ url: `/content/comment/${id}`, method: 'delete' })
}

/**
 * 获取评论数量
 */
export function getCommentCount(targetType, targetId) {
  return request({ url: '/content/comment/count', method: 'get', params: { targetType, targetId } })
}

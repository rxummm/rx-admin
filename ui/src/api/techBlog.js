import request from '@/utils/request'
import { API } from './routes'

/** 分页查询文章列表 */
export function getArticlesApi(params) {
  return request({
    url: API.TECH_BLOG.ARTICLES,
    method: 'get',
    params
  })
}

/** 获取文章详情 */
export function getArticleDetailApi(id) {
  return request({
    url: API.TECH_BLOG.ARTICLE_BY_ID(id),
    method: 'get'
  })
}

/** 获取所有分类标签（可选按来源过滤） */
export function getCategoriesApi(source) {
  return request({
    url: API.TECH_BLOG.CATEGORIES,
    method: 'get',
    params: source ? { source } : {}
  })
}

/** 获取最近文章（可选按来源过滤） */
export function getRecentArticlesApi(limit = 5, source) {
  return request({
    url: API.TECH_BLOG.RECENT,
    method: 'get',
    params: { limit, ...(source ? { source } : {}) }
  })
}

/** 触发指定来源的抓取 */
export function startFetchApi(source = 'nicklitten') {
  return request({
    url: API.TECH_BLOG.FETCH,
    method: 'post',
    data: { source }
  })
}

/**
 * 查看抓取进度（可选按 source 过滤）
 * @param {object} [options] axios 配置覆盖（如 { _skipNProgress: true } 轮询时跳过进度条）
 */
export function getFetchProgressApi(source, options = {}) {
  return request({
    url: API.TECH_BLOG.PROGRESS,
    method: 'get',
    params: source ? { source } : {},
    ...options
  })
}

/** 更新文章 */
export function updateArticleApi(id, data) {
  return request({
    url: API.TECH_BLOG.ARTICLE_BY_ID(id),
    method: 'put',
    data
  })
}

/** 删除单篇文章 */
export function deleteArticleApi(id) {
  return request({
    url: API.TECH_BLOG.ARTICLE_BY_ID(id),
    method: 'delete'
  })
}

/** 批量删除文章 */
export function batchDeleteArticlesApi(ids) {
  return request({
    url: API.TECH_BLOG.ARTICLES_BATCH,
    method: 'delete',
    data: { ids }
  })
}

/** 新增文章 */
export function createArticleApi(data) {
  return request({
    url: API.TECH_BLOG.ARTICLES,
    method: 'post',
    data
  })
}

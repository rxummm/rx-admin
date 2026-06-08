import request from '@/utils/request'

// ====== 水浒诗词 ======

export function getShuihuPoemPageApi(params) {
  return request({ url: '/classics/shuihu/poem/page', method: 'get', params })
}

/**
 * 水浒诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getShuihuPoemDetailApi(id) {
  return request({ url: `/classics/shuihu/poem/${id}`, method: 'get' })
}

export function addShuihuPoemApi(data) {
  return request({ url: '/classics/shuihu/poem', method: 'post', data })
}

export function updateShuihuPoemApi(data) {
  return request({ url: '/classics/shuihu/poem', method: 'put', data })
}

export function deleteShuihuPoemApi(id) {
  return request({ url: `/classics/shuihu/poem/${id}`, method: 'delete' })
}

export function batchDeleteShuihuPoemApi(ids) {
  return request({ url: '/classics/shuihu/poem/batch', method: 'delete', data: ids })
}

// ====== 水浒章节 ======

export function getShuihuChapterPageApi(params) {
  return request({ url: '/classics/shuihu/chapter/page', method: 'get', params })
}

/**
 * 水浒章节详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getShuihuChapterDetailApi(id) {
  return request({ url: `/classics/shuihu/chapter/${id}`, method: 'get' })
}

export function addShuihuChapterApi(data) {
  return request({ url: '/classics/shuihu/chapter', method: 'post', data })
}

export function updateShuihuChapterApi(data) {
  return request({ url: '/classics/shuihu/chapter', method: 'put', data })
}

export function deleteShuihuChapterApi(id) {
  return request({ url: `/classics/shuihu/chapter/${id}`, method: 'delete' })
}

export function batchDeleteShuihuChapterApi(ids) {
  return request({ url: '/classics/shuihu/chapter/batch', method: 'delete', data: ids })
}

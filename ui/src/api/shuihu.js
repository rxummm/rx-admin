import request from '@/utils/request'
import { API } from './routes'

// ====== 水浒诗词 ======

export function getShuihuPoemPageApi(params) {
  return request({ url: API.CLASSICS.SHUIHU.POEM.PAGE, method: 'get', params })
}

/**
 * 水浒诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getShuihuPoemDetailApi(id) {
  return request({ url: API.CLASSICS.SHUIHU.POEM.BY_ID(id), method: 'get' })
}

export function addShuihuPoemApi(data) {
  return request({ url: API.CLASSICS.SHUIHU.POEM.CRUD, method: 'post', data })
}

export function updateShuihuPoemApi(data) {
  return request({ url: API.CLASSICS.SHUIHU.POEM.CRUD, method: 'put', data })
}

export function deleteShuihuPoemApi(id) {
  return request({ url: API.CLASSICS.SHUIHU.POEM.BY_ID(id), method: 'delete' })
}

export function batchDeleteShuihuPoemApi(ids) {
  return request({ url: API.CLASSICS.SHUIHU.POEM.BATCH, method: 'delete', data: ids })
}

// ====== 水浒章节 ======

export function getShuihuChapterPageApi(params) {
  return request({ url: API.CLASSICS.SHUIHU.CHAPTER.PAGE, method: 'get', params })
}

/**
 * 水浒章节详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getShuihuChapterDetailApi(id) {
  return request({ url: API.CLASSICS.SHUIHU.CHAPTER.BY_ID(id), method: 'get' })
}

export function addShuihuChapterApi(data) {
  return request({ url: API.CLASSICS.SHUIHU.CHAPTER.CRUD, method: 'post', data })
}

export function updateShuihuChapterApi(data) {
  return request({ url: API.CLASSICS.SHUIHU.CHAPTER.CRUD, method: 'put', data })
}

export function deleteShuihuChapterApi(id) {
  return request({ url: API.CLASSICS.SHUIHU.CHAPTER.BY_ID(id), method: 'delete' })
}

export function batchDeleteShuihuChapterApi(ids) {
  return request({ url: API.CLASSICS.SHUIHU.CHAPTER.BATCH, method: 'delete', data: ids })
}

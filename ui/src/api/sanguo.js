import request from '@/utils/request'

// ====== 三国诗词 ======

export function getSanguoPoemPageApi(params) {
  return request({ url: '/classics/sanguo/poem/page', method: 'get', params })
}

/**
 * 三国诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getSanguoPoemDetailApi(id) {
  return request({ url: `/classics/sanguo/poem/${id}`, method: 'get' })
}

export function addSanguoPoemApi(data) {
  return request({ url: '/classics/sanguo/poem', method: 'post', data })
}

export function updateSanguoPoemApi(data) {
  return request({ url: '/classics/sanguo/poem', method: 'put', data })
}

export function deleteSanguoPoemApi(id) {
  return request({ url: `/classics/sanguo/poem/${id}`, method: 'delete' })
}

export function batchDeleteSanguoPoemApi(ids) {
  return request({ url: '/classics/sanguo/poem/batch', method: 'delete', data: ids })
}

// ====== 三国人物 ======

export function getSanguoCharacterPageApi(params) {
  return request({ url: '/classics/sanguo/character/page', method: 'get', params })
}

/**
 * 三国人物详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getSanguoCharacterDetailApi(id) {
  return request({ url: `/classics/sanguo/character/${id}`, method: 'get' })
}

export function getSanguoCharactersByCountryApi(country) {
  return request({ url: '/classics/sanguo/character/country', method: 'get', params: { country } })
}

export function addSanguoCharacterApi(data) {
  return request({ url: '/classics/sanguo/character', method: 'post', data })
}

export function updateSanguoCharacterApi(data) {
  return request({ url: '/classics/sanguo/character', method: 'put', data })
}

export function deleteSanguoCharacterApi(id) {
  return request({ url: `/classics/sanguo/character/${id}`, method: 'delete' })
}

export function batchDeleteSanguoCharacterApi(ids) {
  return request({ url: '/classics/sanguo/character/batch', method: 'delete', data: ids })
}

import request from '@/utils/request'

// ====== 西游诗词 ======

export function getXiyouPoemPageApi(params) {
  return request({ url: '/classics/xiyou/poem/page', method: 'get', params })
}

/**
 * 西游诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getXiyouPoemDetailApi(id) {
  return request({ url: `/classics/xiyou/poem/${id}`, method: 'get' })
}

export function addXiyouPoemApi(data) {
  return request({ url: '/classics/xiyou/poem', method: 'post', data })
}

export function updateXiyouPoemApi(data) {
  return request({ url: '/classics/xiyou/poem', method: 'put', data })
}

export function deleteXiyouPoemApi(id) {
  return request({ url: `/classics/xiyou/poem/${id}`, method: 'delete' })
}

export function batchDeleteXiyouPoemApi(ids) {
  return request({ url: '/classics/xiyou/poem/batch', method: 'delete', data: ids })
}

// ====== 西游人物 ======

export function getXiyouCharacterPageApi(params) {
  return request({ url: '/classics/xiyou/character/page', method: 'get', params })
}

/**
 * 西游人物详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getXiyouCharacterDetailApi(id) {
  return request({ url: `/classics/xiyou/character/${id}`, method: 'get' })
}

export function getXiyouCharactersByRaceApi(race) {
  return request({ url: '/classics/xiyou/character/race', method: 'get', params: { race } })
}

export function addXiyouCharacterApi(data) {
  return request({ url: '/classics/xiyou/character', method: 'post', data })
}

export function updateXiyouCharacterApi(data) {
  return request({ url: '/classics/xiyou/character', method: 'put', data })
}

export function deleteXiyouCharacterApi(id) {
  return request({ url: `/classics/xiyou/character/${id}`, method: 'delete' })
}

export function batchDeleteXiyouCharacterApi(ids) {
  return request({ url: '/classics/xiyou/character/batch', method: 'delete', data: ids })
}

// ====== 西游八十一难 ======

export function getXiyouEventAllApi() {
  return request({ url: '/classics/xiyou/event/list/all', method: 'get' })
}

export function getXiyouEventPageApi(params) {
  return request({ url: '/classics/xiyou/event/page', method: 'get', params })
}

/**
 * 西游八十一难详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getXiyouEventDetailApi(id) {
  return request({ url: `/classics/xiyou/event/${id}`, method: 'get' })
}

export function addXiyouEventApi(data) {
  return request({ url: '/classics/xiyou/event', method: 'post', data })
}

export function updateXiyouEventApi(data) {
  return request({ url: '/classics/xiyou/event', method: 'put', data })
}

export function deleteXiyouEventApi(id) {
  return request({ url: `/classics/xiyou/event/${id}`, method: 'delete' })
}

export function batchDeleteXiyouEventApi(ids) {
  return request({ url: '/classics/xiyou/event/batch', method: 'delete', data: ids })
}

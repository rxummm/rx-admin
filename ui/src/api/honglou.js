import request from '@/utils/request'

// ====== 红楼诗词 ======

export function getHonglouPoemPageApi(params) {
  return request({ url: '/classics/honglou/poem/page', method: 'get', params })
}

/**
 * 红楼诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getHonglouPoemDetailApi(id) {
  return request({ url: `/classics/honglou/poem/${id}`, method: 'get' })
}

export function addHonglouPoemApi(data) {
  return request({ url: '/classics/honglou/poem', method: 'post', data })
}

export function updateHonglouPoemApi(data) {
  return request({ url: '/classics/honglou/poem', method: 'put', data })
}

export function deleteHonglouPoemApi(id) {
  return request({ url: `/classics/honglou/poem/${id}`, method: 'delete' })
}

export function batchDeleteHonglouPoemApi(ids) {
  return request({ url: '/classics/honglou/poem/batch', method: 'delete', data: ids })
}

// ====== 红楼人物 ======

export function getHonglouCharacterPageApi(params) {
  return request({ url: '/classics/honglou/character/page', method: 'get', params })
}

/**
 * 红楼人物详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getHonglouCharacterDetailApi(id) {
  return request({ url: `/classics/honglou/character/${id}`, method: 'get' })
}

export function getHonglouCharactersByRoleApi(role) {
  return request({ url: '/classics/honglou/character/role', method: 'get', params: { role } })
}

export function addHonglouCharacterApi(data) {
  return request({ url: '/classics/honglou/character', method: 'post', data })
}

export function updateHonglouCharacterApi(data) {
  return request({ url: '/classics/honglou/character', method: 'put', data })
}

export function deleteHonglouCharacterApi(id) {
  return request({ url: `/classics/honglou/character/${id}`, method: 'delete' })
}

export function batchDeleteHonglouCharacterApi(ids) {
  return request({ url: '/classics/honglou/character/batch', method: 'delete', data: ids })
}

// ====== 红楼人物关系 ======

export function getHonglouRelationApi(characterId) {
  return request({ url: `/classics/honglou/relation/${characterId}`, method: 'get' })
}

export function addHonglouRelationApi(data) {
  return request({ url: '/classics/honglou/relation', method: 'post', data })
}

export function updateHonglouRelationApi(data) {
  return request({ url: '/classics/honglou/relation', method: 'put', data })
}

export function deleteHonglouRelationApi(id) {
  return request({ url: `/classics/honglou/relation/${id}`, method: 'delete' })
}

// ====== 红楼人物关系图专用 ======

export function getAllHonglouCharactersApi() {
  return request({ url: '/classics/honglou/character/all', method: 'get' })
}

export function getAllHonglouRelationsApi() {
  return request({ url: '/classics/honglou/relation/all', method: 'get' })
}

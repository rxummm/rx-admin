import request from '@/utils/request'
import { API } from './routes'

// ====== 红楼诗词 ======

export function getHonglouPoemPageApi(params) {
  return request({ url: API.CLASSICS.HONGLOU.POEM.PAGE, method: 'get', params })
}

/**
 * 红楼诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getHonglouPoemDetailApi(id) {
  return request({ url: API.CLASSICS.HONGLOU.POEM.BY_ID(id), method: 'get' })
}

export function addHonglouPoemApi(data) {
  return request({ url: API.CLASSICS.HONGLOU.POEM.CRUD, method: 'post', data })
}

export function updateHonglouPoemApi(data) {
  return request({ url: API.CLASSICS.HONGLOU.POEM.CRUD, method: 'put', data })
}

export function deleteHonglouPoemApi(id) {
  return request({ url: API.CLASSICS.HONGLOU.POEM.BY_ID(id), method: 'delete' })
}

export function batchDeleteHonglouPoemApi(ids) {
  return request({ url: API.CLASSICS.HONGLOU.POEM.BATCH, method: 'delete', data: ids })
}

// ====== 红楼人物 ======

export function getHonglouCharacterPageApi(params) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.PAGE, method: 'get', params })
}

/**
 * 红楼人物详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getHonglouCharacterDetailApi(id) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.BY_ID(id), method: 'get' })
}

export function getHonglouCharactersByRoleApi(role) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.BY_ROLE, method: 'get', params: { role } })
}

export function addHonglouCharacterApi(data) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.CRUD, method: 'post', data })
}

export function updateHonglouCharacterApi(data) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.CRUD, method: 'put', data })
}

export function deleteHonglouCharacterApi(id) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.BY_ID(id), method: 'delete' })
}

export function batchDeleteHonglouCharacterApi(ids) {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.BATCH, method: 'delete', data: ids })
}

// ====== 红楼人物关系 ======

export function getHonglouRelationApi(characterId) {
  return request({ url: API.CLASSICS.HONGLOU.RELATION.BY_CHARACTER(characterId), method: 'get' })
}

export function addHonglouRelationApi(data) {
  return request({ url: API.CLASSICS.HONGLOU.RELATION.CRUD, method: 'post', data })
}

export function updateHonglouRelationApi(data) {
  return request({ url: API.CLASSICS.HONGLOU.RELATION.CRUD, method: 'put', data })
}

export function deleteHonglouRelationApi(id) {
  return request({ url: API.CLASSICS.HONGLOU.RELATION.BY_ID(id), method: 'delete' })
}

// ====== 红楼人物关系图专用 ======

export function getAllHonglouCharactersApi() {
  return request({ url: API.CLASSICS.HONGLOU.CHARACTER.ALL, method: 'get' })
}

export function getAllHonglouRelationsApi() {
  return request({ url: API.CLASSICS.HONGLOU.RELATION.ALL, method: 'get' })
}

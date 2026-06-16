import request from '@/utils/request'
import { API } from './routes'

// ====== 西游诗词 ======

export function getXiyouPoemPageApi(params) {
  return request({ url: API.CLASSICS.XIYOU.POEM.PAGE, method: 'get', params })
}

/**
 * 西游诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getXiyouPoemDetailApi(id) {
  return request({ url: API.CLASSICS.XIYOU.POEM.BY_ID(id), method: 'get' })
}

export function addXiyouPoemApi(data) {
  return request({ url: API.CLASSICS.XIYOU.POEM.CRUD, method: 'post', data })
}

export function updateXiyouPoemApi(data) {
  return request({ url: API.CLASSICS.XIYOU.POEM.CRUD, method: 'put', data })
}

export function deleteXiyouPoemApi(id) {
  return request({ url: API.CLASSICS.XIYOU.POEM.BY_ID(id), method: 'delete' })
}

export function batchDeleteXiyouPoemApi(ids) {
  return request({ url: API.CLASSICS.XIYOU.POEM.BATCH, method: 'delete', data: ids })
}

// ====== 西游人物 ======

export function getXiyouCharacterPageApi(params) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.PAGE, method: 'get', params })
}

/**
 * 西游人物详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getXiyouCharacterDetailApi(id) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.BY_ID(id), method: 'get' })
}

export function getXiyouCharactersByRaceApi(race) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.BY_RACE, method: 'get', params: { race } })
}

export function addXiyouCharacterApi(data) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.CRUD, method: 'post', data })
}

export function updateXiyouCharacterApi(data) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.CRUD, method: 'put', data })
}

export function deleteXiyouCharacterApi(id) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.BY_ID(id), method: 'delete' })
}

export function batchDeleteXiyouCharacterApi(ids) {
  return request({ url: API.CLASSICS.XIYOU.CHARACTER.BATCH, method: 'delete', data: ids })
}

// ====== 西游八十一难 ======

export function getXiyouEventAllApi() {
  return request({ url: API.CLASSICS.XIYOU.EVENT.ALL, method: 'get' })
}

export function getXiyouEventPageApi(params) {
  return request({ url: API.CLASSICS.XIYOU.EVENT.PAGE, method: 'get', params })
}

/**
 * 西游八十一难详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getXiyouEventDetailApi(id) {
  return request({ url: API.CLASSICS.XIYOU.EVENT.BY_ID(id), method: 'get' })
}

export function addXiyouEventApi(data) {
  return request({ url: API.CLASSICS.XIYOU.EVENT.CRUD, method: 'post', data })
}

export function updateXiyouEventApi(data) {
  return request({ url: API.CLASSICS.XIYOU.EVENT.CRUD, method: 'put', data })
}

export function deleteXiyouEventApi(id) {
  return request({ url: API.CLASSICS.XIYOU.EVENT.BY_ID(id), method: 'delete' })
}

export function batchDeleteXiyouEventApi(ids) {
  return request({ url: API.CLASSICS.XIYOU.EVENT.BATCH, method: 'delete', data: ids })
}

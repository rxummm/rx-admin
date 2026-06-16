import request from '@/utils/request'
import { API } from './routes'

// ====== 三国诗词 ======

export function getSanguoPoemPageApi(params) {
  return request({ url: API.CLASSICS.SANGUO.POEM.PAGE, method: 'get', params })
}

/**
 * 三国诗词详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getSanguoPoemDetailApi(id) {
  return request({ url: API.CLASSICS.SANGUO.POEM.BY_ID(id), method: 'get' })
}

export function addSanguoPoemApi(data) {
  return request({ url: API.CLASSICS.SANGUO.POEM.CRUD, method: 'post', data })
}

export function updateSanguoPoemApi(data) {
  return request({ url: API.CLASSICS.SANGUO.POEM.CRUD, method: 'put', data })
}

export function deleteSanguoPoemApi(id) {
  return request({ url: API.CLASSICS.SANGUO.POEM.BY_ID(id), method: 'delete' })
}

export function batchDeleteSanguoPoemApi(ids) {
  return request({ url: API.CLASSICS.SANGUO.POEM.BATCH, method: 'delete', data: ids })
}

// ====== 三国人物 ======

export function getSanguoCharacterPageApi(params) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.PAGE, method: 'get', params })
}

/**
 * 三国人物详情
 * @reserved 预留接口，当前详情在弹窗中直接展示
 */
export function getSanguoCharacterDetailApi(id) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.BY_ID(id), method: 'get' })
}

export function getSanguoCharactersByCountryApi(country) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.BY_COUNTRY, method: 'get', params: { country } })
}

export function addSanguoCharacterApi(data) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.CRUD, method: 'post', data })
}

export function updateSanguoCharacterApi(data) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.CRUD, method: 'put', data })
}

export function deleteSanguoCharacterApi(id) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.BY_ID(id), method: 'delete' })
}

export function batchDeleteSanguoCharacterApi(ids) {
  return request({ url: API.CLASSICS.SANGUO.CHARACTER.BATCH, method: 'delete', data: ids })
}

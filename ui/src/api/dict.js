import request from '@/utils/request'
import { API } from './routes'

// 字典类型
export function getDictTypePageApi(params) {
  return request({ url: API.SYS.DICT.TYPE.PAGE, method: 'get', params })
}

export function addDictTypeApi(data) {
  return request({ url: API.SYS.DICT.TYPE.CRUD, method: 'post', data })
}

export function updateDictTypeApi(data) {
  return request({ url: API.SYS.DICT.TYPE.CRUD, method: 'put', data })
}

export function deleteDictTypeApi(id) {
  return request({ url: API.SYS.DICT.TYPE.BY_ID(id), method: 'delete' })
}

/**
 * 根据ID查询字典类型详情
 * @reserved 预留接口
 */
export function getDictTypeByIdApi(id) {
  return request({ url: API.SYS.DICT.TYPE.BY_ID(id), method: 'get' })
}

// 字典数据
export function getDictDataByTypeApi(typeId) {
  return request({ url: API.SYS.DICT.DATA.LIST_BY_TYPE(typeId), method: 'get' })
}

export function addDictDataApi(data) {
  return request({ url: API.SYS.DICT.DATA.CRUD, method: 'post', data })
}

export function updateDictDataApi(data) {
  return request({ url: API.SYS.DICT.DATA.CRUD, method: 'put', data })
}

export function deleteDictDataApi(id) {
  return request({ url: API.SYS.DICT.DATA.BY_ID(id), method: 'delete' })
}

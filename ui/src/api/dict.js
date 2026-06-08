import request from '@/utils/request'

// 字典类型
export function getDictTypePageApi(params) {
  return request({ url: '/sys/dict/type/page', method: 'get', params })
}

export function addDictTypeApi(data) {
  return request({ url: '/sys/dict/type', method: 'post', data })
}

export function updateDictTypeApi(data) {
  return request({ url: '/sys/dict/type', method: 'put', data })
}

export function deleteDictTypeApi(id) {
  return request({ url: `/sys/dict/type/${id}`, method: 'delete' })
}

/**
 * 根据ID查询字典类型详情
 * @reserved 预留接口
 */
export function getDictTypeByIdApi(id) {
  return request({ url: `/sys/dict/type/${id}`, method: 'get' })
}

// 字典数据
export function getDictDataByTypeApi(typeId) {
  return request({ url: `/sys/dict/data/list/${typeId}`, method: 'get' })
}

export function addDictDataApi(data) {
  return request({ url: '/sys/dict/data', method: 'post', data })
}

export function updateDictDataApi(data) {
  return request({ url: '/sys/dict/data', method: 'put', data })
}

export function deleteDictDataApi(id) {
  return request({ url: `/sys/dict/data/${id}`, method: 'delete' })
}

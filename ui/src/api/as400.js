import request from '@/utils/request'
import { API } from './routes'

// AS400 连接远程服务器，超时通过 VITE_AS400_REQUEST_TIMEOUT 配置（默认 60 秒）
const AS400_TIMEOUT = Number(import.meta.env.VITE_AS400_REQUEST_TIMEOUT) || 60000

/**
 * 查询指定 Library 下的所有 Object
 */
export function getAs400ObjectsByLibApi(library) {
  return request({
    url: API.AS400.OBJECTS_BY_LIB(library),
    method: 'get',
    timeout: AS400_TIMEOUT
  })
}

/**
 * 查询所有 Library 下的 Object（默认 A7RXUZZ1,A7RXUZZ2,A7RXUZZB）
 */
export function getAs400ObjectsApi(libraries) {
  return request({
    url: API.AS400.OBJECTS,
    method: 'get',
    params: { libraries },
    timeout: AS400_TIMEOUT
  })
}

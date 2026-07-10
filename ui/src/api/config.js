import request from '@/utils/request'
import { API } from './routes'

export function getConfigGrouped() {
  return request({
    url: API.SYS.CONFIG.GROUPED,
    method: 'get'
  })
}

export function getConfigValue(key) {
  return request({
    url: API.SYS.CONFIG.VALUE_BY_KEY(key),
    method: 'get'
  })
}

export function getConfigValues(keys) {
  return request({
    url: API.SYS.CONFIG.VALUES,
    method: 'post',
    data: keys
  })
}

export function updateConfigValue(key, value) {
  return request({
    url: API.SYS.CONFIG.VALUE_BY_KEY(key),
    method: 'put',
    data: { value }
  })
}

export function addConfig(data) {
  return request({
    url: API.SYS.CONFIG.CRUD,
    method: 'post',
    data
  })
}

export function updateConfig(data) {
  return request({
    url: API.SYS.CONFIG.CRUD,
    method: 'put',
    data
  })
}

export function deleteConfig(id) {
  return request({
    url: API.SYS.CONFIG.BY_ID(id),
    method: 'delete'
  })
}

export async function getConfigPage(params = {}) {
  const res = await getConfigGrouped()
  const allConfigs = Object.values(res.data || {}).flat()
  let filtered = allConfigs
  if (params.configName) {
    const kw = params.configName.toLowerCase()
    filtered = allConfigs.filter(
      (c) => (c.configName || '').toLowerCase().includes(kw) || (c.configKey || '').toLowerCase().includes(kw)
    )
  }
  return {
    data: {
      records: filtered.slice(0, params.pageSize || 10),
      total: filtered.length
    }
  }
}

export default {
  getConfigGrouped,
  getConfigValue,
  getConfigValues,
  updateConfigValue,
  addConfig,
  updateConfig,
  deleteConfig,
  getConfigPage
}

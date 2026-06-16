import request from '@/utils/request'
import { API } from './routes'

export const getIpRulePageApi = (params) => request.get(API.SYSTEM.IP_RULE.PAGE, { params })
export const getIpRuleByIdApi = (id) => request.get(API.SYSTEM.IP_RULE.BY_ID(id))
export const addIpRuleApi = (data) => request.post(API.SYSTEM.IP_RULE.CRUD, data)
export const updateIpRuleApi = (data) => request.put(API.SYSTEM.IP_RULE.CRUD, data)
export const deleteIpRuleApi = (id) => request.delete(API.SYSTEM.IP_RULE.BY_ID(id))
export const getIpRuleModeApi = () => request.get(API.SYSTEM.IP_RULE.MODE_GET)
export const setIpRuleModeApi = (mode) => request.put(API.SYSTEM.IP_RULE.MODE_SET, { mode })

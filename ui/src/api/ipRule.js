import request from '@/utils/request'

export const getIpRulePageApi = (params) => request.get('/system/ip-rule/page', { params })
export const getIpRuleByIdApi = (id) => request.get(`/system/ip-rule/${id}`)
export const addIpRuleApi = (data) => request.post('/system/ip-rule', data)
export const updateIpRuleApi = (data) => request.put('/system/ip-rule', data)
export const deleteIpRuleApi = (id) => request.delete(`/system/ip-rule/${id}`)
export const getIpRuleModeApi = () => request.get('/system/ip-rule/mode')
export const setIpRuleModeApi = (mode) => request.put('/system/ip-rule/mode', { mode })

import request from '@/utils/request'

export const getMessagePageApi = (params) => request.get('/content/message/page', { params })
export const getUnreadCountApi = (config) => request.get('/content/message/unread-count', config)
export const markAsReadApi = (id) => request.put(`/content/message/${id}/read`)
export const markAllReadApi = () => request.put('/content/message/read-all')
export const deleteMessageApi = (id) => request.delete(`/content/message/${id}`)

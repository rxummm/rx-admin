import request from '@/utils/request'
import { API } from './routes'

export const getMessagePageApi = (params) => request.get(API.CONTENT.MESSAGE.PAGE, { params })
export const getUnreadCountApi = (config) => request.get(API.CONTENT.MESSAGE.UNREAD_COUNT, config)
export const markAsReadApi = (id) => request.put(API.CONTENT.MESSAGE.MARK_READ(id))
export const markAllReadApi = () => request.put(API.CONTENT.MESSAGE.MARK_ALL_READ)
export const deleteMessageApi = (id) => request.delete(API.CONTENT.MESSAGE.BY_ID(id))

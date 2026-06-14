import request from '@/utils/request'
import { API } from './routes'

export const getPopupAnnouncementsApi = () => request.get(API.CONTENT.ANNOUNCEMENT.POPUP)
export const markAnnouncementReadApi = (noticeId) => request.post(API.CONTENT.ANNOUNCEMENT.READ(noticeId))

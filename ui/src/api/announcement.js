import request from '@/utils/request'

export const getPopupAnnouncementsApi = () => request.get('/content/announcement/popup')
export const markAnnouncementReadApi = (noticeId) => request.post(`/content/announcement/read/${noticeId}`)

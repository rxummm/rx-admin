import request from '@/utils/request'

export const getBackupListApi = () => request.get('/tool/backup/list')
export const createBackupApi = () => request.post('/tool/backup/create')
export const deleteBackupApi = (filename) => request.delete(`/tool/backup/${filename}`)
export const downloadBackupApi = (filename) => request.get(`/tool/backup/download/${filename}`, { responseType: 'blob' })

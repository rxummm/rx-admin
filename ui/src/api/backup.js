import request from '@/utils/request'
import { API } from './routes'

export const getBackupListApi = () => request.get(API.TOOL.BACKUP.LIST)
export const createBackupApi = () => request.post(API.TOOL.BACKUP.CREATE)
export const deleteBackupApi = (filename) => request.delete(API.TOOL.BACKUP.BY_NAME(filename))
export const downloadBackupApi = (filename) => request.get(API.TOOL.BACKUP.DOWNLOAD(filename), { responseType: 'blob' })

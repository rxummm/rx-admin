import request from '@/utils/request'
import { API } from './routes'

export function executeSqlApi(data) { return request.post(API.TOOL.DATABASE.EXECUTE, data) }
export function getTablesApi() { return request.get(API.TOOL.DATABASE.TABLES) }
export function getTableColumnsApi(tableName) { return request.get(API.TOOL.DATABASE.TABLE_COLUMNS(tableName)) }
export function getPoolStatusApi() { return request.get(API.TOOL.DATABASE.POOL_STATUS) }

import request from '@/utils/request'

export function executeSqlApi(data) { return request.post('/tool/database/execute', data) }
export function getTablesApi() { return request.get('/tool/database/tables') }
export function getTableColumnsApi(tableName) { return request.get(`/tool/database/tables/${encodeURIComponent(tableName)}/columns`) }
export function getPoolStatusApi() { return request.get('/tool/database/pool-status') }

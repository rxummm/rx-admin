/**
 * 全局工具函数
 */

/**
 * 格式化 ISO 时间字符串
 * 将 2026-05-30T18:18:07 格式转为 2026-05-30 18:18:07
 */
export function formatTime(val) {
  if (!val) return '-'
  return String(val).replace('T', ' ').substring(0, 19)
}

/**
 * 浅层处理响应数据中的时间字段
 * 后端 Jackson 已配置 yyyy-MM-dd HH:mm:ss，通常不需要额外处理
 * 此处仅作为兜底处理：如果仍有 T 分隔符，则替换为空格
 *
 * 相比旧版深拷贝递归，性能大幅提升：
 * - 不再对全量数据做深层递归拷贝
 * - 仅对 records 数组中的顶层字段做浅层遍历
 */
const TIME_FIELDS = ['createTime', 'updateTime', 'createdTime', 'updatedTime', 'operateTime', 'loginTime']

export function formatResponseData(data) {
  if (!data || typeof data !== 'object') return data

  // 仅处理 records 数组中的时间字段（最常见场景），不做深拷贝
  if (data.data && Array.isArray(data.data.records)) {
    const records = data.data.records
    for (const record of records) {
      if (!record || typeof record !== 'object') continue
      for (const key of TIME_FIELDS) {
        if (typeof record[key] === 'string' && record[key].includes('T')) {
          record[key] = record[key].replace('T', ' ')
        }
      }
    }
  }

  return data
}

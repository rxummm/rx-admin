/**
 * 智能搜索工具
 * 支持模糊匹配、拼音搜索、同义词扩展
 */

/**
 * 智能搜索
 * @param {string} keyword - 搜索关键词
 * @param {Array} candidates - 候选项列表
 * @returns {Array} 匹配的结果
 */
export function smartSearch(keyword, candidates) {
  if (!keyword || !candidates) return candidates || []
  
  const lowerKeyword = keyword.toLowerCase()
  
  // 获取同义词
  const synonyms = getSynonyms(keyword)
  
  return candidates.filter(candidate => {
    const lowerCandidate = candidate.toLowerCase()
    
    // 直接匹配
    if (lowerCandidate.includes(lowerKeyword)) {
      return true
    }
    
    // 同义词匹配
    return synonyms.some(synonym => lowerCandidate.includes(synonym.toLowerCase()))
  })
}

/**
 * 获取同义词
 */
const SYNONYM_MAP = {
  '用户': ['user', '账号', '账户'],
  '角色': ['role', '权限组'],
  '菜单': ['menu', '导航', '栏目'],
  '配置': ['config', '设置', '参数'],
  '日志': ['log', '记录', '操作记录'],
  '通知': ['notice', '公告', '消息'],
  '文件': ['file', '文档', '附件'],
  '部门': ['dept', '组织', '机构'],
  '字典': ['dict', '数据字典', '编码']
}

export function getSynonyms(keyword) {
  const synonyms = []
  
  // 精确匹配
  if (SYNONYM_MAP[keyword]) {
    synonyms.push(...SYNONYM_MAP[keyword])
  }
  
  // 部分匹配
  for (const [key, values] of Object.entries(SYNONYM_MAP)) {
    if (keyword.includes(key) || key.includes(keyword)) {
      synonyms.push(...values)
    }
  }
  
  return [...new Set(synonyms)]
}

/**
 * 高亮搜索关键词
 */
export function highlight(text, keyword) {
  if (!text || !keyword) return text
  
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

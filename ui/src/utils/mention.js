/**
 * @提及解析工具
 * 用于在前端解析文本中的@提及
 */

/**
 * 解析文本中的@提及
 * @param {string} text - 输入文本
 * @returns {Array} 提及的用户名列表
 */
export function parseMentions(text) {
  if (!text) return []
  
  const mentions = []
  const regex = /@(\w+)/g
  let match
  
  while ((match = regex.exec(text)) !== null) {
    const username = match[1]
    if (!mentions.includes(username)) {
      mentions.push(username)
    }
  }
  
  return mentions
}

/**
 * 高亮文本中的@提及
 * @param {string} text - 输入文本
 * @returns {string} 高亮后的HTML
 */
export function highlightMentions(text) {
  if (!text) return ''
  
  return text.replace(/@(\w+)/g, '<span class="mention">@$1</span>')
}

/**
 * 检查文本中是否包含@提及
 * @param {string} text - 输入文本
 * @returns {boolean}
 */
export function hasMentions(text) {
  if (!text) return false
  return /@\w+/.test(text)
}

/**
 * HTML 安全过滤工具（基于 DOMPurify）
 * 对所有 v-html 绑定的内容进行 XSS 防护
 */
import DOMPurify from 'dompurify'

/** 允许的安全 HTML 标签（白名单） */
const ALLOWED_TAGS = [
  'p', 'div', 'span', 'br', 'hr',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'ul', 'ol', 'li', 'dl', 'dt', 'dd',
  'a', 'img', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
  'code', 'pre', 'blockquote',
  'strong', 'em', 'b', 'i', 'u', 's', 'del', 'sub', 'sup',
  'mark', 'small',
]

/** 允许的安全属性 */
const ALLOWED_ATTR = ['href', 'src', 'alt', 'title', 'class', 'id', 'target', 'rel']

/**
 * 对 HTML 字符串进行 XSS 安全过滤
 * @param {string} html - 原始 HTML 字符串
 * @returns {string} 安全的 HTML 字符串
 */
export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS,
    ALLOWED_ATTR,
  })
}

export default sanitizeHtml

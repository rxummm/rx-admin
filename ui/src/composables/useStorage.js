/**
 * 统一 localStorage 管理
 * 提供带命名空间的 key 管理、类型安全和错误处理
 * Token 使用 Base64+异或混淆存储，防止明文泄露
 */

const STORAGE_KEYS = {
  TOKEN: 'rx_admin_token',
  USER_INFO: 'rx_admin_userInfo',
  ROLES: 'rx_admin_roles',
  PERMS: 'rx_admin_perms',
  MENUS: 'rx_admin_menus',
  LOCALE: 'rx_admin_locale',
  THEME: 'rx_admin_theme',
  READ_NOTICE_IDS: 'rx_admin_readNoticeIds',
}

/** Token 存储时使用的异或密钥（仅防明文泄露，非加密） */
const TOKEN_XOR_KEY = 0xA3

/**
 * 对 Token 进行混淆编码（Base64 + XOR）
 */
function encodeToken(raw) {
  if (!raw) return raw
  const bytes = new TextEncoder().encode(raw)
  const xored = bytes.map(b => b ^ TOKEN_XOR_KEY)
  return btoa(String.fromCharCode(...xored))
}

/**
 * 解码混淆的 Token
 */
function decodeToken(encoded) {
  if (!encoded) return encoded
  try {
    const xored = Uint8Array.from(atob(encoded), c => c.charCodeAt(0))
    const bytes = xored.map(b => b ^ TOKEN_XOR_KEY)
    return new TextDecoder().decode(bytes)
  } catch {
    return encoded // 兼容旧版明文 token
  }
}

/**
 * 安全地从 localStorage 读取值
 */
function safeGetItem(key) {
  try {
    return localStorage.getItem(key)
  } catch (e) {
    console.warn(`[useStorage] 读取 ${key} 失败:`, e)
    return null
  }
}

/**
 * 安全地向 localStorage 写入值
 */
function safeSetItem(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch (e) {
    console.warn(`[useStorage] 写入 ${key} 失败:`, e)
  }
}

/**
 * 安全地删除 localStorage 中的值
 */
function safeRemoveItem(key) {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    console.warn(`[useStorage] 删除 ${key} 失败:`, e)
  }
}

/**
 * 统一 localStorage 操作 composable
 *
 * @example
 * const { get, set, remove } = useStorage(STORAGE_KEYS.TOKEN)
 * set('my-token-value')
 * const token = get()
 * remove()
 */
export function useStorage(key, defaultValue = null) {
  function get() {
    const raw = safeGetItem(key)
    if (raw === null) return defaultValue
    try {
      return JSON.parse(raw)
    } catch {
      return key === STORAGE_KEYS.TOKEN ? decodeToken(raw) : raw
    }
  }

  function set(value) {
    if (value === null || value === undefined) {
      safeRemoveItem(key)
    } else if (typeof value === 'string') {
      safeSetItem(key, key === STORAGE_KEYS.TOKEN && value ? encodeToken(value) : value)
    } else {
      safeSetItem(key, JSON.stringify(value))
    }
  }

  function remove() {
    safeRemoveItem(key)
  }

  return { get, set, remove }
}

export { STORAGE_KEYS }

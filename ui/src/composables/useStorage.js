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
  // 业务 key：之前散落在各组件里直接用 localStorage.xxx 调用，无命名空间、易冲突。
  // 集中到 STORAGE_KEYS 后统一带 rx_admin_ 前缀、可观测、可清理。
  ANNOUNCEMENT_DISMISSED: 'rx_admin_announcement_dismissed',
  FAVORITES: 'rx_admin_favorites',
  FAVORITE_STAR: 'rx_admin_favorite_star',  // 旧 key fav_${path} 的标准化替代
  SEARCH_HISTORY: 'rx_admin_searchHistory',
  TAGS_VIEW: 'rx_admin_tagsView',
  EMAIL_DRAFT: 'rx_admin_emailDraft',
  EMAIL_SIGNATURE: 'rx_admin_email_signature',
  EMOJI_RECENT: 'rx_admin_emoji_recent',
  APP_ERRORS: 'rx_admin_app_errors',
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

/**
 * 命名空间 key 工厂：在基 key 上拼接动态后缀（如 path/userId），统一带命名空间。
 * 用于"每个 path 单独存一个值"这类场景，替代散落的 `fav_${path}` 拼接。
 *
 * @example
 * const favKey = useNamespacedKey(STORAGE_KEYS.FAVORITE_STAR, props.path)
 * const store = useStorage(favKey)
 * store.get(); store.set(id); store.remove()
 */
export function useNamespacedKey(baseKey, suffix) {
  // 过滤后缀里可能带的前缀 rx_admin_ / 特殊字符，确保最终 key 形态一致
  const safeSuffix = String(suffix).replace(/[^a-zA-Z0-9_\-:./]/g, '_')
  return `${baseKey}:${safeSuffix}`
}

/**
 * 按前缀遍历所有 localStorage key
 * 用于需要枚举命名空间 key 的场景（如收藏夹路径列表）
 * 替代直接访问 localStorage.length/key(i)
 */
export function getKeysByPrefix(prefix) {
  const keys = []
  try {
    for (let i = 0; i < localStorage.length; i++) {
      const k = localStorage.key(i)
      if (k && k.startsWith(prefix)) {
        keys.push(k)
      }
    }
  } catch (e) {
    console.warn('[useStorage] 遍历 key 失败:', e)
  }
  return keys
}

/**
 * 安全批量删除匹配前缀的 key
 */
export function removeKeysByPrefix(prefix) {
  const keys = getKeysByPrefix(prefix)
  keys.forEach(k => safeRemoveItem(k))
}

export { STORAGE_KEYS }

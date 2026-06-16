/**
 * 全局智能搜索服务
 * 支持菜单、用户、角色、配置等数据的模糊搜索
 */

import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserPage } from '@/api/user'
import { getRolePage } from '@/api/role'
import { getConfigPage } from '@/api/config'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

// 搜索结果类型
export const SEARCH_TYPES = {
  MENU: 'menu',
  USER: 'user',
  ROLE: 'role',
  CONFIG: 'config',
  ACTION: 'action'
}

// 搜索历史记录（统一用 useStorage 封装，避免散落 localStorage 调用）
const searchHistoryStore = useStorage(STORAGE_KEYS.SEARCH_HISTORY, [])
const searchHistory = ref(searchHistoryStore.get() || [])
const MAX_HISTORY = 5

/**
 * 执行全局搜索
 * @param {string} keyword - 搜索关键词
 * @returns {Promise<Array>} 搜索结果数组
 */
export async function globalSearch(keyword) {
  if (!keyword || keyword.trim().length === 0) {
    return []
  }

  const results = []
  const userStore = useUserStore()

  // 1. 搜索菜单（已有）
  const menuResults = searchMenus(keyword, userStore.menus)
  results.push(...menuResults)

  // 2. 搜索用户
  try {
    const userResults = await searchUsers(keyword)
    results.push(...userResults)
  } catch (e) {
    console.warn('用户搜索失败:', e)
  }

  // 3. 搜索角色
  try {
    const roleResults = await searchRoles(keyword)
    results.push(...roleResults)
  } catch (e) {
    console.warn('角色搜索失败:', e)
  }

  // 4. 搜索系统配置
  try {
    const configResults = await searchConfigs(keyword)
    results.push(...configResults)
  } catch (e) {
    console.warn('配置搜索失败:', e)
  }

  // 按相关性排序
  return sortResults(results, keyword)
}

/**
 * 搜索菜单（本地搜索）
 */
function searchMenus(keyword, menus) {
  const results = []
  const lowerKeyword = keyword.toLowerCase()

  function traverse(menuList) {
    menuList.forEach(menu => {
      const nameMatch = menu.name?.toLowerCase().includes(lowerKeyword)
      const pathMatch = menu.path?.toLowerCase().includes(lowerKeyword)

      if (nameMatch || pathMatch) {
        results.push({
          type: SEARCH_TYPES.MENU,
          id: menu.id,
          name: menu.name,
          path: menu.path,
          icon: menu.icon || 'Menu',
          score: calculateScore(menu.name, keyword)
        })
      }

      // 递归搜索子菜单
      if (menu.children && menu.children.length > 0) {
        traverse(menu.children)
      }
    })
  }

  traverse(menus)
  return results
}

/**
 * 搜索用户（API 搜索）
 */
async function searchUsers(keyword) {
  const res = await getUserPage({
    page: 1,
    pageSize: 10,
    keyword: keyword
  })

  return (res.data?.records || []).map(user => ({
    type: SEARCH_TYPES.USER,
    id: user.id,
    name: user.nickname || user.username,
    subtitle: `用户名: ${user.username}`,
    path: `/system/user?id=${user.id}`,
    icon: 'User',
    score: calculateScore(user.nickname || user.username, keyword)
  }))
}

/**
 * 搜索角色（API 搜索）
 */
async function searchRoles(keyword) {
  const res = await getRolePage({
    page: 1,
    pageSize: 10,
    roleName: keyword
  })

  return (res.data?.records || []).map(role => ({
    type: SEARCH_TYPES.ROLE,
    id: role.id,
    name: role.roleName,
    subtitle: `标识: ${role.roleKey}`,
    path: `/system/role?id=${role.id}`,
    icon: 'UserFilled',
    score: calculateScore(role.roleName, keyword)
  }))
}

/**
 * 搜索系统配置（API 搜索）
 */
async function searchConfigs(keyword) {
  const res = await getConfigPage({
    page: 1,
    pageSize: 10,
    configName: keyword
  })

  return (res.data?.records || []).map(config => ({
    type: SEARCH_TYPES.CONFIG,
    id: config.id,
    name: config.configName,
    subtitle: `键: ${config.configKey}`,
    path: `/system/config?id=${config.id}`,
    icon: 'Setting',
    score: calculateScore(config.configName, keyword)
  }))
}

/**
 * 计算搜索相关性分数
 */
function calculateScore(text, keyword) {
  if (!text) return 0

  const lowerText = text.toLowerCase()
  const lowerKeyword = keyword.toLowerCase()

  // 完全匹配得分最高
  if (lowerText === lowerKeyword) return 100

  // 开头匹配得分较高
  if (lowerText.startsWith(lowerKeyword)) return 80

  // 包含匹配
  if (lowerText.includes(lowerKeyword)) return 60

  // 分词匹配
  const words = lowerKeyword.split(/\s+/)
  const matchCount = words.filter(word => lowerText.includes(word)).length
  return matchCount * 20
}

/**
 * 按分数排序结果
 */
function sortResults(results, keyword) {
  return results
    .sort((a, b) => b.score - a.score)
    .slice(0, 20) // 最多返回 20 条结果
}

/**
 * 添加搜索历史
 */
export function addToHistory(keyword) {
  if (!keyword || keyword.trim().length === 0) return

  // 移除重复项
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }

  // 添加到开头
  searchHistory.value.unshift(keyword)

  // 限制历史记录数量
  if (searchHistory.value.length > MAX_HISTORY) {
    searchHistory.value.pop()
  }

  // 持久化到 localStorage（通过 useStorage 封装，自动带命名空间与异常兜底）
  searchHistoryStore.set(searchHistory.value)
}

/**
 * 获取搜索历史
 */
export function getSearchHistory() {
  // 从 useStorage 兜底重新读取（兼容其他标签页/控制台手动写入的场景）
  const stored = searchHistoryStore.get() || []
  searchHistory.value = stored
  return searchHistory.value
}

/**
 * 清除搜索历史
 */
export function clearSearchHistory() {
  searchHistory.value = []
  searchHistoryStore.remove()
}

/**
 * 防抖搜索（延迟执行）
 */
export function debounceSearch(delay = 300) {
  let timer = null
  return function (fn) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn()
      timer = null
    }, delay)
  }
}

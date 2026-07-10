import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, getUserInfoApi, getRoutersApi } from '@/api/auth'
import { formatResponseData } from '@/utils'
import { resetDynamicRoutes, generateDynamicRoutes } from '@/router'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

export const useUserStore = defineStore('user', () => {
  const tokenStore = useStorage(STORAGE_KEYS.TOKEN, '')
  const userInfoStore = useStorage(STORAGE_KEYS.USER_INFO, null)
  const rolesStore = useStorage(STORAGE_KEYS.ROLES, [])
  const permsStore = useStorage(STORAGE_KEYS.PERMS, [])
  const menusStore = useStorage(STORAGE_KEYS.MENUS, [])

  const token = ref(tokenStore.get())
  const userInfo = ref(formatResponseData(userInfoStore.get()))
  // 持久化 roles/permissions/menus，刷新后恢复，避免每次切换菜单都请求后端
  const roles = ref(rolesStore.get())
  const perms = ref(permsStore.get())
  const menus = ref(menusStore.get())

  // 登录
  async function login(username, password, captchaUuid, captchaCode) {
    const res = await loginApi(username, password, captchaUuid, captchaCode)
    token.value = res.data.token
    tokenStore.set(res.data.token)
    const info = res.data.userInfo
    userInfo.value = info
    userInfoStore.set(info)
    // 登录后立即获取 roles/permissions/menus，避免 beforeEach 中再发请求
    await fetchUserInfo()
    await fetchRouters()
    return res
  }

  // 获取用户信息和权限
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    const data = res.data
    userInfo.value = {
      ...userInfo.value,
      ...data
    }
    roles.value = data.roles || []
    perms.value = data.perms || []
    userInfoStore.set(userInfo.value)
    rolesStore.set(roles.value)
    permsStore.set(perms.value)
  }

  // 获取路由菜单
  async function fetchRouters() {
    const res = await getRoutersApi()
    menus.value = res.data.menus || []
    // 持久化到 localStorage，页面刷新后可恢复，避免路由丢失
    menusStore.set(menus.value)
    return menus.value
  }

  // 刷新路由菜单（菜单管理修改后即时生效，无需重新登录）
  async function refreshRouters() {
    resetDynamicRoutes()
    await Promise.all([fetchRouters(), fetchUserInfo()])
    generateDynamicRoutes(menus.value)
  }

  // 登出
  function logout() {
    resetDynamicRoutes()
    token.value = ''
    userInfo.value = null
    roles.value = []
    perms.value = []
    menus.value = []
    tokenStore.remove()
    userInfoStore.remove()
    rolesStore.remove()
    permsStore.remove()
    menusStore.remove()
  }

  // 检查是否有指定角色
  function hasRole(role) {
    return roles.value.includes(role)
  }

  // 检查是否有权限（admin角色自动拥有所有权限）
  function hasPerm(perm) {
    return perms.value.includes(perm) || roles.value.includes('admin')
  }

  return {
    token,
    userInfo,
    roles,
    perms,
    menus,
    login,
    fetchUserInfo,
    fetchRouters,
    refreshRouters,
    logout,
    hasRole,
    hasPerm
  }
})

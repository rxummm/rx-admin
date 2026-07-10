import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'
import { generateDynamicRoutes } from '@/router'

export function useRouterGuard(router) {
  const whiteList = ['/login']
  let dynamicRoutesAdded = false
  let fetchingRoutes = false
  let lastRedirectedPath = null
  let lastRedirectedTime = 0
  const REDIRECT_COOLDOWN = 1000

  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    const baseTitle = 'rx-admin'
    if (to.meta?.title) {
      document.title = `${to.meta.title} - ${baseTitle}`
    } else {
      document.title = baseTitle
    }
    const userStore = useUserStore()
    const token = userStore.token

    if (token) {
      if (to.path === '/login') {
        next('/dashboard')
        NProgress.done()
        return
      }

      if (to.path === '/') {
        next('/dashboard')
        NProgress.done()
        return
      }

      if (to.matched.length <= 1) {
        const now = Date.now()
        if (to.path === lastRedirectedPath && now - lastRedirectedTime < REDIRECT_COOLDOWN) {
          next(false)
          NProgress.done()
          return
        }

        if (fetchingRoutes) {
          next(false)
          NProgress.done()
          return
        }
        if (dynamicRoutesAdded) {
          fetchingRoutes = true
          try {
            await userStore.fetchRouters()
            generateDynamicRoutes(userStore.menus)
            fetchingRoutes = false
            const rematched = router.resolve(to.path)
            if (rematched.matched.length <= 1) {
              lastRedirectedPath = to.path
              lastRedirectedTime = Date.now()
              next({ path: '/dashboard', replace: true })
            } else {
              next({ ...to, replace: true })
            }
          } catch {
            fetchingRoutes = false
            lastRedirectedPath = to.path
            lastRedirectedTime = Date.now()
            next({ path: '/dashboard', replace: true })
          }
          return
        }
        fetchingRoutes = true
        try {
          await userStore.fetchRouters()
          generateDynamicRoutes(userStore.menus)
          dynamicRoutesAdded = true
          fetchingRoutes = false
          const rematched = router.resolve(to.path)
          if (rematched.matched.length <= 1) {
            lastRedirectedPath = to.path
            lastRedirectedTime = Date.now()
            next({ path: '/dashboard', replace: true })
          } else {
            next({ ...to, replace: true })
          }
        } catch {
          fetchingRoutes = false
          lastRedirectedPath = to.path
          lastRedirectedTime = Date.now()
          next({ path: '/dashboard', replace: true })
        }
        return
      }

      next()
    } else {
      if (whiteList.includes(to.path)) {
        next()
        return
      } else {
        next('/login')
        NProgress.done()
        return
      }
    }
  })

  router.afterEach(() => {
    NProgress.done()
  })

  router.onError((err, to, from) => {
    console.warn('[router.onError]', { from: from?.fullPath, to: to?.fullPath, err })
    if (err?.message?.includes('Failed to fetch dynamically imported module')) {
      console.warn('[router] 动态加载失败，建议刷新页面 (F5)')
    }
  })

  return {
    setDynamicRoutesAdded: (value) => {
      dynamicRoutesAdded = value
    },
    isDynamicRoutesAdded: () => dynamicRoutesAdded
  }
}

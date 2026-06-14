import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import { componentMap } from './componentMap'


// Layout 组件
import LoginView from '@/views/login/index.vue'
const Layout = () => import('@/layout/index.vue')

// ============================================================
// 静态路由 — 只保留 Login + Layout 空壳
// 所有业务路由通过 generateDynamicRoutes() 动态注入
// 注意：Layout 不能有 redirect（children 为空时 redirect 会死循环）
// ============================================================
const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    children: [
      // 内置固定路由（不依赖后端菜单树），避免动态路由未注册时死循环
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      },
      {
        path: 'permission/request',
        name: 'PermissionRequest',
        component: () => import('@/views/permission/request/index.vue'),
        meta: { title: '权限申请', hidden: true }
      },
      {
        path: 'as400/techblog/detail',
        name: 'TechBlogDetail',
        component: () => import('@/views/as400/techblog/detail.vue'),
        meta: { title: '文章详情', hidden: true }
      },
      // 错误页面路由
      {
        path: 'error/:code(\\d+)',
        name: 'ErrorPage',
        component: () => import('@/views/error/ErrorPage.vue'),
        meta: { title: '错误页面', hidden: true }
      }
    ]
  },
  // 404 页面（必须放在最后）
  {
    path: '/:pathMatch(.*)*',
    redirect: '/error/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// ============================================================
// 动态路由生成：从后端菜单树递归遍历，匹配 componentMap 并 addRoute
// ============================================================
// 已注册的路由名称集合，避免重复注册
// 内置固定路由（如 Profile）预先加入，防止 generateDynamicRoutes 重复注册
const registeredRouteNames = new Set(['Profile', 'PermissionRequest', 'TechBlogDetail'])

export function generateDynamicRoutes(menuTree) {
  function walk(menus) {
    menus.forEach(menu => {
      // 只处理有 component 且映射表中存在的菜单项
      const mapEntry = menu.component && componentMap[menu.component]
      if (mapEntry) {
        // 避免重复注册已存在的路由
        if (!registeredRouteNames.has(mapEntry.name)) {
          router.addRoute('Layout', {
            path: menu.path,
            // 使用组件 defineOptions name 作为路由 name，确保 keep-alive 缓存匹配
            name: mapEntry.name,
            component: mapEntry.component,
            meta: {
              title: menu.menuName,
              icon: menu.icon
            }
          })
          registeredRouteNames.add(mapEntry.name)
        }
      }
      if (menu.children && menu.children.length) {
        walk(menu.children)
      }
    })
  }
  walk(menuTree)
}

// ============================================================
// 路由守卫
// ============================================================

// 白名单
const whiteList = ['/login']

// 标记：动态路由是否已注册（模块级变量，确保同一会话只注册一次）
let dynamicRoutesAdded = false

// 防抖标记：防止路由守卫中 fetchRouters 循环触发
let fetchingRoutes = false

// 记录最近被重定向到 dashboard 的目标路径，防止 el-menu router 模式循环触发
let lastRedirectedPath = null
let lastRedirectedTime = 0
const REDIRECT_COOLDOWN = 1000 // 1秒内同一路径不重复重定向

// 重置动态路由标记（登录成功时调用，确保用最新 menus 重新注册）
export function resetDynamicRoutes() {
  dynamicRoutesAdded = false
  registeredRouteNames.clear()
  // 保留内置固定路由
  ;['Profile', 'PermissionRequest', 'TechBlogDetail'].forEach(n => registeredRouteNames.add(n))
}

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  // 动态设置页面标题（支持国际化路径：to.meta.title 可能是 i18n key）
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

    // 根路径 '/' 重定向到 dashboard
    if (to.path === '/') {
      next('/dashboard')
      NProgress.done()
      return
    }

    // 目标路由不存在时：重新从后端拉取最新 menus 并注册（处理首次登录或新增菜单场景）
    if (to.matched.length <= 1) {
      const now = Date.now()
      // 如果同一路径在冷却时间内被重复重定向，说明 el-menu router 模式触发了循环，直接拦截不再重定向
      if (to.path === lastRedirectedPath && (now - lastRedirectedTime) < REDIRECT_COOLDOWN) {
        // 静默拦截，不产生任何导航，打断死循环
        next(false)
        NProgress.done()
        return
      }

      // 如果正在拉取菜单中，等待完成
      if (fetchingRoutes) {
        next(false)
        NProgress.done()
        return
      }
      // 动态路由已注册但目标仍不匹配：可能是后端新增了菜单，重新拉取
      if (dynamicRoutesAdded) {
        fetchingRoutes = true
        try {
          await userStore.fetchRouters()
          generateDynamicRoutes(userStore.menus)
          fetchingRoutes = false
          // 重新拉取后，如果路由仍未匹配，设置冷却标记并重定向到 dashboard
          const rematched = router.resolve(to.path)
          if (rematched.matched.length <= 1) {
            // 路径不存在（如父级菜单 /system），打断循环
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
      // 首次注册动态路由
      fetchingRoutes = true
      try {
        await userStore.fetchRouters()
        generateDynamicRoutes(userStore.menus)
        dynamicRoutesAdded = true
        fetchingRoutes = false
        // 注册后验证目标路径是否已匹配
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

// ============================================================
// 全局路由错误处理
// 防止动态 import 失败时 unhandledrejection 把 keep-alive / 标签页状态弄退化
// （用户感知：之前打开的标签被关掉、页面回退）
// ============================================================
router.onError((err, to, from) => {
  console.warn('[router.onError]', { from: from?.fullPath, to: to?.fullPath, err })
  // 动态 import 失败：通常是开发模式下 Vite 临时文件失效，提示用户刷新
  if (err?.message?.includes('Failed to fetch dynamically imported module')) {
    // 避免在错误处理里再触发导航（可能死循环），用 location.reload 让用户重新加载整个 SPA
    // 这里不直接 reload，只打印；用户可手动 F5
    console.warn('[router] 动态加载失败，建议刷新页面 (F5)')
  }
})

export default router

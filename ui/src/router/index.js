import { createRouter, createWebHistory } from 'vue-router'
import { componentMap } from './componentMap'
import { useRouterGuard } from '@/composables/useRouterGuard'


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

export function resetDynamicRoutes() {
  registeredRouteNames.clear()
  ;['Profile', 'PermissionRequest', 'TechBlogDetail'].forEach(n => registeredRouteNames.add(n))
}

useRouterGuard(router)

export default router

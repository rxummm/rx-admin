import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTagsStore = defineStore('tags', () => {
  // 已打开的标签列表
  const visitedViews = ref([])
  // 缓存的组件名列表（用于 KeepAlive include）
  const cachedViews = ref([])
  // 刷新计数器：每个组件名对应一个递增的key后缀，key变化时强制重建组件实例
  const refreshKeys = ref({})

  // 添加标签（affix 标签始终排在最前面）
  function addView(view) {
    if (visitedViews.value.some((v) => v.path === view.path)) return
    const newView = { ...view }
    if (newView.meta?.affix) {
      // 找到最后一个 affix 标签的位置，插入到它后面
      let insertIndex = 0
      for (let i = visitedViews.value.length - 1; i >= 0; i--) {
        if (visitedViews.value[i].meta?.affix) {
          insertIndex = i + 1
          break
        }
      }
      visitedViews.value.splice(insertIndex, 0, newView)
    } else {
      visitedViews.value.push(newView)
    }
    if (!cachedViews.value.includes(view.name)) {
      cachedViews.value.push(view.name)
    }
  }

  // 删除标签
  function removeView(view) {
    const index = visitedViews.value.findIndex((v) => v.path === view.path)
    if (index > -1) {
      visitedViews.value.splice(index, 1)
      // 同步移除缓存
      const cacheIdx = cachedViews.value.indexOf(view.name)
      if (cacheIdx > -1) {
        cachedViews.value.splice(cacheIdx, 1)
      }
      // 清除刷新计数
      delete refreshKeys.value[view.name]
    }
  }

  // 删除其他标签
  function removeOtherViews(view) {
    // 保留当前标签和affix标签
    const keepNames = visitedViews.value.filter((v) => v.path === view.path || v.meta?.affix).map((v) => v.name)
    // 清理被删除标签的refreshKeys
    Object.keys(refreshKeys.value).forEach((key) => {
      if (!keepNames.includes(key)) {
        delete refreshKeys.value[key]
      }
    })
    visitedViews.value = visitedViews.value.filter((v) => v.path === view.path || v.meta?.affix)
    cachedViews.value = visitedViews.value.map((v) => v.name)
  }

  // 删除所有标签
  function removeAllViews() {
    visitedViews.value = visitedViews.value.filter((v) => v.meta?.affix)
    cachedViews.value = visitedViews.value.map((v) => v.name)
    // 清理被删除标签的refreshKeys
    const keepNames = visitedViews.value.map((v) => v.name)
    Object.keys(refreshKeys.value).forEach((key) => {
      if (!keepNames.includes(key)) {
        delete refreshKeys.value[key]
      }
    })
  }

  // 关闭当前标签（需要返回下一个要激活的路径）
  function closeView(view) {
    const index = visitedViews.value.findIndex((v) => v.path === view.path)
    removeView(view)
    // 返回下一个标签路径用于跳转
    if (view.path === getActivePath()) {
      if (visitedViews.value.length > 0) {
        const nextIndex = Math.min(index, visitedViews.value.length - 1)
        return visitedViews.value[nextIndex].path
      }
      return '/dashboard'
    }
    return null
  }

  // 刷新标签：通过改变组件的 key 来强制销毁旧实例并重建新实例
  // key 变化 → Vue 认为这是新组件 → 销毁旧组件 → 创建新组件 → onMounted 重新执行 → 重新请求数据
  function refreshView(view) {
    const name = view.name
    if (!refreshKeys.value[name]) {
      refreshKeys.value[name] = 1
    } else {
      refreshKeys.value[name]++
    }
  }

  // 当前活跃路径（需要外部设置）
  const activePath = ref('')
  function setActivePath(path) {
    activePath.value = path
  }
  function getActivePath() {
    return activePath.value
  }

  // 根据路由查找对应的标签信息
  function findViewByRoute(route) {
    return visitedViews.value.find((v) => v.path === route.path)
  }

  return {
    visitedViews,
    cachedViews,
    refreshKeys,
    activePath,
    addView,
    removeView,
    removeOtherViews,
    removeAllViews,
    closeView,
    refreshView,
    setActivePath,
    getActivePath,
    findViewByRoute
  }
})

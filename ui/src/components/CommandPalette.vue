<template>
  <el-dialog v-model="visible" :show-close="false" width="560px" top="15vh"
    :before-close="close" class="command-palette-dialog"
    :close-on-click-modal="true" :close-on-press-escape="true"
    @opened="onOpened">
    <div class="command-palette">
      <div class="cp-input-wrapper">
        <el-icon :size="18"><Search /></el-icon>
        <input ref="inputRef" v-model="keyword" placeholder="搜索菜单、页面或操作..."
          class="cp-input" @keydown="handleKeydown" @input="handleInput" />
        <el-tag size="small" style="margin-right: 8px; cursor: pointer;" @click="close">Esc</el-tag>
      </div>

      <div class="cp-results" v-if="keyword">
        <!-- 页面 -->
        <div v-if="filteredMenus.length" class="cp-group">
          <div class="cp-group-title">页面</div>
          <div v-for="(item, i) in filteredMenus" :key="item.path"
            :class="['cp-item', { active: i === activeIndex }]"
            @click="goMenu(item)" @mouseenter="activeIndex = i">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
            <span class="cp-path">{{ item.path }}</span>
          </div>
        </div>

        <!-- 最近访问 -->
        <div v-if="filteredRecent.length" class="cp-group">
          <div class="cp-group-title">最近访问</div>
          <div v-for="(item, i) in filteredRecent" :key="item.path"
            :class="['cp-item', { active: i + filteredMenus.length === activeIndex }]"
            @click="goMenu(item)" @mouseenter="activeIndex = i + filteredMenus.length">
            <el-icon><Clock /></el-icon>
            <span>{{ item.name }}</span>
            <span class="cp-path">{{ item.path }}</span>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div v-if="filteredActions.length" class="cp-group">
          <div class="cp-group-title">快捷操作</div>
          <div v-for="(item, i) in filteredActions" :key="item.name"
            :class="['cp-item', { active: i + filteredMenus.length + filteredRecent.length === activeIndex }]"
            @click="doAction(item)" @mouseenter="activeIndex = i + filteredMenus.length + filteredRecent.length">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </div>

        <div v-if="!filteredMenus.length && !filteredRecent.length && !filteredActions.length"
          style="padding: 32px; text-align: center; color: #909399;">无匹配结果</div>
      </div>

      <div v-else class="cp-placeholder">
        <div style="color: #c0c4cc; font-size: 48px; margin-bottom: 8px;">
          <el-icon :size="48"><Search /></el-icon>
        </div>
        <div style="color: #909399; font-size: 14px;">输入关键字搜索页面和功能...</div>
        <div style="color: #c0c4cc; font-size: 12px; margin-top: 8px;">试试 "用户"、"角色"、"日志"、"暗黑模式"</div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Clock, Moon, FullScreen, Lock, Back } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useTagsStore } from '@/stores/tags'

const router = useRouter()
const userStore = useUserStore()
const tagsStore = useTagsStore()

const visible = ref(false)
const keyword = ref('')
const activeIndex = ref(0)
const inputRef = ref(null)

function flattenMenus(menus, result = []) {
  for (const m of menus) {
    if (m.component && m.menuType === 2) {
      result.push({ name: m.menuName, path: m.path, icon: m.icon || 'Menu' })
    }
    if (m.children) flattenMenus(m.children, result)
  }
  return result
}

const allMenus = computed(() => flattenMenus(userStore.menus || []))

const recentViews = computed(() =>
  tagsStore.visitedViews.filter(v => v.path && v.name !== 'Dashboard').slice(0, 5)
    .map(v => ({ name: v.title || v.name, path: v.path }))
)

const actions = [
  { name: '暗黑模式切换', iconComponent: Moon, action: () => document.documentElement.classList.toggle('dark') },
  { name: '全屏切换', iconComponent: FullScreen, action: () => { if (document.fullscreenElement) document.exitFullscreen(); else document.documentElement.requestFullscreen() } },
  { name: '返回首页', iconComponent: Back, action: () => router.push('/') },
  { name: '退出登录', iconComponent: Lock, action: () => userStore.logout() },
]

const filteredMenus = computed(() => {
  if (!keyword.value) return []
  const kw = keyword.value.toLowerCase()
  return allMenus.value.filter(m => m.name.toLowerCase().includes(kw) || m.path.toLowerCase().includes(kw)).slice(0, 8)
})

const filteredRecent = computed(() => {
  if (!keyword.value) return []
  const kw = keyword.value.toLowerCase()
  return recentViews.value.filter(v => v.name.toLowerCase().includes(kw) || v.path.toLowerCase().includes(kw)).slice(0, 3)
})

const filteredActions = computed(() => {
  if (!keyword.value) return []
  const kw = keyword.value.toLowerCase()
  return actions.filter(a => a.name.toLowerCase().includes(kw))
})

const allResults = computed(() => [...filteredMenus.value, ...filteredRecent.value, ...filteredActions.value.map(a => ({ ...a, isAction: true }))])

function handleInput() { activeIndex.value = 0 }

function handleKeydown(e) {
  if (e.key === 'ArrowDown') { e.preventDefault(); activeIndex.value = Math.min(activeIndex.value + 1, allResults.value.length - 1) }
  else if (e.key === 'ArrowUp') { e.preventDefault(); activeIndex.value = Math.max(activeIndex.value - 1, 0) }
  else if (e.key === 'Enter') {
    e.preventDefault()
    const item = allResults.value[activeIndex.value]
    if (item) {
      if (item.isAction) doAction(item)
      else goMenu(item)
    }
  }
}

function goMenu(item) {
  visible.value = false
  keyword.value = ''
  router.push(item.path)
}

function doAction(item) {
  visible.value = false
  keyword.value = ''
  item.action()
}

function open() {
  visible.value = true
  keyword.value = ''
  activeIndex.value = 0
}

function onOpened() {
  // el-dialog 动画完成后，input DOM 已就绪再聚焦
  nextTick(() => inputRef.value?.focus())
}

function close() {
  visible.value = false
  keyword.value = ''
}

function onGlobalKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    open()
  }
}

onMounted(() => document.addEventListener('keydown', onGlobalKeydown))
onUnmounted(() => document.removeEventListener('keydown', onGlobalKeydown))

defineExpose({ open, close, visible })
</script>

<style scoped>
.command-palette :deep(.el-dialog__header) { display: none; padding: 0; }
.command-palette :deep(.el-dialog__body) { padding: 0; }

.cp-input-wrapper {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; border-bottom: 1px solid #ebeef5;
}
.cp-input {
  flex: 1; border: none; outline: none; font-size: 16px;
  background: transparent; color: var(--el-text-color-primary);
}
.cp-input::placeholder { color: #c0c4cc; }

.cp-results { max-height: 400px; overflow-y: auto; padding: 8px 0; }
.cp-placeholder { padding: 40px 0; text-align: center; }
.cp-group-title { padding: 4px 16px; font-size: 11px; color: #909399; text-transform: uppercase; letter-spacing: 0.5px; }
.cp-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; cursor: pointer; transition: background 0.1s;
}
.cp-item:hover, .cp-item.active { background: #ecf5ff; }
.cp-item .cp-path { margin-left: auto; font-size: 11px; color: #c0c4cc; }
</style>

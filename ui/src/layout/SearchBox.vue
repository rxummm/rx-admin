<template>
  <div class="header-search-box">
    <el-input
      ref="inputRef"
      v-model="keyword"
      :placeholder="$t('layout.searchMenu')"
      :prefix-icon="Search"
      :clearable="true"
      size="default"
      autocomplete="off"
      @focus="handleFocus"
      @clear="clearSearch"
      @update:model-value="handleInput"
      @keydown.enter.prevent="handleEnter"
      @keydown.down.prevent="highlightNext"
      @keydown.up.prevent="highlightPrev"
    >
    </el-input>
    <!-- 搜索结果下拉 -->
    <transition name="search-dropdown-fade">
      <ul v-if="showDropdown && results.length" class="header-search-dropdown">
        <li
          v-for="(item, index) in results"
          :key="item.path"
          :class="{ active: index === highlightIndex }"
          @mousedown.prevent="goTo(item)"
        >
          <el-icon><component :is="getIconComponent(item.icon)" /></el-icon>
          <span class="result-title">{{ item.menuName }}</span>
          <span class="result-path">{{ item.path }}</span>
        </li>
      </ul>
    </transition>
    <div v-if="showDropdown && keyword.trim() && !results.length" class="header-search-empty">
      {{ $t('layout.noMatchResult') }}
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'SearchBox' })
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const emit = defineEmits(['openCommand'])
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()

const keyword = ref('')
const inputRef = ref(null)
const results = ref([])
const highlightIndex = ref(0)
const showDropdown = ref(false)

function flattenMenus(menus) {
  const result = []
  function walk(list) {
    list.forEach(menu => {
      if (menu.component) result.push(menu)
      if (menu.children && menu.children.length) walk(menu.children)
    })
  }
  walk(menus)
  return result
}

function getIconComponent(iconName) {
  if (!iconName || iconName.startsWith('fa-')) {
    return ElementPlusIconsVue.Menu
  }
  return ElementPlusIconsVue[iconName] || ElementPlusIconsVue.Menu
}

function handleInput() {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) {
    results.value = []
    highlightIndex.value = 0
    return
  }
  const allMenus = flattenMenus(userStore.menus)
  results.value = allMenus.filter(item =>
    item.menuName.toLowerCase().includes(kw) || item.path.toLowerCase().includes(kw)
  )
  highlightIndex.value = 0
}

function handleFocus() {
  showDropdown.value = true
  if (keyword.value.trim()) handleInput()
}

function clearSearch() {
  keyword.value = ''
  results.value = []
  highlightIndex.value = 0
  showDropdown.value = false
}

function handleEnter() {
  if (results.value.length === 0) return
  goTo(results.value[highlightIndex.value])
}

function goTo(item) {
  showDropdown.value = false
  keyword.value = ''
  results.value = []
  router.push(item.path)
}

function highlightNext() {
  if (results.value.length > 0) {
    highlightIndex.value = (highlightIndex.value + 1) % results.value.length
  }
}

function highlightPrev() {
  if (results.value.length > 0) {
    highlightIndex.value = highlightIndex.value <= 0 ? results.value.length - 1 : highlightIndex.value - 1
  }
}

function isOpen() {
  return showDropdown.value
}

defineExpose({ isOpen, clearSearch })
</script>

<style scoped lang="scss">
.header-search-box {
  position: relative;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px var(--el-border-color) inset;
    transition: all 0.3s;

    &.is-focus {
      box-shadow: 0 0 0 1px var(--el-color-primary, #409eff) inset !important;
    }
  }

  :deep(.el-input__prefix .el-icon) {
    font-size: 15px;
  }

  :deep(.el-input__suffix .el-icon) {
    font-size: 13px;
  }
}

.header-search-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--el-bg-color, #fff);
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  list-style: none;
  margin: 0;
  padding: 6px 0;
  max-height: 280px;
  overflow-y: auto;
  z-index: var(--z-search, 3000);

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 9px 14px;
    cursor: pointer;
    font-size: 13px;
    color: var(--text-primary);
    transition: all 0.15s;
    white-space: nowrap;

    &.active,
    &:hover {
      background: var(--el-fill-color-light, #f5f7fa);
      color: var(--el-color-primary, #409eff);

      .result-path {
        color: var(--el-color-primary-light-5, #a0cfff);
      }
    }

    .el-icon {
      flex-shrink: 0;
      font-size: 16px;
      color: var(--text-secondary);
    }

    .result-title {
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 140px;
    }

    .result-path {
      flex-shrink: 0;
      font-size: 11px;
      color: var(--text-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 160px;
    }
  }
}

.header-search-empty {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  text-align: center;
  padding: 16px;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--el-bg-color, #fff);
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  z-index: var(--z-search, 3000);
}
</style>
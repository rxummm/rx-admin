<template>
  <el-dialog
    v-model="visible"
    title="⌨️ 全局快捷键"
    width="600px"
    :close-on-click-modal="true"
    class="shortcuts-help-dialog"
  >
    <div class="shortcuts-content">
      <!-- 搜索框 -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索快捷键..."
        clearable
        prefix-icon="Search"
        class="search-input"
      />

      <!-- 快捷键列表 -->
      <div class="shortcuts-list">
        <div v-for="group in filteredGroups" :key="group.title" class="shortcut-group">
          <h3 class="group-title">{{ group.title }}</h3>
          <div v-for="item in group.items" :key="item.key" class="shortcut-item">
            <div class="shortcut-keys">
              <kbd v-for="key in item.keys" :key="key" class="key-badge">{{ key }}</kbd>
            </div>
            <span class="shortcut-desc">{{ item.description }}</span>
          </div>
        </div>

        <!-- 无结果提示 -->
        <div v-if="!filteredGroups.length" class="empty-state">
          <el-icon :size="48" color="#8b949e"><Search /></el-icon>
          <p>未找到匹配的快捷键</p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span class="footer-tip">💡 提示: 输入框内仅 Esc 键生效</span>
        <el-button type="primary" @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'

const visible = ref(false)
const searchKeyword = ref('')

// 快捷键分组数据
const shortcutGroups = ref([
  {
    title: '📌 导航类',
    items: [
      { keys: ['Ctrl', 'B'], description: '切换侧边栏折叠/展开' },
      { keys: ['Alt', '←'], description: '浏览器后退' },
      { keys: ['Alt', '→'], description: '浏览器前进' }
    ]
  },
  {
    title: '🎨 外观类',
    items: [
      { keys: ['Ctrl', 'D'], description: '切换暗色/亮色主题' }
    ]
  },
  {
    title: '🔄 操作类',
    items: [
      { keys: ['Ctrl', 'R'], description: '刷新当前页面数据' },
      { keys: ['Ctrl', 'F'], description: '页面内搜索（聚焦搜索框）' },
      { keys: ['Esc'], description: '关闭弹窗/下拉菜单/右键菜单' }
    ]
  },
  {
    title: '❓ 帮助类',
    items: [
      { keys: ['?'], description: '显示此快捷键帮助' },
      { keys: ['Ctrl', 'K'], description: '打开命令面板' }
    ]
  }
])

// 过滤后的分组
const filteredGroups = computed(() => {
  if (!searchKeyword.value) {
    return shortcutGroups.value
  }

  const keyword = searchKeyword.value.toLowerCase()
  return shortcutGroups.value
    .map(group => ({
      ...group,
      items: group.items.filter(item =>
        item.description.toLowerCase().includes(keyword) ||
        item.keys.some(k => k.toLowerCase().includes(keyword))
      )
    }))
    .filter(group => group.items.length > 0)
})

// 打开对话框
function open() {
  visible.value = true
  searchKeyword.value = ''
}

// 暴露方法给父组件
defineExpose({ open })
</script>

<style scoped lang="scss">
.shortcuts-help-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.shortcuts-content {
  .search-input {
    margin: 16px;
    
    :deep(.el-input__wrapper) {
      border-radius: var(--radius-sm);
    }
  }
}

.shortcuts-list {
  padding: 0 16px 16px;
}

.shortcut-group {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }

  .group-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--border-light);
  }
}

.shortcut-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }

  .shortcut-keys {
    display: flex;
    gap: 6px;
    align-items: center;
  }

  .key-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 28px;
    height: 26px;
    padding: 0 8px;
    font-size: 12px;
    font-family: var(--font-family-mono);
    color: var(--text-regular);
    background: var(--bg-container);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-xs);
    box-shadow: 0 2px 0 var(--border-light);
    user-select: none;
  }

  .shortcut-desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin-left: 16px;
  }
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .footer-tip {
    font-size: 12px;
    color: var(--text-secondary);
  }
}
</style>

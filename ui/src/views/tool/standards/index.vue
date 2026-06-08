<template>
  <div class="docs-container">
    <!-- 侧边栏目录导航 -->
    <aside class="docs-sidebar">
      <div class="docs-sidebar-title">目录</div>
      <el-scrollbar class="docs-sidebar-scroll">
        <ul class="toc-list">
          <li
            v-for="item in tocItems"
            :key="item.id"
            :class="['toc-item', 'toc-level-' + item.level, { active: activeTocId === item.id }]"
            @click="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </li>
        </ul>
      </el-scrollbar>
    </aside>

    <!-- 主内容区 -->
    <main class="docs-main">
      <div class="docs-content markdown-body" v-html="sanitizeHtml(renderedHtml)" ref="contentRef"></div>
    </main>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolStandards' })
import { ref, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css/github-markdown.css'
import { sanitizeHtml } from '@/utils/sanitize'
import 'github-markdown-css/github-markdown.css'

const contentRef = ref(null)
const tocItems = ref([])
const activeTocId = ref('')

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true
})

// 代码高亮
const renderer = new marked.Renderer()
renderer.code = function ({ text, lang }) {
  const validLang = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlighted = hljs.highlight(text, { language: validLang }).value
  return `<pre><code class="hljs language-${validLang}">${highlighted}</code></pre>`
}

// 自定义标题渲染，添加 id 便于目录导航
let headingIndex = 0
renderer.heading = function ({ text, depth }) {
  headingIndex++
  const id = `heading-${headingIndex}`
  return `<h${depth} id="${id}">${text}</h${depth}>`
}

marked.use({ renderer })

const renderedHtml = ref('')

// 从 public 目录加载 markdown 文档
async function loadDocument() {
  try {
    const response = await fetch('/docs/rxadmin-dev-skills.md')
    const text = await response.text()
    renderedHtml.value = marked.parse(text)
    await nextTick()
    extractToc()
    setupHeadingLinks()
  } catch (err) {
    console.error('加载文档失败:', err)
    renderedHtml.value = '<p style="color:red;text-align:center;padding:40px;">文档加载失败，请检查文件是否存在</p>'
  }
}

// 提取目录
function extractToc() {
  if (!contentRef.value) return
  const headings = contentRef.value.querySelectorAll('h1, h2, h3')
  tocItems.value = Array.from(headings).map(h => ({
    id: h.id,
    text: h.textContent,
    level: parseInt(h.tagName.charAt(1))
  }))
}

// 设置标题点击跳转
function setupHeadingLinks() {
  if (!contentRef.value) return
  const headings = contentRef.value.querySelectorAll('h1, h2, h3')
  headings.forEach(h => {
    h.style.cursor = 'pointer'
  })
}

// 点击目录项滚动
function scrollToHeading(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeTocId.value = id
  }
}

// 监听滚动高亮当前标题
function handleScroll() {
  if (!contentRef.value) return
  const headings = contentRef.value.querySelectorAll('h1, h2, h3')
  let currentId = ''
  headings.forEach(h => {
    const rect = h.getBoundingClientRect()
    if (rect.top <= 120) {
      currentId = h.id
    }
  })
  if (currentId) {
    activeTocId.value = currentId
  }
}

onMounted(() => {
  loadDocument()
  window.addEventListener('scroll', handleScroll, { passive: true })
})
</script>

<style lang="scss" scoped>
.docs-container {
  display: flex;
  height: 100%;
  overflow: hidden;
}

// 侧边栏
.docs-sidebar {
  width: 200px;
  min-width: 200px;
  border-right: 1px solid var(--border-color, #e4e7ed);
  background: var(--bg-container, #fff);
  display: flex;
  flex-direction: column;

  .docs-sidebar-title {
    padding: 14px 16px;
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary, #303133);
    border-bottom: 1px solid var(--border-color, #e4e7ed);
  }

  .docs-sidebar-scroll {
    flex: 1;
    overflow-y: auto;
  }
}

.toc-list {
  list-style: none;
  padding: 8px 0;
  margin: 0;
}

.toc-item {
  padding: 5px 16px;
  font-size: 13px;
  color: var(--text-regular, #606266);
  cursor: pointer;
  transition: all 0.15s;
  border-left: 2px solid transparent;
  line-height: 1.5;

  &:hover {
    color: var(--color-primary, #409eff);
    background: var(--bg-hover, #f5f7fa);
  }

  &.active {
    color: var(--color-primary, #409eff);
    border-left-color: var(--color-primary, #409eff);
    background: rgba(64, 158, 255, 0.06);
  }

  &.toc-level-1 {
    padding-left: 16px;
    font-weight: 600;
    font-size: 14px;
    margin-top: 4px;
  }

  &.toc-level-2 {
    padding-left: 30px;
    font-size: 13px;
  }

  &.toc-level-3 {
    padding-left: 44px;
    font-size: 12px;
    color: var(--text-secondary, #909399);
  }
}

// 主内容区
.docs-main {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.docs-content {
  max-width: 1250px;
  margin: 0 auto;
  padding: 32px 48px 60px;

  // 代码块样式
  :deep(pre) {
    border-radius: 8px;
    margin: 16px 0;
    padding: 16px 20px;
    background: #f6f8fa !important;
    border: 1px solid #e1e4e8;
    overflow-x: auto;
  }

  :deep(code) {
    font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
    font-size: 13px;
    line-height: 1.6;
  }

  :deep(p code) {
    background: rgba(175, 184, 193, 0.2);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
  }

  // 标题
  :deep(h1) {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e1e4e8;
    color: var(--text-primary, #303133);
  }

  :deep(h2) {
    font-size: 22px;
    font-weight: 600;
    margin: 32px 0 12px;
    padding-bottom: 6px;
    border-bottom: 1px solid #e1e4e8;
    color: var(--text-primary, #303133);
  }

  :deep(h3) {
    font-size: 18px;
    font-weight: 600;
    margin: 24px 0 10px;
    color: var(--text-primary, #303133);
  }

  // 表格
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    font-size: 13px;

    th, td {
      padding: 8px 12px;
      border: 1px solid #dfe2e5;
      text-align: left;
    }

    th {
      background: #f6f8fa;
      font-weight: 600;
    }

    tr:nth-child(even) {
      background: #fafbfc;
    }
  }

  // 引用
  :deep(blockquote) {
    border-left: 4px solid var(--color-primary, #409eff);
    padding: 8px 16px;
    margin: 12px 0;
    background: rgba(64, 158, 255, 0.04);
    color: var(--text-regular, #606266);

    p {
      margin: 4px 0;
    }
  }

  // 链接
  :deep(a) {
    color: var(--color-primary, #409eff);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  // 列表
  :deep(ul), :deep(ol) {
    padding-left: 24px;
    margin: 8px 0;
    line-height: 1.7;
  }

  // 分割线
  :deep(hr) {
    border: none;
    border-top: 1px solid #e1e4e8;
    margin: 24px 0;
  }
}

// 暗黑模式适配
html.dark .docs-content {
  :deep(pre) {
    background: #1e1e1e !important;
    border-color: #333;
  }

  :deep(table) {
    th, td {
      border-color: #333;
    }

    th {
      background: #252525;
    }

    tr:nth-child(even) {
      background: #1a1a1a;
    }
  }

  :deep(blockquote) {
    background: rgba(64, 158, 255, 0.06);
  }

  :deep(h1) { border-bottom-color: #333; }
  :deep(h2) { border-bottom-color: #333; }
  :deep(hr) { border-top-color: #333; }
  :deep([style*="color:red"]) { color: #f56c6c !important; }
}
</style>

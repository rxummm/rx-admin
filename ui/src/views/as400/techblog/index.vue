<template>
  <div class="techblog-page">
    <!-- 博客来源切换 -->
    <div class="source-tabs">
      <div
        v-for="src in sourceList"
        :key="src.key"
        :class="['source-tab', { active: currentSource === src.key }]"
        @click="switchSource(src.key)"
      >
        <span class="tab-label">{{ src.label }}</span>
        <span class="tab-count" v-if="src.key === currentSource">共 {{ total }} 篇</span>
      </div>
    </div>

    <!-- 搜索 & 分类过滤 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="keyword"
          placeholder="搜索标题或内容..."
          clearable
          style="width: 320px"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="categoryFilter"
          placeholder="分类筛选"
          clearable
          style="width: 200px"
          @change="handleSearch"
        >
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
      </div>
      <div class="toolbar-actions">
        <el-button v-if="userStore.hasPerm('techblog:add')" type="primary" size="small" @click="openCreate">新增文章</el-button>
        <div v-if="userStore.hasPerm('techblog:sync')" class="sync-toggle" @click="showSyncPanel = !showSyncPanel">
          <span class="sync-toggle-label">数据同步</span>
          <el-icon class="sync-arrow" :class="{ expanded: showSyncPanel }"><ArrowDown /></el-icon>
        </div>
      </div>
    </div>

    <!-- 抓取状态面板（默认折叠） -->
    <div v-if="showSyncPanel" class="fetch-panel">
      <div class="fetch-panel-header">
        <span class="panel-title">数据同步</span>
        <span class="panel-desc">点击「开始抓取」按钮获取最新文章，支持多源同时抓取</span>
      </div>
      <div class="fetch-sources">
        <div v-for="src in sourceList" :key="src.key" class="fetch-source-row">
          <div class="source-info">
            <span :class="['source-status-dot', statusClass(src.key)]"></span>
            <span class="source-name">{{ src.label }}</span>
          </div>
          <div class="source-progress" v-if="fetchStatus[src.key] >= 0 && fetchStatus[src.key] < 100">
            <el-progress
              :percentage="fetchStatus[src.key]"
              :stroke-width="6"
              style="width: 120px"
            />
          </div>
          <div class="source-progress done" v-else-if="fetchStatus[src.key] === 100">
            <el-icon color="#67C23A"><CircleCheckFilled /></el-icon>
            <span>同步完成</span>
          </div>
          <div class="source-progress idle" v-else>
            <span>待抓取</span>
          </div>
          <div class="source-actions">
            <el-button
              size="small"
              :type="fetchStatus[src.key] >= 0 && fetchStatus[src.key] < 100 ? 'warning' : 'primary'"
              :loading="fetchingSource === src.key"
              :disabled="fetchStatus[src.key] >= 0 && fetchStatus[src.key] < 100"
              @click="handleFetchSource(src.key)"
            >
              {{ fetchStatus[src.key] >= 0 && fetchStatus[src.key] < 100 ? '抓取中...' : '开始抓取' }}
            </el-button>
            <el-button
              v-if="(fetchLogs[src.key] || []).length > 0"
              size="small"
              text
              @click="showLogs = showLogs === src.key ? null : src.key"
            >
              {{ showLogs === src.key ? '收起日志' : '查看日志' }}
            </el-button>
          </div>
        </div>
      </div>
      <!-- 日志面板 -->
      <div v-if="showLogs" class="fetch-logs-panel">
        <div class="logs-header">
          <span>{{ sourceLabel(showLogs) }} 抓取日志</span>
          <el-button text size="small" @click="showLogs = null">收起</el-button>
        </div>
        <div class="logs-content">
          <div v-for="(log, idx) in (fetchLogs[showLogs] || [])" :key="idx" class="log-line">{{ log }}</div>
        </div>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedIds.length > 0 && userStore.hasPerm('techblog:batchDelete')" class="batch-bar">
      <el-checkbox
        :model-value="isAllSelected"
        :indeterminate="isIndeterminate"
        @change="toggleSelectAll"
      >
        全选
      </el-checkbox>
      <span class="batch-count">已选 {{ selectedIds.length }} 篇</span>
      <el-button type="danger" size="small" @click="handleBatchDelete" :loading="batchDeleting">
        批量删除
      </el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 文章列表 -->
    <div v-loading="loading" class="article-list">
      <div v-if="articles.length === 0 && !loading" class="empty-hint">
        <el-empty :description="emptyHint" />
      </div>

      <div
        v-for="article in articles"
        :key="article.id"
        :class="['article-card', { selected: selectedIds.includes(article.id) }]"
      >
        <div class="card-main" @click="goDetail(article.id)">
          <div class="card-checkbox" v-if="userStore.hasPerm('techblog:batchDelete')" @click.stop="toggleSelect(article.id)">
            <el-checkbox :model-value="selectedIds.includes(article.id)" />
          </div>
          <div class="card-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" loading="lazy" />
          </div>
          <div class="card-body" :class="{ 'has-cover': article.coverImage }">
            <h3 class="card-title">{{ article.title }}</h3>
            <div class="card-meta">
              <span class="meta-date">
                <el-icon><Calendar /></el-icon> {{ article.publishDate }}
              </span>
              <span class="meta-author">
                <el-icon><User /></el-icon> {{ article.author }}
              </span>
              <span class="meta-views">
                <el-icon><View /></el-icon> {{ article.viewCount || 0 }}
              </span>
            </div>
            <p class="card-excerpt">{{ article.excerptText || '暂无摘要' }}</p>
            <div class="card-categories" v-if="article.categories">
              <el-tag
                v-for="cat in splitCats(article.categories)"
                :key="cat"
                size="small"
                type="info"
                effect="plain"
                class="cat-tag"
                @click.stop="categoryFilter = cat; handleSearch()"
              >
                {{ cat }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="card-actions">
          <el-button text type="primary" size="small" @click="goDetail(article.id)">
            阅读全文 <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button v-if="userStore.hasPerm('techblog:edit')" text size="small" @click.stop="openEdit(article)">编辑</el-button>
          <el-popconfirm v-if="userStore.hasPerm('techblog:delete')" title="确认删除该文章?" @confirm="handleDelete(article.id)">
            <template #reference>
              <el-button text type="danger" size="small" @click.stop>删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        background
        @current-change="loadArticles"
      />
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增文章' : '简介编辑'"
      width="75vw"
      :close-on-click-modal="false"
      destroy-on-close
      draggable
      :modal="true"
      class="resizable-dialog"
      top="5vh"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="formData.author" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-select v-model="formData.source" style="width: 100%" filterable allow-create default-first-option>
                <el-option v-for="s in sourceList" :key="s.key" :label="s.label" :value="s.key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发布日期" prop="publishDate">
              <el-input v-model="formData.publishDate" placeholder="yyyy-MM-dd" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categories">
              <el-input v-model="formData.categories" placeholder="多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要" prop="excerptText">
          <el-input v-model="formData.excerptText" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="formMode === 'create'" label="正文内容" prop="contentText">
          <md-editor
            v-model="formData.contentText"
            language="en-US"
            :theme="isDark ? 'dark' : 'light'"
            :toolbars="toolbars"
            :sanitize="customSanitize"
            style="height: 500px"
            @on-html-change="onContentHtmlChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSaving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Calendar, User, View, ArrowRight, ArrowDown, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticlesApi, getCategoriesApi, startFetchApi, getFetchProgressApi, updateArticleApi, deleteArticleApi, batchDeleteArticlesApi, createArticleApi } from '@/api/techBlog'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/composables/useTheme'

defineOptions({ name: 'TechBlogIndex' })
const userStore = useUserStore()
const { isDark } = useTheme()
const userName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '')
const router = useRouter()

// 来源定义
const sourceList = [
  { key: 'nicklitten', label: 'Nick Litten', url: 'nicklitten.com' },
  { key: 'faq400', label: 'BlogFaq400', url: 'blog.faq400.com' },
  { key: 'rpgpgm', label: 'RPGPGM', url: 'rpgpgm.com' },
  { key: 'as400sql', label: 'AS400 SQL Tricks', url: 'as400andsqltricks.com' },
  { key: 'apimy', label: 'API My My My', url: 'apimymymy.wordpress.com' }
]

// 状态
const loading = ref(false)
const articles = ref([])
const categories = ref([])
const keyword = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentSource = ref('nicklitten')

// 多源抓取状态
const fetchingSource = ref(null)
const fetchStatus = ref({})
const fetchLogs = ref({})
const showLogs = ref(null)
const showSyncPanel = ref(false)
const timers = {}

// 批量选择和删除
const selectedIds = ref([])
const batchDeleting = ref(false)

// 编辑/新增
const formRef = ref(null)
const formVisible = ref(false)
const formMode = ref('edit') // 'create' | 'edit'
const formSaving = ref(false)

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  source: [{ required: true, message: '请选择或输入来源', trigger: 'change' }],
  publishDate: [{ required: true, message: '请输入发布日期', trigger: 'blur' }],
  categories: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  excerptText: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  contentText: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}
const formData = ref({
  id: null,
  title: '',
  author: '',
  source: 'nicklitten',
  publishDate: '',
  categories: '',
  excerptText: '',
  contentText: '',
  contentHtml: ''
})
// md-editor-v3 toolbar config
const toolbars = [
  'bold', 'italic', 'strikethrough', 'title', '-',
  'unorderedList', 'orderedList', 'checkedList', '-',
  'code', 'codeRow', 'quote', 'link', 'image', 'table', '-',
  'preview', 'fullscreen'
]

// 自定义 HTML 消毒函数：保留图片等安全标签，防止 XSS
function customSanitize(html) {
  // 允许的安全标签列表
  const allowedTags = ['img', 'table', 'thead', 'tbody', 'tr', 'td', 'th', 'figure', 'figcaption', 'div', 'span', 'br', 'hr', 'pre', 'sup', 'sub', 'kbd', 'abbr', 'mark', 'details', 'summary']
  // 移除危险的 script/iframe/object/embed 等
  html = html.replace(/<script[\s\S]*?<\/script>/gi, '')
  html = html.replace(/<iframe[\s\S]*?<\/iframe>/gi, '')
  html = html.replace(/<object[\s\S]*?<\/object>/gi, '')
  html = html.replace(/<embed[\s\S]*?>/gi, '')
  // 移除所有标签的事件属性 (onerror, onload, onclick 等)
  html = html.replace(/\s+on\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]*)/gi, '')
  return html
}

// 初始化各来源状态
sourceList.forEach(s => {
  fetchStatus.value[s.key] = -1
  fetchLogs.value[s.key] = []
})

// 计算属性
const currentSourceInfo = computed(() => sourceList.find(s => s.key === currentSource.value) || sourceList[0])
const currentSourceTitle = computed(() => currentSourceInfo.value.label + ' Blog')
const currentSourceUrl = computed(() => currentSourceInfo.value.url)
const emptyHint = computed(() => `暂无文章，请从 ${currentSourceInfo.value.label} 博客抓取内容`)

function sourceLabel(key) {
  const s = sourceList.find(i => i.key === key)
  return s ? s.label : key
}

function statusClass(key) {
  const p = fetchStatus.value[key]
  if (p >= 0 && p < 100) return 'syncing'
  if (p === 100) return 'done'
  return 'idle'
}

function splitCats(cats) {
  if (!cats) return []
  return cats.split(',').map(c => c.trim()).filter(Boolean)
}

// 数据加载
async function loadArticles() {
  loading.value = true
  try {
    const res = await getArticlesApi({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      category: categoryFilter.value || undefined,
      source: currentSource.value
    })
    if (res.code === 200 && res.data) {
      articles.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategoriesApi(currentSource.value)
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) { /* ignore */ }
}

function handleSearch() {
  currentPage.value = 1
  loadArticles()
}

function goDetail(id) {
  router.push({ name: 'TechBlogDetail', query: { id } })
}

// 批量选择
const isAllSelected = computed(() => articles.value.length > 0 && selectedIds.value.length === articles.value.length)
const isIndeterminate = computed(() => selectedIds.value.length > 0 && selectedIds.value.length < articles.value.length)

function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

function toggleSelectAll(val) {
  if (val) {
    selectedIds.value = articles.value.map(a => a.id)
  } else {
    selectedIds.value = []
  }
}

function clearSelection() {
  selectedIds.value = []
}

// 删除单篇
async function handleDelete(id) {
  try {
    await deleteArticleApi(id)
    ElMessage.success('删除成功')
    selectedIds.value = selectedIds.value.filter(i => i !== id)
    loadArticles()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 批量删除
async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 篇文章？`, '批量删除', { type: 'warning' })
  } catch {
    return
  }
  batchDeleting.value = true
  try {
    await batchDeleteArticlesApi(selectedIds.value)
    ElMessage.success(`已删除 ${selectedIds.value.length} 篇`)
    selectedIds.value = []
    loadArticles()
  } catch (e) {
    ElMessage.error('批量删除失败')
  } finally {
    batchDeleting.value = false
  }
}

// 编辑 / 新增
function todayStr() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

function resetForm() {
  formData.value = {
    id: null,
    title: '',
    author: '',
    source: userName.value,
    publishDate: todayStr(),
    categories: '',
    excerptText: '',
    contentText: '',
    contentHtml: ''
  }
  formRef.value?.clearValidate()
}

function openCreate() {
  formMode.value = 'create'
  resetForm()
  formVisible.value = true
}

function openEdit(article) {
  formMode.value = 'edit'
  formData.value = {
    id: article.id,
    title: article.title || '',
    author: article.author || '',
    source: article.source || '',
    publishDate: article.publishDate || '',
    categories: article.categories || '',
    excerptText: article.excerptText || '',
    contentText: article.contentHtml || article.contentText || '',
    contentHtml: article.contentHtml || ''
  }
  formVisible.value = true
}

function onContentHtmlChange(html) {
  formData.value.contentHtml = html
}

async function submitForm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  formSaving.value = true
  try {
    const payload = {
      title: formData.value.title,
      author: formData.value.author,
      source: formData.value.source,
      publishDate: formData.value.publishDate,
      categories: formData.value.categories,
      excerptText: formData.value.excerptText
    }
    if (formMode.value === 'create') {
      payload.contentHtml = formData.value.contentHtml
      payload.contentText = formData.value.contentText
      await createArticleApi(payload)
      ElMessage.success('创建成功')
    } else {
      payload.id = formData.value.id
      await updateArticleApi(formData.value.id, payload)
      ElMessage.success('保存成功')
    }
    formVisible.value = false
    loadArticles()
  } catch (e) {
    ElMessage.error(formMode.value === 'create' ? '创建失败' : '保存失败')
  } finally {
    formSaving.value = false
  }
}

function switchSource(key) {
  if (currentSource.value === key) return
  currentSource.value = key
  keyword.value = ''
  categoryFilter.value = ''
  currentPage.value = 1
  loadArticles()
  loadCategories()
}

// 多源抓取
async function handleFetchSource(source) {
  fetchingSource.value = source
  try {
    const res = await startFetchApi(source)
    ElMessage.success(res.message || `[${sourceLabel(source)}] 抓取任务已启动`)
    fetchStatus.value[source] = 0

    // 为每个 source 启动独立轮询
    startPolling(source)
  } catch (e) {
    ElMessage.error(`[${sourceLabel(source)}] 启动抓取失败`)
  } finally {
    fetchingSource.value = null
  }
}

function startPolling(source) {
  if (timers[source]) clearInterval(timers[source])
  timers[source] = setInterval(async () => {
    try {
      const pRes = await getFetchProgressApi(source, { _skipNProgress: true })
      if (pRes.code === 200 && pRes.data) {
        const progress = pRes.data.progress
        fetchStatus.value[source] = progress
        if (pRes.data.logs) {
          fetchLogs.value[source] = pRes.data.logs
        }
        if (progress >= 100 || progress < 0) {
          clearInterval(timers[source])
          timers[source] = null
          if (progress >= 100) {
            ElMessage.success(`[${sourceLabel(source)}] 同步完成！`)
            if (source === currentSource.value) {
              loadArticles()
              loadCategories()
            }
          }
        }
      }
    } catch (e) { /* ignore */ }
  }, Number(import.meta.env.VITE_FETCH_PROGRESS_POLL_INTERVAL) || 2000)
}

onMounted(() => {
  loadArticles()
  loadCategories()
})

onUnmounted(() => {
  Object.values(timers).forEach(t => t && clearInterval(t))
})
</script>

<style scoped>
.techblog-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--layout-content-offset, 107px));
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
}

/* 来源切换 */
.source-tabs {
  display: flex;
  gap: 0;
  padding: 0 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  background: var(--el-bg-color);
}

.source-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  user-select: none;
}

.source-tab:hover {
  color: var(--el-color-primary);
}

.source-tab.active {
  color: var(--el-color-primary);
  border-bottom-color: var(--el-color-primary);
  font-weight: 600;
}

.tab-count {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  font-weight: normal;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sync-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  user-select: none;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s;
}

.sync-toggle:hover {
  color: var(--el-color-primary);
  background: var(--el-fill-color-lighter);
}

.sync-arrow {
  font-size: 12px;
  transition: transform 0.3s;
}

.sync-arrow.expanded {
  transform: rotate(180deg);
}

/* 抓取面板 */
.fetch-panel {
  padding: 0 24px 12px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.fetch-panel-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 10px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.panel-desc {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.fetch-sources {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.fetch-source-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
}

.source-info {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 140px;
  flex-shrink: 0;
}

.source-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.source-status-dot.idle { background: var(--el-text-color-placeholder); }
.source-status-dot.syncing { background: var(--el-color-warning); animation: pulse 1s infinite; }
.source-status-dot.done { background: #67C23A; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.source-name {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.source-progress {
  width: 160px;
  flex-shrink: 0;
}

.source-progress.done {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67C23A;
}

.source-progress.idle {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.source-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 日志面板 */
.fetch-logs-panel {
  margin-top: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: var(--el-fill-color-light);
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.logs-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px 12px;
  background: var(--el-bg-color);
}

.log-line {
  font-size: 12px;
  color: var(--el-text-color-regular);
  font-family: 'Consolas', 'Monaco', monospace;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 文章列表 */
.article-list {
  flex: 1;
  overflow: auto;
  padding: 0 24px 20px;
}

.empty-hint {
  padding: 60px 0;
  text-align: center;
}

/* 批量操作栏 */
.batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 24px;
  background: var(--el-color-primary-light-9);
  border-bottom: 1px solid var(--el-color-primary-light-5);
  flex-shrink: 0;
  font-size: 13px;
}

.batch-count {
  color: var(--el-text-color-secondary);
}

.article-card {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.25s;
  background: var(--el-bg-color);
}

.article-card.selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-5);
}

.article-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.card-main {
  display: flex;
  cursor: pointer;
  flex: 1;
}

.card-checkbox {
  display: flex;
  align-items: flex-start;
  padding: 16px 12px 0 20px;
  cursor: default;
  flex-shrink: 0;
}

.card-cover {
  width: 220px;
  min-height: 150px;
  flex-shrink: 0;
  overflow: hidden;
  background: var(--el-fill-color-light);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-body {
  padding: 16px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 10px;
}

.meta-date, .meta-author, .meta-views {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-excerpt {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.cat-tag {
  cursor: pointer;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 20px 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.card-actions .el-button:first-child {
  margin-right: auto;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 12px 24px 16px;
  flex-shrink: 0;
}

/* 可调整大小的弹窗 */
.resizable-dialog :deep(.el-dialog) {
  resize: both;
  overflow: hidden;
  min-width: 600px;
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

.resizable-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow: auto;
  padding-bottom: 20px;
}

/* 右下角拖拽调整大小手柄 */
.resizable-dialog :deep(.el-dialog)::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: 0;
  width: 16px;
  height: 16px;
  cursor: se-resize;
  background: linear-gradient(135deg, transparent 50%, var(--el-border-color-lighter) 50%, transparent 75%);
}
</style>
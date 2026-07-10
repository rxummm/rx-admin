<template>
  <div class="techblog-detail" v-loading="loading">
    <!-- 顶部导航 -->
    <div class="detail-nav">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      <el-button v-if="article && userStore.hasPerm('techblog:edit')" text type="primary" @click="openEdit">
        <el-icon><Edit /></el-icon> 编辑
      </el-button>
    </div>

    <div v-if="article" class="article-container">
      <!-- 文章头部 -->
      <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span>
            <el-icon><Calendar /></el-icon> {{ article.publishDate }}
          </span>
          <span>
            <el-icon><User /></el-icon> {{ article.author }}
          </span>
          <span>
            <el-icon><View /></el-icon> {{ article.viewCount || 0 }} 次阅读
          </span>
          <span v-if="article.sourceUrl">
            <el-link type="primary" :href="article.sourceUrl" target="_blank" underline="never">
              <el-icon><Link /></el-icon> 原文链接
            </el-link>
          </span>
        </div>
        <div class="article-categories" v-if="article.categories">
          <el-tag v-for="cat in splitCats(article.categories)" :key="cat" size="small" type="info" effect="plain">
            {{ cat }}
          </el-tag>
        </div>
      </header>

      <!-- 封面图 -->
      <div class="article-cover" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />
      </div>

      <!-- 正文内容 -->
      <div class="article-body" v-html="sanitizeHtml(article.contentHtml)"></div>

      <!-- 底部操作 -->
      <footer class="article-footer">
        <el-divider />
        <div class="footer-actions">
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon> 返回列表
          </el-button>
          <el-button type="primary" v-if="article.sourceUrl" @click="openOriginal">
            <el-icon><Link /></el-icon> 查看原文
          </el-button>
        </div>
      </footer>
    </div>

    <el-empty v-else-if="!loading" description="文章不存在或已删除" />

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editVisible"
      title="编辑文章"
      width="75vw"
      :close-on-click-modal="false"
      destroy-on-close
      draggable
      :modal="true"
      class="resizable-dialog"
      top="5vh"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="editForm.author" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-select v-model="editForm.source" style="width: 100%" filterable allow-create default-first-option>
                <el-option v-for="s in sourceList" :key="s.key" :label="s.label" :value="s.key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发布日期" prop="publishDate">
              <el-input v-model="editForm.publishDate" placeholder="yyyy-MM-dd" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categories">
              <el-input v-model="editForm.categories" placeholder="多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要" prop="excerptText">
          <el-input v-model="editForm.excerptText" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="正文内容" prop="contentText">
          <md-editor
            v-model="editForm.contentText"
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
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Calendar, User, View, Link, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticleDetailApi, updateArticleApi } from '@/api/techBlog'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/composables/useTheme'
import { sanitizeHtml } from '@/utils/sanitize'

defineOptions({ name: 'TechBlogDetail' })
const route = useRoute()
const { isDark } = useTheme()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const article = ref(null)

// 来源列表
const sourceList = [
  { key: 'nicklitten', label: 'Nick Litten', url: 'nicklitten.com' },
  { key: 'faq400', label: 'BlogFaq400', url: 'blog.faq400.com' },
  { key: 'rpgpgm', label: 'RPGPGM', url: 'rpgpgm.com' },
  { key: 'as400sql', label: 'AS400 SQL Tricks', url: 'as400andsqltricks.com' },
  { key: 'apimy', label: 'API My My My', url: 'apimymymy.wordpress.com' },
  { key: 'think400', label: 'Think400', url: 'think400.dk' }
]

// 编辑
const editFormRef = ref(null)
const editVisible = ref(false)
const editSaving = ref(false)

const editFormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  source: [{ required: true, message: '请选择或输入来源', trigger: 'change' }],
  publishDate: [{ required: true, message: '请输入发布日期', trigger: 'blur' }],
  categories: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  excerptText: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  contentText: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

const editForm = ref({
  title: '',
  author: '',
  source: '',
  publishDate: '',
  categories: '',
  excerptText: '',
  contentText: '',
  contentHtml: ''
})
const toolbars = [
  'bold',
  'italic',
  'strikethrough',
  'title',
  '-',
  'unorderedList',
  'orderedList',
  'checkedList',
  '-',
  'code',
  'codeRow',
  'quote',
  'link',
  'image',
  'table',
  '-',
  'preview',
  'fullscreen'
]

// 自定义 HTML 消毒函数：保留图片等安全标签，防止 XSS
function customSanitize(html) {
  html = html.replace(/<script[\s\S]*?<\/script>/gi, '')
  html = html.replace(/<iframe[\s\S]*?<\/iframe>/gi, '')
  html = html.replace(/<object[\s\S]*?<\/object>/gi, '')
  html = html.replace(/<embed[\s\S]*?>/gi, '')
  html = html.replace(/\s+on\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]*)/gi, '')
  return html
}

function splitCats(cats) {
  if (!cats) return []
  return cats
    .split(',')
    .map((c) => c.trim())
    .filter(Boolean)
}

async function loadDetail() {
  const id = route.query.id
  if (!id) return
  loading.value = true
  try {
    const res = await getArticleDetailApi(id)
    if (res.code === 200 && res.data) {
      article.value = res.data
    }
  } catch {
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({ name: 'TechBlogIndex' })
}

function openOriginal() {
  if (article.value?.sourceUrl) {
    window.open(article.value.sourceUrl, '_blank')
  }
}

// 编辑
function openEdit() {
  if (!article.value) return
  editForm.value = {
    title: article.value.title || '',
    author: article.value.author || '',
    source: article.value.source || '',
    publishDate: article.value.publishDate || '',
    categories: article.value.categories || '',
    excerptText: article.value.excerptText || '',
    contentText: article.value.contentHtml || article.value.contentText || '',
    contentHtml: article.value.contentHtml || ''
  }
  editVisible.value = true
  editFormRef.value?.clearValidate()
}

function onContentHtmlChange(html) {
  editForm.value.contentHtml = html
}

async function submitEdit() {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
  } catch {
    return
  }
  editSaving.value = true
  try {
    await updateArticleApi(article.value.id, {
      id: article.value.id,
      title: editForm.value.title,
      author: editForm.value.author,
      source: editForm.value.source,
      publishDate: editForm.value.publishDate,
      categories: editForm.value.categories,
      excerptText: editForm.value.excerptText,
      contentHtml: editForm.value.contentHtml,
      contentText: editForm.value.contentText
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    loadDetail()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    editSaving.value = false
  }
}

onMounted(() => {
  loadDetail()
})

// 监听路由参数变化（keep-alive 缓存下 onMounted 不触发，需 watch 手动刷新）
watch(
  () => route.query.id,
  (newId) => {
    if (newId) {
      loadDetail()
    }
  }
)
</script>

<style scoped>
.techblog-detail {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--layout-content-offset, 107px));
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: auto;
}

.detail-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  position: sticky;
  top: 0;
  background: var(--el-bg-color);
  z-index: var(--z-content, 10);
}

.article-container {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px;
}

.article-header {
  margin-bottom: 24px;
}

.article-title {
  margin: 0 0 12px 0;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--el-text-color-primary);
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.article-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.article-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.article-cover {
  margin-bottom: 24px;
  border-radius: 10px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

/* 正文样式 */
.article-body {
  font-size: 15px;
  line-height: 1.85;
  color: var(--el-text-color-primary);
  word-break: break-word;
}

.article-body :deep(h1),
.article-body :deep(h2),
.article-body :deep(h3),
.article-body :deep(h4) {
  margin-top: 1.6em;
  margin-bottom: 0.6em;
  font-weight: 600;
  line-height: 1.35;
  color: var(--el-text-color-primary);
}

.article-body :deep(h2) {
  font-size: 22px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 8px;
}

.article-body :deep(h3) {
  font-size: 18px;
}

.article-body :deep(p) {
  margin: 0 0 1em 0;
}

.article-body :deep(ul),
.article-body :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 1em;
}

.article-body :deep(li) {
  margin-bottom: 0.4em;
}

.article-body :deep(blockquote) {
  margin: 1em 0;
  padding: 12px 20px;
  border-left: 4px solid var(--el-color-primary);
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  color: var(--el-text-color-secondary);
}

.article-body :deep(code) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  background: var(--el-fill-color);
  padding: 2px 6px;
  border-radius: 3px;
  color: var(--el-color-primary);
}

.article-body :deep(pre) {
  margin: 1em 0;
  padding: 16px 20px;
  background: #1e1e1e;
  color: #dcdcaa;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
}

.article-body :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

.article-body :deep(img) {
  max-width: 100%;
  border-radius: 6px;
  margin: 12px 0;
}

.article-body :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

.article-body :deep(a:hover) {
  text-decoration: underline;
}

.article-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.article-body :deep(th),
.article-body :deep(td) {
  border: 1px solid var(--el-border-color-lighter);
  padding: 8px 12px;
  text-align: left;
}

.article-body :deep(th) {
  background: var(--el-fill-color-light);
  font-weight: 600;
}

.article-footer {
  margin-top: 40px;
}

.footer-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
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

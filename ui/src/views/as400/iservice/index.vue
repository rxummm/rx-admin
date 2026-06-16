<template>
  <div class="iservice-page">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">i-Service</h2>
        <span class="page-subtitle">IBM i Services</span>
      </div>
      <div class="header-right">
        <el-button :loading="loading" @click="fetchData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="category-tab"
        :class="{ active: activeCategory === cat.id }"
        @click="activeCategory = cat.id"
      >
        <span class="tab-name">{{ cat.name }}</span>
        <span class="tab-count">{{ cat.items?.length || 0 }}</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索服务名称或描述..."
        clearable
        style="width: 320px"
        :prefix-icon="Search"
      />
      <el-select
        v-model="typeFilter"
        placeholder="服务类型"
        clearable
        style="width: 180px"
      >
        <el-option
          v-for="t in serviceTypes"
          :key="t.value"
          :label="t.label"
          :value="t.value"
        />
      </el-select>
      <el-tag type="info" size="small">
        共 {{ filteredItems.length }} 项服务
      </el-tag>
    </div>

    <!-- 服务列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading || detailLoading"
        :data="filteredItems"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        row-key="id"
        @expand-change="handleExpandChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div v-if="row._detail" class="expand-detail">
              <!-- 基本信息 -->
              <el-descriptions :column="2" border size="small" class="detail-section">
                <el-descriptions-item label="服务名称" :span="2">
                  <el-tag type="primary" effect="dark">{{ row.serviceName }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="服务类型">
                  <el-tag :type="getTypeTag(row.serviceType)" size="small">
                    {{ row.serviceType }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="所属分类">
                  {{ currentCategory?.name }}
                </el-descriptions-item>
                <el-descriptions-item v-if="row.systemObjectName" label="系统对象名">
                  <code>{{ row.systemObjectName }}</code>
                </el-descriptions-item>
                <el-descriptions-item v-if="row.earliestPossibleRelease" label="最早版本">
                  {{ row.earliestPossibleRelease }}
                </el-descriptions-item>
                <el-descriptions-item v-if="row.initialDb2GroupLevel" label="初始 PTF 级别">
                  {{ row.initialDb2GroupLevel }}
                </el-descriptions-item>
                <el-descriptions-item v-if="row.latestDb2GroupLevel" label="最新 PTF 级别">
                  {{ row.latestDb2GroupLevel }}
                </el-descriptions-item>
                <el-descriptions-item v-if="row.briefDescription" label="功能描述" :span="2">
                  {{ row.briefDescription }}
                </el-descriptions-item>
                <el-descriptions-item v-if="row.fullDescription" label="详细说明" :span="2">
                  <div class="full-desc" v-html="sanitizeHtml(formatDesc(row.fullDescription))"></div>
                </el-descriptions-item>
                <el-descriptions-item v-if="row.docUrl" label="官方文档" :span="2">
                  <el-link type="primary" :href="row.docUrl" target="_blank">
                    {{ row.docUrl }}
                  </el-link>
                </el-descriptions-item>
              </el-descriptions>

              <!-- 调用语法 -->
              <div class="detail-section">
                <h4 class="section-title">
                  调用语法
                  <el-tooltip content="点击复制" placement="top">
                    <el-button text size="small" type="primary" style="margin-left:8px" @click.stop="copySyntax(row)">
                      复制
                    </el-button>
                  </el-tooltip>
                </h4>
                <el-input
                  :model-value="getCallSyntax(row)"
                  type="textarea"
                  :rows="countLines(getCallSyntax(row))"
                  readonly
                  class="sql-input"
                />
              </div>

              <!-- 权限要求 -->
              <div v-if="row._detail.authorities?.length" class="detail-section">
                <h4 class="section-title">权限要求 (Authorization)</h4>
                <el-table :data="row._detail.authorities" border size="small" max-height="300">
                  <el-table-column prop="authority" label="权限要求" min-width="400" />
                  <el-table-column prop="context" label="适用场景" width="180">
                    <template #default="{ row: a }">
                      <el-tag v-if="a.context" size="small" type="info">{{ a.context }}</el-tag>
                      <span v-else style="color: var(--el-text-color-placeholder)">通用</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 参数定义 -->
              <div v-if="row._detail.parameters?.length" class="detail-section">
                <h4 class="section-title">参数定义 (Parameters)</h4>
                <el-table :data="row._detail.parameters" border size="small" max-height="400">
                  <el-table-column prop="paramName" label="参数名" min-width="200">
                    <template #default="{ row: p }">
                      <code class="code-text">{{ p.paramName }}</code>
                    </template>
                  </el-table-column>
                  <el-table-column prop="paramType" label="数据类型" width="140">
                    <template #default="{ row: p }">
                      <el-tag size="small" type="info">{{ p.paramType }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="paramDirection" label="方向" width="70" align="center">
                    <template #default="{ row: p }">
                      <el-tag :type="p.paramDirection === 'IN' ? 'info' : 'warning'" size="small">
                        {{ p.paramDirection }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="isRequired" label="必填" width="60" align="center">
                    <template #default="{ row: p }">
                      <span v-if="p.isRequired === 1" style="color: var(--el-color-danger)">是</span>
                      <span v-else style="color: var(--el-text-color-placeholder)">否</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="defaultValue" label="默认值" width="100">
                    <template #default="{ row: p }">
                      <code v-if="p.defaultValue" class="code-text">{{ p.defaultValue }}</code>
                      <span v-else style="color: var(--el-text-color-placeholder)">—</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="说明" min-width="250">
                    <template #default="{ row: p }">
                      <div class="multi-line" v-html="sanitizeHtml(formatDesc(p.description))"></div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 结果列定义 -->
              <div v-if="row._detail.columns?.length" class="detail-section">
                <h4 class="section-title">结果列定义 (Result Columns)</h4>
                <el-table :data="row._detail.columns" border size="small" max-height="500">
                  <el-table-column prop="columnName" label="列名" min-width="220">
                    <template #default="{ row: c }">
                      <code class="code-text">{{ c.columnName }}</code>
                    </template>
                  </el-table-column>
                  <el-table-column prop="systemColumnName" label="系统短名" width="130">
                    <template #default="{ row: c }">
                      <code v-if="c.systemColumnName" class="code-text">{{ c.systemColumnName }}</code>
                      <span v-else style="color: var(--el-text-color-placeholder)">—</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="dataType" label="数据类型" width="140">
                    <template #default="{ row: c }">
                      <el-tag size="small" type="info">{{ c.dataType }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="isNullable" label="可空" width="60" align="center">
                    <template #default="{ row: c }">
                      <span v-if="c.isNullable === 1" style="color: var(--el-text-color-secondary)">✓</span>
                      <span v-else style="color: var(--el-color-danger)">✗</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="说明" min-width="300">
                    <template #default="{ row: c }">
                      <div class="multi-line" v-html="sanitizeHtml(formatDesc(c.description))"></div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 示例代码 -->
              <div v-if="row._detail.examples?.length" class="detail-section">
                <h4 class="section-title">使用示例 (Examples)</h4>
                <div
                  v-for="(ex, idx) in row._detail.examples"
                  :key="idx"
                  class="example-block"
                >
                  <div v-if="ex.description" class="example-desc">{{ ex.description }}</div>
                  <el-input
                    :model-value="ex.sqlCode"
                    type="textarea"
                    :rows="countLines(ex.sqlCode)"
                    readonly
                    class="sql-input"
                  />
                </div>
              </div>
            </div>
            <!-- 未加载详情时的占位 -->
            <div v-else class="expand-detail expand-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载详情中...</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务名称" min-width="280">
          <template #default="{ row }">
            <div class="service-name-cell">
              <span class="schema-name">{{ getSchema(row.serviceName) }}</span>
              <span class="object-name">{{ getObject(row.serviceName) }}</span>
              <span v-if="getParams(row.serviceName)" class="param-hint">()</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceType" label="类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.serviceType)" size="small" effect="plain">
              {{ row.serviceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="briefDescription" label="功能描述" min-width="300" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh, Search, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCategoriesApi, getItemDetailApi } from '@/api/iService'
import { sanitizeHtml } from '@/utils/sanitize'

defineOptions({ name: 'As400IService' })

const loading = ref(false)
const detailLoading = ref(false)
const categories = ref([])
const activeCategory = ref(null)
const keyword = ref('')
const typeFilter = ref('')

const serviceTypes = [
  { label: 'Table Function', value: 'TABLE FUNCTION' },
  { label: 'View', value: 'VIEW' },
  { label: 'Procedure', value: 'PROCEDURE' },
  { label: 'Scalar Function', value: 'SCALAR FUNCTION' },
  { label: 'Table', value: 'TABLE' },
  { label: 'Global Variable', value: 'GLOBAL VARIABLE' },
]

const currentCategory = computed(() => {
  return categories.value.find(c => c.id === activeCategory.value) || null
})

const currentItems = computed(() => {
  return currentCategory.value?.items || []
})

const filteredItems = computed(() => {
  let items = currentItems.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    items = items.filter(
      item =>
        item.serviceName.toLowerCase().includes(kw) ||
        (item.briefDescription && item.briefDescription.toLowerCase().includes(kw))
    )
  }
  if (typeFilter.value) {
    items = items.filter(item => item.serviceType === typeFilter.value)
  }
  return items
})

// ---------- 格式化辅助 ----------
function getSchema(name) {
  const dotIdx = name.indexOf('.')
  return dotIdx > -1 ? name.substring(0, dotIdx + 1) : ''
}

function getObject(name) {
  const dotIdx = name.indexOf('.')
  const parenIdx = name.indexOf('(')
  if (dotIdx > -1) {
    const end = parenIdx > -1 ? parenIdx : name.length
    return name.substring(dotIdx + 1, end)
  }
  return name
}

function getParams(name) {
  return name.includes('(')
}

function getTypeTag(type) {
  const map = {
    'VIEW': 'success',
    'PROCEDURE': 'warning',
    'TABLE FUNCTION': 'primary',
    'SCALAR FUNCTION': 'info',
    'TABLE': 'info',
    'GLOBAL VARIABLE': 'danger',
  }
  return map[type] || 'info'
}

/** 生成调用语法语句 */
function getCallSyntax(row) {
  const name = row.serviceName
  const type = row.serviceType
  const params = row._detail?.parameters || []
  // 只取 IN 方向参数生成示例
  const inParams = params.filter(p => !p.paramDirection || p.paramDirection === 'IN' || p.paramDirection === 'INOUT')
  const args = inParams.map(p => {
    const val = p.defaultValue ? p.defaultValue.replace(/^'|'$/g, '') : `/* ${p.paramType || 'value'} */`
    return `${p.paramName} => ${val}`
  }).join(', ')

  // 去掉服务名中可能包含的 ()
  const cleanName = name.replace(/\(\)$/, '')

  switch (type) {
    case 'TABLE FUNCTION':
      return `SELECT * FROM TABLE(${cleanName}(${args}));`
    case 'PROCEDURE':
      return `CALL ${cleanName}(${args});`
    case 'SCALAR FUNCTION':
      return `SELECT ${cleanName}(${args}) FROM SYSIBM.SYSDUMMY1;`
    case 'VIEW':
      return `SELECT * FROM ${cleanName};`
    case 'TABLE':
      return `SELECT * FROM ${cleanName};`
    case 'GLOBAL VARIABLE':
      return `SET ${cleanName} = /* value */;`
    default:
      return `${cleanName}`
  }
}

/** 复制语法到剪贴板 */
function copySyntax(row) {
  const text = getCallSyntax(row)
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制调用语法')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

/** 将 \n 和枚举值格式化 */
function formatDesc(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/(枚举值[：:]?)/g, '<br><strong>$1</strong>')
}

/** 计算代码行数 */
function countLines(code) {
  return Math.min((code || '').split('\n').length + 1, 15)
}

// ---------- 展开加载详情 ----------
const loadedDetails = ref({})

async function handleExpandChange(row, expandedRows) {
  if (!expandedRows.includes(row)) return // 折叠时不处理
  // 已加载缓存
  if (row._detail) return
  if (loadedDetails.value[row.id]) {
    row._detail = loadedDetails.value[row.id]
    return
  }
  detailLoading.value = true
  try {
    const res = await getItemDetailApi(row.id)
    if (res.code === 200 && res.data) {
      row._detail = res.data
      loadedDetails.value[row.id] = res.data
    }
  } catch (e) {
    ElMessage.error('加载服务详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleRowClick(row) {
  // 已加载缓存
  if (row._detail) return
  if (loadedDetails.value[row.id]) {
    row._detail = loadedDetails.value[row.id]
    return
  }
  detailLoading.value = true
  try {
    const res = await getItemDetailApi(row.id)
    if (res.code === 200 && res.data) {
      row._detail = res.data
      loadedDetails.value[row.id] = res.data
    }
  } catch (e) {
    ElMessage.error('加载服务详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ---------- 数据加载 ----------
async function fetchData() {
  loading.value = true
  try {
    const res = await getCategoriesApi()
    if (res.code === 200) {
      categories.value = res.data || []
      if (categories.value.length > 0 && !activeCategory.value) {
        activeCategory.value = categories.value[0].id
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载服务数据失败: ' + (e.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.iservice-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--layout-content-offset, 107px));
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* 分类标签 */
.category-tabs {
  display: flex;
  gap: 0;
  padding: 0 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  flex-shrink: 0;
  overflow-x: auto;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
  user-select: none;
}

.category-tab:hover {
  color: var(--el-color-primary);
  background: var(--el-fill-color-light);
}

.category-tab.active {
  color: var(--el-color-primary);
  border-bottom-color: var(--el-color-primary);
  font-weight: 600;
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  font-size: 12px;
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
}

.category-tab.active .tab-count {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  flex-shrink: 0;
}

/* 表格容器 */
.table-container {
  flex: 1;
  overflow: auto;
  padding: 0 20px 20px;
}

.service-name-cell {
  display: flex;
  align-items: center;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.schema-name {
  color: var(--el-text-color-secondary);
}

.object-name {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.param-hint {
  color: var(--el-text-color-secondary);
}

/* 展开详情 */
.expand-detail {
  padding: 20px 40px;
  background: var(--el-fill-color-lighter);
}

.expand-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  min-height: 60px;
}

.detail-section {
  margin-top: 16px;
}

.section-title {
  margin: 0 0 8px 0;
  padding-left: 8px;
  padding-bottom: 4px;
  border-left: 3px solid var(--el-color-primary);
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.code-text {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  background: var(--el-fill-color);
  padding: 2px 6px;
  border-radius: 3px;
  color: var(--el-color-primary);
}

.full-desc {
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

.multi-line {
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

/* 示例块 */
.example-block {
  margin-bottom: 12px;
}

.example-desc {
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.sql-input :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  background: #1e1e1e;
  color: #dcdcaa;
  border-color: #333;
  line-height: 1.5;
}
</style>

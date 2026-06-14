<template>
  <div class="kg-page" v-loading="loading">
    <!-- 头部统计 -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#38bdf8"><Connection /></el-icon>
          <span>项目知识图谱</span>
        </div>
      </template>
      <div class="stats-row">
        <div class="kg-stat" v-for="s in summaryCards" :key="s.label">
          <span class="kg-stat-num">{{ s.value }}</span>
          <span class="kg-stat-label">{{ s.label }}</span>
        </div>
      </div>
    </el-card>

    <!-- 语言分布 + 架构层 -->
    <el-row :gutter="20" class="kg-row">
      <el-col :span="10">
        <el-card shadow="hover" class="section-card" style="height:100%">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#f59e0b"><PieChart /></el-icon>
              <span>语言分布</span>
            </div>
          </template>
          <div ref="langChartRef" class="kg-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover" class="section-card" style="height:100%">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#67c23a"><Grid /></el-icon>
              <span>架构层</span>
            </div>
          </template>
          <div ref="layerChartRef" class="kg-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 关系统计 + 框架标签 -->
    <el-row :gutter="20" class="kg-row">
      <el-col :span="12">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#e6a23c"><Link /></el-icon>
              <span>文件关系统计</span>
            </div>
          </template>
          <div ref="edgeChartRef" class="kg-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#f56c6c"><Collection /></el-icon>
              <span>技术栈</span>
            </div>
          </template>
          <div class="framework-tags">
            <el-tag v-for="fw in frameworks" :key="fw" type="primary" effect="plain" size="large">{{ fw }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 代码导览步骤 -->
    <el-card shadow="hover" class="section-card" v-if="tourSteps.length">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#f59e0b"><Compass /></el-icon>
          <span>代码导览</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="step in tourSteps"
          :key="step.order"
          :timestamp="'步骤 ' + step.order"
          placement="top"
          :color="timelineColors[step.order % timelineColors.length]"
        >
          <el-card shadow="hover" class="tour-card">
            <h4>{{ step.title }}</h4>
            <p>{{ step.description }}</p>
            <div class="tour-files">
              <el-tag
                v-for="nid in step.nodeIds"
                :key="nid"
                size="small"
                effect="plain"
                @click="selectFile(nid)"
                style="cursor:pointer;margin:2px;"
              >{{ fileNameById(nid) }}</el-tag>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 文件搜索 -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#909399"><Search /></el-icon>
          <span>文件检索（共 {{ totalFiles }} 个源文件）</span>
        </div>
      </template>
      <div style="margin-bottom:12px">
        <el-input v-model="searchQuery" placeholder="搜索文件名、路径或摘要..." clearable prefix-icon="Search" @input="doSearch" />
      </div>
      <div v-if="searchResults.length" class="search-results">
        <div
          v-for="f in searchResults"
          :key="f.id"
          class="search-item"
          @click="selectedFile = selectedFile === f.id ? null : f.id"
        >
          <div class="search-item-head">
            <span class="dot" :class="langClass(f.filePath)"></span>
            <span class="search-name">{{ f.name }}</span>
            <el-tag size="small" effect="plain">{{ f.complexity || 'simple' }}</el-tag>
          </div>
          <div class="search-path">{{ f.filePath }}</div>
          <div class="search-summary" v-if="f.summary">{{ f.summary }}</div>
          <!-- 展开详情 -->
          <div v-if="selectedFile === f.id" class="search-relations">
            <div v-if="fileRelations[f.id] && fileRelations[f.id].length" style="margin-top:6px;">
              <span style="font-size:11px;color:#94a3b8;">关联文件：</span>
              <el-tag
                v-for="rel in fileRelations[f.id]"
                :key="rel.target"
                size="small"
                style="margin:2px;cursor:pointer"
                @click="jumpToFile(rel.target)"
              >{{ rel.type }}: {{ fileNameById(rel.target) }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="searchQuery && !searchResults.length" class="empty-hint">无匹配结果</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { cyberTheme } from '@/utils/echartsTheme'
import {
  Connection, PieChart, Grid, Link, Collection, Compass, Search
} from '@element-plus/icons-vue'

const loading = ref(true)
const graph = ref(null)
const searchQuery = ref('')
const selectedFile = ref(null)
const fileRelations = ref({})

// chart refs
const langChartRef = ref(null)
const layerChartRef = ref(null)
const edgeChartRef = ref(null)
let langChart = null, layerChart = null, edgeChart = null

// Colors
const colorPalette = ['#f59e0b', '#10b981', '#e6a23c', '#ef4444', '#8b5cf6', '#6b7280', '#38bdf8', '#ec4899', '#14b8a6', '#f97316']
const timelineColors = ['#f59e0b', '#10b981', '#e6a23c', '#ef4444', '#8b5cf6']

const frameworks = computed(() => graph.value?.project?.frameworks || [])
const totalFiles = computed(() => {
  if (!graph.value) return 0
  return graph.value.nodes.filter(n => n.type === 'file').length
})

const summaryCards = computed(() => {
  if (!graph.value) return []
  return [
    { label: '源文件', value: graph.value.nodes.filter(n => n.type === 'file').length },
    { label: '类/接口', value: graph.value.nodes.filter(n => n.type === 'class').length },
    { label: '关系边', value: graph.value.edges.length },
    { label: '架构层', value: graph.value.layers?.length || 0 },
    { label: '导览步骤', value: graph.value.tour?.length || 0 },
    { label: '编程语言', value: graph.value.project?.languages?.length || 0 },
  ]
})

const tourSteps = computed(() => (graph.value?.tour || []).sort((a, b) => (a.order || 0) - (b.order || 0)))

const searchResults = computed(() => {
  if (!graph.value || !searchQuery.value || searchQuery.value.length < 2) return []
  const q = searchQuery.value.toLowerCase()
  return graph.value.nodes
    .filter(n => n.type === 'file')
    .filter(f =>
      f.name.toLowerCase().includes(q) ||
      f.filePath.toLowerCase().includes(q) ||
      (f.summary && f.summary.toLowerCase().includes(q))
    )
    .slice(0, 50)
})

function langClass(fp) {
  if (fp.endsWith('.java')) return 'java'
  if (fp.endsWith('.vue')) return 'vue'
  if (fp.endsWith('.js')) return 'javascript'
  if (fp.endsWith('.scss') || fp.endsWith('.css')) return 'scss'
  return ''
}

function fileNameById(id) {
  const n = graph.value?.nodes.find(x => x.id === id)
  return n ? n.name : id
}

function selectFile(id) {
  selectedFile.value = selectedFile.value === id ? null : id
  if (selectedFile.value && !fileRelations.value[id]) {
    buildFileRelations(id)
  }
}

function jumpToFile(id) {
  selectedFile.value = id
  if (!fileRelations.value[id]) buildFileRelations(id)
}

function buildFileRelations(id) {
  const edges = graph.value.edges.filter(e => e.source === id || e.target === id)
  const rels = edges.map(e => ({
    type: e.type,
    target: e.source === id ? e.target : e.source,
    direction: e.source === id ? 'out' : 'in'
  }))
  fileRelations.value[id] = rels
}

function doSearch() {
  selectedFile.value = null
}

// Charts
function renderLangChart() {
  if (!langChartRef.value || !graph.value) return
  const files = graph.value.nodes.filter(n => n.type === 'file')
  const langCount = {}
  files.forEach(f => {
    const lang = graph.value.project.languages.find(l => f.filePath.endsWith('.' + l)) || 'other'
    langCount[lang] = (langCount[lang] || 0) + 1
  })
  const data = Object.entries(langCount).map(([k, v]) => ({ name: k, value: v }))
  if (!langChart) langChart = echarts.init(langChartRef.value, cyberTheme)
  langChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['45%', '72%'], center: ['50%', '50%'],
      data: data.map((d, i) => ({ ...d, itemStyle: { color: colorPalette[i % colorPalette.length] } })),
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

function renderLayerChart() {
  if (!layerChartRef.value || !graph.value || !graph.value.layers) return
  const names = graph.value.layers.map(l => l.name)
  const counts = graph.value.layers.map(l => l.nodeIds.length)
  if (!layerChart) layerChart = echarts.init(layerChartRef.value, cyberTheme)
  layerChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '6%', top: 10, bottom: 5, containLabel: true },
    xAxis: { type: 'value', name: '文件数' },
    yAxis: { type: 'category', data: names.reverse(), axisLabel: { fontSize: 11 } },
    series: [{
      type: 'bar',
      data: counts.reverse().map((v, i) => ({ value: v, itemStyle: { color: colorPalette[i % colorPalette.length], borderRadius: [0, 4, 4, 0] } })),
      barMaxWidth: 28,
      label: { show: true, position: 'right', fontSize: 11 }
    }]
  })
}

function renderEdgeChart() {
  if (!edgeChartRef.value || !graph.value) return
  const edgeTypes = {}
  graph.value.edges.forEach(e => { edgeTypes[e.type] = (edgeTypes[e.type] || 0) + 1 })
  const data = Object.entries(edgeTypes).sort((a, b) => b[1] - a[1])
  if (!edgeChart) edgeChart = echarts.init(edgeChartRef.value, cyberTheme)
  edgeChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '6%', top: 10, bottom: 5, containLabel: true },
    xAxis: { type: 'value', name: '边数' },
    yAxis: { type: 'category', data: data.map(d => d[0]).reverse(), axisLabel: { fontSize: 11 } },
    series: [{
      type: 'bar',
      data: data.map(d => d[1]).reverse().map((v, i) => ({ value: v, itemStyle: { color: colorPalette[i % colorPalette.length], borderRadius: [0, 4, 4, 0] } })),
      barMaxWidth: 28,
      label: { show: true, position: 'right', fontSize: 11 }
    }]
  })
}

function disposeAll() {
  [langChart, layerChart, edgeChart].forEach(c => { if (c) { c.dispose(); c = null } })
}

function onResize() {
  [langChart, layerChart, edgeChart].forEach(c => { if (c) c.resize() })
}

async function loadGraph() {
  loading.value = true
  try {
    const res = await fetch('/data/knowledge-graph.json')
    graph.value = await res.json()
    await nextTick()
    renderLangChart()
    renderLayerChart()
    renderEdgeChart()
  } catch (e) {
    console.warn('加载知识图谱失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadGraph()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  disposeAll()
})
</script>

<style scoped lang="scss">
.kg-page {
  padding: 20px 24px;
  background: var(--bg-page);
  min-height: 100%;
}

.section-card {
  margin-bottom: 20px;
  border-radius: 12px;
  background: var(--bg-container);
  border: none;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
}

.section-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-lighter);
}
.section-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  padding-left: 14px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 18px;
    border-radius: 2px;
    background: var(--color-primary);
  }
}

.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.kg-stat {
  flex: 1;
  min-width: 100px;
  text-align: center;
  padding: 12px 8px;
  background: var(--bg-hover);
  border-radius: 10px;
}

.kg-stat-num {
  font-size: 26px;
  font-weight: 700;
  color: #38bdf8;
  display: block;
}

.kg-stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  display: block;
}

.kg-row {
  .section-card { height: 100%; }
}

.kg-chart {
  width: 100%;
  height: 300px;
}

.framework-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 10px 0;
}

.tour-card {
  margin-bottom: 4px;
  h4 { margin: 0 0 6px; font-size: 14px; color: var(--text-primary); }
  p { margin: 0 0 6px; font-size: 12px; color: var(--text-secondary); }
}

.search-results {
  max-height: 480px;
  overflow-y: auto;
}

.search-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  border: 1px solid transparent;

  &:hover { background: var(--bg-hover); }
}

.search-item-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  &.java { background: #f97316; }
  &.vue { background: #22c55e; }
  &.javascript { background: #eab308; }
  &.scss { background: #ec4899; }
}

.search-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.search-path {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
  word-break: break-all;
}

.search-summary {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.search-relations {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-lighter);
}

.empty-hint {
  text-align: center;
  padding: 30px;
  color: var(--text-secondary);
  font-size: 13px;
}
</style>
<template>
  <div class="analysis-container">
    <!-- 顶部搜索区 -->
    <div class="search-bar">
      <el-autocomplete
        v-model="menuName"
        :fetch-suggestions="searchMenus"
        placeholder="输入菜单名称，如：红楼人物、西游人物..."
        clearable
        style="width: 400px"
        @select="handleSelect"
        @keyup.enter="handleAnalyze"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-autocomplete>
      <el-button type="primary" :loading="loading" @click="handleAnalyze">
        <el-icon><DataAnalysis /></el-icon> 分析
      </el-button>
      <el-button @click="handleExportMd" :disabled="!analysisData">
        <el-icon><Download /></el-icon> 导出 Markdown
      </el-button>
    </div>

    <!-- 快捷入口 -->
    <div v-if="!analysisData" class="quick-menus">
      <el-text type="info" style="margin-right: 12px">快速分析：</el-text>
      <el-tag
        v-for="menu in quickMenus"
        :key="menu"
        style="cursor: pointer; margin-right: 8px"
        :type="menu === menuName ? 'primary' : 'info'"
        @click="menuName = menu; handleAnalyze()"
      >
        {{ menu }}
      </el-tag>
    </div>

    <el-divider v-if="!analysisData" />

    <!-- 初始引导 -->
    <el-empty v-if="!analysisData && !loading" description="请输入菜单名称开始分析，例如「红楼人物」" />

    <!-- 加载中 -->
    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 分析结果 -->
    <div v-if="analysisData && !loading" class="analysis-result">
      <!-- 标题 -->
      <div class="result-header">
        <h2>
          {{ analysisData.menuName }}
          <el-tag type="primary" size="small" style="margin-left: 8px">{{ analysisData.module }}</el-tag>
        </h2>
        <p class="desc">{{ analysisData.description }}</p>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- Tab 1: 数据流 -->
        <el-tab-pane label="数据流" name="flow">
          <el-timeline>
            <el-timeline-item
              v-for="step in analysisData.dataFlow"
              :key="step.seq"
              :timestamp="'第' + step.seq + '步 · ' + step.layer"
              placement="top"
              :type="step.seq <= 4 ? 'primary' : step.seq <= 8 ? 'success' : 'warning'"
            >
              <el-card shadow="hover">
                <template #header>
                  <strong>{{ step.component }}</strong>
                </template>
                <p><el-tag size="small" type="info">操作</el-tag> {{ step.action }}</p>
                <p v-if="step.detail"><el-tag size="small" type="warning">详情</el-tag> {{ step.detail }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <!-- Tab 2: 接口列表 -->
        <el-tab-pane label="接口列表" name="apis">
          <el-table :data="analysisData.apis" border stripe style="width: 100%">
            <el-table-column prop="method" label="方法" width="80">
              <template #default="{ row }">
                <el-tag :type="methodTagType(row.method)" size="small">{{ row.method }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="path" label="接口路径" min-width="260" show-overflow-tooltip />
            <el-table-column prop="description" label="说明" width="120" />
            <el-table-column prop="params" label="请求参数" min-width="180" show-overflow-tooltip />
            <el-table-column prop="returns" label="返回类型" min-width="180" show-overflow-tooltip />
            <el-table-column prop="auth" label="权限" width="150" show-overflow-tooltip />
            <el-table-column prop="frontendFunc" label="前端调用函数" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: 实体字段 -->
        <el-tab-pane label="实体字段" name="entity">
          <el-table :data="analysisData.entityFields" border stripe style="width: 100%">
            <el-table-column prop="name" label="字段名" width="160" />
            <el-table-column prop="type" label="类型" width="140" />
            <el-table-column prop="description" label="说明" min-width="200" />
            <el-table-column prop="example" label="示例值" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <!-- Tab 4: Service方法 -->
        <el-tab-pane label="Service方法" name="service">
          <el-table :data="analysisData.serviceMethods" border stripe style="width: 100%">
            <el-table-column prop="signature" label="方法签名" min-width="280" show-overflow-tooltip />
            <el-table-column prop="description" label="说明" width="140" />
            <el-table-column prop="detail" label="实现逻辑" min-width="300" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <!-- Tab 5: 前端组件 -->
        <el-tab-pane label="前端组件" name="frontend">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="组件名">{{
              analysisData.frontendStructure?.componentName
            }}</el-descriptions-item>
            <el-descriptions-item label="文件路径">{{ analysisData.frontendStructure?.filePath }}</el-descriptions-item>
            <el-descriptions-item label="技术栈">{{ analysisData.frontendStructure?.techStack }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">组件结构</el-divider>
          <ul class="section-list">
            <li v-for="(s, i) in analysisData.frontendStructure?.sections" :key="i">{{ s }}</li>
          </ul>
          <el-divider v-if="analysisData.frontendStructure?.stateFlow?.length" content-position="left"
            >数据状态流转</el-divider
          >
          <ul class="section-list">
            <li v-for="(s, i) in analysisData.frontendStructure?.stateFlow" :key="i">{{ s }}</li>
          </ul>
          <el-divider v-if="analysisData.frontendStructure?.permissionControls?.length" content-position="left"
            >权限控制</el-divider
          >
          <ul class="section-list">
            <li v-for="(s, i) in analysisData.frontendStructure?.permissionControls" :key="i">{{ s }}</li>
          </ul>
        </el-tab-pane>

        <!-- Tab 6: 流程图 -->
        <el-tab-pane label="流程图" name="diagrams">
          <el-radio-group v-model="diagramType" style="margin-bottom: 16px">
            <el-radio-button value="sequence">时序图</el-radio-button>
            <el-radio-button value="flowchart">调用关系图</el-radio-button>
          </el-radio-group>
          <div class="mermaid-wrapper" v-if="diagramType === 'sequence'">
            <pre class="mermaid-code">{{ analysisData.mermaidDiagrams?.sequenceDiagram }}</pre>
          </div>
          <div class="mermaid-wrapper" v-if="diagramType === 'flowchart'">
            <pre class="mermaid-code">{{ analysisData.mermaidDiagrams?.flowchart }}</pre>
          </div>
          <el-alert title="提示" type="info" :closable="false" show-icon style="margin-top: 12px">
            <template #default>
              复制上方 Mermaid 代码到支持 Mermaid 的工具中即可渲染为图形（如 GitHub、Typora、Notion、语雀等）。
              或点击「导出 Markdown」按钮下载完整文档。
            </template>
          </el-alert>
        </el-tab-pane>

        <!-- Tab 7: 权限标识 -->
        <el-tab-pane label="权限标识" name="permissions">
          <el-table :data="permTableData" border stripe style="width: 100%">
            <el-table-column prop="perm" label="权限标识" min-width="300" />
            <el-table-column prop="desc" label="说明" min-width="200" />
          </el-table>
        </el-tab-pane>

        <!-- Tab 8: 概览 -->
        <el-tab-pane label="概览" name="overview">
          <el-descriptions :column="2" border>
            <el-descriptions-item v-for="(val, key) in analysisData.overview" :key="key" :label="key">
              {{ val }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolAnalysis' })
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnalysisMenusApi, analyzeMenuApi } from '@/api/analysis'

const menuName = ref('')
const loading = ref(false)
const analysisData = ref(null)
const activeTab = ref('flow')
const diagramType = ref('sequence')

const quickMenus = ['红楼人物', '红楼诗词', '红楼关系', '西游人物', '三国人物', '水浒章节']

onMounted(async () => {
  // 预加载菜单列表用于自动补全
  try {
    const res = await getAnalysisMenusApi()
    allMenus.value = res.data.map((m) => m.name)
  } catch {}
})

const allMenus = ref([])

function searchMenus(queryString, cb) {
  const results = queryString
    ? allMenus.value.filter((m) => m.includes(queryString)).map((m) => ({ value: m }))
    : allMenus.value.map((m) => ({ value: m }))
  cb(results)
}

function handleSelect(item) {
  menuName.value = item.value
  handleAnalyze()
}

async function handleAnalyze() {
  if (!menuName.value.trim()) {
    ElMessage.warning('请输入菜单名称')
    return
  }
  loading.value = true
  analysisData.value = null
  try {
    const res = await analyzeMenuApi(menuName.value.trim())
    analysisData.value = res.data
    activeTab.value = 'flow'
    ElMessage.success('分析完成')
  } catch {
    ElMessage.error('分析失败，请检查菜单名称')
  } finally {
    loading.value = false
  }
}

function methodTagType(method) {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

const permTableData = computed(() => {
  if (!analysisData.value?.permissions) return []
  return analysisData.value.permissions.map((p) => {
    const [perm, ...rest] = p.split(' - ')
    return { perm, desc: rest.join(' - ') }
  })
})

function handleExportMd() {
  if (!analysisData.value) return
  const d = analysisData.value
  let md = `# ${d.menuName} 接口分析文档\n\n`
  md += `> 模块：${d.module}  \n`
  md += `> 描述：${d.description}  \n`
  md += `> 生成时间：${new Date().toLocaleString()}\n\n`

  // 概览
  md += `## 概览\n\n`
  md += `| 属性 | 值 |\n|------|----|\n`
  for (const [k, v] of Object.entries(d.overview || {})) {
    md += `| ${k} | ${v} |\n`
  }

  // 数据流
  md += `\n## 数据流\n\n`
  md += `| 步骤 | 层级 | 组件 | 操作 | 详情 |\n|------|------|------|------|------|\n`
  for (const s of d.dataFlow || []) {
    md += `| ${s.seq} | ${s.layer} | ${s.component} | ${s.action} | ${s.detail || '-'} |\n`
  }

  // 接口列表
  md += `\n## 接口列表\n\n`
  md += `| 方法 | 路径 | 说明 | 参数 | 返回 | 权限 | 前端函数 |\n|------|------|------|------|------|------|------|\n`
  for (const a of d.apis || []) {
    md += `| ${a.method} | ${a.path} | ${a.description} | ${a.params} | ${a.returns} | ${a.auth} | ${a.frontendFunc} |\n`
  }

  // 实体字段
  md += `\n## 实体字段 (${d.overview?.entityClass} / ${d.overview?.tableName})\n\n`
  md += `| 字段 | 类型 | 说明 | 示例 |\n|------|------|------|------|\n`
  for (const f of d.entityFields || []) {
    md += `| ${f.name} | ${f.type} | ${f.description} | ${f.example} |\n`
  }

  // Service方法
  md += `\n## Service方法\n\n`
  md += `| 方法签名 | 说明 | 实现逻辑 |\n|------|------|------|\n`
  for (const m of d.serviceMethods || []) {
    md += `| ${m.signature} | ${m.description} | ${m.detail} |\n`
  }

  // 前端组件
  md += `\n## 前端组件\n\n`
  md += `- 组件名：${d.frontendStructure?.componentName}\n`
  md += `- 文件路径：${d.frontendStructure?.filePath}\n`
  md += `- 技术栈：${d.frontendStructure?.techStack}\n`

  // 权限
  md += `\n## 权限标识\n\n`
  for (const p of d.permissions || []) {
    md += `- ${p}\n`
  }

  // 流程图
  md += `\n## 时序图 (Mermaid)\n\n`
  md += '```mermaid\n' + (d.mermaidDiagrams?.sequenceDiagram || '') + '\n```\n'
  md += `\n## 调用关系图 (Mermaid)\n\n`
  md += '```mermaid\n' + (d.mermaidDiagrams?.flowchart || '') + '\n```\n'

  // 下载
  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${d.menuName}_分析文档.md`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('Markdown 文档已下载')
}
</script>

<style scoped>
.analysis-container {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.quick-menus {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.loading-area {
  padding: 20px;
}

.analysis-result {
  flex: 1;
  overflow: auto;
}

.result-header h2 {
  margin-bottom: 4px;
}

.result-header .desc {
  color: var(--el-text-color-secondary);
  margin-bottom: 16px;
}

.mermaid-wrapper {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
}

.mermaid-code {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre;
  margin: 0;
}

.section-list {
  padding-left: 20px;
  line-height: 2;
}

.section-list li {
  color: var(--el-text-color-regular);
}
</style>

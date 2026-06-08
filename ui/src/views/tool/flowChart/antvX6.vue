<template>
  <div class="x6-page">
    <!-- 工具栏 -->
    <div class="x6-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">X6 流程图</span>
        <el-divider direction="vertical" />
        <el-button size="small" @click="addNode('rect')">
          <svg width="14" height="14" viewBox="0 0 16 16"><rect x="1" y="3" width="14" height="10" fill="none" stroke="currentColor" stroke-width="1.5" rx="1"/></svg>
          矩形
        </el-button>
        <el-button size="small" @click="addNode('diamond')">
          <svg width="14" height="14" viewBox="0 0 16 16"><polygon points="8,0 16,8 8,16 0,8" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>
          菱形
        </el-button>
        <el-button size="small" @click="addNode('circle')">
          <svg width="14" height="14" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>
          圆形
        </el-button>
        <el-button size="small" @click="addNode('ellipse')">
          <svg width="14" height="14" viewBox="0 0 16 16"><ellipse cx="8" cy="8" rx="7" ry="5" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>
          椭圆
        </el-button>
        <el-divider direction="vertical" />
        <el-color-picker v-model="nodeColor" size="small" :predefine="predefineColors" />
        <el-divider direction="vertical" />
        <el-select v-model="fontSize" size="small" style="width:70px">
          <el-option v-for="s in [12,14,16,18,20,24]" :key="s" :label="s+'px'" :value="s" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button size="small" @click="undo" :disabled="historyIndex <= 0">
          <el-icon><DArrowLeft /></el-icon> 撤销
        </el-button>
        <el-button size="small" @click="redo" :disabled="historyIndex >= history.length - 1">
          重做 <el-icon><DArrowRight /></el-icon>
        </el-button>
        <el-button size="small" @click="clearCanvas" :disabled="!graph?.value">
          <el-icon><Close /></el-icon> 清空
        </el-button>
        <el-button size="small" type="primary" @click="exportImage">
          <el-icon><Download /></el-icon> 导出图片
        </el-button>
      </div>
    </div>

    <!-- 画布容器 -->
    <div ref="containerRef" class="x6-container"></div>

    <!-- 编辑文字弹窗 -->
    <el-dialog v-model="editDialog.visible" title="编辑文字" width="360px" destroy-on-close>
      <el-input v-model="editDialog.text" placeholder="输入文字" @keyup.enter="confirmEditText" />
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmEditText">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { Graph } from '@antv/x6'
import { DArrowLeft, DArrowRight, Close, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { registerCustomShapes } from './shapes'
registerCustomShapes()

// ==================== 状态 ====================
const containerRef = ref(null)
const graph = ref(null)
const nodeColor = ref('#409EFF')
const fontSize = ref(14)
const predefineColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8B5CF6', '#EC4899', '#14B8A6']

let nodeIdCounter = 1

// 撤销重做
const history = ref([])
const historyIndex = ref(-1)

// 编辑弹窗
const editDialog = reactive({ visible: false, text: '', targetNodeId: null })

// graph 是否已初始化
const graphReady = computed(() => !!graph.value)


// ==================== 初始化 X6 ====================
let initTimer = null

onMounted(() => {
  initGraph()
})

function initGraph() {
  // 防止重复初始化
  if (graph.value) return

  // 确保 DOM 完全渲染后再初始化 X6
  if (!containerRef.value) {
    initTimer = setTimeout(initGraph, Number(import.meta.env.VITE_FLOWCHART_INIT_RETRY_MS) || 50)
    return
  }
  const w = containerRef.value.clientWidth
  const h = containerRef.value.clientHeight
  if (!w || !h) {
    initTimer = setTimeout(initGraph, Number(import.meta.env.VITE_FLOWCHART_INIT_RETRY_MS) || 50)
    return
  }
  graph.value = new Graph({
    container: containerRef.value,
    width: w,
    height: h,
    background: { color: '#fafafa' },
    grid: {
      size: 20,
      type: 'dot',
      args: { color: '#e0e0e0', thickness: 1 },
    },
    snapline: true,
    mousewheel: true,
    connecting: {
      snap: true,
      anchor: 'center',
      connectionPoint: 'boundary',
      router: 'manhattan',
      connector: 'rounded',
      style: { stroke: '#909399', strokeWidth: 2 },
    },
    defaultNode: {
      attrs: {
        body: { stroke: '#409EFF', strokeWidth: 2, fill: '#ffffff' },
        label: { fontSize: 14, fill: '#303133' },
      },
      router: 'manhattan',
      connector: 'rounded',
    },
    defaultEdge: {
      attrs: {
        line: {
          stroke: '#909399',
          strokeWidth: 2,
          targetMarker: { name: 'classic', size: 8 },
        },
      },
      router: 'manhattan',
      connector: 'rounded',
    },
    keyboard: { enabled: true },
    clipboard: { enabled: true },
  })

  graph.value.on('node:doubleclick', ({ cell }) => {
    editDialog.targetNodeId = cell.id
    editDialog.text = cell.attr('label/text') || ''
    editDialog.visible = true
  })

  graph.value.on('blank:doubleclick', () => {
    editDialog.visible = false
  })

  graph.value.on('cell:added', saveHistory)
  graph.value.on('cell:removed', saveHistory)

  window.addEventListener('resize', handleResize)
  saveHistory()
}

onBeforeUnmount(() => {
  if (initTimer) clearTimeout(initTimer)
  window.removeEventListener('resize', handleResize)
  if (graph.value) {
    graph.value.dispose()
    graph.value = null
  }
})

function handleResize() {
  if (graph.value && containerRef.value && containerRef.value.clientWidth) {
    graph.value.resize(containerRef.value.clientWidth, containerRef.value.clientHeight)
  }
}

// ==================== 添加节点 ====================
function addNode(type) {
  if (!graph.value) return

  const container = containerRef.value
  const centerX = container.clientWidth / 2
  const centerY = container.clientHeight / 2
  const typeLabels = { rect: '矩形', diamond: '菱形', circle: '圆形', ellipse: '椭圆' }
  const color = nodeColor.value
  const id = `node_${nodeIdCounter++}`
  const x = centerX + (Math.random() - 0.5) * 200
  const y = centerY + (Math.random() - 0.5) * 150

  const nodeMap = {
    rect: () => graph.value.addNode({
      id, x: x - 50, y: y - 25, width: 100, height: 50,
      attrs: { body: { stroke: color, fill: '#ffffff' }, label: { text: typeLabels[type], fontSize: fontSize.value, fill: '#303133' } },
    }),
    diamond: () => graph.value.addNode({
      id, x: x - 50, y: y - 30, width: 100, height: 60, shape: 'diamond-shape',
      attrs: { body: { stroke: color }, label: { text: typeLabels[type], fontSize: fontSize.value, fill: '#303133' } },
    }),
    circle: () => graph.value.addNode({
      id, x: x - 35, y: y - 35, width: 70, height: 70, shape: 'circle-shape',
      attrs: { body: { stroke: color }, label: { text: typeLabels[type], fontSize: fontSize.value, fill: '#303133' } },
    }),
    ellipse: () => graph.value.addNode({
      id, x: x - 60, y: y - 30, width: 120, height: 60, shape: 'ellipse-shape',
      attrs: { body: { stroke: color }, label: { text: typeLabels[type], fontSize: fontSize.value, fill: '#303133' } },
    }),
  }

  nodeMap[type]?.()
}

// ==================== 编辑文字 ====================
function confirmEditText() {
  if (!graph.value || !editDialog.targetNodeId) {
    editDialog.visible = false
    return
  }
  const cell = graph.value.getCellById(editDialog.targetNodeId)
  if (cell) cell.attr('label/text', editDialog.text)
  editDialog.visible = false
}

// ==================== 撤销 / 重做 ====================
function saveHistory() {
  if (!graph.value) return
  const data = graph.value.toJSON()
  history.value = history.value.slice(0, historyIndex.value + 1)
  history.value.push(JSON.stringify(data))
  historyIndex.value = history.value.length - 1
  if (history.value.length > 50) {
    history.value.shift()
    historyIndex.value--
  }
}

function undo() {
  if (historyIndex.value <= 0) return
  historyIndex.value--
  restoreHistory()
}

function redo() {
  if (historyIndex.value >= history.value.length - 1) return
  historyIndex.value++
  restoreHistory()
}

function restoreHistory() {
  if (!graph.value) return
  const data = JSON.parse(history.value[historyIndex.value])
  graph.value.clearCells()
  graph.value.fromJSON(data)
}

function clearCanvas() {
  if (!graph.value) return
  graph.value.clearCells()
  nodeIdCounter = 1
  saveHistory()
}

// ==================== 导出图片 ====================
function exportImage() {
  if (!graph.value) {
    ElMessage.warning('画布未就绪')
    return
  }
  try {
    const dataURL = graph.value.toPNG({ backgroundColor: '#ffffff', padding: 20 })
    const link = document.createElement('a')
    link.download = `流程图_${new Date().toLocaleDateString()}.png`
    link.href = dataURL
    link.click()
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败: ' + e.message)
  }
}
</script>

<style scoped>
.x6-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 107px);
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
}

.x6-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 8px;
  z-index: 10;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 6px;
}
.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.x6-container {
  flex: 1;
  min-height: 0;
}
</style>

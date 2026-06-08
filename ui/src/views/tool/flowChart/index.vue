<template>
  <div class="flowchart-page">
    <!-- 工具栏 -->
    <div class="fc-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">流程图编辑器</span>
        <el-divider direction="vertical" />
        <el-button size="small" @click="addNode('default')">
          <svg width="14" height="14" viewBox="0 0 16 16"><rect x="1" y="3" width="14" height="10" fill="none" stroke="currentColor" stroke-width="1.5" rx="1"/></svg>
          矩形
        </el-button>
        <el-button size="small" @click="addNode('diamond')">
          <svg width="14" height="14" viewBox="0 0 16 16"><polygon points="8,0 16,8 8,16 0,8" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>
          菱形
        </el-button>
        <el-button size="small" @click="addNode('roundRect')">
          <svg width="14" height="14" viewBox="0 0 16 16"><rect x="1" y="3" width="14" height="10" rx="5" ry="5" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>
          圆角矩形
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
        <el-button size="small" @click="clearCanvas" :disabled="nodes.length === 0 && edges.length === 0">
          <el-icon><Delete /></el-icon> 清空
        </el-button>
        <el-button size="small" type="primary" @click="exportImage">
          <el-icon><Download /></el-icon> 导出图片
        </el-button>
      </div>
    </div>

    <!-- 画布 -->
    <div class="fc-canvas-wrapper">
      <VueFlow
        ref="vueFlowRef"
        :nodes="nodes"
        :edges="edges"
        :default-viewport="{ x: 0, y: 0, zoom: 1 }"
        :min-zoom="0.2"
        :max-zoom="4"
        :snap-to-grid="true"
        :snap-grid="[20, 20]"
        :connection-line-style="{ stroke: '#409EFF', strokeWidth: 2 }"
        :default-edge-options="defaultEdgeOptions"
        fit-view-on-init
        @connect="onConnect"
        @node-double-click="onNodeDblClick"
        @pane-click="onPaneClick"
        @pane-context-menu="onPaneContextMenu"
        @node-context-menu="onNodeContextMenu"
      >
        <!-- 背景网格 -->
        <Background :gap="20" :size="1" />

        <!-- 缩放控件 -->
        <Controls position="bottom-right" />

        <!-- 小地图 -->
        <MiniMap position="bottom-left" />

        <!-- 自定义节点模板 -->
        <template #node-default="nodeProps">
          <FlowNode
            :id="nodeProps.id"
            :data="nodeProps.data"
            :selected="nodeProps.selected"
            :type="nodeProps.data?.shape || 'rect'"
          />
        </template>

        <template #node-diamond="nodeProps">
          <FlowNode
            :id="nodeProps.id"
            :data="nodeProps.data"
            :selected="nodeProps.selected"
            type="diamond"
          />
        </template>

        <template #node-roundRect="nodeProps">
          <FlowNode
            :id="nodeProps.id"
            :data="nodeProps.data"
            :selected="nodeProps.selected"
            type="roundRect"
          />
        </template>

        <template #node-ellipse="nodeProps">
          <FlowNode
            :id="nodeProps.id"
            :data="nodeProps.data"
            :selected="nodeProps.selected"
            type="ellipse"
          />
        </template>
      </VueFlow>
    </div>

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
import { ref, reactive, markRaw, h, nextTick } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { DArrowLeft, DArrowRight, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { MarkerType } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import FlowNode from './FlowNode.vue'

defineOptions({ name: 'ToolFlowChart' })

// ==================== 状态 ====================
const vueFlowRef = ref(null)
const nodes = ref([])
const edges = ref([])
const nodeColor = ref('#409EFF')
const fontSize = ref(14)
const predefineColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8B5CF6', '#EC4899', '#14B8A6']

let nodeId = 1

// 默认连线样式
const defaultEdgeOptions = {
  type: 'smoothstep',
  animated: false,
  style: { stroke: '#909399', strokeWidth: 2 },
  markerEnd: MarkerType.ArrowClosed,
}

// 撤销重做
const history = ref([{ nodes: [], edges: [] }])
const historyIndex = ref(0)

// 编辑弹窗
const editDialog = reactive({ visible: false, text: '', targetId: null })

// 右键菜单目标
const contextNodeId = ref(null)

// ==================== 添加节点 ====================
function addNode(shape) {
  const colors = {
    rect: '#409EFF',
    diamond: '#E6A23C',
    roundRect: '#67C23A',
    ellipse: '#8B5CF6',
  }

  const nodeType = shape === 'default' || shape === 'rect' ? 'default' : shape

  const newNode = {
    id: `node_${nodeId++}`,
    type: nodeType,
    position: { x: 100 + Math.random() * 300, y: 100 + Math.random() * 200 },
    data: {
      label: '',
      shape: shape === 'default' ? 'rect' : shape,
      color: nodeColor.value,
      fontSize: fontSize.value,
    },
  }
  nodes.value.push(newNode)
  saveHistory()
}

// ==================== 连线事件 ====================
function onConnect(connection) {
  edges.value.push({
    id: `edge_${connection.source}-${connection.target}`,
    source: connection.source,
    target: connection.target,
    sourceHandle: connection.sourceHandle,
    targetHandle: connection.targetHandle,
    type: 'smoothstep',
    animated: false,
    style: { stroke: nodeColor.value, strokeWidth: 2 },
    markerEnd: MarkerType.ArrowClosed,
  })
  saveHistory()
}

// ==================== 节点双击编辑 ====================
function onNodeDblClick({ node }) {
  editDialog.targetId = node.id
  editDialog.text = node.data.label || ''
  editDialog.visible = true
}

function confirmEditText() {
  const node = nodes.value.find(n => n.id === editDialog.targetId)
  if (node) {
    node.data = { ...node.data, label: editDialog.text }
    saveHistory()
  }
  editDialog.visible = false
}

// ==================== 右键菜单 ====================
function onNodeContextMenu({ event, node }) {
  event.preventDefault()
  contextNodeId.value = node.id
}

function onPaneContextMenu(event) {
  event.preventDefault()
  contextNodeId.value = null
}

// 空白区域点击添加节点
function onPaneClick() {
  contextNodeId.value = null
}

// ==================== 撤销 / 重做 ====================
function saveHistory() {
  const snapshot = {
    nodes: JSON.parse(JSON.stringify(nodes.value)),
    edges: JSON.parse(JSON.stringify(edges.value)),
  }
  history.value = history.value.slice(0, historyIndex.value + 1)
  history.value.push(snapshot)
  historyIndex.value = history.value.length - 1
  if (history.value.length > 50) {
    history.value.shift()
    historyIndex.value--
  }
}

function undo() {
  if (historyIndex.value <= 0) return
  historyIndex.value--
  nodes.value = JSON.parse(JSON.stringify(history.value[historyIndex.value].nodes))
  edges.value = JSON.parse(JSON.stringify(history.value[historyIndex.value].edges))
}

function redo() {
  if (historyIndex.value >= history.value.length - 1) return
  historyIndex.value++
  nodes.value = JSON.parse(JSON.stringify(history.value[historyIndex.value].nodes))
  edges.value = JSON.parse(JSON.stringify(history.value[historyIndex.value].edges))
}

function clearCanvas() {
  nodes.value = []
  edges.value = []
  saveHistory()
}

// ==================== 导出图片 ====================
function exportImage() {
  const vfInstance = vueFlowRef.value
  if (!vfInstance) return

  try {
    // 使用 Vue Flow 内置的 toObject 获取数据
    const viewport = document.querySelector('.vue-flow__viewport')
    if (!viewport) {
      ElMessage.warning('无法获取画布元素')
      return
    }

    // 用 transform 信息计算导出区域
    const transform = viewport.style.transform
    // 简单方案：用 html2canvas 或直接用 canvas
    const flowPane = document.querySelector('.vue-flow__pane')
    if (!flowPane) return

    // 使用 SVG 方式导出
    const svgEl = flowPane.querySelector('svg')
    if (!svgEl) {
      ElMessage.warning('画布为空，无法导出')
      return
    }

    const svgClone = svgEl.cloneNode(true)
    const bbox = svgEl.getBBox()
    const padding = 20
    svgClone.setAttribute('width', bbox.width + padding * 2)
    svgClone.setAttribute('height', bbox.height + padding * 2)
    svgClone.setAttribute('viewBox', `${bbox.x - padding} ${bbox.y - padding} ${bbox.width + padding * 2} ${bbox.height + padding * 2}`)

    const svgData = new XMLSerializer().serializeToString(svgClone)
    const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' })
    const url = URL.createObjectURL(svgBlob)

    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = bbox.width + padding * 2
      canvas.height = bbox.height + padding * 2
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.drawImage(img, 0, 0)
      URL.revokeObjectURL(url)

      const link = document.createElement('a')
      link.download = `流程图_${new Date().toLocaleDateString()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
      ElMessage.success('导出成功')
    }
    img.src = url
  } catch (e) {
    ElMessage.error('导出失败: ' + e.message)
  }
}
</script>

<style scoped>
.flowchart-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 107px);
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
}

/* 工具栏 */
.fc-toolbar {
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

/* 画布 */
.fc-canvas-wrapper {
  flex: 1;
  position: relative;
  min-height: 0;
}

/* 去掉 Vue Flow 默认节点样式（白框背景） */
:deep(.vue-flow__node) {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
  border-radius: 0 !important;
}
:deep(.vue-flow__node.selected) {
  box-shadow: none !important;
}
</style>

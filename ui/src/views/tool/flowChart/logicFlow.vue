<template>
  <div class="lf-page">
    <!-- 工具栏 -->
    <div class="lf-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">LogicFlow 流程图</span>
        <el-divider direction="vertical" />
        <el-button size="small" @click="addNode('rect')">
          <svg width="14" height="14" viewBox="0 0 16 16">
            <rect x="1" y="3" width="14" height="10" fill="none" stroke="currentColor" stroke-width="1.5" rx="1" />
          </svg>
          矩形
        </el-button>
        <el-button size="small" @click="addNode('diamond')">
          <svg width="14" height="14" viewBox="0 0 16 16">
            <polygon points="8,0 16,8 8,16 0,8" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
          菱形
        </el-button>
        <el-button size="small" @click="addNode('circle')">
          <svg width="14" height="14" viewBox="0 0 16 16">
            <circle cx="8" cy="8" r="7" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
          圆形
        </el-button>
        <el-button size="small" @click="addNode('ellipse')">
          <svg width="14" height="14" viewBox="0 0 16 16">
            <ellipse cx="8" cy="8" rx="7" ry="5" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
          椭圆
        </el-button>
        <el-divider direction="vertical" />
        <el-color-picker v-model="nodeColor" size="small" :predefine="predefineColors" />
        <el-divider direction="vertical" />
        <el-select v-model="fontSize" size="small" style="width: 70px">
          <el-option v-for="s in [12, 14, 16, 18, 20, 24]" :key="s" :label="s + 'px'" :value="s" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button size="small" @click="undo">
          <el-icon><DArrowLeft /></el-icon> 撤销
        </el-button>
        <el-button size="small" @click="redo">
          重做 <el-icon><DArrowRight /></el-icon>
        </el-button>
        <el-button size="small" @click="clearCanvas">
          <el-icon><Delete /></el-icon> 清空
        </el-button>
        <el-button size="small" type="primary" @click="exportImage">
          <el-icon><Download /></el-icon> 导出图片
        </el-button>
      </div>
    </div>

    <!-- 画布容器 -->
    <div ref="containerRef" class="lf-container"></div>

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
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import LogicFlow from '@logicflow/core'
import '@logicflow/core/dist/index.css'
import { DArrowLeft, DArrowRight, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'ToolLogicFlowChart' })

// ==================== 状态 ====================
const containerRef = ref(null)
const nodeColor = ref('#409EFF')
const fontSize = ref(14)
const predefineColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8B5CF6', '#EC4899', '#14B8A6']

let lf = null
let nodeIdCounter = 1

// 编辑弹窗
const editDialog = reactive({ visible: false, text: '', targetNodeId: null })

// ==================== 初始化 LogicFlow ====================
onMounted(() => {
  nextTick(() => {
    initLF()
  })
})

function initLF() {
  if (!containerRef.value) return

  lf = new LogicFlow({
    container: containerRef.value,
    stopScrollGraph: false,
    stopZoomGraph: false,
    style: {
      rect: {
        width: 100,
        height: 50,
        radius: 4
      },
      diamond: {
        width: 100,
        height: 60
      },
      ellipse: {
        rx: 60,
        ry: 35
      },
      circle: {
        r: 35
      },
      nodeText: {
        fontSize: 14,
        color: '#303133',
        overflowMode: 'autoWrap',
        lineHeight: 20
      }
    },
    background: {
      backgroundColor: '#fafafa'
    },
    grid: {
      type: 'dot',
      size: 20,
      config: {
        color: '#e0e0e0',
        thickness: 1
      }
    },
    keyboard: {
      enabled: true
    },
    snapline: true
  })

  // 双击节点编辑文字
  lf.on('node:dbclick', ({ data }) => {
    editDialog.targetNodeId = data.id
    const props = data.properties || {}
    editDialog.text = props.text || data.text?.value || ''
    editDialog.visible = true
  })

  // 渲染空画布
  lf.render({ nodes: [], edges: [] })
}

onBeforeUnmount(() => {
  if (lf) {
    lf.destroy()
    lf = null
  }
})

// ==================== 添加节点 ====================
function addNode(type) {
  if (!lf) return

  const typeLabels = { rect: '矩形', diamond: '菱形', circle: '圆形', ellipse: '椭圆' }

  const nodeId = `node_${nodeIdCounter++}`

  // 获取视口中心位置
  const viewportCenter = lf.getViewPortCenter?.() || { x: 300, y: 200 }
  const x = viewportCenter.x + (Math.random() - 0.5) * 200
  const y = viewportCenter.y + (Math.random() - 0.5) * 150

  const nodeData = {
    id: nodeId,
    type: type,
    x: Math.round(x),
    y: Math.round(y),
    properties: {
      text: typeLabels[type],
      color: nodeColor.value,
      fontSize: fontSize.value
    },
    text: {
      x: Math.round(x),
      y: Math.round(y),
      value: typeLabels[type]
    }
  }

  const nodeModel = lf.addNode(nodeData)
  if (nodeModel) {
    nodeModel.setStyle({
      stroke: nodeColor.value,
      strokeWidth: 2
    })
  }
}

// ==================== 编辑文字 ====================
function confirmEditText() {
  if (!lf || !editDialog.targetNodeId) {
    editDialog.visible = false
    return
  }

  const model = lf.getNodeModelById(editDialog.targetNodeId)
  if (model) {
    const props = model.getProperties()
    model.setProperties({ ...props, text: editDialog.text })
    model.setText(editDialog.text)
  }
  editDialog.visible = false
}

// ==================== 撤销 / 重做 ====================
function undo() {
  lf?.undo()
}

function redo() {
  lf?.redo()
}

function clearCanvas() {
  if (!lf) return
  const graphData = lf.getGraphData()
  const allEdgeIds = graphData.edges.map((e) => e.id)
  const allNodeIds = graphData.nodes.map((n) => n.id)
  allEdgeIds.forEach((id) => lf.deleteEdge(id))
  allNodeIds.forEach((id) => lf.deleteNode(id))
  nodeIdCounter = 1
}

// ==================== 导出图片 ====================
function exportImage() {
  if (!lf) {
    ElMessage.warning('画布未就绪')
    return
  }
  try {
    const imgData = lf.getSnapshotBlob('#ffffff')
    if (!imgData) {
      ElMessage.warning('导出失败')
      return
    }
    const link = document.createElement('a')
    link.download = `流程图_${new Date().toLocaleDateString()}.png`
    link.href = URL.createObjectURL(imgData)
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败: ' + e.message)
  }
}
</script>

<style scoped>
.lf-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--layout-content-offset, 107px));
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
}

.lf-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 8px;
  z-index: var(--z-content, 10);
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

.lf-container {
  flex: 1;
  min-height: 0;
}
</style>

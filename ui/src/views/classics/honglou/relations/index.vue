<template>
  <div class="page-container relation-graph-page">
    <!-- 顶部工具栏 -->
    <div class="graph-toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索人物..."
          clearable
          style="width: 200px"
          @keyup.enter="searchCharacter"
          @clear="clearSearch"
        >
          <template #prefix
            ><el-icon><Search /></el-icon
          ></template>
        </el-input>
        <el-button type="primary" @click="searchCharacter">搜索</el-button>
        <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width: 130px" @change="onFilterChange">
          <el-option label="主角" value="主角" />
          <el-option label="重要配角" value="重要配角" />
          <el-option label="一般角色" value="一般角色" />
        </el-select>
        <el-button @click="resetAll">重置</el-button>
      </div>
      <div class="toolbar-right">
        <span class="graph-stats">共 {{ filteredNodes.length }} 人，{{ filteredLinks.length }} 条关系</span>
        <el-button @click="fitView" circle title="适应画布">
          <el-icon><FullScreen /></el-icon>
        </el-button>
        <el-button @click="zoomIn" circle title="放大">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
        <el-button @click="zoomOut" circle title="缩小">
          <el-icon><ZoomOut /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Canvas 画布 -->
    <div
      class="canvas-wrapper"
      ref="canvasWrapper"
      @wheel.prevent="onWheel"
      @mousedown="onMouseDown"
      @mousemove="onMouseMove"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
    >
      <canvas ref="canvasRef" @click="onCanvasClick"></canvas>
      <div v-if="loading" class="canvas-loading">
        <el-icon class="is-loading"><Loading /></el-icon> 加载关系数据...
      </div>

      <!-- 图例 -->
      <div class="graph-legend">
        <div class="legend-item"><span class="legend-dot" style="background: #e74c3c"></span>主角</div>
        <div class="legend-item"><span class="legend-dot" style="background: #f39c12"></span>重要配角</div>
        <div class="legend-item"><span class="legend-dot" style="background: #3498db"></span>一般角色</div>
      </div>

      <!-- 悬浮提示 -->
      <div v-if="tooltip.visible" class="graph-tooltip" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
        <strong>{{ tooltip.name }}</strong>
        <span v-if="tooltip.role" class="tooltip-role" :style="{ color: getRoleColor(tooltip.role) }">{{
          tooltip.role
        }}</span>
        <p v-if="tooltip.desc">{{ tooltip.desc }}</p>
      </div>
    </div>

    <!-- 选中人物详情抽屉 -->
    <el-drawer v-model="detailVisible" :title="detailTitle" direction="rtl" size="400px">
      <div v-if="selectedCharacter" class="character-detail">
        <div class="detail-header">
          <div class="detail-avatar" :style="{ background: getRoleColor(selectedCharacter.role) }">
            {{ selectedCharacter.name?.charAt(0) }}
          </div>
          <div>
            <h3>{{ selectedCharacter.name }}</h3>
            <el-tag size="small" :type="getRoleTagType(selectedCharacter.role)">{{ selectedCharacter.role }}</el-tag>
            <span v-if="selectedCharacter.nickname" class="nickname">（{{ selectedCharacter.nickname }}）</span>
          </div>
        </div>

        <div class="detail-section" v-if="selectedCharacter.appearanceDescription">
          <h4>
            <el-icon><Brush /></el-icon> 外貌描述
          </h4>
          <p>{{ selectedCharacter.appearanceDescription }}</p>
        </div>
        <div class="detail-section" v-if="selectedCharacter.personalityTraits">
          <h4>
            <el-icon><MagicStick /></el-icon> 性格特点
          </h4>
          <p>{{ selectedCharacter.personalityTraits }}</p>
        </div>
        <div class="detail-section" v-if="selectedCharacter.fateSummary">
          <h4>
            <el-icon><Sunny /></el-icon> 命运概述
          </h4>
          <p>{{ selectedCharacter.fateSummary }}</p>
        </div>

        <div class="detail-section">
          <h4>
            <el-icon><Connection /></el-icon> 直接关系 ({{ relatedCharacters.length }})
          </h4>
          <div v-if="relatedCharacters.length > 0" class="relation-tags">
            <div
              v-for="r in relatedCharacters"
              :key="r.id"
              class="relation-tag-item"
              @click="focusCharacter(r.character.id)"
            >
              <el-tag size="small" type="warning">{{ r.relationType }}</el-tag>
              <span class="relation-target">{{ r.character.name }}</span>
              <span class="relation-desc">{{ r.relationDesc }}</span>
            </div>
          </div>
          <p v-else class="empty-hint">暂无关系数据</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsHonglouRelations' })
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllHonglouCharactersApi, getAllHonglouRelationsApi } from '@/api/honglou'
import { COLORS } from '@/config/colors'

// ---- 数据 ----
const loading = ref(false)
const allCharacters = ref([])
const allRelations = ref([])
const searchKeyword = ref('')
const filterRole = ref('')

// ---- Canvas 上下文 ----
const canvasWrapper = ref(null)
const canvasRef = ref(null)
let ctx = null
let canvasW = 1200
let canvasH = 800
let dpr = 1
let animationId = null

// ---- 力导向图数据 ----
let simulationNodes = []
let simulationLinks = []
const nodes = ref([])
const links = ref([])

// 视图状态
let offsetX = 0
let offsetY = 0
let scaleVal = 1
let isDragging = false
let dragStartX = 0
let dragStartY = 0
let dragNode = null

// 选中/高亮
const selectedNodeId = ref(null)
const hoveredNodeId = ref(null)

// Tooltip
const tooltip = reactive({ visible: false, x: 0, y: 0, name: '', role: '', desc: '' })

// ---- 计算属性 ----
const filteredNodes = computed(() => {
  let result = allCharacters.value
  if (filterRole.value) result = result.filter((c) => c.role === filterRole.value)
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(
      (c) => (c.name && c.name.toLowerCase().includes(kw)) || (c.nickname && c.nickname.toLowerCase().includes(kw))
    )
  }
  return result
})

const filteredLinks = computed(() => {
  const nodeIds = new Set(filteredNodes.value.map((n) => n.id))
  return allRelations.value.filter((r) => nodeIds.has(r.fromCharacterId) && nodeIds.has(r.toCharacterId))
})

// ---- 详情抽屉 ----
const detailVisible = ref(false)
const detailTitle = ref('')
const selectedCharacter = ref(null)
const relatedCharacters = computed(() => {
  if (!selectedCharacter.value) return []
  const cid = selectedCharacter.value.id
  const rels = allRelations.value.filter((r) => r.fromCharacterId === cid || r.toCharacterId === cid)
  return rels.map((r) => {
    const otherId = r.fromCharacterId === cid ? r.toCharacterId : r.fromCharacterId
    const otherChar = allCharacters.value.find((c) => c.id === otherId)
    return {
      id: r.id,
      relationType: r.relationType,
      relationDesc: r.relationDesc,
      character: otherChar || { id: otherId, name: '未知人物' }
    }
  })
})

// ---- 工具函数 ----
function getRoleColor(role) {
  const map = { 主角: COLORS.DANGER, 重要配角: COLORS.WARNING, 一般角色: COLORS.PRIMARY }
  return map[role] || COLORS.TEXT_SECONDARY
}

function getRoleTagType(role) {
  const map = { 主角: 'danger', 重要配角: 'warning', 一般角色: 'info' }
  return map[role] || 'info'
}

function getNodeRadius(role) {
  const map = { 主角: 22, 重要配角: 17, 一般角色: 13 }
  return map[role] || 12
}

function lightenColor(hex, factor) {
  hex = hex.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  return `rgb(${Math.min(255, Math.round(r + (255 - r) * factor))},${Math.min(255, Math.round(g + (255 - g) * factor))},${Math.min(255, Math.round(b + (255 - b) * factor))})`
}

// ---- 数据加载 ----
async function loadData() {
  loading.value = true
  try {
    const [charRes, relRes] = await Promise.all([getAllHonglouCharactersApi(), getAllHonglouRelationsApi()])
    allCharacters.value = charRes.data || []
    allRelations.value = relRes.data || []
    buildSimulation()
  } catch {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

// ---- 构建模拟 ----
function buildSimulation() {
  const validLinks = filteredLinks.value.filter((l) => {
    const hasSource = filteredNodes.value.some((n) => n.id === l.fromCharacterId)
    const hasTarget = filteredNodes.value.some((n) => n.id === l.toCharacterId)
    return hasSource && hasTarget
  })

  simulationNodes = filteredNodes.value.map((c) => ({
    id: c.id,
    name: c.name,
    nickname: c.nickname,
    role: c.role,
    appearanceDescription: c.appearanceDescription,
    personalityTraits: c.personalityTraits,
    fateSummary: c.fateSummary,
    radius: getNodeRadius(c.role),
    x: Math.random() * canvasW * 0.6 + canvasW * 0.2,
    y: Math.random() * canvasH * 0.6 + canvasH * 0.2,
    vx: 0,
    vy: 0
  }))

  simulationLinks = validLinks.map((l) => ({
    id: l.id,
    source: simulationNodes.find((n) => n.id === l.fromCharacterId),
    target: simulationNodes.find((n) => n.id === l.toCharacterId),
    relationType: l.relationType,
    relationDesc: l.relationDesc
  }))

  nodes.value = simulationNodes
  links.value = simulationLinks
  selectedNodeId.value = null
  fitView()
}

// ---- 力导向迭代 ----
function tick() {
  const _alpha = 0.3
  const repulsion = 8000
  const attraction = 0.005
  const damping = 0.85

  for (let i = 0; i < simulationNodes.length; i++) {
    for (let j = i + 1; j < simulationNodes.length; j++) {
      const a = simulationNodes[i],
        b = simulationNodes[j]
      let dx = b.x - a.x,
        dy = b.y - a.y
      let dist = Math.sqrt(dx * dx + dy * dy) || 1
      const minDist = a.radius + b.radius + 20
      if (dist < minDist) dist = minDist
      const force = repulsion / (dist * dist)
      const fx = (dx / dist) * force,
        fy = (dy / dist) * force
      a.vx -= fx
      a.vy -= fy
      b.vx += fx
      b.vy += fy
    }
  }

  for (const link of simulationLinks) {
    const s = link.source,
      t = link.target
    let dx = t.x - s.x,
      dy = t.y - s.y
    const dist = Math.sqrt(dx * dx + dy * dy) || 1
    const restDist = s.radius + t.radius + 50
    const force = (dist - restDist) * attraction
    const fx = (dx / dist) * force,
      fy = (dy / dist) * force
    s.vx += fx
    s.vy += fy
    t.vx -= fx
    t.vy -= fy
  }

  const cx = canvasW / 2,
    cy = canvasH / 2
  for (const node of simulationNodes) {
    node.vx += (cx - node.x) * 0.0001
    node.vy += (cy - node.y) * 0.0001
    node.vx *= damping
    node.vy *= damping
    const speed = Math.sqrt(node.vx * node.vx + node.vy * node.vy)
    if (speed > 10) {
      node.vx = (node.vx / speed) * 10
      node.vy = (node.vy / speed) * 10
    }
    node.x += node.vx
    node.y += node.vy
    node.x = Math.max(node.radius, Math.min(canvasW - node.radius, node.x))
    node.y = Math.max(node.radius, Math.min(canvasH - node.radius, node.y))
  }

  draw()
}

// ---- 绘制 ----
function draw() {
  if (!ctx) return
  // 使用物理像素尺寸清除整个画布（关键修复：消除残影）
  ctx.setTransform(1, 0, 0, 1, 0, 0)
  ctx.clearRect(0, 0, canvasW * dpr, canvasH * dpr)
  // 设置用户坐标系变换
  ctx.translate(offsetX * dpr, offsetY * dpr)
  ctx.scale(scaleVal * dpr, scaleVal * dpr)

  const isHighlighted = selectedNodeId.value !== null
  const highlightedSet = new Set()
  if (isHighlighted) {
    highlightedSet.add(selectedNodeId.value)
    for (const link of simulationLinks) {
      if (link.source.id === selectedNodeId.value) highlightedSet.add(link.target.id)
      if (link.target.id === selectedNodeId.value) highlightedSet.add(link.source.id)
    }
  }

  // 连线
  for (const link of simulationLinks) {
    const s = link.source,
      t = link.target
    const dimmed = isHighlighted && !(highlightedSet.has(s.id) && highlightedSet.has(t.id))
    ctx.beginPath()
    ctx.moveTo(s.x, s.y)
    ctx.lineTo(t.x, t.y)
    ctx.strokeStyle = dimmed ? 'rgba(180,180,180,0.12)' : 'rgba(180,180,180,0.5)'
    ctx.lineWidth = dimmed ? 0.5 : 1.5
    ctx.stroke()

    const label = link.relationType || link.relationDesc
    if (label && (!dimmed || highlightedSet.size <= 2)) {
      const mx = (s.x + t.x) / 2,
        my = (s.y + t.y) / 2
      const fontSize = Math.max(9, 11 / scaleVal)
      ctx.save()
      ctx.font = `${fontSize}px sans-serif`
      const metrics = ctx.measureText(label)
      const tw = metrics.width + 6,
        th = 16 / scaleVal
      ctx.fillStyle = dimmed ? 'rgba(250,250,250,0.3)' : 'rgba(255,255,255,0.85)'
      ctx.fillRect(mx - tw / 2, my - th / 2, tw, th)
      ctx.fillStyle = dimmed ? 'rgba(150,150,150,0.4)' : '#8B7355'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText(label, mx, my)
      ctx.restore()
    }
  }

  // 节点
  for (const node of simulationNodes) {
    const dimmed = isHighlighted && !highlightedSet.has(node.id)
    const isSelected = node.id === selectedNodeId.value
    const isHovered = node.id === hoveredNodeId.value
    const r = node.radius
    const color = getRoleColor(node.role)

    if (isSelected || isHovered) {
      ctx.beginPath()
      ctx.arc(node.x, node.y, r + 8, 0, Math.PI * 2)
      ctx.fillStyle = isSelected ? 'rgba(231,76,60,0.2)' : 'rgba(52,152,219,0.15)'
      ctx.fill()
    }

    ctx.beginPath()
    ctx.arc(node.x, node.y, r, 0, Math.PI * 2)
    const gradient = ctx.createRadialGradient(node.x - r * 0.3, node.y - r * 0.3, r * 0.1, node.x, node.y, r)
    gradient.addColorStop(0, dimmed ? '#bbb' : lightenColor(color, 0.4))
    gradient.addColorStop(1, dimmed ? '#999' : color)
    ctx.fillStyle = gradient
    ctx.fill()
    ctx.strokeStyle = dimmed ? '#aaa' : isSelected ? '#fff' : 'rgba(255,255,255,0.6)'
    ctx.lineWidth = isSelected ? 3 : 1.5
    ctx.stroke()

    const fontSize = Math.max(10, Math.min(r * 0.65, 13) / scaleVal)
    ctx.font = `bold ${fontSize}px sans-serif`
    ctx.fillStyle = dimmed ? 'rgba(150,150,150,0.4)' : '#2c3e50'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(node.name, node.x, node.y + r + 14 / scaleVal)
  }

  ctx.restore()
}

// ---- 交互 ----
function screenToWorld(sx, sy) {
  return { x: (sx - offsetX) / scaleVal, y: (sy - offsetY) / scaleVal }
}

function findNodeAt(wx, wy) {
  for (let i = simulationNodes.length - 1; i >= 0; i--) {
    const n = simulationNodes[i]
    const dx = wx - n.x,
      dy = wy - n.y
    if (Math.sqrt(dx * dx + dy * dy) <= n.radius + 4) return n
  }
  return null
}

function getCanvasPos(e) {
  const rect = canvasRef.value.getBoundingClientRect()
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

function onMouseDown(e) {
  const pos = getCanvasPos(e)
  const world = screenToWorld(pos.x, pos.y)
  const node = findNodeAt(world.x, world.y)
  if (node) {
    dragNode = node
    node.vx = 0
    node.vy = 0
  } else {
    isDragging = true
    dragStartX = e.clientX - offsetX
    dragStartY = e.clientY - offsetY
  }
}

function onMouseMove(e) {
  const pos = getCanvasPos(e)
  const world = screenToWorld(pos.x, pos.y)

  if (dragNode) {
    dragNode.x = world.x
    dragNode.y = world.y
    draw() // 拖拽节点时立即重绘，消除拖影
    return
  }

  if (isDragging) {
    offsetX = e.clientX - dragStartX
    offsetY = e.clientY - dragStartY
    draw() // 平移画布时立即重绘，消除拖影
    return
  }

  const node = findNodeAt(world.x, world.y)
  if (node) {
    hoveredNodeId.value = node.id
    canvasRef.value.style.cursor = 'pointer'
    tooltip.visible = true
    tooltip.x = pos.x + 14
    tooltip.y = pos.y - 8
    tooltip.name = node.name
    tooltip.role = node.role
    tooltip.desc = node.nickname || ''
  } else {
    hoveredNodeId.value = null
    canvasRef.value.style.cursor = 'grab'
    tooltip.visible = false
  }
}

function onMouseUp() {
  dragNode = null
  isDragging = false
}

function onCanvasClick(e) {
  const pos = getCanvasPos(e)
  const world = screenToWorld(pos.x, pos.y)
  const node = findNodeAt(world.x, world.y)
  if (node) {
    if (selectedNodeId.value === node.id) {
      selectedNodeId.value = null
      detailVisible.value = false
    } else {
      selectedNodeId.value = node.id
      openDetail(node)
    }
  } else {
    selectedNodeId.value = null
    detailVisible.value = false
  }
}

function onWheel(e) {
  const delta = e.deltaY > 0 ? 0.9 : 1.1
  const newScale = Math.max(0.2, Math.min(3, scaleVal * delta))
  const rect = canvasRef.value.getBoundingClientRect()
  const mx = e.clientX - rect.left,
    my = e.clientY - rect.top
  offsetX = mx - (mx - offsetX) * (newScale / scaleVal)
  offsetY = my - (my - offsetY) * (newScale / scaleVal)
  scaleVal = newScale
}

function fitView() {
  if (simulationNodes.length === 0) return
  if (simulationNodes.length === 1) {
    offsetX = canvasW / 2 - simulationNodes[0].x
    offsetY = canvasH / 2 - simulationNodes[0].y
    scaleVal = 1.5
    return
  }
  let minX = Infinity,
    minY = Infinity,
    maxX = -Infinity,
    maxY = -Infinity
  for (const n of simulationNodes) {
    minX = Math.min(minX, n.x - n.radius)
    minY = Math.min(minY, n.y - n.radius)
    maxX = Math.max(maxX, n.x + n.radius)
    maxY = Math.max(maxY, n.y + n.radius)
  }
  const graphW = maxX - minX + 100
  const graphH = maxY - minY + 100
  scaleVal = Math.min(canvasW / graphW, canvasH / graphH, 1.5)
  offsetX = canvasW / 2 - ((minX + maxX) / 2) * scaleVal
  offsetY = canvasH / 2 - ((minY + maxY) / 2) * scaleVal
}

function zoomIn() {
  scaleVal = Math.min(3, scaleVal * 1.2)
}
function zoomOut() {
  scaleVal = Math.max(0.2, scaleVal / 1.2)
}

function searchCharacter() {
  if (!searchKeyword.value) return
  const kw = searchKeyword.value.toLowerCase()
  const found = simulationNodes.find(
    (n) => (n.name && n.name.toLowerCase().includes(kw)) || (n.nickname && n.nickname.toLowerCase().includes(kw))
  )
  if (found) {
    selectedNodeId.value = found.id
    openDetail(found)
    offsetX = canvasW / 2 - found.x * scaleVal
    offsetY = canvasH / 2 - found.y * scaleVal
  } else {
    ElMessage.info('未找到匹配人物')
  }
}

function clearSearch() {
  searchKeyword.value = ''
  selectedNodeId.value = null
  detailVisible.value = false
  fitView()
}

function onFilterChange() {
  buildSimulation()
}

function resetAll() {
  searchKeyword.value = ''
  filterRole.value = ''
  selectedNodeId.value = null
  detailVisible.value = false
  buildSimulation()
}

function focusCharacter(characterId) {
  const node = simulationNodes.find((n) => n.id === characterId)
  if (node) {
    selectedNodeId.value = node.id
    openDetail(node)
    offsetX = canvasW / 2 - node.x * scaleVal
    offsetY = canvasH / 2 - node.y * scaleVal
  }
}

function openDetail(node) {
  const char = allCharacters.value.find((c) => c.id === node.id)
  if (char) {
    selectedCharacter.value = char
    detailTitle.value = `${char.name} - 人物关系`
    detailVisible.value = true
  }
}

// ---- 动画循环 ----
function animate() {
  tick()
  animationId = requestAnimationFrame(animate)
}

// ---- Canvas 尺寸 ----
function resizeCanvas() {
  if (!canvasRef.value || !canvasWrapper.value) return
  dpr = window.devicePixelRatio || 1
  const w = canvasWrapper.value.clientWidth
  const h = canvasWrapper.value.clientHeight
  canvasRef.value.width = w * dpr
  canvasRef.value.height = h * dpr
  canvasRef.value.style.width = w + 'px'
  canvasRef.value.style.height = h + 'px'
  canvasW = w
  canvasH = h
  ctx = canvasRef.value.getContext('2d')
  // DPR 缩放由 draw() 函数内部处理，不在初始化时设置
}

// ---- 生命周期 ----
onMounted(async () => {
  await nextTick()
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)
  await loadData()
  for (let i = 0; i < 300; i++) tick()
  animate()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
  if (animationId) cancelAnimationFrame(animationId)
})
</script>

<style scoped>
.relation-graph-page {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  height: calc(100vh - var(--layout-content-offset, 107px));
}

.graph-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: var(--bg-container, #fff);
  border-bottom: 1px solid var(--border-light, #ebeef5);
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.graph-stats {
  font-size: 13px;
  color: var(--text-secondary);
  margin-right: 8px;
}

.canvas-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #fdf6f0 0%, #faf3e8 30%, #fef9f3 60%, #fdf5e6 100%);
  min-height: 0;
}

.canvas-wrapper canvas {
  display: block;
  cursor: grab;
}

.canvas-wrapper canvas:active {
  cursor: grabbing;
}

.canvas-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: var(--text-secondary);
  background: var(--bg-container);
  padding: 16px 28px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.graph-legend {
  position: absolute;
  bottom: 16px;
  left: 16px;
  background: var(--bg-container);
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 2px 8px var(--header-shadow);
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--text-regular);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.graph-tooltip {
  position: absolute;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  max-width: 200px;
  z-index: var(--z-graphic, 100);
}

.graph-tooltip strong {
  display: block;
  margin-bottom: 2px;
}

.tooltip-role {
  font-size: 11px;
  opacity: 0.85;
}

.graph-tooltip p {
  margin: 4px 0 0;
  font-size: 11px;
  opacity: 0.7;
}

/* 详情抽屉 */
.character-detail {
  padding: 0 4px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.detail-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: bold;
  flex-shrink: 0;
}

.detail-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.nickname {
  color: var(--text-secondary);
  font-size: 13px;
  margin-left: 4px;
}

.detail-section {
  margin-bottom: 18px;
}

.detail-section h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--text-primary);
}

.detail-section p {
  margin: 0;
  line-height: 1.8;
  color: var(--text-regular);
  font-size: 14px;
}

.relation-tags {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.relation-tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: var(--bg-page, #f5f7fa);
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.relation-tag-item:hover {
  background: var(--el-color-primary-light-9);
}

.relation-target {
  color: var(--el-color-primary);
  font-weight: 500;
  font-size: 14px;
}

.relation-desc {
  color: var(--text-color-secondary);
  font-size: 12px;
  margin-left: auto;
}

.empty-hint {
  color: var(--text-placeholder);
  font-size: 13px;
  text-align: center;
  padding: 16px 0;
}
</style>

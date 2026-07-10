<template>
  <div class="code-analysis-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ $t('as400.analysis.title') }}</h2>
        <span class="page-subtitle">{{ $t('as400.analysis.subtitle') }}</span>
      </div>
    </div>

    <div class="analysis-layout">
      <div class="input-panel">
        <div class="panel-header">
          <div class="source-type-group">
            <label class="panel-label">{{ $t('as400.analysis.sourceType') }}</label>
            <el-select v-model="sourceType" style="width: 180px">
              <el-option v-for="t in sourceTypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </div>
          <div class="action-group">
            <el-button type="primary" :loading="loading" @click="handleAnalyze">
              <el-icon><Monitor /></el-icon>
              {{ $t('as400.analysis.analyze') }}
            </el-button>
            <el-button @click="loadSample">
              {{ $t('as400.analysis.sample') }}
            </el-button>
            <el-button @click="clearAll">
              {{ $t('as400.analysis.clear') }}
            </el-button>
          </div>
        </div>

        <div class="editor-wrapper">
          <textarea
            v-model="sourceCode"
            class="code-editor"
            :placeholder="$t('as400.analysis.editorPlaceholder')"
            spellcheck="false"
            wrap="off"
          ></textarea>
        </div>
      </div>

      <div class="result-panel" v-loading="loading">
        <template v-if="result">
          <div class="panel-header">
            <div class="result-tabs">
              <span class="result-tab" :class="{ active: activeTab === 'flowchart' }" @click="activeTab = 'flowchart'">
                <el-icon><Connection /></el-icon>
                {{ $t('as400.analysis.flowchart') }}
              </span>
              <span class="result-tab" :class="{ active: activeTab === 'details' }" @click="activeTab = 'details'">
                <el-icon><Document /></el-icon>
                {{ $t('as400.analysis.details') }}
              </span>
            </div>
            <div class="action-group">
              <el-button v-if="activeTab === 'flowchart'" text size="small" @click="exportFlowchartImage">
                {{ $t('as400.analysis.exportFlowchart') }}
              </el-button>
              <el-button v-if="activeTab === 'details'" text size="small" @click="copyResult">
                {{ $t('as400.analysis.copy') }}
              </el-button>
            </div>
          </div>

          <!-- Flowchart Tab -->
          <div v-show="activeTab === 'flowchart'" class="flowchart-body">
            <div class="flowchart-legend">
              <span class="legend-item"><span class="legend-dot proc" />{{ $t('as400.analysis.legProcedure') }}</span>
              <span class="legend-item"><span class="legend-dot control" />{{ $t('as400.analysis.legControl') }}</span>
              <span class="legend-item"><span class="legend-dot io" />{{ $t('as400.analysis.legIO') }}</span>
              <span class="legend-item"><span class="legend-dot op" />{{ $t('as400.analysis.legOperation') }}</span>
              <span class="legend-item"><span class="legend-dot ret" />{{ $t('as400.analysis.legReturn') }}</span>
              <span class="legend-item"><span class="legend-dot var" />{{ $t('as400.analysis.legVariable') }}</span>
            </div>
            <div ref="flowchartRef" class="x6-container"></div>
          </div>

          <!-- Details Tab -->
          <div v-show="activeTab === 'details'" class="result-body">
            <div class="summary-section">
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item :label="$t('as400.analysis.parseStatus')">
                  <el-tag :type="isSuccess ? 'success' : 'danger'" size="small">
                    {{ summary?.parseStatus || '-' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item :label="$t('as400.analysis.sourceType')">
                  {{ summary?.sourceType || '-' }}
                </el-descriptions-item>
                <el-descriptions-item :label="$t('as400.analysis.totalLines')">
                  {{ summary?.totalLines ?? '-' }}
                </el-descriptions-item>
              </el-descriptions>

              <div v-if="summary?.referencedFiles?.length" class="stat-block">
                <span class="stat-label">{{ $t('as400.analysis.referencedFiles') }}</span>
                <div class="tag-list">
                  <el-tag v-for="f in summary.referencedFiles" :key="f" size="small" type="info">{{ f }}</el-tag>
                </div>
              </div>

              <div v-if="summary?.calledPrograms?.length" class="stat-block">
                <span class="stat-label">{{ $t('as400.analysis.calledPrograms') }}</span>
                <div class="tag-list">
                  <el-tag v-for="p in summary.calledPrograms" :key="p" size="small" type="warning">{{ p }}</el-tag>
                </div>
              </div>

              <div v-if="summary?.procedures?.length" class="stat-block">
                <span class="stat-label">{{ $t('as400.analysis.procedures') }}</span>
                <div class="tag-list">
                  <el-tag v-for="p in summary.procedures" :key="p" size="small" type="primary">{{ p }}</el-tag>
                </div>
              </div>
            </div>

            <el-divider />

            <div class="json-section">
              <div class="section-header" @click="showRawJson = !showRawJson">
                <span>{{ $t('as400.analysis.rawJson') }}</span>
                <el-icon><ArrowRight v-if="!showRawJson" /><ArrowDown v-else /></el-icon>
              </div>
              <pre v-show="showRawJson" class="json-output">{{ formattedJson }}</pre>
            </div>
          </div>
        </template>

        <div v-else class="result-empty">
          <el-icon :size="48" color="var(--el-text-color-placeholder)"><Document /></el-icon>
          <p>{{ $t('as400.analysis.empty') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { Monitor, Connection, Document, ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { Graph } from '@antv/x6'
import { registerCustomShapes } from '@/views/tool/flowChart/shapes'
import { API } from '@/api/routes'
import request from '@/utils/request'

registerCustomShapes()

defineOptions({ name: 'As400CodeAnalysis' })

const BLOCK_START_TYPES = ['for', 'if', 'doWhile', 'doUntil', 'select', 'monitor', 'else', 'elseif', 'when', 'other']

function classifyNode(stmt) {
  const nt = stmt.nodeType
  const op = (stmt.operation || '').toUpperCase()
  if (['for', 'if', 'doWhile', 'doUntil', 'select', 'monitor', 'else', 'elseif', 'when', 'other'].includes(nt))
    return 'control'
  if (nt === 'endBlock') return 'control-end'
  if (['return', 'leaveSubroutine'].includes(nt) || op === 'RETURN' || op === 'LEAVE' || op === 'LEAVESR')
    return 'return'
  if (['display', 'dump', 'read', 'write', 'update', 'delete', 'chain', 'setPosition'].includes(nt)) return 'io'
  if (['eval', 'callp', 'exsr', 'clear', 'sort'].includes(nt)) return 'operation'
  if (nt === 'operation') {
    if (['DSPLY', 'SEND', 'RECEIVE', 'OPEN', 'CLOSE', 'FEOD', 'COMMIT', 'ROLBK'].includes(op)) return 'io'
    if (['CALLP', 'CALL', 'EXSR', 'EVAL', 'CLEAR'].includes(op)) return 'operation'
    return 'operation'
  }
  return 'operation'
}

function classifyColor(type) {
  const map = {
    procedure: { stroke: '#409EFF', fill: '#EBF5FF' },
    'control-end': { stroke: '#E6A23C', fill: '#FFF7E6' },
    control: { stroke: '#E6A23C', fill: '#FFF7E6' },
    io: { stroke: '#67C23A', fill: '#EDF7ED' },
    operation: { stroke: '#5B7FFF', fill: '#EEF1FF' },
    return: { stroke: '#F56C6C', fill: '#FDEEEE' },
    variable: { stroke: '#8B5CF6', fill: '#F3EEFF' }
  }
  return map[type] || { stroke: '#909399', fill: '#F5F5F5' }
}

function truncateLabel(text, maxLen = 50) {
  if (!text) return ''
  const decoded = text
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&#x27;/g, "'")
    .replace(/&#(\d+);/g, (_, c) => String.fromCharCode(c))
  if (decoded.length <= maxLen) return decoded
  return decoded.substring(0, maxLen - 3) + '...'
}

// ==================== Source State ====================
const sourceTypes = [
  { label: 'RPGLE / RPG IV (Free/Fixed)', value: 'RPGLE' },
  { label: 'RPG III (Fixed-format)', value: 'RPG3' },
  { label: 'SQLRPGLE', value: 'SQLRPGLE' },
  { label: 'CL / CLLE', value: 'CL' },
  { label: 'DDS - Physical File (PF)', value: 'PF' },
  { label: 'DDS - Logical File (LF)', value: 'LF' },
  { label: 'DDS - Display File (DSPF)', value: 'DSPF' },
  { label: 'DDS - Printer File (PRTF)', value: 'PRTF' }
]

const samples = {
  RPGLE: `**free
ctl-opt dftactgrp(*no);

dcl-s name char(30);
dcl-s count int(10) inz(0);

dcl-proc main;
  dcl-s i int(10);
  for i = 1 to 10;
    count += i;
  endfor;
  name = 'Hello AS400';
  dsply name;
  return;
end-proc;`,
  RPG3: `     H OPTION(*NODEBUGIO)
      FEMPLPF   IF   E           K DISK
      DSPLY 'Hello RPG III'
      C                     SETON                     LR`,
  CL: `             PGM
              DCL        VAR(&MSG) TYPE(*CHAR) LEN(50)
              CHGVAR     VAR(&MSG) VALUE('Hello CL Program')
              SNDPGMMSG  MSG(&MSG)
              RETURN
              ENDPGM`
}

const sourceType = ref('RPGLE')
const sourceCode = ref('')
const loading = ref(false)
const result = ref(null)
const showRawJson = ref(false)
const activeTab = ref('flowchart')

// ==================== X6 Graph ====================
const flowchartRef = ref(null)
let graph = null
let initTimer = null

const summary = computed(() => result.value?._summary || null)
const isSuccess = computed(() => summary.value?.parseStatus === 'complete')

const formattedJson = computed(() => {
  if (!result.value) return ''
  const { _summary, ...rest } = result.value
  return JSON.stringify(rest, null, 2)
})

// ==================== Flowchart Builder ====================
function buildFlowchart(irData) {
  if (!flowchartRef.value || !irData?.metadata) return

  // Dispose old graph and recreate to avoid stale state
  if (graph) {
    graph.dispose()
    graph = null
  }

  const container = flowchartRef.value
  const w = container.clientWidth || 800
  const h = container.clientHeight || 500
  graph = new Graph({
    container,
    width: w,
    height: h,
    background: { color: '#fafafa' },
    grid: false,
    panning: true,
    mousewheel: true,
    interacting: false,
    selecting: { enabled: false },
    keyboard: { enabled: false }
  })

  const _metadata = irData.metadata || {}
  const content = irData.content || {}
  const statements = content.freeFormatStatements || content.calculationSpecs || []
  const procSpecs = content.procedureSpecs || []
  const defSpecs = content.definitionSpecs || []

  if (!statements.length && !defSpecs.length && !procSpecs.length) {
    const sourceCodeText = irData?.content?.sourceLines?.map?.((l) => l.rawText).join('\n') || ''
    const isFreeFormat = sourceCodeText.includes('**free')
    let msg
    if (!isFreeFormat && sourceType.value === 'RPGLE') {
      msg = 'Free-format RPGLE code requires **free on line 1'
    } else {
      msg = 'No parseable statements found'
    }
    const _n = graph.addNode({
      x: 60,
      y: 60,
      width: 340,
      height: 60,
      shape: 'rect',
      attrs: {
        body: { stroke: '#E6A23C', strokeWidth: 2, fill: '#FFF7E6', rx: 8, ry: 8 },
        label: { text: msg, fontSize: 14, fill: '#E6A23C' }
      }
    })
    return
  }

  // Process definitions
  const defNodes = defSpecs
    .filter((d) => d.rawSourceLine)
    .map((d, i) => ({
      id: `def_${i}`,
      type: 'variable',
      label: truncateLabel(d.rawSourceLine)
    }))

  // Process freeFormatStatements into a flat list with depth
  const depthEntries = []
  let depth = 0

  for (const stmt of statements) {
    const nt = stmt.nodeType
    const raw = stmt.rawSourceLine
    if (!raw) continue

    const isBlockStart = BLOCK_START_TYPES.includes(nt)
    const isBlockEnd = nt === 'endBlock'

    if (isBlockStart) {
      depthEntries.push({ stmt, depth, isBlockStart: true })
      depth++
    } else if (isBlockEnd) {
      depth = Math.max(0, depth - 1)
      depthEntries.push({ stmt, depth, isBlockEnd: true })
    } else {
      depthEntries.push({ stmt, depth })
    }
  }

  // Build nodes
  const NODE_W = 300
  const NODE_H = 38
  const V_GAP = 16
  const INDENT = 36
  const PAD_X = 50
  const PAD_Y = 40
  const DEF_H = 32

  let y = PAD_Y

  function addNode(label, type, x, y, width, height) {
    const colors = classifyColor(type)
    const isRounded = type === 'procedure' || type === 'return' || type === 'io'
    const isControlEnd = type === 'control-end'
    const bodyAttrs = {
      stroke: colors.stroke,
      strokeWidth: isControlEnd ? 1.5 : 2,
      fill: colors.fill
    }
    if (isRounded) {
      bodyAttrs.rx = 8
      bodyAttrs.ry = 8
    }
    if (isControlEnd) {
      bodyAttrs.strokeDasharray = '4,2'
    }

    const node = graph.addNode({
      x,
      y,
      width: width || NODE_W,
      height: height || NODE_H,
      shape: type === 'control' ? 'diamond-shape' : 'rect',
      attrs: {
        body: bodyAttrs,
        label: {
          text: label,
          fontSize: 12,
          fill: '#303133',
          fontFamily: 'Consolas, Monaco, monospace',
          textWrap: { width: (width || NODE_W) - 16 }
        }
      }
    })
    return node
  }

  // Add definition nodes first
  if (defNodes.length) {
    const defLabel = defNodes.length === 1 ? defNodes[0].label : `Variables (${defNodes.length} declared)`
    addNode(defLabel, 'variable', PAD_X, y, NODE_W, Math.max(DEF_H, defNodes.length * 20))
    y += Math.max(DEF_H, defNodes.length * 20) + V_GAP
  }

  // Add procedure containers using frames
  let prevNode = null

  for (const entry of depthEntries) {
    const stmt = entry.stmt
    const raw = stmt.rawSourceLine
    const type = entry.isBlockStart ? 'control' : entry.isBlockEnd ? 'control-end' : classifyNode(stmt)

    const x = PAD_X + entry.depth * INDENT
    const label = truncateLabel(raw, 55)
    // Use a wider width for root-level, narrower for indented
    const w = Math.max(NODE_W - entry.depth * 8, 210)

    // Check if it's a procedure boundary
    let isProcBegin = false
    let isProcEnd = false
    if (procSpecs.length) {
      const ps2 = procSpecs.find((p) => p.rawSourceLine === raw)
      if (ps2?.beginEnd === 'B') isProcBegin = true
      if (ps2?.beginEnd === 'E') isProcEnd = true
    }

    const nodeType = isProcBegin ? 'procedure' : isProcEnd ? 'procedure' : type
    const node = addNode(label, nodeType, x, y, w, NODE_H)

    if (prevNode) {
      graph.addEdge({
        source: prevNode.id,
        target: node.id,
        attrs: {
          line: {
            stroke: '#909399',
            strokeWidth: 1.5,
            targetMarker: { name: 'classic', size: 6 }
          }
        },
        router: 'manhattan',
        connector: 'rounded'
      })
    }

    prevNode = node
    y += NODE_H + V_GAP
  }

  // Auto-fit content
  const totalH = y + PAD_Y
  graph.options.width = undefined
  graph.options.height = undefined
  if (container) {
    graph.resize(container.clientWidth || 800, Math.max(totalH, container.clientHeight || 500))
  }

  // Center content
  nextTick(() => {
    if (graph) {
      graph.centerContent({ padding: 40 })
    }
  })
}

function initGraph() {
  if (graph) return
  if (!flowchartRef.value) {
    initTimer = setTimeout(initGraph, 50)
    return
  }
  const w = flowchartRef.value.clientWidth
  const h = flowchartRef.value.clientHeight
  if (!w || !h) {
    initTimer = setTimeout(initGraph, 50)
    return
  }
  graph = new Graph({
    container: flowchartRef.value,
    width: w,
    height: h,
    background: { color: '#fafafa' },
    grid: false,
    panning: true,
    mousewheel: true,
    connecting: {
      router: 'manhattan',
      connector: 'rounded'
    },
    defaultNode: {
      attrs: {
        body: { stroke: '#909399', strokeWidth: 2, fill: '#ffffff' },
        label: { fontSize: 14, fill: '#303133' }
      }
    },
    defaultEdge: {
      attrs: {
        line: {
          stroke: '#909399',
          strokeWidth: 1.5,
          targetMarker: { name: 'classic', size: 6 }
        }
      },
      router: 'manhattan',
      connector: 'rounded'
    },
    interacting: false,
    selecting: { enabled: false },
    keyboard: { enabled: false }
  })

  if (result.value) {
    nextTick(() => buildFlowchart(result.value))
  }
}

function handleResize() {
  if (graph && flowchartRef.value) {
    const w = flowchartRef.value.clientWidth
    const h = flowchartRef.value.clientHeight
    if (w && h) graph.resize(w, h)
  }
}

onMounted(() => {
  initGraph()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (initTimer) clearTimeout(initTimer)
  window.removeEventListener('resize', handleResize)
  if (graph) {
    graph.dispose()
    graph = null
  }
})

watch(
  () => result.value,
  (val) => {
    if (val && graph) {
      activeTab.value = 'flowchart'
      buildFlowchart(val)
    }
  }
)

function exportFlowchartImage() {
  if (!graph || !flowchartRef.value) {
    ElMessage.warning('Graph not ready')
    return
  }
  try {
    const svgEl = flowchartRef.value.querySelector('svg')
    if (!svgEl) {
      ElMessage.warning('No SVG found')
      return
    }
    const scale = 2
    const pad = 24
    const markerPad = 14
    const LEAF_TAGS = ['rect', 'polygon', 'path', 'circle', 'ellipse', 'line', 'polyline', 'use', 'text', 'image']

    // ============================================================
    // Step 1: Compute content bbox on the ORIGINAL svgEl.
    //
    // CRITICAL: getBBox()/getCTM() only work reliably on elements
    // that are attached to the document. We compute on the live,
    // mounted SVG so the coordinates are accurate.
    // ============================================================

    function isInsideDefs(el, rootEl) {
      let p = el.parentNode
      while (p && p !== rootEl) {
        if (p.nodeType === 1 && p.tagName.toLowerCase() === 'defs') return true
        p = p.parentNode
      }
      return false
    }

    function hasMarkers(el) {
      if (el.nodeType !== 1) return false
      if (el.hasAttribute('marker-end') || el.hasAttribute('marker-start') || el.hasAttribute('marker-mid')) return true
      for (const child of el.children) {
        if (
          child.nodeType === 1 &&
          (child.hasAttribute('marker-end') || child.hasAttribute('marker-start') || child.hasAttribute('marker-mid'))
        )
          return true
      }
      return false
    }

    let minX = Infinity,
      minY = Infinity,
      maxX = -Infinity,
      maxY = -Infinity
    let hasContent = false

    const targetEls = svgEl.querySelectorAll('g, rect, polygon, path, circle, ellipse, line, polyline, use, text')

    for (const el of targetEls) {
      if (isInsideDefs(el, svgEl)) continue

      // Skip pure-container <g>s — their bbox equals their children's
      // union, and getBBox() on empty groups can throw.
      const tag = el.tagName.toLowerCase()
      if (tag === 'g') {
        let hasLeafChild = false
        for (const child of el.children) {
          if (LEAF_TAGS.includes(child.tagName.toLowerCase())) {
            hasLeafChild = true
            break
          }
        }
        if (!hasLeafChild) continue
      }

      try {
        const bbox = el.getBBox()
        if (!bbox || bbox.width === 0 || bbox.height === 0) continue

        const ctm = el.getCTM()
        if (!ctm) continue

        const corners = [
          { x: bbox.x, y: bbox.y },
          { x: bbox.x + bbox.width, y: bbox.y },
          { x: bbox.x, y: bbox.y + bbox.height },
          { x: bbox.x + bbox.width, y: bbox.y + bbox.height },
          { x: bbox.x + bbox.width / 2, y: bbox.y },
          { x: bbox.x + bbox.width / 2, y: bbox.y + bbox.height },
          { x: bbox.x, y: bbox.y + bbox.height / 2 },
          { x: bbox.x + bbox.width, y: bbox.y + bbox.height / 2 }
        ]

        const extra = hasMarkers(el) ? markerPad : 0

        for (const pt of corners) {
          const x = pt.x * ctm.a + pt.y * ctm.c + ctm.e
          const y = pt.x * ctm.b + pt.y * ctm.d + ctm.f
          if (x - extra < minX) minX = x - extra
          if (y - extra < minY) minY = y - extra
          if (x + extra > maxX) maxX = x + extra
          if (y + extra > maxY) maxY = y + extra
        }
        hasContent = true
      } catch {
        // Skip: empty groups, disconnected elements, etc.
      }
    }

    if (!hasContent || !isFinite(minX) || !isFinite(minY) || !isFinite(maxX) || !isFinite(maxY)) {
      ElMessage.warning('No content to export')
      return
    }

    const vbX = minX - pad
    const vbY = minY - pad
    const vbW = maxX - minX + pad * 2
    const vbH = maxY - minY + pad * 2
    const finalW = vbW
    const finalH = vbH

    // ============================================================
    // Step 2: Clone the original SVG as-is, then set viewBox to
    // frame the computed content area.
    //
    // CRITICAL: We do NOT modify any internal <g> transforms because
    // getCTM() already accounts for them. We simply set viewBox to
    // frame the computed content area — this tells the SVG renderer
    // "treat this rectangle as the visible region".
    //
    // Additionally we prepend a white background <rect>, drawn in
    // viewBox coordinates so it fills the entire visible area.
    // ============================================================
    const clone = svgEl.cloneNode(true)

    // White background rect - drawn in viewBox coordinates so it
    // fills the exported canvas without overflow/underflow.
    const bgRect = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
    bgRect.setAttribute('x', vbX)
    bgRect.setAttribute('y', vbY)
    bgRect.setAttribute('width', vbW)
    bgRect.setAttribute('height', vbH)
    bgRect.setAttribute('fill', '#ffffff')

    // Insert background as the FIRST child so it sits behind everything.
    // If the first child is <defs>, insert after it (markers/defs must
    // come before referencing elements).
    let inserted = false
    for (const child of clone.children) {
      if (child.nodeType === 1 && child.tagName.toLowerCase() !== 'defs') {
        clone.insertBefore(bgRect, child)
        inserted = true
        break
      }
    }
    if (!inserted) {
      clone.appendChild(bgRect)
    }

    // viewBox + explicit width/height define the final output size
    clone.setAttribute('viewBox', `${vbX} ${vbY} ${vbW} ${vbH}`)
    clone.setAttribute('width', finalW)
    clone.setAttribute('height', finalH)
    clone.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
    clone.setAttribute('xmlns:xlink', 'http://www.w3.org/1999/xlink')

    const serializer = new XMLSerializer()
    let svgStr = serializer.serializeToString(clone)
    if (!svgStr.startsWith('<?xml')) {
      svgStr = '<?xml version="1.0" encoding="UTF-8"?>' + svgStr
    }

    const svgBlob = new Blob([svgStr], { type: 'image/svg+xml;charset=utf-8' })
    const url = URL.createObjectURL(svgBlob)

    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      const canvas = document.createElement('canvas')
      canvas.width = Math.ceil(finalW * scale)
      canvas.height = Math.ceil(finalH * scale)
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
      canvas.toBlob((blob) => {
        if (!blob) {
          ElMessage.error('Export failed: canvas empty')
          return
        }
        const link = document.createElement('a')
        link.download = `AS400_Flowchart_${Date.now()}.png`
        link.href = URL.createObjectURL(blob)
        link.click()
        URL.revokeObjectURL(link.href)
        ElMessage.success('Exported')
      }, 'image/png')
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      ElMessage.error('SVG render failed')
    }
    img.src = url
  } catch (e) {
    ElMessage.error('Export failed: ' + e.message)
  }
}

// ==================== API ====================
async function handleAnalyze() {
  if (!sourceCode.value.trim()) {
    ElMessage.warning('请输入源码')
    return
  }
  loading.value = true
  showRawJson.value = false
  try {
    const res = await request.post(API.AS400.ANALYSIS, {
      sourceCode: sourceCode.value,
      sourceType: sourceType.value,
      fileName: `input.${sourceType.value.toLowerCase()}`
    })
    if (res.code === 200 && res.data) {
      result.value = res.data
      activeTab.value = 'flowchart'
      if (graph) {
        requestAnimationFrame(() => buildFlowchart(result.value))
      }
    } else {
      ElMessage.error(res.message || '分析失败')
    }
  } catch (e) {
    ElMessage.error('请求失败: ' + (e.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

function loadSample() {
  sourceCode.value = samples[sourceType.value] || samples.RPGLE
}

function clearAll() {
  sourceCode.value = ''
  result.value = null
  showRawJson.value = false
  activeTab.value = 'flowchart'
  if (graph) {
    graph.dispose()
    graph = null
  }
}

async function copyResult() {
  const text = formattedJson.value
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.code-analysis-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--layout-content-offset, 107px));
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
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

.analysis-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.input-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-lighter);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  gap: 12px;
}

.source-type-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.action-group {
  display: flex;
  gap: 8px;
}

.editor-wrapper {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

.code-editor {
  width: 100%;
  height: 100%;
  padding: 16px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  background: #1e1e1e;
  color: #d4d4d4;
  tab-size: 2;
  box-sizing: border-box;
}

.code-editor::placeholder {
  color: #6a6a6a;
}

.result-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.result-tabs {
  display: flex;
  gap: 4px;
}

.result-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.result-tab:hover {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.result-tab.active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  font-weight: 600;
}

.flowchart-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.flowchart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 8px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  border: 2px solid;
}

.legend-dot.proc {
  border-color: #409eff;
  background: #ebf5ff;
}
.legend-dot.control {
  border-color: #e6a23c;
  background: #fff7e6;
}
.legend-dot.io {
  border-color: #67c23a;
  background: #edf7ed;
}
.legend-dot.op {
  border-color: #5b7fff;
  background: #eef1ff;
}
.legend-dot.ret {
  border-color: #f56c6c;
  background: #fdeeee;
}
.legend-dot.var {
  border-color: #8b5cf6;
  background: #f3eeff;
}

.x6-container {
  flex: 1;
  min-height: 300px;
}

.result-body {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.summary-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.json-section {
  margin-top: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  user-select: none;
}

.section-header:hover {
  color: var(--el-color-primary);
}

.json-output {
  margin: 8px 0 0;
  padding: 12px;
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 6px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.result-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--el-text-color-placeholder);
}

.result-empty p {
  margin: 0;
  font-size: 14px;
}
</style>

<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600;">系统健康监控</span>
      <span style="color: #909399; font-size: 12px; margin-left: 8px;">每10秒自动刷新</span>
      <div style="flex: 1" />
      <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px' }">
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div>
              <div style="color: #909399; font-size: 13px;">{{ card.title }}</div>
              <div style="font-size: 28px; font-weight: bold; margin-top: 4px;" :style="{ color: card.color }">
                {{ card.value }}{{ card.unit }}
              </div>
            </div>
            <el-progress type="dashboard" :percentage="card.percent" :color="card.color" :width="60" :stroke-width="8">
              <template #default>{{ card.percent }}%</template>
            </el-progress>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16" style="flex: 1; min-height: 0;">
      <el-col :span="12" style="height: 100%;">
        <el-card shadow="hover" style="height: 100%;" :body-style="{ height: '100%', padding: '16px' }">
          <div style="font-weight: 600; margin-bottom: 8px;">资源使用率</div>
          <div ref="gaugeChart" style="width: 100%; height: calc(100% - 40px);"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="height: 100%;">
        <el-card shadow="hover" style="height: 100%;" :body-style="{ height: '100%', padding: '16px' }">
          <div style="font-weight: 600; margin-bottom: 8px;">JVM 详情</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="堆内存使用">{{ data.jvm?.heapUsed || 0 }} MB</el-descriptions-item>
            <el-descriptions-item label="堆内存最大">{{ data.jvm?.heapMax || 0 }} MB</el-descriptions-item>
            <el-descriptions-item label="非堆内存使用">{{ data.jvm?.nonHeapUsed || 0 }} MB</el-descriptions-item>
            <el-descriptions-item label="当前线程数">{{ data.threads?.current || 0 }}</el-descriptions-item>
            <el-descriptions-item label="峰值线程数">{{ data.threads?.peak || 0 }}</el-descriptions-item>
            <el-descriptions-item label="守护线程数">{{ data.threads?.daemon || 0 }}</el-descriptions-item>
            <el-descriptions-item label="CPU核心数">{{ data.cpu?.cores || 0 }}</el-descriptions-item>
            <el-descriptions-item label="磁盘总空间">{{ data.disk?.total || 0 }} GB</el-descriptions-item>
            <el-descriptions-item label="磁盘已用">{{ data.disk?.used || 0 }} GB</el-descriptions-item>
            <el-descriptions-item label="GC次数">{{ gcData.totalCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="GC总耗时">{{ gcData.totalTimeSeconds || 0 }}s</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getSystemHealthApi, getGcStatsApi } from '@/api/health'

const data = ref({})
const gcData = ref({})
let gaugeChartInstance = null
const gaugeChart = ref(null)
let timer = null

function clamp(n) { return Math.max(0, Math.min(100, n || 0)) }

const cards = computed(() => [
  { title: 'CPU 使用率', value: data.value.cpu?.usage ?? 0, unit: '%', percent: clamp(data.value.cpu?.usage), color: '#409EFF' },
  { title: '内存使用率', value: data.value.memory?.usage ?? 0, unit: '%', percent: clamp(data.value.memory?.usage), color: '#67C23A' },
  { title: '堆内存使用', value: data.value.jvm?.heapUsed ?? 0, unit: 'MB', percent: data.value.jvm?.heapMax ? clamp(Math.round(data.value.jvm.heapUsed / data.value.jvm.heapMax * 100)) : 0, color: '#E6A23C' },
  { title: '磁盘使用率', value: data.value.disk?.usage ?? 0, unit: '%', percent: clamp(data.value.disk?.usage), color: '#F56C6C' },
])

const statusType = computed(() => {
  const cpu = data.value.cpu?.usage || 0
  const mem = data.value.memory?.usage || 0
  if (cpu > 90 || mem > 90) return 'danger'
  if (cpu > 70 || mem > 70) return 'warning'
  return 'success'
})

const statusText = computed(() => {
  const t = statusType.value
  return t === 'success' ? '运行正常' : t === 'warning' ? '负载较高' : '告警'
})

function renderGauge() {
  const el = gaugeChart.value
  if (!el) return
  // 确保容器在 DOM 中且有实际尺寸后再初始化
  if (el.clientWidth === 0 || el.clientHeight === 0) {
    requestAnimationFrame(() => renderGauge())
    return
  }
  if (!gaugeChartInstance) {
    gaugeChartInstance = echarts.init(el)
  } else {
    gaugeChartInstance.resize()
  }
  const d = data.value
  const cpu = clamp(d.cpu?.usage || 0)
  const mem = clamp(d.memory?.usage || 0)
  const disk = clamp(d.disk?.usage || 0)
  gaugeChartInstance.setOption({
    textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
    series: [
      { type: 'gauge', center: ['20%', '55%'], radius: '46%', min: 0, max: 100,
        progress: { show: true, width: 14, itemStyle: { color: '#409EFF' } },
        axisLine: { lineStyle: { width: 14 } },
        axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
        detail: { fontSize: 13, fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif', formatter: 'CPU\n{value}%' }, data: [{ value: cpu }] },
      { type: 'gauge', center: ['50%', '55%'], radius: '46%', min: 0, max: 100,
        progress: { show: true, width: 14, itemStyle: { color: '#67C23A' } },
        axisLine: { lineStyle: { width: 14 } },
        axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
        detail: { fontSize: 13, fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif', formatter: '内存\n{value}%' }, data: [{ value: mem }] },
      { type: 'gauge', center: ['80%', '55%'], radius: '46%', min: 0, max: 100,
        progress: { show: true, width: 14, itemStyle: { color: '#F56C6C' } },
        axisLine: { lineStyle: { width: 14 } },
        axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
        detail: { fontSize: 13, fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif', formatter: '磁盘\n{value}%' }, data: [{ value: disk }] },
    ]
  })
}

const fetchData = async () => {
  try {
    const [health, gc] = await Promise.all([getSystemHealthApi(), getGcStatsApi()])
    data.value = health.data || {}
    gcData.value = gc.data || {}
    nextTick(() => renderGauge())
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  fetchData()
  timer = setInterval(fetchData, 10000)
})

onUnmounted(() => clearInterval(timer))
</script>

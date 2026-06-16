<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600">{{ $t('monitor.logAnalysis.title') }}</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6" v-for="c in summaryCards" :key="c.label">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px' }">
          <div style="color: #909399; font-size: 13px">{{ c.label }}</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 4px; color: #303133">{{ c.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16" style="flex: 0 0 320px; min-height: 0">
      <el-col :span="12" style="height: 320px">
        <el-card
          shadow="hover"
          :header="$t('monitor.logAnalysis.hourlyDist')"
          :body-style="{ padding: '0', height: 'calc(100% - 48px)' }"
        >
          <div ref="hourlyChart" style="width: 100%; height: 100%"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="height: 320px">
        <el-card
          shadow="hover"
          :header="$t('monitor.logAnalysis.typeDist')"
          :body-style="{ padding: '0', height: 'calc(100% - 48px)' }"
        >
          <div ref="pieChart" style="width: 100%; height: 100%"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="flex: 0 0 320px; min-height: 0; margin-top: 16px">
      <el-col :span="24" style="height: 320px">
        <el-card
          shadow="hover"
          :header="$t('monitor.logAnalysis.trend')"
          :body-style="{ padding: '0', height: 'calc(100% - 48px)' }"
        >
          <div ref="trendChart" style="width: 100%; height: 100%"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorLogAnalysis' })
import { ref, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCyberTheme } from '@/utils/echartsTheme'

const { t } = useI18n()

let _echarts = null
async function loadEcharts() {
  if (!_echarts) {
    const mod = await import('echarts')
    _echarts = mod.default || mod
  }
  return _echarts
}
const logWarn = import.meta.env.DEV ? console.warn : () => {}
import { getLogSummaryApi, getLogHourlyApi, getLogTypeDistributionApi, getLogTrendApi } from '@/api/logAnalysis'

const summary = ref({})
const hourlyData = ref([])
const typeData = ref([])
const trendData = ref([])
const hourlyChart = ref(null)
const pieChart = ref(null)
const trendChart = ref(null)

const summaryCards = computed(() => [
  { label: t('monitor.logAnalysis.todayTotal'), value: summary.value.totalToday || 0 },
  { label: t('monitor.logAnalysis.todayError'), value: summary.value.errorToday || 0 },
  { label: t('monitor.logAnalysis.activeUsers'), value: summary.value.activeUsers || 0 },
  { label: t('monitor.logAnalysis.topOperation'), value: summary.value.topOperations?.[0]?.operation || '-' }
])

async function renderCharts() {
  const lazyEcharts = await loadEcharts()
  // 时段分布柱状图
  const hDom = hourlyChart.value
  if (hDom && hDom.clientWidth > 0 && hDom.clientHeight > 0) {
    const chart = lazyEcharts.init(hDom, getCyberTheme())
    chart.setOption({
      tooltip: { trigger: 'axis' },
      textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
      grid: { left: 40, right: 16, top: 16, bottom: 30 },
      xAxis: {
        type: 'category',
        data: hourlyData.value.map((h) => h.hour + ':00'),
        axisLabel: { rotate: 45, fontSize: 10 }
      },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'bar',
          data: hourlyData.value.map((h) => h.count),
          itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' }
        }
      ]
    })
  }

  // 类型分布饼图
  const pDom = pieChart.value
  if (pDom && pDom.clientWidth > 0 && pDom.clientHeight > 0) {
    const chart = lazyEcharts.init(pDom, getCyberTheme())
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { fontSize: 11 } },
      textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          label: {
            show: true,
            formatter: '{b}\n{d}%',
            fontSize: 11,
            fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif'
          },
          data: typeData.value.map((t) => ({ name: t.type, value: t.count }))
        }
      ]
    })
  }

  // 趋势折线图
  const tDom = trendChart.value
  if (tDom && tDom.clientWidth > 0 && tDom.clientHeight > 0) {
    const chart = lazyEcharts.init(tDom, getCyberTheme())
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: [t('monitor.logAnalysis.totalOps'), t('monitor.logAnalysis.errorOps')] },
      textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
      grid: { left: 50, right: 16, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: trendData.value.map((t) => t.date.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        {
          name: t('monitor.logAnalysis.totalOps'),
          type: 'line',
          data: trendData.value.map((t) => t.count),
          smooth: true,
          itemStyle: { color: '#409EFF' }
        },
        {
          name: t('monitor.logAnalysis.errorOps'),
          type: 'line',
          data: trendData.value.map((t) => t.errorCount),
          smooth: true,
          itemStyle: { color: '#F56C6C' }
        }
      ]
    })
  }
}

const fetchData = async () => {
  const [s, h, t, tr] = await Promise.all([
    getLogSummaryApi(),
    getLogHourlyApi(),
    getLogTypeDistributionApi(),
    getLogTrendApi(7)
  ])
  summary.value = s.data || {}
  hourlyData.value = h.data || []
  typeData.value = t.data || []
  trendData.value = tr.data || []
  await nextTick()
  requestAnimationFrame(() => {
    renderCharts()
  })
}

onMounted(fetchData)
</script>

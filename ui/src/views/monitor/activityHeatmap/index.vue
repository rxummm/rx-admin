<template>
  <div class="page-container">
    <div class="search-bar">
      <el-date-picker v-model="dateRange" type="daterange" :start-placeholder="$t('common.startDate')" :end-placeholder="$t('common.endDate')" @change="loadData" />
    </div>
    <el-card>
      <template #header>
        <span>{{ $t('monitor.activity.heatmap') }}</span>
      </template>
      <div ref="heatmapRef" style="height: 400px"></div>
    </el-card>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>{{ $t('monitor.activity.typeStats') }}</span></template>
          <div ref="typeChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>{{ $t('monitor.activity.topUsers') }}</span></template>
          <div ref="topUsersRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorActivityHeatmap' })
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const dateRange = ref([])
const heatmapRef = ref(null)
const typeChartRef = ref(null)
const topUsersRef = ref(null)

const loadData = async () => {
  const start = dateRange.value?.[0] || new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10)
  const end = dateRange.value?.[1] || new Date().toISOString().slice(0, 10)
  const { data } = await request({ url: '/monitor/activity/heatmap', method: 'get', params: { startDate: start, endDate: end } })

  if (data.heatmap && heatmapRef.value) {
    const chart = echarts.init(heatmapRef.value)
    const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
    const days = [...new Set(data.heatmap.map(h => h.activityDate))].sort()
    const heatData = data.heatmap.map(h => [hours.indexOf(`${h.hour}:00`), days.indexOf(h.activityDate), h.count])
    chart.setOption({
      tooltip: { position: 'top' },
      grid: { height: '80%', top: '10%' },
      xAxis: { type: 'category', data: hours, splitArea: { show: true } },
      yAxis: { type: 'category', data: days, splitArea: { show: true } },
      visualMap: { min: 0, calculable: true, orient: 'horizontal', left: 'center', bottom: '0%' },
      series: [{ type: 'heatmap', data: heatData }]
    })
  }
}

onMounted(loadData)
</script>

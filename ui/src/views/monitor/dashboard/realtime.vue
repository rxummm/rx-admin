<template>
  <div class="monitor-dashboard" :class="{ 'is-fullscreen': isFullscreen }">
    <!-- 顶部工具栏 -->
    <div class="toolbar" v-if="!isFullscreen">
      <h2>实时监控大屏</h2>
      <div class="toolbar-actions">
        <el-button @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
          {{ isFullscreen ? '退出全屏' : '全屏显示' }}
        </el-button>
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in statsCards" :key="index">
        <div class="stat-card" :style="{ borderColor: stat.color }">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <div class="chart-card">
          <h3>系统负载</h3>
          <div ref="loadChart" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <h3>请求响应时间</h3>
          <div ref="responseChart" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="8">
        <div class="chart-card">
          <h3>在线用户</h3>
          <div class="online-users">
            <div class="user-count">{{ onlineUsers }}</div>
            <div class="user-label">当前在线</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3>今日操作</h3>
          <div ref="operationChart" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3>错误率</h3>
          <div class="error-rate">
            <div class="rate-value" :class="errorRate > 5 ? 'danger' : 'normal'">
              {{ errorRate }}%
            </div>
            <div class="rate-label">近1小时错误率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近操作日志 -->
    <div class="chart-card">
      <h3>最近操作</h3>
      <el-table :data="recentLogs" stripe size="small" max-height="300">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="user" label="用户" width="120" />
        <el-table-column prop="operation" label="操作" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { FullScreen, Refresh, User, Document, Warning, CircleCheck } from '@element-plus/icons-vue'

const isFullscreen = ref(false)
const loading = ref(false)
const onlineUsers = ref(0)
const errorRate = ref(0)

const statsCards = ref([
  { label: '总用户数', value: 0, icon: 'User', color: '#409eff', trend: 0 },
  { label: '今日登录', value: 0, icon: 'CircleCheck', color: '#67c23a', trend: 0 },
  { label: '今日操作', value: 0, icon: 'Document', color: '#e6a23c', trend: 0 },
  { label: '异常告警', value: 0, icon: 'Warning', color: '#f56c6c', trend: 0 }
])

const recentLogs = ref([])

const loadChart = ref(null)
const responseChart = ref(null)
const operationChart = ref(null)
let loadChartInstance = null
let responseChartInstance = null
let operationChartInstance = null
let refreshTimer = null

const toggleFullscreen = () => {
  if (!isFullscreen.value) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
  isFullscreen.value = !isFullscreen.value
}

const refreshData = async () => {
  loading.value = true
  try {
    // 模拟数据更新
    statsCards.value[0].value = Math.floor(Math.random() * 1000) + 500
    statsCards.value[1].value = Math.floor(Math.random() * 100) + 50
    statsCards.value[2].value = Math.floor(Math.random() * 500) + 200
    statsCards.value[3].value = Math.floor(Math.random() * 10)
    onlineUsers.value = Math.floor(Math.random() * 50) + 10
    errorRate.value = (Math.random() * 5).toFixed(1)
    
    await nextTick()
    renderCharts()
  } catch (e) {
    console.error('刷新数据失败:', e)
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  // 系统负载图
  if (loadChart.value) {
    if (!loadChartInstance) {
      loadChartInstance = echarts.init(loadChart.value)
    }
    const hours = Array.from({ length: 24 }, (_, i) => i + ':00')
    const cpuData = hours.map(() => Math.floor(Math.random() * 100))
    const memData = hours.map(() => Math.floor(Math.random() * 100))
    
    loadChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['CPU', '内存'] },
      xAxis: { type: 'category', data: hours },
      yAxis: { type: 'value', max: 100 },
      series: [
        { name: 'CPU', type: 'line', data: cpuData, smooth: true, areaStyle: { opacity: 0.3 } },
        { name: '内存', type: 'line', data: memData, smooth: true, areaStyle: { opacity: 0.3 } }
      ]
    })
  }
  
  // 响应时间图
  if (responseChart.value) {
    if (!responseChartInstance) {
      responseChartInstance = echarts.init(responseChart.value)
    }
    const hours = Array.from({ length: 24 }, (_, i) => i + ':00')
    const p50 = hours.map(() => Math.floor(Math.random() * 200) + 50)
    const p95 = hours.map(() => Math.floor(Math.random() * 500) + 200)
    
    responseChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['P50', 'P95'] },
      xAxis: { type: 'category', data: hours },
      yAxis: { type: 'value', name: 'ms' },
      series: [
        { name: 'P50', type: 'line', data: p50, smooth: true },
        { name: 'P95', type: 'line', data: p95, smooth: true }
      ]
    })
  }
  
  // 操作分布图
  if (operationChart.value) {
    if (!operationChartInstance) {
      operationChartInstance = echarts.init(operationChart.value)
    }
    operationChartInstance.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 35, name: '查询' },
          { value: 25, name: '新增' },
          { value: 20, name: '修改' },
          { value: 15, name: '删除' },
          { value: 5, name: '导出' }
        ]
      }]
    })
  }
}

onMounted(() => {
  refreshData()
  refreshTimer = setInterval(refreshData, 30000) // 30秒刷新一次
  
  window.addEventListener('resize', () => {
    loadChartInstance?.resize()
    responseChartInstance?.resize()
    operationChartInstance?.resize()
  })
  
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.monitor-dashboard {
  padding: 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

.monitor-dashboard.is-fullscreen {
  background: #1a1a2e;
  color: #fff;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar h2 {
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border-left: 4px solid;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-card .stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 12px;
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-card .stat-trend {
  font-size: 12px;
  margin-top: 8px;
}

.stat-card .stat-trend.up { color: #67c23a; }
.stat-card .stat-trend.down { color: #f56c6c; }

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.chart-container {
  height: 250px;
}

.online-users, .error-rate {
  text-align: center;
  padding: 40px 0;
}

.user-count, .rate-value {
  font-size: 48px;
  font-weight: 600;
}

.user-label, .rate-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.rate-value.normal { color: #67c23a; }
.rate-value.danger { color: #f56c6c; }
</style>

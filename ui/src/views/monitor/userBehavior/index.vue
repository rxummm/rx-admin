<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ frequencyStats.uniqueUsers || 0 }}</div>
              <div class="stat-label">活跃用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ frequencyStats.totalLogins || 0 }}</div>
              <div class="stat-label">总登录次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (preferenceStats.successRate || 0).toFixed(1) }}%</div>
              <div class="stat-label">登录成功率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ preferenceStats.failCount || 0 }}</div>
              <div class="stat-label">失败登录次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>活跃时段分布（近30天）</span>
          </template>
          <div ref="timeChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>Top 10 登录用户</span>
          </template>
          <el-table :data="topUsers" stripe size="small">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="key" label="用户名" />
            <el-table-column prop="value" label="登录次数" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, CircleCheck, TrendCharts, Warning } from '@element-plus/icons-vue'
import { getLoginFrequency, getActiveTimeDistribution, getOperationPreference } from '@/api/userBehavior'

const frequencyStats = ref({})
const preferenceStats = ref({})
const topUsers = ref([])
const timeChart = ref(null)
let timeChartInstance = null

const loadData = async () => {
  try {
    const [freqRes, timeRes, prefRes] = await Promise.all([
      getLoginFrequency(7),
      getActiveTimeDistribution(),
      getOperationPreference()
    ])
    
    frequencyStats.value = freqRes.data || {}
    topUsers.value = (freqRes.data?.topUsers || []).slice(0, 10)
    preferenceStats.value = prefRes.data || {}
    
    await nextTick()
    renderTimeChart(timeRes.data || [])
  } catch (e) {
    console.error('加载用户行为数据失败:', e)
  }
}

const renderTimeChart = (data) => {
  if (!timeChart.value) return
  
  if (!timeChartInstance) {
    timeChartInstance = echarts.init(timeChart.value)
  }
  
  const hours = data.map(d => d.hour + ':00')
  const counts = data.map(d => d.count)
  
  timeChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '登录次数' },
    series: [{
      name: '登录次数',
      type: 'bar',
      data: counts,
      itemStyle: { color: '#409eff' }
    }]
  })
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => timeChartInstance?.resize())
})
</script>

<style scoped>
.page-container { padding: 20px; }
.stats-row { margin-bottom: 20px; }
.stat-card { height: 100%; }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 600; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.chart-card { margin-bottom: 20px; }
.chart-container { height: 300px; }
</style>

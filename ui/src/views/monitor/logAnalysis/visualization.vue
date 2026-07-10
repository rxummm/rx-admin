<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="loadData"
      />
      <el-select v-model="queryModule" placeholder="选择模块" clearable style="width: 150px" @change="loadData">
        <el-option label="全部模块" value="" />
        <el-option v-for="item in modules" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
              <div class="stat-label">总操作数</div>
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
              <div class="stat-value">{{ stats.successCount || 0 }}</div>
              <div class="stat-label">成功操作</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.failCount || 0 }}</div>
              <div class="stat-label">失败操作</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
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
            <span>操作趋势图</span>
          </template>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>模块分布</span>
          </template>
          <div ref="moduleChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户操作排行 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <span>用户操作排行</span>
      </template>
      <el-table :data="userRanking" v-loading="loading" stripe>
        <el-table-column type="index" label="排名" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="count" label="操作次数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modules" label="操作模块" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="m in row.modules.slice(0, 3)" :key="m" size="small" style="margin-right: 4px">
              {{ m }}
            </el-tag>
            <el-tag v-if="row.modules.length > 3" size="small" type="info">
              +{{ row.modules.length - 3 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastTime" label="最近操作" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { Search, Document, CircleCheck, CircleClose, User } from '@element-plus/icons-vue'
import { getLogSummary, getLogHourly, getLogTypeDistribution } from '@/api/logAnalysis'

const loading = ref(false)
const dateRange = ref([])
const queryModule = ref('')
const stats = ref({})
const userRanking = ref([])
const modules = ref([])

const trendChart = ref(null)
const moduleChart = ref(null)
let trendChartInstance = null
let moduleChartInstance = null

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (queryModule.value) {
      params.module = queryModule.value
    }
    
    const [summaryRes, hourlyRes, distributionRes] = await Promise.all([
      getLogSummary(params),
      getLogHourly(params),
      getLogTypeDistribution(params)
    ])
    
    stats.value = summaryRes.data || {}
    userRanking.value = (summaryRes.data?.userRanking || []).slice(0, 10)
    modules.value = summaryRes.data?.modules || []
    
    await nextTick()
    renderTrendChart(hourlyRes.data || [])
    renderModuleChart(distributionRes.data || [])
  } catch (e) {
    console.error('加载操作日志数据失败:', e)
  } finally {
    loading.value = false
  }
}

const renderTrendChart = (data) => {
  if (!trendChart.value) return
  
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChart.value)
  }
  
  const hours = data.map(d => d.hour + ':00')
  const counts = data.map(d => d.count)
  
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '操作次数' },
    series: [{
      name: '操作次数',
      type: 'line',
      data: counts,
      smooth: true,
      areaStyle: { opacity: 0.3 }
    }]
  })
}

const renderModuleChart = (data) => {
  if (!moduleChart.value) return
  
  if (!moduleChartInstance) {
    moduleChartInstance = echarts.init(moduleChart.value)
  }
  
  const pieData = data.map(d => ({ name: d.module, value: d.count }))
  
  moduleChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: pieData,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

onMounted(() => {
  loadData()
  
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    moduleChartInstance?.resize()
  })
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.table-card {
  margin-top: 20px;
}
</style>

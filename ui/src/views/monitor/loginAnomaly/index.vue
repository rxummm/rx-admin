<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-button type="primary" @click="handleDetect" :loading="detecting">
        <el-icon><Warning /></el-icon>
        {{ detecting ? '检测中...' : '手动检测' }}
      </el-button>
      <el-select v-model="queryDays" placeholder="选择时间范围" style="width: 150px" @change="loadData">
        <el-option label="最近7天" :value="7" />
        <el-option label="最近30天" :value="30" />
        <el-option label="最近90天" :value="90" />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><CloseBold /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayFailCount || 0 }}</div>
              <div class="stat-label">今日失败登录</div>
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
              <div class="stat-value">{{ stats.todaySuccessCount || 0 }}</div>
              <div class="stat-label">今日成功登录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.weekUnusualHourCount || 0 }}</div>
              <div class="stat-label">异常时间登录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #909399">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highRiskIpCount || 0 }}</div>
              <div class="stat-label">高风险IP</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 高风险IP列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>高风险IP列表</span>
          <el-tag v-if="stats.highRiskIps && stats.highRiskIps.length" type="danger">
            {{ stats.highRiskIps.length }} 个
          </el-tag>
        </div>
      </template>
      <el-table :data="failedLogins" v-loading="loading" stripe>
        <el-table-column prop="ip" label="IP地址" width="180" />
        <el-table-column prop="count" label="失败次数" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.count >= 10 ? 'danger' : row.count >= 5 ? 'warning' : 'info'">
              {{ row.count }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usernames" label="涉及用户" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="user in row.usernames" :key="user" size="small" style="margin-right: 4px">
              {{ user }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastAttempt" label="最近尝试" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastAttempt) }}
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.count >= 10 ? 'danger' : row.count >= 5 ? 'warning' : 'info'">
              {{ row.count >= 10 ? '高危' : row.count >= 5 ? '中危' : '低危' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, CloseBold, CircleCheck, Clock, Connection } from '@element-plus/icons-vue'
import { getAnomalyStats, getFailedLoginsByIp, triggerDetection } from '@/api/loginAnomaly'

const loading = ref(false)
const detecting = ref(false)
const queryDays = ref(7)
const stats = ref({})
const failedLogins = ref([])

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const [statsRes, failedRes] = await Promise.all([
      getAnomalyStats(),
      getFailedLoginsByIp(queryDays.value)
    ])
    stats.value = statsRes.data || {}
    failedLogins.value = failedRes.data || []
  } catch (e) {
    console.error('加载登录异常数据失败:', e)
  } finally {
    loading.value = false
  }
}

const handleDetect = async () => {
  detecting.value = true
  try {
    await triggerDetection()
    ElMessage.success('异常检测已触发')
    await loadData()
  } catch (e) {
    console.error('触发检测失败:', e)
  } finally {
    detecting.value = false
  }
}

onMounted(() => {
  loadData()
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

.table-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

<template>
  <el-drawer
    v-model="visible"
    title="📊 性能监控面板"
    size="600px"
    :close-on-click-modal="true"
    class="performance-panel"
  >
    <div class="panel-content">
      <!-- 性能评分 -->
      <el-card shadow="hover" class="score-card">
        <div class="score-header">
          <span class="score-emoji">{{ performanceLevel.emoji }}</span>
          <div class="score-info">
            <div class="score-value" :style="{ color: performanceLevel.color }">
              {{ performanceScore }}
            </div>
            <div class="score-label">性能评分</div>
          </div>
          <el-tag :color="performanceLevel.color" effect="dark" class="score-tag">
            {{ performanceLevel.level }}
          </el-tag>
        </div>
      </el-card>

      <!-- FPS 监控 -->
      <el-card shadow="hover" class="metric-card">
        <template #header>
          <div class="card-header">
            <el-icon><Monitor /></el-icon>
            <span>FPS 实时监控</span>
          </div>
        </template>
        <div class="fps-display">
          <div class="fps-value" :class="fpsClass">{{ currentFPS }}</div>
          <div class="fps-label">帧/秒</div>
          <el-progress
            :percentage="Math.min(100, (currentFPS / 60) * 100)"
            :color="fpsColor"
            :stroke-width="8"
            :show-text="false"
          />
        </div>
      </el-card>

      <!-- API 请求统计 -->
      <el-card shadow="hover" class="metric-card">
        <template #header>
          <div class="card-header">
            <el-icon><Connection /></el-icon>
            <span>API 请求统计</span>
            <el-button size="small" text @click="resetStats">重置</el-button>
          </div>
        </template>
        <div class="api-stats">
          <div class="stat-row">
            <span class="stat-label">总请求数</span>
            <span class="stat-value">{{ apiStats.totalRequests }}</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">失败请求</span>
            <span class="stat-value" :style="{ color: apiStats.failedRequests > 0 ? '#f85149' : '#3fb950' }">
              {{ apiStats.failedRequests }}
            </span>
          </div>
          <div class="stat-row">
            <span class="stat-label">平均响应时间</span>
            <span class="stat-value">{{ apiStats.avgResponseTime }}ms</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">最慢请求</span>
            <span class="stat-value">{{ apiStats.slowestRequest }}ms</span>
          </div>
        </div>

        <!-- 最近请求列表 -->
        <div v-if="apiStats.requests.length > 0" class="recent-requests">
          <div class="section-title">最近请求</div>
          <el-table :data="apiStats.requests.slice(0, 10)" size="small" max-height="200">
            <el-table-column prop="method" label="方法" width="70" />
            <el-table-column prop="url" label="URL" show-overflow-tooltip />
            <el-table-column prop="duration" label="耗时" width="80">
              <template #default="{ row }">
                <span :style="{ color: getDurationColor(row.duration) }">
                  {{ row.duration }}ms
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusType(row.status)">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 首屏加载指标 -->
      <el-card shadow="hover" class="metric-card">
        <template #header>
          <div class="card-header">
            <el-icon><Timer /></el-icon>
            <span>首屏加载指标</span>
          </div>
        </template>
        <div class="load-metrics">
          <div class="metric-item">
            <div class="metric-name">FCP</div>
            <div class="metric-value" :style="{ color: getMetricColor(pageLoad.FCP, 1500, 3000) }">
              {{ pageLoad.FCP || '-' }}ms
            </div>
            <div class="metric-desc">首次内容绘制</div>
          </div>
          <div class="metric-item">
            <div class="metric-name">LCP</div>
            <div class="metric-value" :style="{ color: getMetricColor(pageLoad.LCP, 2500, 4000) }">
              {{ pageLoad.LCP || '-' }}ms
            </div>
            <div class="metric-desc">最大内容绘制</div>
          </div>
          <div class="metric-item">
            <div class="metric-name">FID</div>
            <div class="metric-value" :style="{ color: getMetricColor(pageLoad.FID, 100, 300) }">
              {{ pageLoad.FID || '-' }}ms
            </div>
            <div class="metric-desc">首次输入延迟</div>
          </div>
          <div class="metric-item">
            <div class="metric-name">CLS</div>
            <div class="metric-value" :style="{ color: getMetricColor(pageLoad.CLS * 1000, 100, 250) }">
              {{ pageLoad.CLS || '-' }}
            </div>
            <div class="metric-desc">累积布局偏移</div>
          </div>
          <div class="metric-item">
            <div class="metric-name">TTFB</div>
            <div class="metric-value" :style="{ color: getMetricColor(pageLoad.TTFB, 500, 1000) }">
              {{ pageLoad.TTFB || '-' }}ms
            </div>
            <div class="metric-desc">首字节时间</div>
          </div>
        </div>
      </el-card>

      <!-- 内存使用 -->
      <el-card shadow="hover" class="metric-card">
        <template #header>
          <div class="card-header">
            <el-icon><Cpu /></el-icon>
            <span>内存使用</span>
          </div>
        </template>
        <div class="memory-stats">
          <div class="memory-bar">
            <el-progress
              :percentage="Math.min(100, (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100)"
              :color="getMemoryColor()"
              :stroke-width="12"
            />
          </div>
          <div class="memory-details">
            <div class="detail-item">
              <span class="detail-label">已使用</span>
              <span class="detail-value">{{ memory.usedJSHeapSize }} MB</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">总堆大小</span>
              <span class="detail-value">{{ memory.totalJSHeapSize }} MB</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">堆限制</span>
              <span class="detail-value">{{ memory.jsHeapSizeLimit }} MB</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <span class="footer-tip">💡 提示: 仅开发环境可用，生产环境自动禁用</span>
        <el-button type="primary" @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Monitor, Connection, Timer, Cpu } from '@element-plus/icons-vue'
import { usePerformanceMonitor, resetAPIStats } from '@/composables/usePerformanceMonitor'

const visible = ref(false)
const {
  fps,
  apiStats,
  pageLoadMetrics: pageLoad,
  memoryUsage: memory,
  calculatePerformanceScore,
  getPerformanceLevel
} = usePerformanceMonitor()

// 性能评分
const performanceScore = computed(() => calculatePerformanceScore())
const performanceLevel = computed(() => getPerformanceLevel())

// FPS 相关
const currentFPS = computed(() => fps.value)
const fpsClass = computed(() => {
  if (fps.value >= 55) return 'fps-good'
  if (fps.value >= 30) return 'fps-warning'
  return 'fps-bad'
})
const fpsColor = computed(() => {
  if (fps.value >= 55) return '#3fb950'
  if (fps.value >= 30) return '#d29922'
  return '#f85149'
})

// 辅助函数
function getDurationColor(duration) {
  if (duration < 300) return '#3fb950'
  if (duration < 1000) return '#d29922'
  return '#f85149'
}

function getStatusType(status) {
  if (status < 300) return 'success'
  if (status < 400) return 'warning'
  return 'danger'
}

function getMetricColor(value, warning, danger) {
  if (!value) return '#8b949e'
  if (value < warning) return '#3fb950'
  if (value < danger) return '#d29922'
  return '#f85149'
}

function getMemoryColor() {
  const usage = (memory.value.usedJSHeapSize / memory.value.jsHeapSizeLimit) * 100
  if (usage < 50) return '#3fb950'
  if (usage < 80) return '#d29922'
  return '#f85149'
}

function resetStats() {
  resetAPIStats()
}

// 打开面板
function open() {
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.performance-panel {
  :deep(.el-drawer__body) {
    padding: 0;
  }
}

.panel-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 性能评分卡片
.score-card {
  :deep(.el-card__body) {
    padding: 20px;
  }

  .score-header {
    display: flex;
    align-items: center;
    gap: 16px;

    .score-emoji {
      font-size: 48px;
    }

    .score-info {
      flex: 1;

      .score-value {
        font-size: 48px;
        font-weight: bold;
        line-height: 1;
      }

      .score-label {
        font-size: 14px;
        color: var(--text-secondary);
        margin-top: 4px;
      }
    }

    .score-tag {
      font-size: 16px;
      padding: 8px 16px;
    }
  }
}

// 指标卡片
.metric-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;

    .el-button {
      margin-left: auto;
    }
  }
}

// FPS 显示
.fps-display {
  text-align: center;
  padding: 16px 0;

  .fps-value {
    font-size: 56px;
    font-weight: bold;
    line-height: 1;
    margin-bottom: 8px;

    &.fps-good {
      color: #3fb950;
    }

    &.fps-warning {
      color: #d29922;
    }

    &.fps-bad {
      color: #f85149;
    }
  }

  .fps-label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 16px;
  }
}

// API 统计
.api-stats {
  .stat-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid var(--border-light);

    &:last-child {
      border-bottom: none;
    }

    .stat-label {
      color: var(--text-secondary);
    }

    .stat-value {
      font-weight: 600;
    }
  }
}

.recent-requests {
  margin-top: 16px;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
    color: var(--text-primary);
  }
}

// 加载指标
.load-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 16px;

  .metric-item {
    text-align: center;
    padding: 12px;
    background: var(--bg-hover);
    border-radius: var(--radius-sm);

    .metric-name {
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 8px;
    }

    .metric-value {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 4px;
    }

    .metric-desc {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

// 内存统计
.memory-stats {
  .memory-bar {
    margin-bottom: 16px;
  }

  .memory-details {
    display: flex;
    justify-content: space-around;

    .detail-item {
      text-align: center;

      .detail-label {
        font-size: 12px;
        color: var(--text-secondary);
        margin-bottom: 4px;
      }

      .detail-value {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }
}

.drawer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .footer-tip {
    font-size: 13px;
    color: var(--text-secondary);
  }
}
</style>

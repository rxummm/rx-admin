<template>
  <div class="page-container" v-loading="loading">
    <!-- ==================== 系统管理统计 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#409eff"><Setting /></el-icon>
          <span>系统管理</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="3" v-for="item in systemStats" :key="item.key">
          <div class="stat-item" :style="{ borderLeftColor: item.borderColor, borderRightColor: item.borderColor, background: item.cardBgColor }" @click="safeNavigate(item.link)">
            <div class="stat-icon" :style="{ background: item.bgColor }">
              <el-icon :size="24" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ item.value }}</span>
              <span class="stat-label">{{ item.label }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 登录统计 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#409eff"><User /></el-icon>
          <span>登录统计</span>
        </div>
      </template>
      <el-row :gutter="16" style="margin-bottom:16px">
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#67c23a', background: green.cardBgColor }">
            <div class="stat-icon" :style="{ background: green.bgColor }">
              <el-icon :size="24" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ loginStats.todayLogins }}</span>
              <span class="stat-label">今日成功登录</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#f56c6c', background: red.cardBgColor }">
            <div class="stat-icon" :style="{ background: red.bgColor }">
              <el-icon :size="24" color="#f56c6c"><CircleClose /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ loginStats.todayFailLogins }}</span>
              <span class="stat-label">今日失败登录</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#e6a23c', background: orange.cardBgColor }">
            <div class="stat-icon" :style="{ background: orange.bgColor }">
              <el-icon :size="24" color="#e6a23c"><Promotion /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ exportStats.todayExports }}</span>
              <span class="label">今日导出次数</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#909399', background: gray.cardBgColor }">
            <div class="stat-icon" :style="{ background: gray.bgColor }">
              <el-icon :size="24" color="#909399"><Tickets /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ exportStats.todayExcelExports + exportStats.todayPdfExports }}</span>
              <span class="stat-label">Excel/PDF导出</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 图表区：登录趋势 + 操作日志Top10 ==================== -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#409eff"><TrendCharts /></el-icon>
              <span>最近7天登录趋势</span>
            </div>
          </template>
          <div ref="loginTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#f56c6c"><Sort /></el-icon>
              <span>今日操作频次 Top10</span>
            </div>
          </template>
          <div ref="operationTopChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 通知与消息统计 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#409eff"><Bell /></el-icon>
          <span>通知与消息</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#409eff', background: blue.cardBgColor }" @click="safeNavigate('/content/notice')">
            <div class="stat-icon" :style="{ background: blue.bgColor }">
              <el-icon :size="24" color="#409eff"><Notification /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ noticeStats.noticeCount }}</span>
              <span class="stat-label">通知总数</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#67c23a', background: green.cardBgColor }" @click="safeNavigate('/content/notice')">
            <div class="stat-icon" :style="{ background: green.bgColor }">
              <el-icon :size="24" color="#67c23a"><Reading /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ noticeStats.announcementCount }}</span>
              <span class="stat-label">公告数</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#e6a23c', background: orange.cardBgColor }" @click="safeNavigate('/content/notice')">
            <div class="stat-icon" :style="{ background: orange.bgColor }">
              <el-icon :size="24" color="#e6a23c"><Clock /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ noticeStats.todoCount }}</span>
              <span class="stat-label">待办事项</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#f56c6c', background: red.cardBgColor }" @click="safeNavigate('/content/message')">
            <div class="stat-icon" :style="{ background: red.bgColor }">
              <el-icon :size="24" color="#f56c6c"><Message /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ messageUnread }}</span>
              <span class="stat-label">未读消息</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 系统健康监控 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#67c23a"><Monitor /></el-icon>
          <span>系统健康</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#409eff', background: blue.cardBgColor }">
            <div class="stat-icon" :style="{ background: blue.bgColor }">
              <el-icon :size="24" color="#409eff"><Odometer /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ healthStats.jvmUsed }}<small style="font-size:14px;font-weight:400"> / {{ healthStats.jvmMax }}MB</small></span>
              <span class="stat-label">JVM堆内存</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#e6a23c', background: orange.cardBgColor }">
            <div class="stat-icon" :style="{ background: orange.bgColor }">
              <el-icon :size="24" color="#e6a23c"><Cpu /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ healthStats.cpuUsage }}<small style="font-size:16px;font-weight:400">%</small></span>
              <span class="stat-label">CPU使用率</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#67c23a', background: green.cardBgColor }">
            <div class="stat-icon" :style="{ background: green.bgColor }">
              <el-icon :size="24" color="#67c23a"><Coin /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ healthStats.diskUsed }}<small style="font-size:14px;font-weight:400"> / {{ healthStats.diskTotal }}GB</small></span>
              <span class="stat-label">磁盘使用</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" :style="{ borderLeftColor: '#f56c6c', background: red.cardBgColor }">
            <div class="stat-icon" :style="{ background: red.bgColor }">
              <el-icon :size="24" color="#f56c6c"><Delete /></el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ healthStats.gcCount }}<small style="font-size:14px;font-weight:400"> / {{ healthStats.gcTime }}s</small></span>
              <span class="stat-label">GC次数/耗时</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 经典文学统计 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#e6a23c"><Reading /></el-icon>
          <span>经典文学</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="4" v-for="item in literatureStats" :key="item.key">
          <div class="stat-item" :style="{ borderLeftColor: item.borderColor, borderRightColor: item.borderColor, background: item.cardBgColor }" @click="safeNavigate(item.link)">
            <div class="stat-icon" :style="{ background: item.bgColor }">
              <el-icon :size="24" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ item.value }}</span>
              <span class="stat-label">{{ item.label }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 图表区一：朝代柱状图 + 体裁饼图 ==================== -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#409eff"><Histogram /></el-icon>
              <span>各朝代作品与作者分布</span>
            </div>
          </template>
          <div ref="dynastyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#67c23a"><PieChart /></el-icon>
              <span>体裁分布</span>
            </div>
          </template>
          <div ref="genreChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 图表区二：难度分布 + 浏览量排行 ==================== -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="10">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#e6a23c"><TrendCharts /></el-icon>
              <span>难度等级分布</span>
            </div>
          </template>
          <div ref="difficultyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#f56c6c"><View /></el-icon>
              <span>浏览量 Top10</span>
            </div>
          </template>
          <div ref="viewRankChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 图表区三：四大名著对比 + 作者作品排行 ==================== -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#409eff"><DataAnalysis /></el-icon>
              <span>四大名著数据对比</span>
            </div>
          </template>
          <div ref="classicsChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="section-card chart-card">
          <template #header>
            <div class="section-header">
              <el-icon :size="20" color="#67c23a"><Sort /></el-icon>
              <span>作者作品排行</span>
            </div>
          </template>
          <div ref="authorRankChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 数据总览 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#909399"><DataBoard /></el-icon>
          <span>数据总览</span>
        </div>
      </template>
      <div class="overview-grid">
        <div class="overview-item">
          <div class="overview-circle primary">
            <span>{{ summaryData.totalSystemItems }}</span>
          </div>
          <p>系统数据</p>
        </div>
        <div class="overview-item">
          <div class="overview-circle warning">
            <span>{{ summaryData.totalLiteratureItems }}</span>
          </div>
          <p>文学数据</p>
        </div>
        <div class="overview-item">
          <div class="overview-circle danger">
            <span>{{ summaryData.totalPoems }}</span>
          </div>
          <p>名著诗词</p>
        </div>
        <div class="overview-item">
          <div class="overview-circle success">
            <span>{{ summaryData.totalClassicChars }}</span>
          </div>
          <p>名著人物</p>
        </div>
      </div>
    </el-card>

    <!-- ==================== 数据分布 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#409eff"><DataAnalysis /></el-icon>
          <span>数据分布</span>
        </div>
      </template>
      <div class="distribution-list">
        <div class="distribution-item" v-for="item in distributionData" :key="item.name">
          <div class="distribution-info">
            <span class="distribution-name">{{ item.name }}</span>
            <span class="distribution-count">{{ item.count }} ({{ item.percent }}%)</span>
          </div>
          <div class="distribution-bar">
            <div class="distribution-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- ==================== 四大名著详情 ==================== -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="book in classicBooks" :key="book.key">
        <el-card shadow="hover" class="classic-card">
          <template #header>
            <div class="classic-header">
              <span class="classic-title">{{ book.title }}</span>
              <el-tag :type="book.tagType" size="small">{{ book.dynasty }}</el-tag>
            </div>
          </template>
          <div class="classic-stats">
            <div class="classic-stat-row" v-for="stat in book.stats" :key="stat.label">
              <span class="classic-stat-label">{{ stat.label }}</span>
              <span class="classic-stat-value">{{ stat.value }}</span>
            </div>
          </div>
          <div class="classic-footer" @click="safeNavigate(book.link)">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 技术博客统计 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#409eff"><Reading /></el-icon>
          <span>技术博客统计</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="item in techblogStats" :key="item.key">
          <div class="stat-item" :style="{ borderLeftColor: item.borderColor, borderRightColor: item.borderColor, background: item.cardBgColor }" @click="safeNavigate(item.link)">
            <div class="stat-icon" :style="{ background: item.bgColor }">
              <el-icon :size="24" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ item.value }}</span>
              <span class="stat-label">{{ item.label }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 音乐播放器统计 ==================== -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#e6a23c"><Headset /></el-icon>
          <span>音乐播放器统计</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="item in musicStats" :key="item.key">
          <div class="stat-item" :style="{ borderLeftColor: item.borderColor, borderRightColor: item.borderColor, background: item.cardBgColor }" @click="safeNavigate(item.link)">
            <div class="stat-icon" :style="{ background: item.bgColor }">
              <el-icon :size="24" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-meta">
              <span class="stat-value">{{ item.value }}</span>
              <span class="stat-label">{{ item.label }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue"
import { useRouter } from "vue-router"
import { getDashboardStatsApi, getLoginStatsApi, getExportStatsApi, getOperationTop10Api } from "@/api/dashboard"
import { getNoticeSummaryApi } from "@/api/notice"
import { getUnreadCountApi } from "@/api/message"
import { getSystemHealthApi, getGcStatsApi } from "@/api/health"
import * as echarts from "echarts"
import {
  Setting, Reading, Histogram, PieChart, TrendCharts, View,
  DataAnalysis, Sort, DataBoard, ArrowRight, Headset,
  UserFilled, Collection, EditPen, MagicStick,
  User, Select, CloseBold, Download, Document,
  Bell, Notification, Clock, Message, Monitor, Odometer, Cpu, Coin, Delete,
  Lock, Grid, OfficeBuilding, Tickets, Compass, Promotion, Stamp,
  CircleCheck, CircleClose
} from "@element-plus/icons-vue"

const loading = ref(true)
const router = useRouter()

// 安全导航：路由存在才跳转，避免 No match 警告
function safeNavigate(link) {
  if (!link) return
  if (typeof link === 'object' && link.name) {
    // 命名路由：检查是否已注册
    if (router.hasRoute(link.name)) {
      router.push(link)
    }
    return
  }
  // 路径字符串：直接 push，由路由守卫兜底处理
  router.push(link)
}

const statsData = ref({})
const loginStats = ref({ todayLogins: 0, todayFailLogins: 0, trend: {} })
const exportStats = ref({ todayExports: 0, todayExcelExports: 0, todayPdfExports: 0 })
const operationTop10 = ref([])

const dynastyChartRef = ref(null)
const genreChartRef = ref(null)
const difficultyChartRef = ref(null)
const viewRankChartRef = ref(null)
const classicsChartRef = ref(null)
const authorRankChartRef = ref(null)
const loginTrendChartRef = ref(null)
const operationTopChartRef = ref(null)

const noticeStats = ref({ noticeCount: 0, announcementCount: 0, todoCount: 0 })
const messageUnread = ref(0)
const healthStats = ref({ jvmUsed: 0, jvmMax: 0, cpuUsage: 0, diskUsed: 0, diskTotal: 0, gcCount: 0, gcTime: 0 })

let dynastyChart = null, genreChart = null, difficultyChart = null
let viewRankChart = null, classicsChart = null, authorRankChart = null
let loginTrendChart = null, operationTopChart = null

const isDark = ref(document.documentElement.classList.contains("dark"))

function statCardColors(colorKey) {
  // 图标背景改为极淡的半透明色，让彩色图标清晰可见
  const iconBgLight = {
    blue: "rgba(64,158,255,0.08)",
    green: "rgba(103,194,58,0.08)",
    orange: "rgba(230,162,60,0.08)",
    red: "rgba(245,108,108,0.08)",
    gray: "rgba(144,147,153,0.06)"
  }
  const iconBgDark = {
    blue: "rgba(64,158,255,0.12)",
    green: "rgba(103,194,58,0.12)",
    orange: "rgba(230,162,60,0.12)",
    red: "rgba(245,108,108,0.12)",
    gray: "rgba(144,147,153,0.08)"
  }
  const lightMap = {
    blue: { bgColor: iconBgLight.blue, cardBgColor: "#eef6ff" },
    green: { bgColor: iconBgLight.green, cardBgColor: "#f2f9ee" },
    orange: { bgColor: iconBgLight.orange, cardBgColor: "#fef9eb" },
    red: { bgColor: iconBgLight.red, cardBgColor: "#fef3f3" },
    gray: { bgColor: iconBgLight.gray, cardBgColor: "#f7f7f8" }
  }
  const darkMap = {
    blue: { bgColor: iconBgDark.blue, cardBgColor: "rgba(64,158,255,0.06)" },
    green: { bgColor: iconBgDark.green, cardBgColor: "rgba(103,194,58,0.06)" },
    orange: { bgColor: iconBgDark.orange, cardBgColor: "rgba(230,162,60,0.06)" },
    red: { bgColor: iconBgDark.red, cardBgColor: "rgba(245,108,108,0.06)" },
    gray: { bgColor: iconBgDark.gray, cardBgColor: "rgba(144,147,153,0.04)" }
  }
  return isDark.value ? darkMap[colorKey] : lightMap[colorKey]
}

const blue = statCardColors("blue")
const green = statCardColors("green")
const orange = statCardColors("orange")
const red = statCardColors("red")
const gray = statCardColors("gray")

const systemStats = computed(() => {
  const d = statsData.value.system || {}
  return [
    { key: "user", label: "\u7528\u6237", value: d.userCount || 0, icon: UserFilled, color: "#409eff", ...blue, borderColor: "#409eff", link: "/system/user" },
    { key: "role", label: "\u89d2\u8272", value: d.roleCount || 0, icon: Lock, color: "#67c23a", ...green, borderColor: "#67c23a", link: "/system/role" },
    { key: "menu", label: "\u83dc\u5355", value: d.menuCount || 0, icon: Grid, color: "#e6a23c", ...orange, borderColor: "#e6a23c", link: "/system/menu" },
    { key: "dept", label: "\u90e8\u95e8", value: d.deptCount || 0, icon: OfficeBuilding, color: "#f56c6c", ...red, borderColor: "#f56c6c", link: "/system/dept" },
    { key: "dict", label: "\u5b57\u5178", value: d.dictTypeCount || 0, icon: EditPen, color: "#909399", ...gray, borderColor: "#909399", link: "/tool/dict" },
    { key: "notice", label: "\u516c\u544a", value: d.noticeCount || 0, icon: Reading, color: "#f56c6c", ...red, borderColor: "#f56c6c", link: "/content/notice" },
    { key: "log", label: "\u65e5\u5fd7", value: d.logCount || 0, icon: Reading, color: "#409eff", ...blue, borderColor: "#409eff", link: "/monitor/log" },
    { key: "online", label: "\u5728\u7ebf", value: d.onlineCount || 0, icon: Monitor, color: "#67c23a", ...green, borderColor: "#67c23a", link: "/monitor/online" }
  ]
})

const literatureStats = computed(() => {
  const d = statsData.value.literature || {}
  return [
    { key: "work", label: "\u6587\u5b66\u4f5c\u54c1", value: d.workCount || 0, icon: Reading, color: "#409eff", ...blue, borderColor: "#409eff", link: "/classics/literature" },
    { key: "author", label: "\u4f5c\u8005", value: d.authorCount || 0, icon: EditPen, color: "#67c23a", ...green, borderColor: "#67c23a", link: "/classics/literature" },
    { key: "dynasty", label: "\u671d\u4ee3", value: d.dynastyCount || 0, icon: Collection, color: "#e6a23c", ...orange, borderColor: "#e6a23c", link: "/classics/literature" },
    { key: "genre", label: "\u4f53\u88c1", value: d.genreCount || 0, icon: EditPen, color: "#909399", ...gray, borderColor: "#909399", link: "/classics/literature" },
    { key: "category", label: "\u5206\u7c7b", value: d.categoryCount || 0, icon: Collection, color: "#f56c6c", ...red, borderColor: "#f56c6c", link: "/classics/literature" }
  ]
})

const classicBooks = computed(() => {
  const cls = statsData.value.classics || {}
  return [
    {
      key: "honglou", title: "\u7ea2\u697c\u68a6", dynasty: "\u6e05\u4ee3", tagType: "danger", link: "/classics/honglou/poems",
      stats: [
        { label: "\u8bd7\u8bcd", value: (cls.honglou && cls.honglou.poemCount) || 0 },
        { label: "\u4eba\u7269", value: (cls.honglou && cls.honglou.characterCount) || 0 },
        { label: "\u4eba\u7269\u5173\u7cfb", value: (cls.honglou && cls.honglou.relationCount) || 0 }
      ]
    },
    {
      key: "xiyou", title: "\u897f\u6e38\u8bb0", dynasty: "\u660e\u4ee3", tagType: "warning", link: "/classics/xiyou/poems",
      stats: [
        { label: "\u8bd7\u8bcd", value: (cls.xiyou && cls.xiyou.poemCount) || 0 },
        { label: "\u4eba\u7269", value: (cls.xiyou && cls.xiyou.characterCount) || 0 },
        { label: "\u516b\u5341\u4e00\u96be", value: (cls.xiyou && cls.xiyou.eventCount) || 0 }
      ]
    },
    {
      key: "sanguo", title: "\u4e09\u56fd\u6f14\u4e49", dynasty: "\u5143\u672b\u660e\u521d", tagType: "primary", link: "/classics/sanguo/poems",
      stats: [
        { label: "\u8bd7\u8bcd", value: (cls.sanguo && cls.sanguo.poemCount) || 0 },
        { label: "\u4eba\u7269", value: (cls.sanguo && cls.sanguo.characterCount) || 0 }
      ]
    },
    {
      key: "shuihu", title: "\u6c34\u6d52\u4f20", dynasty: "\u5143\u672b\u660e\u521d", tagType: "success", link: "/classics/shuihu/poems",
      stats: [
        { label: "\u8bd7\u8bcd", value: (cls.shuihu && cls.shuihu.poemCount) || 0 },
        { label: "\u7ae0\u8282", value: (cls.shuihu && cls.shuihu.chapterCount) || 0 }
      ]
    }
  ]
})

const summaryData = computed(() => {
  const sys = statsData.value.system || {}
  const lit = statsData.value.literature || {}
  const cls = statsData.value.classics || {}
  return {
    totalSystemItems: (sys.userCount||0)+(sys.roleCount||0)+(sys.menuCount||0)+(sys.deptCount||0)+(sys.dictTypeCount||0)+(sys.noticeCount||0)+(sys.logCount||0),
    totalLiteratureItems: (lit.workCount||0)+(lit.authorCount||0)+(lit.dynastyCount||0)+(lit.genreCount||0)+(lit.categoryCount||0),
    totalPoems: cls.totalPoems||0,
    totalClassicChars: cls.totalCharacters||0
  }
})

const distributionData = computed(() => {
  const total = summaryData.value.totalSystemItems + summaryData.value.totalLiteratureItems + summaryData.value.totalPoems + summaryData.value.totalClassicChars || 1
  return [
    { name: "\u7cfb\u7edf\u7ba1\u7406", count: summaryData.value.totalSystemItems, color: "#409eff", percent: Math.round(summaryData.value.totalSystemItems/total*100) },
    { name: "\u7ecf\u5178\u6587\u5b66", count: summaryData.value.totalLiteratureItems, color: "#e6a23c", percent: Math.round(summaryData.value.totalLiteratureItems/total*100) },
    { name: "\u540d\u8457\u8bd7\u8bcd", count: summaryData.value.totalPoems, color: "#67c23a", percent: Math.round(summaryData.value.totalPoems/total*100) },
    { name: "\u540d\u8457\u4eba\u7269", count: summaryData.value.totalClassicChars, color: "#f56c6c", percent: Math.round(summaryData.value.totalClassicChars/total*100) }
  ]
})

const techblogStats = computed(() => {
  const d = statsData.value.techblog || {}
  return [
    { key: "articles", label: "\u603b\u6587\u7ae0\u6570", value: d.totalArticles||0, icon: Reading, color: "#409eff", ...blue, borderColor: "#409eff", link: {name: "TechBlogIndex"} },
    { key: "views", label: "\u603b\u6d4f\u89c8\u91cf", value: d.totalViews||0, icon: View, color: "#67c23a", ...green, borderColor: "#67c23a", link: {name: "TechBlogIndex"} }
  ]
})

const musicStats = computed(() => {
  const d = statsData.value.music || {}
  return [
    { key: "songs", label: "\u6b4c\u66f2\u6570", value: d.totalSongs||0, icon: Headset, color: "#409eff", ...blue, borderColor: "#409eff", link: {name: "ToolMusicPlayer"} },
    { key: "albums", label: "\u4e13\u8f91\u6570", value: d.totalAlbums||0, icon: Collection, color: "#67c23a", ...green, borderColor: "#67c23a", link: {name: "ToolMusicPlayer"} },
    { key: "plays", label: "\u603b\u64ad\u653e\u6b21\u6570", value: d.totalPlays||0, icon: MagicStick, color: "#f56c6c", ...red, borderColor: "#f56c6c", link: {name: "ToolMusicPlayer"} },
    { key: "artists", label: "\u827a\u672f\u5bb6\u6570", value: d.totalArtists||0, icon: UserFilled, color: "#e6a23c", ...orange, borderColor: "#e6a23c", link: {name: "ToolMusicPlayer"} }
  ]
})

const colorPalette = ["#409eff","#67c23a","#e6a23c","#f56c6c","#909399","#36cfc9","#9254de","#f759ab","#ffc53d","#13c2c2"]

function darkEchartStyle() {
  if (!isDark.value) return {}
  return {
    legend: { textStyle: { color: "#a3a6ad" } },
    xAxis: { axisLine: { lineStyle: { color: "#4c4d4f" } }, axisTick: { lineStyle: { color: "#4c4d4f" } }, axisLabel: { color: "#a3a6ad" }, splitLine: { lineStyle: { color: "#363637" } } },
    yAxis: { axisLine: { lineStyle: { color: "#4c4d4f" } }, axisTick: { lineStyle: { color: "#4c4d4f" } }, axisLabel: { color: "#a3a6ad" }, splitLine: { lineStyle: { color: "#363637" } } }
  }
}

function pieBorderColor() {
  return isDark.value ? "#1d1e1f" : "#fff"
}

function initOrUpdateChart(refEl, getOption) {
  if (!refEl) return null
  let chart = echarts.getInstanceByDom(refEl)
  if (!chart) chart = echarts.init(refEl)
  chart.setOption({ textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' }, ...getOption() }, { notMerge: true })
  return chart
}

function disposeAllCharts() {
  [dynastyChart, genreChart, difficultyChart, viewRankChart, classicsChart, authorRankChart, loginTrendChart, operationTopChart].forEach(c => { if (c) c.dispose() })
  dynastyChart = null; genreChart = null; difficultyChart = null
  viewRankChart = null; classicsChart = null; authorRankChart = null
  loginTrendChart = null; operationTopChart = null
}

function renderAllCharts() {
  disposeAllCharts()
  renderDynastyChart()
  renderGenreChart()
  renderDifficultyChart()
  renderViewRankChart()
  renderClassicsChart()
  renderAuthorRankChart()
  renderLoginTrendChart()
  renderOperationTopChart()
}

function renderDynastyChart() {
  const list = (statsData.value.literature?.dynastyStats || []).slice().sort((a, b) => (b.workCount + b.authorCount) - (a.workCount + a.authorCount)).slice(0, 12)
  if (!list.length || !dynastyChartRef.value) return
  const ds = darkEchartStyle()
  dynastyChart = initOrUpdateChart(dynastyChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    legend: { data: ["作品数", "作者数"], bottom: 0, textStyle: { fontSize: 12, ...(ds.legend?.textStyle || {}) } },
    grid: { left: "3%", right: "8%", top: 8, bottom: 40, containLabel: true },
    xAxis: { type: "category", data: list.map(i => i.dynastyName), axisLabel: { rotate: 30, fontSize: 11, ...(ds.xAxis?.axisLabel || {}) }, ...(ds.xAxis ? { axisLine: ds.xAxis.axisLine, axisTick: ds.xAxis.axisTick } : {}) },
    yAxis: { type: "value", minInterval: 1, ...(ds.yAxis || {}) },
    series: [
      { name: "作品数", type: "bar", data: list.map(i => i.workCount), itemStyle: { color: colorPalette[0], borderRadius: [4, 4, 0, 0] }, barMaxWidth: 28, emphasis: { itemStyle: { color: colorPalette[0] } } },
      { name: "作者数", type: "bar", data: list.map(i => i.authorCount), itemStyle: { color: colorPalette[3], borderRadius: [4, 4, 0, 0] }, barMaxWidth: 28, emphasis: { itemStyle: { color: colorPalette[3] } } }
    ]
  }))
}

function renderGenreChart() {
  const lit = statsData.value.literature || {}
  let list = (lit.genreStats || []).slice().sort((a, b) => (b.workCount || b.count || 0) - (a.workCount || a.count || 0))
  const top = list.slice(0, 9)
  const rest = list.slice(9).reduce((s, i) => s + (i.workCount || i.count || 0), 0)
  const data = top.map(i => ({ name: i.genreName || i.name, value: i.workCount || i.count }))
  if (rest > 0) data.push({ name: "其他", value: rest })
  if (!data.length || !genreChartRef.value) return
  const ds = darkEchartStyle()
  genreChart = initOrUpdateChart(genreChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "item", formatter: "{b}: {c} 篇 ({d}%)" },
    legend: { type: "scroll", orient: "vertical", right: 5, top: 5, bottom: 5, textStyle: { fontSize: 11, ...(ds.legend?.textStyle || {}) } },
    series: [{
      type: "pie", radius: ["48%", "75%"], center: ["38%", "50%"],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: pieBorderColor(), borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: "bold" } },
      data: data.map((i, idx) => ({ ...i, itemStyle: { color: colorPalette[idx % colorPalette.length], borderRadius: 4, borderColor: pieBorderColor(), borderWidth: 2 } }))
    }]
  }))
}

function renderDifficultyChart() {
  const list = (statsData.value.literature?.difficultyStats || []).slice()
  if (!list.length || !difficultyChartRef.value) return
  const ds = darkEchartStyle()
  difficultyChart = initOrUpdateChart(difficultyChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "item", formatter: "{b}: {c} 篇 ({d}%)" },
    legend: { bottom: 0, textStyle: { fontSize: 11, ...(ds.legend?.textStyle || {}) } },
    series: [{
      type: "pie", radius: ["45%", "72%"], center: ["50%", "45%"],
      itemStyle: { borderRadius: 4, borderColor: pieBorderColor(), borderWidth: 2 },
      label: { formatter: "{b}\n{d}%", fontSize: 11 },
      data: list.map((i, idx) => ({ name: i.label, value: i.workCount, itemStyle: { color: colorPalette[idx % colorPalette.length], borderRadius: 4, borderColor: pieBorderColor(), borderWidth: 2 } }))
    }]
  }))
}

function renderViewRankChart() {
  const list = (statsData.value.literature?.viewRankStats || []).slice()
  if (!list.length || !viewRankChartRef.value) return
  const ds = darkEchartStyle()
  viewRankChart = initOrUpdateChart(viewRankChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" }, formatter: (p) => `${p[0].name}<br/>${"浏览量"} ${p[0].value}` },
    grid: { left: "2%", right: "8%", top: 5, bottom: 5 },
    xAxis: { type: "value", name: "次", ...(ds.xAxis || {}) },
    yAxis: { type: "category", data: list.map(i => i.title).reverse(), axisLabel: { fontSize: 11, ...(ds.yAxis?.axisLabel || {}) }, inverse: true, ...(ds.yAxis ? { axisLine: ds.yAxis.axisLine, axisTick: ds.yAxis.axisTick, splitLine: ds.yAxis.splitLine } : {}) },
    series: [{
      type: "bar", data: list.map(i => i.viewCount).reverse(),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: "#f56c6c" }, { offset: 1, color: isDark.value ? "#5a2a2a" : "#fab6b6" }]) },
      barMaxWidth: 24, label: { show: true, position: "right", fontSize: 10 }
    }]
  }))
}

function renderClassicsChart() {
  const cls = statsData.value.classics || {}
  const books = [
    { key: "honglou", name: "红楼梦", color: "#f56c6c", data: cls.honglou || {} },
    { key: "xiyou", name: "西游记", color: "#e6a23c", data: cls.xiyou || {} },
    { key: "sanguo", name: "三国演义", color: "#409eff", data: cls.sanguo || {} },
    { key: "shuihu", name: "水浒传", color: "#67c23a", data: cls.shuihu || {} }
  ]
  const categories = ["诗词数", "人物数", "关系数", "事件数", "章节数"]
  const catKeyMap = { "诗词数": "poemCount", "人物数": "characterCount", "关系数": "relationCount", "事件数": "eventCount", "章节数": "chapterCount" }
  if (!classicsChartRef.value) return
  const ds = darkEchartStyle()
  classicsChart = initOrUpdateChart(classicsChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    legend: { data: books.map(b => b.name), bottom: 0, textStyle: { fontSize: 12, ...(ds.legend?.textStyle || {}) } },
    grid: { left: "3%", right: "5%", top: 12, bottom: 36 },
    xAxis: { type: "category", data: categories, axisLabel: { fontSize: 12 }, ...(ds.xAxis ? { axisLine: ds.xAxis.axisLine, axisTick: ds.xAxis.axisTick } : {}) },
    yAxis: { type: "value", minInterval: 1, ...(ds.yAxis || {}) },
    series: books.map(book => ({
      name: book.name, type: "bar",
      data: categories.map(cat => book.data[catKeyMap[cat]] || 0),
      itemStyle: { color: book.color, borderRadius: [4, 4, 0, 0] }, barMaxWidth: 24
    }))
  }))
}

function renderAuthorRankChart() {
  const list = (statsData.value.literature?.authorRankStats || []).slice()
  if (!list.length || !authorRankChartRef.value) return
  const ds = darkEchartStyle()
  authorRankChart = initOrUpdateChart(authorRankChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" }, formatter: (p) => `${p[0].name}<br/>${"作品数"} ${p[0].value}` },
    grid: { left: "2%", right: "8%", top: 5, bottom: 5 },
    xAxis: { type: "value", minInterval: 1, ...(ds.xAxis || {}) },
    yAxis: { type: "category", data: list.map(i => i.authorName).reverse(), axisLabel: { fontSize: 11, ...(ds.yAxis?.axisLabel || {}) }, inverse: true, ...(ds.yAxis ? { axisLine: ds.yAxis.axisLine, axisTick: ds.yAxis.axisTick, splitLine: ds.yAxis.splitLine } : {}) },
    series: [{
      type: "bar", data: list.map(i => i.workCount).reverse(),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: "#409eff" }, { offset: 1, color: isDark.value ? "#1a3a5c" : "#a0cfff" }]) },
      barMaxWidth: 24, label: { show: true, position: "right", fontSize: 10 }
    }]
  }))
}

function renderLoginTrendChart() {
  const trend = loginStats.value.trend || {}
  const dates = Object.keys(trend).sort()
  const values = dates.map(d => trend[d] || 0)
  if (!dates.length || !loginTrendChartRef.value) return
  const ds = darkEchartStyle()
  loginTrendChart = initOrUpdateChart(loginTrendChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "6%", top: 10, bottom: 5, containLabel: true },
    xAxis: { type: "category", data: dates.map(d => d.substring(5)), axisLabel: { fontSize: 11 }, ...(ds.xAxis ? { axisLine: ds.xAxis.axisLine, axisTick: ds.xAxis.axisTick } : {}) },
    yAxis: { type: "value", minInterval: 1, ...(ds.yAxis || {}) },
    series: [{
      name: "登录次数", type: "line",
      data: values,
      smooth: true,
      lineStyle: { color: "#409eff", width: 2 },
      itemStyle: { color: "#409eff" },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: "rgba(64,158,255,0.3)" }, { offset: 1, color: "rgba(64,158,255,0.02)" }]) },
      symbol: "circle", symbolSize: 6,
      label: { show: true, position: "top", fontSize: 11, color: "#409eff" }
    }]
  }))
}

function renderOperationTopChart() {
  const list = operationTop10.value || []
  if (!list.length || !operationTopChartRef.value) return
  const ds = darkEchartStyle()
  operationTopChart = initOrUpdateChart(operationTopChartRef.value, () => ({
    ...ds,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" }, formatter: (p) => `${p[0].name}<br/>${"操作次数"} ${p[0].value}` },
    grid: { left: "2%", right: "8%", top: 5, bottom: 5 },
    xAxis: { type: "value", minInterval: 1, ...(ds.xAxis || {}) },
    yAxis: { type: "category", data: list.map(i => i.operation || "未知").reverse(), axisLabel: { fontSize: 11, ...(ds.yAxis?.axisLabel || {}) }, inverse: true, ...(ds.yAxis ? { axisLine: ds.yAxis.axisLine, axisTick: ds.yAxis.axisTick, splitLine: ds.yAxis.splitLine } : {}) },
    series: [{
      type: "bar", data: list.map(i => i.count || 0).reverse(),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: "#f56c6c" }, { offset: 1, color: isDark.value ? "#5a2a2a" : "#fab6b6" }]) },
      barMaxWidth: 24, label: { show: true, position: "right", fontSize: 10 }
    }]
  }))
}

function onResize() {
  [dynastyChart, genreChart, difficultyChart, viewRankChart, classicsChart, authorRankChart, loginTrendChart, operationTopChart].forEach(c => { if (c) c.resize() })
}

async function fetchStats() {
  loading.value = true
  try {
    const res = await getDashboardStatsApi()
    statsData.value = res.data
    renderAllCharts()
  } finally {
    loading.value = false
  }
}

async function fetchEnhancedStats() {
  try {
    const [loginRes, exportRes, opTopRes] = await Promise.all([
      getLoginStatsApi(),
      getExportStatsApi(),
      getOperationTop10Api()
    ])
    if (loginRes.code === 200) loginStats.value = loginRes.data
    if (exportRes.code === 200) exportStats.value = exportRes.data
    if (opTopRes.code === 200) operationTop10.value = opTopRes.data
    renderLoginTrendChart()
    renderOperationTopChart()
  } catch (e) {
    console.warn("获取增强仪表盘数据失败:", e)
  }
}

async function fetchNoticeAndHealthStats() {
  try {
    const [noticeRes, msgRes, healthRes, gcRes] = await Promise.all([
      getNoticeSummaryApi(),
      getUnreadCountApi(),
      getSystemHealthApi(),
      getGcStatsApi()
    ])
    if (noticeRes.code === 200) {
      const d = noticeRes.data
      noticeStats.value = {
        noticeCount: (d.noticeCount || 0) + (d.announcementCount || 0),
        announcementCount: d.announcementCount || 0,
        todoCount: d.todoCount || 0
      }
    }
    if (msgRes.code === 200) messageUnread.value = msgRes.data?.count || 0
    if (healthRes.code === 200) {
      const h = healthRes.data
      healthStats.value = {
        jvmUsed: Math.round(h.jvm?.heapUsed || 0),
        jvmMax: Math.round(h.jvm?.heapMax || 0),
        cpuUsage: Math.round((h.cpu?.usage || 0) * 10) / 10,
        diskUsed: Math.round(h.disk?.used || 0),
        diskTotal: Math.round(h.disk?.total || 0),
        gcCount: 0,
        gcTime: 0
      }
    }
    if (gcRes.code === 200) {
      healthStats.value.gcCount = gcRes.data?.totalCount || 0
      healthStats.value.gcTime = Math.round((gcRes.data?.totalTimeSeconds || 0) * 10) / 10
    }
  } catch (e) {
    console.warn("获取通知与健康数据失败:", e)
  }
}

let sseEventSource = null

function startSse() {
  stopSse()
  try {
    const token = localStorage.getItem("rx_admin_token")
    const url = token ? "/api/dashboard/stream?token=" + encodeURIComponent(token) : "/api/dashboard/stream"
    sseEventSource = new EventSource(url)
    sseEventSource.addEventListener("stats", (event) => {
      try {
        const parsed = JSON.parse(event.data)
        if (parsed.code === 200 && parsed.data) {
          statsData.value = parsed.data
          renderAllCharts()
        }
      } catch (e) { console.warn("SSE parse error:", e) }
    })
    sseEventSource.onerror = () => { stopSse() }
  } catch (e) { console.warn("SSE init error:", e) }
}

function stopSse() {
  if (sseEventSource) { sseEventSource.close(); sseEventSource = null }
}

onBeforeUnmount(() => { stopSse() })

onMounted(() => {
  fetchStats().then(() => fetchEnhancedStats()).then(() => fetchNoticeAndHealthStats())
  startSse()
  window.addEventListener("resize", onResize)
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px 24px;
  background: var(--bg-page);
  min-height: 100%;
  overflow: auto !important;
  margin: 0 !important;
}

.section-card {
  margin-bottom: 24px;
  border-radius: 12px;
  background: var(--bg-container);
  border: none;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
  overflow: visible;
}

.section-card :deep(.el-card__header) {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-lighter);
  background: var(--bg-container);
  border-radius: 12px 12px 0 0;
}

.section-card :deep(.el-card__body) {
  padding: 20px 24px;
  overflow: visible;
}

html.dark .section-card {
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.2);
}

.chart-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  position: relative;
  padding-left: 14px;

  // 左侧色条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 20px;
    border-radius: var(--radius-xs);
    background: var(--color-primary);
  }
}

.section-card :deep(.el-col) {
  margin-bottom: 14px;
}

.chart-row .el-card {
  margin-bottom: 24px;
}

.chart-container {
  width: 100%;
  height: 320px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border-left: 3px solid transparent;
  border-right: 3px solid transparent;
  background: linear-gradient(135deg, var(--bg-container) 0%, var(--bg-hover) 100%);
  animation: card-entry 0.5s ease both;
  overflow: hidden;
  position: relative;
}

// 卡片入场交错动画
.stat-item:nth-child(1) { animation-delay: 0.04s; }
.stat-item:nth-child(2) { animation-delay: 0.08s; }
.stat-item:nth-child(3) { animation-delay: 0.12s; }
.stat-item:nth-child(4) { animation-delay: 0.16s; }
.stat-item:nth-child(5) { animation-delay: 0.2s; }
.stat-item:nth-child(6) { animation-delay: 0.24s; }
.stat-item:nth-child(7) { animation-delay: 0.28s; }
.stat-item:nth-child(8) { animation-delay: 0.32s; }

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);

  :deep(.el-icon) {
    filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
    // 强制清晰渲染
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
  }
}

.stat-item:hover .stat-icon {
  transform: scale(1.08);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.el-row + .el-row {
  margin-top: 24px;
}

.classic-card {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 12px;
  border-top: 3px solid transparent;
  background: var(--bg-container);
  border-left: none;
  border-right: none;
  border-bottom: none;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
}

.classic-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.classic-card:nth-child(1) { border-top-color: #f56c6c; }
.classic-card:nth-child(2) { border-top-color: #e6a23c; }
.classic-card:nth-child(3) { border-top-color: #409eff; }
.classic-card:nth-child(4) { border-top-color: #67c23a; }

.classic-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.classic-card :deep(.el-card__body) {
  padding: 16px 20px 0;
  background: var(--bg-container);
}

html.dark .classic-card {
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.2);
}

.classic-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.classic-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.classic-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.classic-stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed var(--border-light);
}

.classic-stat-row:last-child {
  border-bottom: none;
}

.classic-stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.classic-stat-value {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

.classic-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 12px 0;
  margin-top: 14px;
  border-top: 1px solid var(--border-lighter);
  font-size: 13px;
  color: var(--color-primary);
  cursor: pointer;
  transition: color 0.2s;
}

.classic-footer:hover {
  color: var(--color-primary-light);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.overview-item {
  text-align: center;
}

.overview-item p {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}

.overview-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.overview-circle.primary { background: linear-gradient(135deg, #409eff, #66b1ff); }
.overview-circle.warning { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.overview-circle.danger  { background: linear-gradient(135deg, #f56c6c, #f78989); }
.overview-circle.success { background: linear-gradient(135deg, #67c23a, #85ce61); }

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.distribution-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.distribution-name {
  color: var(--text-regular);
}

.distribution-count {
  color: var(--text-secondary);
}

.distribution-bar {
  height: 10px;
  background: var(--border-lighter);
  border-radius: 5px;
  overflow: hidden;
}

.distribution-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.8s ease;
}

@keyframes card-entry {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

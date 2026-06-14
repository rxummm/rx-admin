# 性能监控与错误边界使用指南

> **版本**: v2.2.0  
> **更新日期**: 2026-06-13

---

## 📊 性能监控面板

### 功能特性

✅ **FPS 实时监控** - 60 FPS 流畅度监测  
✅ **API 请求统计** - 自动记录所有 API 耗时  
✅ **首屏加载指标** - FCP/LCP/FID/CLS/TTFB  
✅ **内存使用监控** - JS Heap 使用情况  
✅ **综合性能评分** - 0-100 分智能评分  

---

### 访问方式

**开发环境专属**（生产环境自动隐藏）：

1. 点击顶栏右侧的 **📊 图标**
2. 或按快捷键 `Ctrl+P`（待实现）

---

### 核心指标说明

#### **1. FPS（帧率）**

| 范围 | 等级 | 颜色 | 说明 |
|------|------|------|------|
| 55-60 | 优秀 | 🟢 #3fb950 | 极其流畅 |
| 30-54 | 警告 | 🟡 #d29922 | 轻微卡顿 |
| < 30 | 较差 | 🔴 #f85149 | 明显卡顿 |

---

#### **2. API 请求统计**

**自动监控项**：
- ✅ 总请求数
- ✅ 失败请求数
- ✅ 平均响应时间
- ✅ 最慢请求
- ✅ 最近 50 个请求详情

**颜色标识**：
- 🟢 < 300ms：快速
- 🟡 300-1000ms：一般
- 🔴 > 1000ms：缓慢

---

#### **3. 首屏加载指标（Web Vitals）**

| 指标 | 全称 | 优秀 | 一般 | 较差 |
|------|------|------|------|------|
| **FCP** | First Contentful Paint | < 1.5s | 1.5-3s | > 3s |
| **LCP** | Largest Contentful Paint | < 2.5s | 2.5-4s | > 4s |
| **FID** | First Input Delay | < 100ms | 100-300ms | > 300ms |
| **CLS** | Cumulative Layout Shift | < 0.1 | 0.1-0.25 | > 0.25 |
| **TTFB** | Time to First Byte | < 500ms | 500-1000ms | > 1000ms |

---

#### **4. 内存使用**

**显示内容**：
- 已使用 JS Heap（MB）
- 总堆大小（MB）
- 堆限制（MB）
- 使用百分比进度条

**颜色标识**：
- 🟢 < 50%：健康
- 🟡 50-80%：注意
- 🔴 > 80%：警告

---

#### **5. 综合性能评分**

**计算公式**：
```javascript
score = 100
  - FPS 扣分（最多 30 分）
  - FCP 扣分（最多 20 分）
  - API 失败率扣分（最多 20 分）
  - 平均响应时间扣分（最多 15 分）
```

**等级划分**：
- 🚀 90-100：优秀
- ✨ 70-89：良好
- ⚠️ 50-69：一般
- ❌ < 50：较差

---

### 技术实现

#### **FPS 监控**
```javascript
// usePerformanceMonitor.js
function updateFPS(currentTime) {
  frames++
  if (currentTime - lastFrameTime >= 1000) {
    fps.value = frames
    frames = 0
    lastFrameTime = currentTime
  }
  requestAnimationFrame(updateFPS)
}
```

#### **API 拦截器**
```javascript
// performanceInterceptor.js
export function performanceRequestInterceptor(config) {
  requestStartTime.set(config.url, Date.now())
  return config
}

export function performanceResponseSuccessInterceptor(response) {
  const duration = Date.now() - requestStartTime.get(url)
  recordAPIRequest(url, method, startTime, endTime, status)
}
```

#### **Web Vitals 收集**
```javascript
// 使用 Performance Observer API
const lcpObserver = new PerformanceObserver((list) => {
  const entries = list.getEntries()
  pageLoadMetrics.value.LCP = Math.round(entries[entries.length - 1].startTime)
})
lcpObserver.observe({ type: 'largest-contentful-paint', buffered: true })
```

---

## 🛡️ 错误边界组件

### 功能特性

✅ **组件级错误捕获** - 不崩溃整个应用  
✅ **友好的错误 UI** - 清晰的错误提示  
✅ **错误详情展示** - 开发环境可查看堆栈  
✅ **一键重试** - 快速恢复  
✅ **错误上报** - 集成 Sentry（可选）  
✅ **剪贴板复制** - 方便反馈问题  

---

### 基础用法

#### **1. 包裹单个组件**

```vue
<template>
  <ErrorBoundary title="图表加载失败" message="数据可视化组件出错">
    <ComplexChart :data="chartData" />
  </ErrorBoundary>
</template>

<script setup>
import ErrorBoundary from '@/components/ErrorBoundary.vue'
</script>
```

---

#### **2. 包裹整个页面**

```vue
<template>
  <ErrorBoundary 
    title="页面加载失败"
    message="抱歉，该页面遇到了一些问题"
    @error="handleError"
    @retry="handleRetry"
  >
    <PageContent />
  </ErrorBoundary>
</template>

<script setup>
function handleError(err, info) {
  console.error('页面错误:', err, info)
  // 上报到监控系统
}

function handleRetry() {
  // 重新加载数据
  fetchData()
}
</script>
```

---

#### **3. 嵌套使用**

```vue
<template>
  <ErrorBoundary title="主区域错误">
    <el-row :gutter="16">
      <el-col :span="12">
        <ErrorBoundary title="左侧面板错误">
          <LeftPanel />
        </ErrorBoundary>
      </el-col>
      <el-col :span="12">
        <ErrorBoundary title="右侧面板错误">
          <RightPanel />
        </ErrorBoundary>
      </el-col>
    </el-row>
  </ErrorBoundary>
</template>
```

---

### Props 配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | String | "组件加载失败" | 错误标题 |
| `message` | String | "抱歉..." | 错误描述 |
| `showDetails` | Boolean | true | 是否显示错误详情 |
| `onError` | Function | null | 错误回调函数 |

---

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `error` | `(err, info)` | 捕获到错误时触发 |
| `retry` | - | 用户点击重试时触发 |

---

### Methods

| 方法名 | 参数 | 说明 |
|--------|------|------|
| `reset()` | - | 手动重置错误状态 |

**使用示例**：
```vue
<template>
  <ErrorBoundary ref="errorBoundaryRef">
    <MyComponent />
  </ErrorBoundary>
</template>

<script setup>
const errorBoundaryRef = ref(null)

function clearError() {
  errorBoundaryRef.value?.reset()
}
</script>
```

---

### 最佳实践

#### **1. 关键组件必须包裹**

```vue
<!-- ✅ 推荐 -->
<ErrorBoundary>
  <EChartsComponent />
</ErrorBoundary>

<!-- ❌ 不推荐 - 图表崩溃会导致整个页面白屏 -->
<EChartsComponent />
```

---

#### **2. 异步组件配合使用**

```vue
<template>
  <ErrorBoundary title="模块加载失败">
    <Suspense>
      <template #default>
        <AsyncComponent />
      </template>
      <template #fallback>
        <SkeletonLoader type="card" />
      </template>
    </Suspense>
  </ErrorBoundary>
</template>
```

---

#### **3. 列表项单独包裹**

```vue
<template>
  <div v-for="item in list" :key="item.id">
    <ErrorBoundary :title="`项目 ${item.name} 加载失败`">
      <ItemCard :data="item" />
    </ErrorBoundary>
  </div>
</template>
```

---

#### **4. 路由视图包裹**

```vue
<!-- App.vue 或 Layout -->
<template>
  <ErrorBoundary title="页面渲染失败">
    <router-view />
  </ErrorBoundary>
</template>
```

---

## 🔧 Sentry 集成（可选）

### 安装依赖

```bash
npm install @sentry/vue @sentry/tracing
```

---

### 配置环境变量

**.env.production**：
```env
VITE_SENTRY_DSN=https://xxx@xxx.ingest.sentry.io/xxx
VITE_APP_VERSION=2.2.0
```

---

### 自动初始化

已在 `main.js` 中配置，生产环境自动启用：

```javascript
if (import.meta.env.PROD && import.meta.env.VITE_SENTRY_DSN) {
  import('./utils/sentry').then(({ initSentry }) => {
    initSentry(app, router)
  })
}
```

---

### 手动上报错误

```javascript
import { captureException, captureMessage } from '@/utils/sentry'

// 捕获异常
try {
  riskyOperation()
} catch (error) {
  captureException(error, {
    tags: { feature: 'user-management' }
  })
}

// 捕获消息
captureMessage('用户执行了特殊操作', 'warning')
```

---

### 设置用户信息

```javascript
import { setSentryUser, clearSentryUser } from '@/utils/sentry'

// 登录成功后
setSentryUser({
  id: user.id,
  username: user.username,
  email: user.email
})

// 登出时
clearSentryUser()
```

---

## 📈 性能优化建议

### **基于监控数据的优化**

#### **1. FPS < 50**

**可能原因**：
- 大量 DOM 操作
- 复杂的 CSS 动画
- 频繁的组件重渲染

**优化方案**：
```javascript
// 使用 requestAnimationFrame
requestAnimationFrame(() => {
  updateDOM()
})

// 使用 v-memo 缓存
<div v-memo="[selected, count]">...</div>

// 减少响应式数据
const nonReactive = shallowRef(largeObject)
```

---

#### **2. API 平均响应时间 > 1000ms**

**可能原因**：
- 后端查询慢
- 网络延迟高
- 请求过多

**优化方案**：
```javascript
// 添加请求缓存
const cache = new Map()
async function fetchData(key) {
  if (cache.has(key)) return cache.get(key)
  const data = await api.get(key)
  cache.set(key, data)
  return data
}

// 合并请求
Promise.all([api.getUsers(), api.getRoles()])

// 添加防抖
const debouncedSearch = debounce(search, 300)
```

---

#### **3. FCP > 3s**

**可能原因**：
- 首屏资源过大
- JavaScript 阻塞渲染
- CSS 未内联关键样式

**优化方案**：
```javascript
// 代码分割
const HeavyComponent = defineAsyncComponent(() => 
  import('./HeavyComponent.vue')
)

// 预加载关键资源
<link rel="preload" href="/critical.css" as="style">

// 服务端渲染（SSR）
// 考虑迁移到 Nuxt.js
```

---

#### **4. CLS > 0.25**

**可能原因**：
- 图片未指定尺寸
- 动态插入广告
- 字体加载导致布局偏移

**优化方案**：
```vue
<!-- 指定图片尺寸 -->
<img src="..." width="800" height="600" />

<!-- 预留空间 -->
<div style="min-height: 200px;">
  <AsyncAd />
</div>

<!-- 字体交换策略 -->
@font-face {
  font-display: swap;
}
```

---

## 🎯 实战案例

### **案例 1：仪表盘性能优化**

**问题**：仪表盘 FPS 降至 45，LCP 超过 4s

**排查过程**：
1. 打开性能监控面板
2. 发现 ECharts 图表频繁重绘
3. API 请求串行执行，总耗时 2.5s

**优化方案**：
```javascript
// 1. 图表按需更新
watch(chartData, (newData) => {
  chart.setOption(newData, { notMerge: false }) // 增量更新
}, { deep: false })

// 2. 并行请求
const [stats, charts, logs] = await Promise.all([
  getStatsApi(),
  getChartsApi(),
  getLogsApi()
])

// 3. 虚拟滚动长列表
import { RecycleScroller } from 'vue-virtual-scroller'
```

**效果**：
- ✅ FPS 提升至 58
- ✅ LCP 降至 1.8s
- ✅ 性能评分从 62 → 89

---

### **案例 2：错误边界防止崩溃**

**问题**：某个用户的特殊数据导致图表组件崩溃，整个页面白屏

**优化前**：
```vue
<template>
  <DashboardCharts :data="userData" />
</template>
```

**优化后**：
```vue
<template>
  <ErrorBoundary 
    title="图表渲染失败"
    message="您的数据格式可能存在问题，请联系管理员"
    @error="(err) => reportToSentry(err)"
  >
    <DashboardCharts :data="userData" />
  </ErrorBoundary>
</template>
```

**效果**：
- ✅ 其他组件正常运行
- ✅ 用户看到友好提示
- ✅ 错误自动上报到 Sentry
- ✅ 开发者快速定位问题

---

## 📝 总结

### **性能监控核心价值**

1. **实时发现问题** - FPS 下降、API 变慢立即感知
2. **量化优化效果** - 用数据证明优化的价值
3. **用户体验保障** - 确保 Web Vitals 达标
4. **开发效率提升** - 快速定位性能瓶颈

---

### **错误边界核心价值**

1. **故障隔离** - 局部错误不影响全局
2. **优雅降级** - 友好的错误提示
3. **快速恢复** - 一键重试机制
4. **问题追踪** - 集成 Sentry 上报

---

### **下一步计划**

- [ ] 添加性能告警（FPS < 30 时通知）
- [ ] 实现自动化性能回归测试
- [ ] 添加更多自定义监控指标
- [ ] 优化移动端性能监控

---

**相关文档**：
- [性能监控服务源码](file://d:\vueprojects\RX\ui\src\composables\usePerformanceMonitor.js)
- [性能面板组件](file://d:\vueprojects\RX\ui\src\components\PerformancePanel.vue)
- [错误边界组件](file://d:\vueprojects\RX\ui\src\components\ErrorBoundary.vue)
- [Sentry 集成](file://d:\vueprojects\RX\ui\src\utils\sentry.js)

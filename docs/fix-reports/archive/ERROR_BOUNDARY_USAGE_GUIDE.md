# 友好的错误 UI - 完整使用指南

> **实施日期**: 2026-06-13  
> **功能**: 组件级错误边界 + 全局错误页面 + 全局错误处理器

---

## 📋 目录

1. [功能概述](#功能概述)
2. [ErrorBoundary 组件](#errorboundary-组件)
3. [ErrorPage 全局错误页面](#errorpage-全局错误页面)
4. [GlobalErrorHandler 全局错误处理器](#globalerrorhandler-全局错误处理器)
5. [使用示例](#使用示例)
6. [最佳实践](#最佳实践)

---

## 🎯 功能概述

### **三层错误处理架构**

```
┌─────────────────────────────────────────────┐
│          GlobalErrorHandler                 │
│     (捕获所有未处理的运行时错误)              │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│            ErrorPage                         │
│     (路由级别的 404/500/403 错误页面)        │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         ErrorBoundary                        │
│     (组件级的错误隔离和友好展示)              │
└─────────────────────────────────────────────┘
```

### **核心特性**

✅ **智能错误分类**：自动识别网络错误、权限错误、服务器错误等  
✅ **动态图标显示**：根据错误类型显示不同的图标和颜色  
✅ **错误代码展示**：清晰显示 HTTP 状态码或业务错误码  
✅ **时间戳记录**：记录错误发生的具体时间  
✅ **技术详情折叠**：开发环境可查看完整堆栈信息  
✅ **一键复制错误**：方便开发者快速定位问题  
✅ **重试机制**：用户可尝试重新加载组件  
✅ **优雅降级**：单个组件错误不影响整个应用  

---

## 🔧 ErrorBoundary 组件

### **基础用法**

```vue
<template>
  <ErrorBoundary>
    <YourComponent />
  </ErrorBoundary>
</template>

<script setup>
import ErrorBoundary from '@/components/ErrorBoundary.vue'
</script>
```

### **自定义配置**

```vue
<ErrorBoundary 
  title="数据加载失败"
  message="无法获取最新数据，请检查网络连接后重试。"
  error-code="NETWORK_ERROR"
  :show-details="true"
  @error="handleError"
  @retry="handleRetry"
>
  <DataChart />
</ErrorBoundary>
```

### **Props 说明**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | String | '组件加载失败' | 错误标题 |
| `message` | String | '抱歉，该组件遇到了一些问题...' | 错误描述 |
| `error-code` | String/Number | '' | 错误代码（HTTP 状态码或业务码） |
| `show-details` | Boolean | true | 是否显示技术详情（开发环境） |
| `onError` | Function | null | 错误回调函数 |

### **Events**

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `error` | `(err, info)` | 捕获到错误时触发 |
| `retry` | - | 用户点击重试按钮时触发 |

### **暴露的方法**

```javascript
const boundaryRef = ref(null)

// 重置错误状态
boundaryRef.value?.reset()
```

### **错误类型自动检测**

组件会自动识别以下错误类型并显示对应图标：

| 错误类型 | 图标 | 颜色 | 触发条件 |
|---------|------|------|---------|
| Network | Connection | #f85149 | "Network Error", "Failed to fetch" |
| Permission | Lock | #d29922 | 401, 403, "permission" |
| Server | Server | #f85149 | 5xx, "Internal Server Error" |
| Not Found | CircleCloseFilled | #8b949e | 404, "Not Found" |
| Unknown | CircleCloseFilled | #f85149 | 其他错误 |

---

## 🌐 ErrorPage 全局错误页面

### **路由配置**

已自动添加到路由中：

```javascript
{
  path: 'error/:code(\\d+)',
  name: 'ErrorPage',
  component: () => import('@/views/error/ErrorPage.vue'),
  meta: { title: '错误页面', hidden: true }
}
```

### **访问方式**

```javascript
// 编程式导航
router.push('/error/404')
router.push('/error/500')
router.push('/error/403')

// 或直接访问 URL
http://localhost:3000/error/404
```

### **支持的错误码**

| 错误码 | 标题 | 描述 |
|--------|------|------|
| 404 | 页面未找到 | 您访问的页面不存在或已被移除 |
| 500 | 服务器内部错误 | 服务器遇到意外情况 |
| 403 | 禁止访问 | 您没有权限访问此页面 |
| 401 | 未授权 | 请先登录后再访问 |
| 503 | 服务不可用 | 服务暂时不可用 |

### **页面特性**

✨ **动态粒子背景**：浮动粒子动画效果  
✨ **大字号错误码**：半透明背景错误码  
✨ **脉冲动画图标**：吸引注意力的动态图标  
✨ **建议操作列表**：引导用户解决问题  
✨ **多语言支持**：可根据 i18n 扩展  

---

## 🛡️ GlobalErrorHandler 全局错误处理器

### **自动捕获的错误类型**

1. **Unhandled Promise Rejection**：未处理的 Promise 拒绝
2. **Runtime Errors**：JavaScript 运行时错误
3. **Resource Load Errors**：图片、脚本等资源加载失败

### **初始化**

已在 `main.js` 中自动启动：

```javascript
import('./utils/globalErrorHandler').then(({ initGlobalErrorHandler }) => {
  initGlobalErrorHandler()
})
```

### **错误节流机制**

- **相同错误节流**：2秒内相同错误只显示一次提示
- **总次数限制**：最多显示 3 次错误提示
- **自动重置**：10秒后重置计数器

### **手动上报错误**

```javascript
import { reportManualError } from '@/utils/globalErrorHandler'

try {
  // 可能出错的代码
  doSomethingRisky()
} catch (error) {
  reportManualError(error, {
    context: '用户点击导出按钮',
    userId: userStore.userId
  })
}
```

### **清除错误缓存**

```javascript
import { clearErrorCache } from '@/utils/globalErrorHandler'

// 在用户登录成功后清除之前的错误记录
clearErrorCache()
```

---

## 💡 使用示例

### **示例 1：包裹 API 调用组件**

```vue
<template>
  <ErrorBoundary 
    title="数据加载失败"
    message="无法从服务器获取数据，请检查网络连接。"
    @retry="fetchData"
  >
    <UserList v-if="dataLoaded" :data="users" />
  </ErrorBoundary>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import UserList from './UserList.vue'
import { getUserList } from '@/api/user'

const dataLoaded = ref(false)
const users = ref([])

async function fetchData() {
  try {
    const res = await getUserList()
    users.value = res.data
    dataLoaded.value = true
  } catch (error) {
    // ErrorBoundary 会自动捕获这个错误
    throw error
  }
}

onMounted(fetchData)
</script>
```

### **示例 2：嵌套错误边界**

```vue
<template>
  <ErrorBoundary title="页面主体">
    <div class="page-content">
      <h1>仪表盘</h1>
      
      <!-- 图表区域独立错误隔离 -->
      <ErrorBoundary title="销售图表">
        <SalesChart />
      </ErrorBoundary>
      
      <!-- 表格区域独立错误隔离 -->
      <ErrorBoundary title="订单列表">
        <OrderTable />
      </ErrorBoundary>
    </div>
  </ErrorBoundary>
</template>
```

**优势**：即使图表加载失败，订单列表仍可正常显示。

### **示例 3：动态错误代码**

```vue
<template>
  <ErrorBoundary 
    :title="errorTitle"
    :message="errorMessage"
    :error-code="errorCode"
  >
    <ApiDebugPanel />
  </ErrorBoundary>
</template>

<script setup>
import { computed } from 'vue'
import ErrorBoundary from '@/components/ErrorBoundary.vue'

const apiResponse = ref(null)

const errorCode = computed(() => {
  return apiResponse.value?.status || ''
})

const errorTitle = computed(() => {
  const titles = {
    400: '请求参数错误',
    401: '认证失败',
    403: '权限不足',
    404: '接口不存在',
    500: '服务器错误'
  }
  return titles[errorCode.value] || 'API 调用失败'
})

const errorMessage = computed(() => {
  return apiResponse.value?.message || '请稍后重试'
})
</script>
```

### **示例 4：查看演示页面**

访问演示页面查看所有错误类型的效果：

```javascript
// 添加路由（临时测试用）
{
  path: '/demo/error-boundary',
  name: 'ErrorBoundaryDemo',
  component: () => import('@/views/demo/ErrorBoundaryDemo.vue'),
  meta: { title: '错误边界演示', hidden: true }
}
```

然后访问：`http://localhost:3000/demo/error-boundary`

---

## 📚 最佳实践

### **1. 何时使用 ErrorBoundary**

✅ **推荐使用场景**：
- 第三方组件或库（可能抛出未知错误）
- 复杂的图表渲染（ECharts、D3.js 等）
- 动态导入的组件
- 用户自定义内容渲染
- 关键业务组件（需要优雅降级）

❌ **不推荐场景**：
- 简单的静态内容
- 已经完善处理错误的组件
- 表单验证错误（应使用 Element Plus 的校验机制）

### **2. 错误提示文案设计**

```vue
<!-- ❌ 不好的提示 -->
<ErrorBoundary message="出错了" />

<!-- ✅ 好的提示 -->
<ErrorBoundary 
  title="数据同步失败"
  message="无法从服务器获取最新数据，请检查网络连接后重试。如果问题持续存在，请联系技术支持。"
/>
```

**原则**：
- 明确说明发生了什么
- 提供解决方案
- 语气友好专业
- 避免技术术语

### **3. 重试逻辑实现**

```vue
<ErrorBoundary @retry="handleRetry">
  <DataTable />
</ErrorBoundary>

<script setup>
import { ref } from 'vue'

const retryCount = ref(0)
const maxRetries = 3

async function handleRetry() {
  if (retryCount.value >= maxRetries) {
    ElMessage.error('重试次数过多，请稍后再试')
    return
  }
  
  retryCount.value++
  
  try {
    await fetchData()
    retryCount.value = 0 // 成功后重置
  } catch (error) {
    // ErrorBoundary 会再次捕获错误
    throw error
  }
}
</script>
```

### **4. 生产环境隐藏技术详情**

```vue
<ErrorBoundary :show-details="import.meta.env.DEV">
  <SensitiveComponent />
</ErrorBoundary>
```

### **5. 与 Sentry 集成**

ErrorBoundary 已内置 Sentry 支持，只需配置 DSN：

```bash
# .env.production
VITE_SENTRY_DSN=https://your-dsn@sentry.io/project-id
```

错误会自动上报到 Sentry，包含：
- 组件名称
- 错误堆栈
- 用户信息
- 浏览器信息

### **6. 性能优化**

```vue
<!-- ❌ 避免：过多的嵌套 -->
<ErrorBoundary>
  <ErrorBoundary>
    <ErrorBoundary>
      <Component />
    </ErrorBoundary>
  </ErrorBoundary>
</ErrorBoundary>

<!-- ✅ 推荐：扁平化结构 -->
<ErrorBoundary>
  <ComponentA />
</ErrorBoundary>
<ErrorBoundary>
  <ComponentB />
</ErrorBoundary>
```

---

## 🎨 自定义样式

### **修改错误图标颜色**

编辑 `ErrorBoundary.vue` 的样式部分：

```scss
.error-icon {
  &.error-type-network {
    color: #ff6b6b; // 自定义红色
  }
  
  &.error-type-permission {
    color: #ffd93d; // 自定义黄色
  }
}
```

### **修改动画效果**

```scss
@keyframes shake {
  0%, 100% { transform: translateX(0) rotate(0); }
  25% { transform: translateX(-10px) rotate(-5deg); }
  75% { transform: translateX(10px) rotate(5deg); }
}
```

### **自定义错误页面背景**

编辑 `ErrorPage.vue`：

```scss
.error-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
```

---

## 🔍 故障排查

### **问题 1：ErrorBoundary 没有捕获到错误**

**原因**：异步错误需要在 `throw` 之前被触发

**解决**：
```vue
<script setup>
import { onErrorCaptured } from 'vue'

// 确保错误在组件生命周期内抛出
onMounted(() => {
  throw new Error('Test error') // ✅ 可以捕获
})

// 或者在模板中
const triggerError = () => {
  throw new Error('Test error') // ✅ 可以捕获
}
</script>
```

### **问题 2：错误提示重复弹出**

**原因**：节流时间设置过短

**解决**：调整 `globalErrorHandler.js` 中的常量

```javascript
const ERROR_THROTTLE = 5000 // 改为 5 秒
const MAX_ERRORS = 5 // 增加最大提示次数
```

### **问题 3：Sentry 未上报错误**

**检查清单**：
1. DSN 是否正确配置
2. 是否在 production 环境
3. 浏览器控制台是否有 Sentry 初始化日志
4. Sentry 项目设置是否允许该域名

---

## 📊 监控与分析

### **本地错误存储**

所有错误会自动保存到 `localStorage`：

```javascript
// 查看最近的错误
const errors = JSON.parse(localStorage.getItem('app_errors') || '[]')
console.log('最近错误:', errors)

// 清除错误历史
localStorage.removeItem('app_errors')
```

### **错误统计**

可以添加错误统计功能：

```javascript
function getErrorStats() {
  const errors = JSON.parse(localStorage.getItem('app_errors') || '[]')
  
  const stats = {
    total: errors.length,
    byType: {},
    byHour: {}
  }
  
  errors.forEach(err => {
    // 按类型统计
    stats.byType[err.type] = (stats.byType[err.type] || 0) + 1
    
    // 按小时统计
    const hour = new Date(err.timestamp).getHours()
    stats.byHour[hour] = (stats.byHour[hour] || 0) + 1
  })
  
  return stats
}
```

---

## 🚀 下一步优化建议

1. **错误热力图**：可视化展示哪些组件最容易出错
2. **自动恢复**：检测到网络恢复后自动重试
3. **离线模式**：断网时显示离线提示和缓存数据
4. **错误订阅**：管理员接收严重错误的邮件通知
5. **A/B 测试**：测试不同错误提示文案的效果

---

## 📝 总结

通过这三层错误处理机制，您的应用现在具备：

✅ **优雅的用户体验**：友好的错误提示，清晰的解决方案  
✅ **强大的开发者工具**：详细的错误堆栈，一键复制  
✅ **完善的监控体系**：Sentry 集成，本地错误日志  
✅ **灵活的定制能力**：自定义文案、样式、行为  

立即开始使用，提升应用的健壮性和用户体验！🎉

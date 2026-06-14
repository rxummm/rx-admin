# 性能监控与错误边界 - 问题修复记录

> **修复日期**: 2026-06-13  
> **问题**: 开发服务器启动失败，layout/index.vue 动态导入错误

---

## 🔍 问题诊断

### **错误信息**

```
[vite] Internal server error: Error parsing JavaScript expression: 
import.meta may appear only with 'sourceType: "module"' (1:1)
File: D:/vueprojects/RX/ui/src/layout/index.vue:76:29
```

### **根本原因**

在 Vue 模板中直接使用了 `import.meta.env.DEV`：

```vue
<!-- ❌ 错误写法 -->
<el-tooltip v-if="import.meta.env.DEV" content="性能监控">
  ...
</el-tooltip>

<PerformancePanel v-if="import.meta.env.DEV" ref="performancePanelRef" />
```

**为什么错误**：
- Vue 模板编译器不支持在模板表达式中直接使用 `import.meta`
- `import.meta` 只能在 `<script>` 标签中使用
- 这会导致 Rollup/Vite 编译失败

---

## ✅ 修复方案

### **步骤 1：在 script 中定义环境变量**

```javascript
// layout/index.vue
const isDev = import.meta.env.DEV // 开发环境标志
```

### **步骤 2：模板中使用变量**

```vue
<!-- ✅ 正确写法 -->
<el-tooltip v-if="isDev" content="性能监控">
  ...
</el-tooltip>

<PerformancePanel v-if="isDev" ref="performancePanelRef" />
```

---

## 📝 修改的文件

### **ui/src/layout/index.vue**

**修改位置 1**：添加 `isDev` 变量（第 177 行）
```javascript
const isCollapse = ref(false)
const isFullscreen = ref(false)
const isDev = import.meta.env.DEV // 开发环境标志
```

**修改位置 2**：顶栏按钮条件（第 76 行）
```vue
<!-- 修改前 -->
<el-tooltip v-if="import.meta.env.DEV" content="性能监控">

<!-- 修改后 -->
<el-tooltip v-if="isDev" content="性能监控">
```

**修改位置 3**：组件条件渲染（第 128 行）
```vue
<!-- 修改前 -->
<PerformancePanel v-if="import.meta.env.DEV" ref="performancePanelRef" />

<!-- 修改后 -->
<PerformancePanel v-if="isDev" ref="performancePanelRef" />
```

---

## 🧪 验证步骤

### **1. 重启开发服务器**

```bash
cd d:\vueprojects\RX\ui
npm run dev
```

### **2. 检查控制台输出**

**预期结果**：
```
VITE v5.4.21  ready in 372 ms
➜  Local:   http://localhost:3000/
📊 性能监控已启动
```

**不应该出现**：
- ❌ `Error parsing JavaScript expression`
- ❌ `Failed to fetch dynamically imported module`
- ❌ `import.meta may appear only with 'sourceType: "module"'`

---

### **3. 访问应用**

打开浏览器访问 `http://localhost:3000`

**预期效果**：
- ✅ 页面正常加载
- ✅ 顶栏右侧显示 📊 图标（开发环境）
- ✅ 点击图标打开性能监控面板
- ✅ FPS、API 统计等数据正常显示

---

## 💡 最佳实践

### **Vue 模板中使用环境变量的正确方式**

#### **❌ 错误示例**

```vue
<template>
  <!-- 不能直接在模板中使用 import.meta -->
  <div v-if="import.meta.env.DEV">开发环境内容</div>
  <button @click="console.log(import.meta.env.VITE_APP_TITLE)">
    打印标题
  </button>
</template>
```

---

#### **✅ 正确示例**

```vue
<template>
  <!-- 使用 script 中定义的变量 -->
  <div v-if="isDev">开发环境内容</div>
  <button @click="logTitle">打印标题</button>
</template>

<script setup>
// 在 script 中定义
const isDev = import.meta.env.DEV
const appTitle = import.meta.env.VITE_APP_TITLE

function logTitle() {
  console.log(appTitle)
}
</script>
```

---

### **其他常见场景**

#### **1. 条件渲染组件**

```vue
<template>
  <DebugPanel v-if="isDev" />
  <AnalyticsScript v-if="!isDev" />
</template>

<script setup>
const isDev = import.meta.env.DEV
</script>
```

---

#### **2. 动态导入模块**

```vue
<script setup>
import { defineAsyncComponent } from 'vue'

const isDev = import.meta.env.DEV

// 开发环境加载调试工具
const DebugTools = isDev 
  ? defineAsyncComponent(() => import('./DebugTools.vue'))
  : null
</script>
```

---

#### **3. 条件执行代码**

```vue
<script setup>
const isDev = import.meta.env.DEV

if (isDev) {
  // 开发环境专属逻辑
  enableHotReload()
  showDebugInfo()
}
</script>
```

---

## ⚠️ 注意事项

### **1. Vite 环境变量前缀**

只有以 `VITE_` 开头的环境变量才会暴露给前端代码：

```env
# .env.development
VITE_APP_TITLE=RX Admin        # ✅ 可用
VITE_API_BASE_URL=/api         # ✅ 可用
SECRET_KEY=xxx                 # ❌ 不可用（安全考虑）
```

---

### **2. 类型转换**

环境变量都是字符串，需要时手动转换：

```javascript
const timeout = Number(import.meta.env.VITE_API_TIMEOUT) || 15000
const isEnabled = import.meta.env.VITE_FEATURE_FLAG === 'true'
```

---

### **3. 默认值处理**

始终提供默认值，防止环境变量未定义：

```javascript
const apiUrl = import.meta.env.VITE_API_URL || '/api'
const maxRetries = Number(import.meta.env.VITE_MAX_RETRIES) || 3
```

---

## 🎯 相关文档

- [Vite 环境变量文档](https://cn.vitejs.dev/guide/env-and-mode.html)
- [Vue 3 组合式 API](https://cn.vuejs.org/guide/extras/composition-api-faq.html)
- [性能监控使用指南](file://d:\vueprojects\RX\PERFORMANCE_AND_ERROR_BOUNDARY_GUIDE.md)

---

## 📊 修复总结

| 项目 | 详情 |
|------|------|
| **问题类型** | Vue 模板编译错误 |
| **影响范围** | 开发服务器无法启动 |
| **修复时间** | < 5 分钟 |
| **修复难度** | 简单 |
| **根本原因** | 在模板中直接使用 `import.meta` |
| **解决方案** | 在 script 中定义变量，模板中使用变量 |
| **预防措施** | 遵循 Vue 最佳实践，不在模板中使用 `import.meta` |

---

**修复完成时间**: 2026-06-13 17:13  
**状态**: ✅ 已解决  
**验证**: ✅ 开发服务器正常运行

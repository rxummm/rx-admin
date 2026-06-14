# ECharts 赛博朋克主题使用指南

## 📦 快速开始

### 1. 在组件中引入主题

```vue
<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { cyberTheme } from '@/utils/echartsTheme'

const chartRef = ref(null)

onMounted(() => {
  const chart = echarts.init(chartRef.value, cyberTheme)
  
  const option = {
    title: { text: '数据趋势' },
    xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      data: [150, 230, 224]
    }]
  }
  
  chart.setOption(option)
})
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 400px"></div>
</template>
```

### 2. 全局注册主题（可选）

```javascript
// main.js
import { registerCyberTheme } from '@/utils/echartsTheme'
import * as echarts from 'echarts'

registerCyberTheme(echarts)

// 之后可以直接使用
const chart = echarts.init(dom, 'cyberpunk')
```

---

## 🎨 主题特性

### 配色方案
- **主色调**：电光蓝 `#58a6ff`
- **成功色**：荧光绿 `#3fb950`
- **警告色**：琥珀黄 `#d29922`
- **危险色**：珊瑚红 `#f85149`

### 视觉效果
- ✨ 折线图发光阴影
- 📊 柱状图圆角 + 边框
- 🥧 饼图分隔线
- 🌈 平滑渐变过渡

### 暗色适配
- 背景透明，继承容器
- 文字高对比度
- 网格线微妙可见

---

## 📊 图表类型示例

### 折线图（带区域填充）

```javascript
{
  series: [{
    type: 'line',
    data: [150, 230, 224, 218, 135],
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(88, 166, 255, 0.3)' },
        { offset: 1, color: 'rgba(88, 166, 255, 0.05)' }
      ])
    }
  }]
}
```

### 柱状图（渐变色）

```javascript
{
  series: [{
    type: 'bar',
    data: [120, 200, 150],
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#58a6ff' },
        { offset: 1, color: '#1f6feb' }
      ])
    }
  }]
}
```

### 饼图（玫瑰图）

```javascript
{
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    roseType: 'area',
    data: [
      { value: 40, name: '类别A' },
      { value: 30, name: '类别B' },
      { value: 20, name: '类别C' }
    ]
  }]
}
```

---

## 🔧 自定义配置

### 修改单个图表颜色

```javascript
const option = {
  color: ['#f85149', '#3fb950', '#d29922'], // 覆盖默认调色板
  series: [...]
}
```

### 调整网格线样式

```javascript
const option = {
  xAxis: {
    splitLine: {
      lineStyle: {
        color: '#30363d',
        type: 'solid' // 改为实线
      }
    }
  }
}
```

### 添加工具提示格式化

```javascript
const option = {
  tooltip: {
    formatter: (params) => {
      return `${params.name}<br/>数值: ${params.value}`
    }
  }
}
```

---

## ⚡ 性能优化

### 1. 大数据量优化

```javascript
{
  series: [{
    type: 'line',
    data: largeDataArray,
    sampling: 'lttb', // 降采样算法
    showSymbol: false // 隐藏数据点
  }]
}
```

### 2. 动画控制

```javascript
{
  animationDuration: 1000, // 动画时长（毫秒）
  animationEasing: 'cubicOut' // 缓动函数
}
```

### 3. 响应式调整

```javascript
window.addEventListener('resize', () => {
  chart.resize()
})
```

---

## 🎯 最佳实践

✅ **推荐**：
- 使用主题预设颜色，保持视觉一致性
- 折线图启用 `smooth: true` 提升美观度
- 柱状图设置 `barMaxWidth` 避免过宽
- 添加 `tooltip` 提升交互体验

❌ **避免**：
- 不要在浅色背景上使用暗色主题
- 避免过多图表类型混用（最多 3 种）
- 不要禁用所有动画（失去活力感）
- 避免在小屏幕上显示复杂图表

---

## 🐛 常见问题

### Q: 图表不显示？
A: 确保容器有明确的宽高，且在 `onMounted` 后初始化。

### Q: 颜色不符合预期？
A: 检查是否传入了 `cyberTheme` 作为第二个参数。

### Q: 暗色模式下文字看不清？
A: 主题已自动适配，如需调整可修改 `textStyle.color`。

### Q: 如何导出图表为图片？
```javascript
const url = chart.getDataURL({
  type: 'png',
  pixelRatio: 2,
  backgroundColor: '#0d1117'
})
```

---

## 📚 参考资源

- [ECharts 官方文档](https://echarts.apache.org/)
- [GitHub Dark 配色](https://github.com/primer/primitives)
- [VS Code 主题](https://code.visualstudio.com/docs/getstarted/themes)

<template>
  <div ref="chartContainer" :style="{ width: width, height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  width: { type: String, default: '100%' },
  height: { type: String, default: '300px' },
  autoResize: { type: Boolean, default: true }
})

const chartContainer = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartContainer.value) return
  chartInstance = echarts.init(chartContainer.value)
  chartInstance.setOption(props.option)
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption(props.option)
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(() => props.option, updateChart, { deep: true })

onMounted(() => {
  initChart()
  if (props.autoResize) {
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  if (props.autoResize) {
    window.removeEventListener('resize', handleResize)
  }
  chartInstance?.dispose()
})

defineExpose({ chartInstance, handleResize })
</script>

<template>
  <BaseChart :option="chartOption" :width="width" :height="height" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  xField: { type: String, default: 'name' },
  yField: { type: String, default: 'value' },
  width: { type: String, default: '100%' },
  height: { type: String, default: '300px' },
  smooth: { type: Boolean, default: true },
  area: { type: Boolean, default: false }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: props.data.map(d => d[props.xField])
  },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    data: props.data.map(d => d[props.yField]),
    smooth: props.smooth,
    areaStyle: props.area ? { opacity: 0.3 } : undefined
  }]
}))
</script>

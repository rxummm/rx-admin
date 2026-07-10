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
  color: { type: String, default: '#409eff' }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: props.data.map(d => d[props.xField])
  },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar',
    data: props.data.map(d => d[props.yField]),
    itemStyle: { color: props.color }
  }]
}))
</script>

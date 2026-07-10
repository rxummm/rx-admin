<template>
  <BaseChart :option="chartOption" :width="width" :height="height" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  nameField: { type: String, default: 'name' },
  valueField: { type: String, default: 'value' },
  width: { type: String, default: '100%' },
  height: { type: String, default: '300px' },
  radius: { type: Array, default: () => ['40%', '70%'] }
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  series: [{
    type: 'pie',
    radius: props.radius,
    data: props.data.map(d => ({
      name: d[props.nameField],
      value: d[props.valueField]
    })),
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }]
}))
</script>

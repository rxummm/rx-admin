<template>
  <div
    class="flow-node"
    :class="[
      `flow-node--${type}`,
      { 'flow-node--selected': selected }
    ]"
    :style="nodeStyle"
  >
    <div class="flow-node__content">
      {{ data.label || placeholder }}
    </div>
    <!-- 连接点 handles -->
    <Handle type="target" :position="Position.Top" />
    <Handle type="source" :position="Position.Bottom" />
    <Handle type="target" :position="Position.Left" />
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'

const props = defineProps({
  id: { type: String, required: true },
  data: { type: Object, default: () => ({}) },
  selected: { type: Boolean, default: false },
  type: { type: String, default: 'rect' },
})

const nodeStyle = computed(() => ({
  borderColor: props.data.color || '#409EFF',
  fontSize: (props.data.fontSize || 14) + 'px',
}))

const placeholder = computed(() => {
  const map = {
    rect: '矩形',
    diamond: '菱形',
    roundRect: '圆角矩形',
    ellipse: '椭圆',
  }
  return map[props.type] || ''
})
</script>

<style scoped>
.flow-node {
  position: relative;
  min-width: 100px;
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #409EFF;
  border-radius: 4px;
  background: #fff;
  padding: 10px 16px;
  font-size: 14px;
  color: #333;
  text-align: center;
  word-break: break-word;
  transition: box-shadow 0.2s;
  box-sizing: border-box;
  cursor: grab;
}

.flow-node--selected {
  box-shadow: 0 0 0 2px #409EFF;
}

/* 菱形 */
.flow-node--diamond {
  border-radius: 0;
  transform: rotate(45deg);
  min-width: 70px;
  min-height: 70px;
  width: 100px;
  height: 100px;
}
.flow-node--diamond .flow-node__content {
  transform: rotate(-45deg);
}
.flow-node--diamond .vue-flow__handle {
  /* 菱形连接点需偏移以对齐边缘 */
}

/* 圆角矩形 */
.flow-node--roundRect {
  border-radius: 20px;
}

/* 椭圆 */
.flow-node--ellipse {
  border-radius: 50%;
  min-width: 100px;
  min-height: 60px;
  padding: 15px 20px;
}

.flow-node__content {
  line-height: 1.4;
  user-select: none;
}
</style>

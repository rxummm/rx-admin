<template>
  <div class="skeleton-container" :style="{ width, height }">
    <!-- 图表骨架屏 -->
    <div v-if="type === 'chart'" class="skeleton-chart">
      <div class="skeleton-header">
        <div class="skeleton-title"></div>
        <div class="skeleton-actions">
          <div class="skeleton-btn"></div>
          <div class="skeleton-btn"></div>
        </div>
      </div>
      <div class="skeleton-chart-body">
        <div class="skeleton-bar" v-for="i in 5" :key="i" :style="{ height: `${30 + Math.random() * 50}%` }"></div>
      </div>
    </div>

    <!-- 表格骨架屏 -->
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-header">
        <div class="skeleton-cell" v-for="i in columns" :key="i"></div>
      </div>
      <div class="skeleton-table-row" v-for="i in rows" :key="i">
        <div class="skeleton-cell" v-for="j in columns" :key="j"></div>
      </div>
    </div>

    <!-- 卡片骨架屏 -->
    <div v-else-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-avatar"></div>
      <div class="skeleton-text skeleton-text-lg"></div>
      <div class="skeleton-text"></div>
      <div class="skeleton-text skeleton-text-sm"></div>
    </div>

    <!-- 列表骨架屏 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div class="skeleton-list-item" v-for="i in count" :key="i">
        <div class="skeleton-avatar-small"></div>
        <div class="skeleton-list-content">
          <div class="skeleton-text"></div>
          <div class="skeleton-text skeleton-text-sm"></div>
        </div>
      </div>
    </div>

    <!-- 默认段落骨架屏 -->
    <div v-else class="skeleton-paragraph">
      <div class="skeleton-text" v-for="i in lines" :key="i" :class="{ 'skeleton-text-sm': i === lines }"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'paragraph', // chart | table | card | list | paragraph
    validator: (value) => ['chart', 'table', 'card', 'list', 'paragraph'].includes(value)
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: 'auto'
  },
  columns: {
    type: Number,
    default: 5
  },
  rows: {
    type: Number,
    default: 5
  },
  lines: {
    type: Number,
    default: 3
  },
  count: {
    type: Number,
    default: 5
  }
})
</script>

<style scoped lang="scss">
.skeleton-container {
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

// 通用骨架样式
.skeleton-text {
  height: 16px;
  background: linear-gradient(
    90deg,
    var(--bg-hover) 25%,
    var(--bg-active) 50%,
    var(--bg-hover) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--radius-xs);
  margin-bottom: 8px;
  animation: skeleton-shimmer 2s ease-in-out infinite;
}

.skeleton-text-lg {
  height: 24px;
  margin-bottom: 12px;
}

.skeleton-text-sm {
  height: 12px;
  width: 60%;
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

// 图表骨架屏
.skeleton-chart {
  padding: 16px;
  
  .skeleton-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .skeleton-title {
      width: 120px;
      height: 20px;
      background: linear-gradient(
        90deg,
        var(--bg-hover) 25%,
        var(--bg-active) 50%,
        var(--bg-hover) 75%
      );
      background-size: 200% 100%;
      border-radius: var(--radius-xs);
      margin-bottom: 8px;
      animation: skeleton-shimmer 2s ease-in-out infinite;
    }
    
    .skeleton-actions {
      display: flex;
      gap: 8px;
      
      .skeleton-btn {
        width: 32px;
        height: 32px;
        border-radius: var(--radius-sm);
        background: linear-gradient(
          90deg,
          var(--bg-hover) 25%,
          var(--bg-active) 50%,
          var(--bg-hover) 75%
        );
        background-size: 200% 100%;
        animation: skeleton-shimmer 2s ease-in-out infinite;
      }
    }
  }
  
  .skeleton-chart-body {
    height: 200px;
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    gap: 8px;
    padding: 16px 0;
    
    .skeleton-bar {
      flex: 1;
      max-width: 60px;
      background: var(--color-primary-soft);
      border-radius: var(--radius-xs) var(--radius-xs) 0 0;
      min-height: 20px;
    }
  }
}

// 表格骨架屏
.skeleton-table {
  .skeleton-table-header,
  .skeleton-table-row {
    display: flex;
    gap: 12px;
    padding: 12px;
    border-bottom: 1px solid var(--border-light);
  }
  
  .skeleton-table-header {
    background: var(--bg-hover);
    font-weight: 600;
  }
  
  .skeleton-cell {
    flex: 1;
    height: 16px;
    background: linear-gradient(
      90deg,
      var(--bg-hover) 25%,
      var(--bg-active) 50%,
      var(--bg-hover) 75%
    );
    background-size: 200% 100%;
    border-radius: var(--radius-xs);
    margin-bottom: 8px;
    animation: skeleton-shimmer 2s ease-in-out infinite;
  }
}

// 卡片骨架屏
.skeleton-card {
  padding: 20px;
  text-align: center;
  
  .skeleton-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    margin: 0 auto 16px;
    background: linear-gradient(
      90deg,
      var(--bg-hover) 25%,
      var(--bg-active) 50%,
      var(--bg-hover) 75%
    );
    background-size: 200% 100%;
    animation: skeleton-shimmer 2s ease-in-out infinite;
  }
}

// 列表骨架屏
.skeleton-list {
  .skeleton-list-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-bottom: 1px solid var(--border-light);
    
    &:last-child {
      border-bottom: none;
    }
    
    .skeleton-avatar-small {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      flex-shrink: 0;
      background: linear-gradient(
        90deg,
        var(--bg-hover) 25%,
        var(--bg-active) 50%,
        var(--bg-hover) 75%
      );
      background-size: 200% 100%;
      animation: skeleton-shimmer 2s ease-in-out infinite;
    }
    
    .skeleton-list-content {
      flex: 1;
    }
  }
}

// 段落骨架屏
.skeleton-paragraph {
  padding: 16px;
}
</style>

<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <!-- 错误图标（根据错误类型动态显示） -->
      <div class="error-icon" :class="errorTypeClass">
        <el-icon :size="72">
          <component :is="errorIcon" />
        </el-icon>
      </div>
      
      <!-- 错误标题 -->
      <h2 class="error-title">{{ title }}</h2>
      
      <!-- 错误代码 -->
      <div v-if="errorCode" class="error-code">
        <el-tag type="danger" effect="plain" size="large">
          错误码: {{ errorCode }}
        </el-tag>
      </div>
      
      <!-- 错误描述 -->
      <p class="error-message">{{ message }}</p>
      
      <!-- 发生时间 -->
      <div v-if="errorTimestamp" class="error-timestamp">
        <el-icon><Clock /></el-icon>
        <span>发生时间: {{ errorTimestamp }}</span>
      </div>
      
      <!-- 错误详情（开发环境） -->
      <div v-if="showDetails && isDev" class="error-details">
        <details>
          <summary>
            <el-icon><WarningFilled /></el-icon>
            查看技术详情
          </summary>
          <div class="error-stack">
            <div class="stack-header">
              <span class="stack-label">错误堆栈</span>
              <el-button size="small" text @click="handleCopyStack">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </div>
            <pre>{{ error?.stack || error?.message }}</pre>
          </div>
        </details>
      </div>
      
      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button type="primary" size="large" @click="handleRetry">
          <el-icon><Refresh /></el-icon>
          重试
        </el-button>
        <el-button size="large" @click="handleReset">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" text @click="handleCopyError">
          <el-icon><DocumentCopy /></el-icon>
          复制错误
        </el-button>
      </div>
      
      <!-- 帮助提示 -->
      <div class="error-help">
        <el-icon><InfoFilled /></el-icon>
        <span>如果问题持续存在，请联系技术支持或查看文档</span>
      </div>
    </div>
  </div>
  
  <!-- 正常内容 -->
  <slot v-else></slot>
</template>

<script setup>
defineOptions({ name: 'ErrorBoundary' })
import { ref, computed, onErrorCaptured, getCurrentInstance } from 'vue'
import {
  CircleCloseFilled,
  Connection,
  Lock,
  Server,
  Refresh,
  HomeFilled,
  DocumentCopy,
  Clock,
  WarningFilled,
  InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { captureException } from '@/utils/sentry'

const props = defineProps({
  // 自定义错误标题
  title: {
    type: String,
    default: '组件加载失败'
  },
  // 自定义错误消息
  message: {
    type: String,
    default: '抱歉，该组件遇到了一些问题。请尝试刷新页面或联系管理员。'
  },
  // 错误代码
  errorCode: {
    type: [String, Number],
    default: ''
  },
  // 是否显示错误详情
  showDetails: {
    type: Boolean,
    default: true
  },
  // 错误回调
  onError: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['error', 'retry'])

const hasError = ref(false)
const error = ref(null)
const errorTimestamp = ref('')
const router = useRouter()
const instance = getCurrentInstance()
const isDev = import.meta.env.DEV

/**
 * 捕获子组件错误
 */
onErrorCaptured((err, componentInstance, info) => {
  console.error('ErrorBoundary 捕获到错误:', err, info)
  
  hasError.value = true
  error.value = err
  errorTimestamp.value = new Date().toLocaleString('zh-CN')
  
  // 调用外部错误回调
  if (props.onError) {
    props.onError(err, info)
  }
  
  // 上报错误（如果有 Sentry）
  reportError(err, info)
  
  // 触发事件
  emit('error', err, info)
  
  // 阻止错误继续向上传播
  return false
})

/**
 * 错误类型检测
 */
const errorType = computed(() => {
  if (!error.value) return 'unknown'
  
  const errMsg = error.value.message || ''
  const errCode = error.value.code || error.value.response?.status
  
  // 网络错误
  if (errMsg.includes('Network Error') || errMsg.includes('Failed to fetch')) {
    return 'network'
  }
  
  // 权限错误
  if (errCode === 401 || errCode === 403 || errMsg.includes('permission')) {
    return 'permission'
  }
  
  // 服务器错误
  if (errCode >= 500 || errMsg.includes('Internal Server Error')) {
    return 'server'
  }
  
  // 资源未找到
  if (errCode === 404 || errMsg.includes('Not Found')) {
    return 'notfound'
  }
  
  return 'unknown'
})

/**
 * 错误图标
 */
const errorIcon = computed(() => {
  const iconMap = {
    network: Connection,
    permission: Lock,
    server: Server,
    notfound: CircleCloseFilled,
    unknown: CircleCloseFilled
  }
  return iconMap[errorType.value] || CircleCloseFilled
})

/**
 * 错误类型样式类
 */
const errorTypeClass = computed(() => {
  return `error-type-${errorType.value}`
})

/**
 * 重试加载
 */
function handleRetry() {
  hasError.value = false
  error.value = null
  
  // 触发自定义事件，父组件可以重新初始化
  emit('retry')
  
  ElMessage.success('已重试，正在重新加载...')
}

/**
 * 返回首页
 */
function handleReset() {
  router.push('/')
}

/**
 * 复制错误信息
 */
async function handleCopyError() {
  const errorInfo = {
    type: errorType.value,
    message: error.value?.message,
    stack: error.value?.stack,
    timestamp: errorTimestamp.value,
    url: window.location.href,
    userAgent: navigator.userAgent
  }
  
  try {
    await navigator.clipboard.writeText(JSON.stringify(errorInfo, null, 2))
    ElMessage.success('错误信息已复制到剪贴板')
  } catch (e) {
    ElMessage.error('复制失败，请手动复制')
  }
}

/**
 * 复制错误堆栈
 */
async function handleCopyStack() {
  const stackText = error.value?.stack || error.value?.message || ''
  
  try {
    await navigator.clipboard.writeText(stackText)
    ElMessage.success('堆栈信息已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

/**
 * 上报错误到监控服务
 */
function reportError(err, info) {
  // 使用 Sentry 上报错误
  try {
    captureException(err, {
      tags: {
        component: instance?.type?.name || 'Unknown',
        error_type: errorType.value,
        info: JSON.stringify(info)
      },
      extra: {
        timestamp: errorTimestamp.value,
        url: window.location.href
      }
    })
  } catch (e) {
    // Sentry 未初始化或上报失败，静默处理
    console.warn('Sentry 上报失败:', e)
  }
  
  // 本地日志
  console.error('[ErrorBoundary]', {
    component: instance?.type?.name,
    error: err,
    info
  })
}

/**
 * 重置错误状态（供外部调用）
 */
function reset() {
  hasError.value = false
  error.value = null
}

defineExpose({ reset })
</script>

<style scoped lang="scss">
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px;
  background: var(--bg-container);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.error-content {
  text-align: center;
  max-width: 550px;
  
  .error-icon {
    margin-bottom: 24px;
    animation: shake 0.6s ease-in-out;
    
    // 错误类型颜色
    &.error-type-network {
      color: #f85149;
    }
    
    &.error-type-permission {
      color: #d29922;
    }
    
    &.error-type-server {
      color: #f85149;
    }
    
    &.error-type-notfound {
      color: #8b949e;
    }
    
    &.error-type-unknown {
      color: #f85149;
    }
  }
  
  .error-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 12px;
  }
  
  .error-code {
    margin-bottom: 16px;
  }
  
  .error-message {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 24px;
    line-height: 1.6;
  }
  
  .error-timestamp {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 13px;
    color: var(--text-tertiary);
    margin-bottom: 20px;
    padding: 8px 16px;
    background: var(--bg-page);
    border-radius: var(--radius-sm);
    display: inline-flex;
  }
  
  .error-details {
    margin-bottom: 24px;
    
    details {
      background: var(--bg-page);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      padding: 12px;
      text-align: left;
      
      summary {
        cursor: pointer;
        font-size: 13px;
        color: var(--color-primary);
        margin-bottom: 8px;
        
        &:hover {
          text-decoration: underline;
        }
      }
      
      .stack-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
        padding-bottom: 8px;
        border-bottom: 1px solid var(--border-color);
        
        .stack-label {
          font-size: 12px;
          font-weight: 600;
          color: var(--text-secondary);
        }
      }
      
      pre {
        font-family: var(--font-family-mono);
        font-size: 12px;
        color: var(--text-secondary);
        overflow-x: auto;
        white-space: pre-wrap;
        word-break: break-word;
        max-height: 300px;
        overflow-y: auto;
        padding: 8px;
        background: rgba(0, 0, 0, 0.2);
        border-radius: var(--radius-xs);
      }
    }
  }
  
  .error-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }
  
  .error-help {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 13px;
    color: var(--text-tertiary);
    padding: 12px;
    background: rgba(56, 139, 253, 0.1);
    border: 1px solid rgba(56, 139, 253, 0.2);
    border-radius: var(--radius-sm);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}
</style>

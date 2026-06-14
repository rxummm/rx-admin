<template>
  <div class="error-demo-page">
    <el-card shadow="hover">
      <template #header>
        <h2>ErrorBoundary 错误边界演示</h2>
      </template>
      
      <!-- 演示区域 -->
      <div class="demo-sections">
        
        <!-- 1. 正常组件 -->
        <el-card shadow="never" class="demo-card">
          <template #header>
            <div class="card-header">
              <span>✅ 正常组件（无错误）</span>
            </div>
          </template>
          <ErrorBoundary>
            <div class="normal-content">
              <p>这是一个正常的组件，没有任何错误。</p>
              <el-tag type="success">运行正常</el-tag>
            </div>
          </ErrorBoundary>
        </el-card>
        
        <!-- 2. 模拟运行时错误 -->
        <el-card shadow="never" class="demo-card">
          <template #header>
            <div class="card-header">
              <span>💥 模拟运行时错误</span>
              <el-button size="small" type="danger" @click="triggerRuntimeError">
                触发错误
              </el-button>
            </div>
          </template>
          <ErrorBoundary 
            title="组件崩溃了"
            message="这个组件遇到了致命错误，无法继续运行。"
            :error-code="runtimeErrorCode"
            @retry="handleRetry"
            @error="handleError"
          >
            <div v-if="!hasRuntimeError" class="normal-content">
              <p>点击按钮触发运行时错误...</p>
            </div>
            <CrashComponent v-else />
          </ErrorBoundary>
        </el-card>
        
        <!-- 3. 模拟网络错误 -->
        <el-card shadow="never" class="demo-card">
          <template #header>
            <div class="card-header">
              <span>🌐 模拟网络错误</span>
              <el-button size="small" type="warning" @click="triggerNetworkError">
                模拟请求失败
              </el-button>
            </div>
          </template>
          <ErrorBoundary 
            title="网络连接失败"
            message="无法连接到服务器，请检查您的网络连接或稍后重试。"
            error-code="NETWORK_ERROR"
          >
            <div class="normal-content">
              <p>点击按钮模拟网络请求失败...</p>
            </div>
          </ErrorBoundary>
        </el-card>
        
        <!-- 4. 模拟权限错误 -->
        <el-card shadow="never" class="demo-card">
          <template #header>
            <div class="card-header">
              <span>🔒 模拟权限错误</span>
              <el-button size="small" type="info" @click="triggerPermissionError">
                模拟权限不足
              </el-button>
            </div>
          </template>
          <ErrorBoundary 
            title="权限不足"
            message="您没有权限访问此资源，请联系管理员获取权限。"
            error-code="403"
          >
            <div class="normal-content">
              <p>点击按钮模拟权限不足错误...</p>
            </div>
          </ErrorBoundary>
        </el-card>
        
        <!-- 5. 模拟服务器错误 -->
        <el-card shadow="never" class="demo-card">
          <template #header>
            <div class="card-header">
              <span>🖥️ 模拟服务器错误</span>
              <el-button size="small" type="danger" @click="triggerServerError">
                模拟服务器异常
              </el-button>
            </div>
          </template>
          <ErrorBoundary 
            title="服务器内部错误"
            message="服务器遇到意外情况，无法完成请求。请稍后重试。"
            error-code="500"
          >
            <div class="normal-content">
              <p>点击按钮模拟服务器错误...</p>
            </div>
          </ErrorBoundary>
        </el-card>
        
        <!-- 6. 嵌套错误边界 -->
        <el-card shadow="never" class="demo-card">
          <template #header>
            <div class="card-header">
              <span>📦 嵌套错误边界</span>
              <el-button size="small" @click="triggerNestedError">
                触发嵌套错误
              </el-button>
            </div>
          </template>
          <ErrorBoundary title="外层组件">
            <div class="nested-container">
              <p>外层组件正常</p>
              <ErrorBoundary title="内层组件">
                <div v-if="!hasNestedError">
                  <p>内层组件正常</p>
                </div>
                <CrashComponent v-else />
              </ErrorBoundary>
            </div>
          </ErrorBoundary>
        </el-card>
        
      </div>
      
      <!-- 使用说明 -->
      <el-divider />
      <div class="usage-guide">
        <h3>📖 使用说明</h3>
        <el-table :data="usageData" stripe border>
          <el-table-column prop="scenario" label="使用场景" width="200" />
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="props" label="关键属性" width="250" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import ErrorBoundary from '@/components/ErrorBoundary.vue'

// 运行时错误状态
const hasRuntimeError = ref(false)
const runtimeErrorCode = ref('')

// 嵌套错误状态
const hasNestedError = ref(false)

/**
 * 触发运行时错误
 */
function triggerRuntimeError() {
  hasRuntimeError.value = true
  runtimeErrorCode.value = 'RUNTIME_ERROR'
}

/**
 * 触发网络错误
 */
function triggerNetworkError() {
  // 这里只是演示，实际项目中可以模拟 API 调用失败
  ElMessage.warning('已模拟网络错误')
}

/**
 * 触发权限错误
 */
function triggerPermissionError() {
  ElMessage.error('已模拟权限错误')
}

/**
 * 触发服务器错误
 */
function triggerServerError() {
  ElMessage.error('已模拟服务器错误')
}

/**
 * 触发嵌套错误
 */
function triggerNestedError() {
  hasNestedError.value = true
}

/**
 * 重试处理
 */
function handleRetry() {
  hasRuntimeError.value = false
  runtimeErrorCode.value = ''
  ElMessage.success('组件已重置')
}

/**
 * 错误回调
 */
function handleError(err, info) {
  console.error('捕获到错误:', err, info)
}

// 使用说明数据
const usageData = [
  {
    scenario: '基础用法',
    description: '包裹可能出错的组件，自动捕获并显示友好错误界面',
    props: '无（使用默认配置）'
  },
  {
    scenario: '自定义标题和消息',
    description: '根据业务场景定制错误提示文案',
    props: 'title="自定义标题" message="自定义消息"'
  },
  {
    scenario: '显示错误代码',
    description: '显示 HTTP 状态码或业务错误码',
    props: 'error-code="404" 或 error-code="BUSINESS_ERROR"'
  },
  {
    scenario: '错误回调',
    description: '监听错误事件，执行自定义逻辑（如上报监控）',
    props: '@error="handleError"'
  },
  {
    scenario: '重试机制',
    description: '用户点击重试时触发自定义逻辑',
    props: '@retry="handleRetry"'
  },
  {
    scenario: '隐藏技术详情',
    description: '生产环境可隐藏错误堆栈信息',
    props: ':show-details="false"'
  }
]
</script>

<script>
/**
 * 故意制造错误的测试组件
 */
const CrashComponent = {
  template: '<div>即将崩溃...</div>',
  mounted() {
    throw new Error('这是一个故意的运行时错误！用于测试 ErrorBoundary 组件。')
  }
}
</script>

<style scoped lang="scss">
.error-demo-page {
  padding: 20px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    color: var(--text-primary);
  }
  
  .demo-sections {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .demo-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    
    .normal-content {
      padding: 20px;
      text-align: center;
      
      p {
        margin-bottom: 12px;
        color: var(--text-secondary);
      }
    }
    
    .nested-container {
      padding: 20px;
      
      > p {
        margin-bottom: 16px;
        color: var(--text-secondary);
      }
    }
  }
  
  .usage-guide {
    margin-top: 20px;
    
    h3 {
      font-size: 16px;
      color: var(--text-primary);
      margin-bottom: 16px;
    }
  }
}
</style>

<template>
  <div class="error-page">
    <div class="error-container">
      <!-- 动态背景粒子效果 -->
      <div class="particles">
        <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)" />
      </div>
      
      <!-- 错误内容 -->
      <div class="error-content">
        <!-- 404 错误 -->
        <div v-if="errorCode === 404" class="error-visual">
          <div class="error-code-large">404</div>
          <div class="error-icon-wrapper">
            <el-icon :size="80" class="error-icon">
              <DocumentDelete />
            </el-icon>
          </div>
        </div>
        
        <!-- 500 错误 -->
        <div v-else-if="errorCode === 500" class="error-visual">
          <div class="error-code-large">500</div>
          <div class="error-icon-wrapper">
            <el-icon :size="80" class="error-icon">
              <CircleCloseFilled />
            </el-icon>
          </div>
        </div>
        
        <!-- 403 错误 -->
        <div v-else-if="errorCode === 403" class="error-visual">
          <div class="error-code-large">403</div>
          <div class="error-icon-wrapper">
            <el-icon :size="80" class="error-icon">
              <Lock />
            </el-icon>
          </div>
        </div>
        
        <!-- 其他错误 -->
        <div v-else class="error-visual">
          <div class="error-code-large">{{ errorCode }}</div>
          <div class="error-icon-wrapper">
            <el-icon :size="80" class="error-icon">
              <WarningFilled />
            </el-icon>
          </div>
        </div>
        
        <!-- 错误信息 -->
        <h1 class="error-title">{{ errorTitle }}</h1>
        <p class="error-description">{{ errorDescription }}</p>
        
        <!-- 建议操作 -->
        <div class="error-suggestions">
          <h3>您可以尝试：</h3>
          <ul>
            <li v-for="(suggestion, index) in suggestions" :key="index">
              <el-icon><Check /></el-icon>
              {{ suggestion }}
            </li>
          </ul>
        </div>
        
        <!-- 操作按钮 -->
        <div class="error-actions">
          <el-button type="primary" size="large" @click="handleGoBack">
            <el-icon><Back /></el-icon>
            返回上一页
          </el-button>
          <el-button size="large" @click="handleGoHome">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" text @click="handleReload">
            <el-icon><Refresh /></el-icon>
            刷新页面
          </el-button>
        </div>
        
        <!-- 技术支持 -->
        <div class="support-info">
          <el-divider />
          <p>
            <el-icon><Service /></el-icon>
            如果问题持续存在，请联系技术支持
          </p>
          <p class="contact-email">support@example.com</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ErrorPage' })
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DocumentDelete,
  CircleCloseFilled,
  Lock,
  WarningFilled,
  Check,
  Back,
  HomeFilled,
  Refresh,
  Service
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 从路由参数获取错误码
const errorCode = computed(() => {
  return Number(route.params.code) || 404
})

// 错误标题
const errorTitle = computed(() => {
  const titles = {
    404: '页面未找到',
    500: '服务器内部错误',
    403: '禁止访问',
    401: '未授权',
    503: '服务不可用'
  }
  return titles[errorCode.value] || '出错了'
})

// 错误描述
const errorDescription = computed(() => {
  const descriptions = {
    404: '抱歉，您访问的页面不存在或已被移除。',
    500: '服务器遇到意外情况，无法完成您的请求。',
    403: '您没有权限访问此页面，请联系管理员。',
    401: '请先登录后再访问此页面。',
    503: '服务暂时不可用，请稍后重试。'
  }
  return descriptions[errorCode.value] || '发生了一个未知错误。'
})

// 建议操作
const suggestions = computed(() => {
  const commonSuggestions = [
    '检查网址是否正确',
    '清除浏览器缓存后重试',
    '联系网站管理员'
  ]
  
  const specificSuggestions = {
    404: [
      '确认链接地址是否正确',
      '从首页重新导航到目标页面',
      ...commonSuggestions
    ],
    500: [
      '稍等片刻后刷新页面',
      '尝试清除浏览器缓存',
      ...commonSuggestions
    ],
    403: [
      '确认您是否有访问权限',
      '联系管理员申请权限',
      ...commonSuggestions
    ]
  }
  
  return specificSuggestions[errorCode.value] || commonSuggestions
})

/**
 * 生成粒子样式
 */
function getParticleStyle(index) {
  const size = Math.random() * 6 + 2
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = Math.random() * 10 + 10
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

/**
 * 返回上一页
 */
function handleGoBack() {
  router.back()
}

/**
 * 返回首页
 */
function handleGoHome() {
  router.push('/')
}

/**
 * 刷新页面
 */
function handleReload() {
  window.location.reload()
}
</script>

<style scoped lang="scss">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-page) 0%, var(--bg-container) 100%);
  position: relative;
  overflow: hidden;
  
  .particles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    
    .particle {
      position: absolute;
      bottom: -10px;
      background: rgba(56, 139, 253, 0.3);
      border-radius: 50%;
      animation: float-up linear infinite;
      
      @keyframes float-up {
        0% {
          transform: translateY(0) rotate(0deg);
          opacity: 0;
        }
        10% {
          opacity: 1;
        }
        90% {
          opacity: 1;
        }
        100% {
          transform: translateY(-100vh) rotate(720deg);
          opacity: 0;
        }
      }
    }
  }
  
  .error-container {
    position: relative;
    z-index: var(--z-decor, 1);
    max-width: 800px;
    padding: 60px 40px;
    text-align: center;
  }
  
  .error-content {
    animation: fade-in 0.6s ease-out;
    
    @keyframes fade-in {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  }
  
  .error-visual {
    margin-bottom: 40px;
    position: relative;
    
    .error-code-large {
      font-size: 120px;
      font-weight: 700;
      color: var(--color-primary);
      opacity: 0.1;
      line-height: 1;
      margin-bottom: -40px;
      user-select: none;
    }
    
    .error-icon-wrapper {
      position: relative;
      z-index: var(--z-decor, 1);
      
      .error-icon {
        color: var(--color-primary);
        animation: pulse 2s ease-in-out infinite;
        
        @keyframes pulse {
          0%, 100% {
            transform: scale(1);
          }
          50% {
            transform: scale(1.1);
          }
        }
      }
    }
  }
  
  .error-title {
    font-size: 32px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 16px;
  }
  
  .error-description {
    font-size: 16px;
    color: var(--text-secondary);
    line-height: 1.8;
    margin-bottom: 32px;
  }
  
  .error-suggestions {
    background: var(--bg-container);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    padding: 24px;
    margin-bottom: 32px;
    text-align: left;
    
    h3 {
      font-size: 16px;
      color: var(--text-primary);
      margin-bottom: 16px;
    }
    
    ul {
      list-style: none;
      padding: 0;
      margin: 0;
      
      li {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 0;
        color: var(--text-secondary);
        font-size: 14px;
        
        .el-icon {
          color: #3fb950;
        }
      }
    }
  }
  
  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    flex-wrap: wrap;
    margin-bottom: 40px;
  }
  
  .support-info {
    color: var(--text-tertiary);
    font-size: 14px;
    
    p {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin: 8px 0;
    }
    
    .contact-email {
      color: var(--color-primary);
      font-weight: 500;
    }
  }
}
</style>

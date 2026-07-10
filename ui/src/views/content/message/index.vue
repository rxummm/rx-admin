<template>
  <div class="page-message">
    <!-- 顶部工具栏 -->
    <div class="msg-header">
      <div class="header-left">
        <span class="title">{{ $t('content.message.title') }}</span>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <span class="unread-label">{{ $t('content.message.unread') }}</span>
        </el-badge>
        <el-radio-group v-model="msgType" size="small" @change="fetchData">
          <el-radio-button value="">{{ $t('content.message.all') }}</el-radio-button>
          <el-radio-button value="system">{{ $t('content.message.system') }}</el-radio-button>
          <el-radio-button value="notice">{{ $t('content.message.notification') }}</el-radio-button>
          <el-radio-button value="info">{{ $t('content.message.info') }}</el-radio-button>
        </el-radio-group>
      </div>
      <el-button @click="handleMarkAllRead" :disabled="unreadCount === 0" type="warning" plain size="small">
        {{ $t('content.message.allRead') }}
      </el-button>
    </div>

    <!-- 消息列表（可滚动区域） -->
    <div class="msg-body" v-loading="loading">
      <el-timeline v-if="tableData.length > 0" class="msg-timeline">
        <el-timeline-item
          v-for="msg in tableData"
          :key="msg.id"
          :timestamp="msg.createTime"
          placement="top"
          :color="msg.isRead ? '#c0c4cc' : '#409EFF'"
          :hollow="!!msg.isRead"
          size="large"
        >
          <el-card shadow="never" class="msg-card" :class="{ 'is-unread': !msg.isRead }" @click="handleClick(msg)">
            <div class="card-header">
              <div class="card-title-row">
                <span class="card-title">{{ formatMsgText(msg.title, msg) }}</span>
                <el-tag size="small" :type="tagType(msg.messageType)" effect="plain">
                  {{ typeLabel(msg.messageType) }}
                </el-tag>
              </div>
              <div class="card-actions">
                <el-button v-if="!msg.isRead" link type="primary" size="small" @click.stop="handleRead(msg)">
                  {{ $t('content.message.markRead') }}
                </el-button>
                <el-button link type="danger" size="small" @click.stop="handleDelete(msg)">
                  {{ $t('common.delete') }}
                </el-button>
              </div>
            </div>
            <div class="card-body" v-html="sanitizeHtml(formatMsgText(msg.content?.substring(0, 200), msg))"></div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else :description="$t('content.message.noMessage')" />
    </div>

    <!-- 分页器（固定底部） -->
    <div class="msg-footer" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        size="small"
        @change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ContentMessage' })
import { ref } from 'vue'
import { sanitizeHtml } from '@/utils/sanitize'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMessagePageApi, getUnreadCountApi, markAsReadApi, markAllReadApi, deleteMessageApi } from '@/api/message'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const msgType = ref('')
const unreadCount = ref(0)

// 管理员视角：将消息中的"您"替换为实际接收人用户名
function formatMsgText(text, msg) {
  if (!text) return ''
  if (userStore.hasRole('admin') && msg.receiverUsername) {
    return text
      .replace(/您申请/g, msg.receiverUsername + '申请')
      .replace(/您的/g, msg.receiverUsername + '的')
      .replace(/^您(?=[，。、：])/g, msg.receiverUsername)
  }
  return text
}

const tagType = (type) => (type === 'system' ? 'warning' : type === 'notice' ? 'danger' : 'info')
const typeLabel = (type) =>
  type === 'system'
    ? t('content.message.typeSys')
    : type === 'notice'
      ? t('content.message.typeNotice')
      : t('content.message.typeInfo')

const fetchData = async () => {
  loading.value = true
  try {
    const [msgRes, countRes] = await Promise.all([
      getMessagePageApi({ page: page.value, size: size.value, messageType: msgType.value }),
      getUnreadCountApi()
    ])
    tableData.value = msgRes.data?.records || []
    total.value = msgRes.data?.total || 0
    unreadCount.value = countRes.data?.count || 0
  } finally {
    loading.value = false
  }
}

const handleRead = async (msg) => {
  await markAsReadApi(msg.id)
  msg.isRead = 1
  unreadCount.value--
  ElMessage.success(t('content.message.markedRead'))
}
const handleMarkAllRead = async () => {
  await markAllReadApi()
  tableData.value.forEach((m) => (m.isRead = 1))
  unreadCount.value = 0
  ElMessage.success(t('content.message.allMarkedRead'))
}
const handleClick = (msg) => {
  if (!msg.isRead) handleRead(msg)
  if (msg.linkPath) router.push(msg.linkPath)
}
const handleDelete = async (msg) => {
  try {
    await ElMessageBox.confirm(t('content.message.deleteConfirm'), t('common.warning'), { type: 'warning' })
    await deleteMessageApi(msg.id)
    tableData.value = tableData.value.filter((m) => m.id !== msg.id)
    total.value--
    if (msg.isRead === 0) unreadCount.value--
    ElMessage.success(t('common.deleteSuccess'))
  } catch {
    /* cancelled */
  }
}

fetchData()
</script>

<style scoped>
/* ========== 页面容器：flex 纵向布局，填满可用高度 ========== */
.page-message {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-page);
  border-radius: 8px;
  overflow: hidden;
}

/* ========== 顶部工具栏 ========== */
.msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--color-white);
  border-bottom: 1px solid var(--border-lighter);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.unread-label {
  color: var(--text-secondary);
  font-size: 12px;
  cursor: default;
}

/* ========== 消息列表（可滚动区域）========== */
.msg-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  position: relative;
}

/* 自定义滚动条 */
.msg-body::-webkit-scrollbar {
  width: 6px;
}
.msg-body::-webkit-scrollbar-track {
  background: transparent;
}
.msg-body::-webkit-scrollbar-thumb {
  background: var(--text-placeholder);
  border-radius: 3px;
}
.msg-body::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

.msg-timeline {
  padding: 4px 32px 16px !important;
}

/* 时间线时间戳 */
.msg-timeline :deep(.el-timeline-item__timestamp) {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ========== 消息卡片 ========== */
.msg-card {
  transition: all 0.2s ease;
  border-radius: 8px;
  border-left-width: 0 !important;
  margin-bottom: 4px;
}

.msg-card.is-unread {
  border-left: 3px solid var(--color-primary) !important;
  background: linear-gradient(135deg, #fef9ee 0%, var(--color-white) 100%);
}

.msg-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08) !important;
  transform: translateY(-1px);
}

.msg-card.is-unread:hover {
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15) !important;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0; /* 允许文字截断 */
}

.card-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  word-break: break-word;
}

.is-unread .card-title {
  color: var(--text-primary);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.msg-card:hover .card-actions {
  opacity: 1;
}

.card-body {
  color: var(--text-regular);
  font-size: 13px;
  margin-top: 8px;
  line-height: 1.7;
  word-break: break-all;
}

.card-body :deep(p) {
  margin: 0;
}

/* ========== 分页器（固定底部）========== */
.msg-footer {
  padding: 12px 20px;
  background: var(--color-white);
  border-top: 1px solid var(--border-lighter);
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.msg-footer :deep(.el-pagination) {
  justify-content: center;
}
</style>

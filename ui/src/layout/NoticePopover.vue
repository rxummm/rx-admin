<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="360"
    trigger="click"
  >
    <template #reference>
      <el-badge :value="totalBadgeCount" :hidden="totalBadgeCount === 0" :max="99" :offset="[-6, 6]">
        <el-tooltip :content="$t('layout.notice')" placement="bottom" :hide-after="0">
          <div class="header-action-btn">
            <el-icon style="font-size: 18px;"><Bell /></el-icon>
          </div>
        </el-tooltip>
      </el-badge>
    </template>
    <div class="notice-popover">
      <div class="notice-popover-header">
        <span>{{ $t('layout.notice') }}</span>
        <el-button v-if="(activeTab === 'message' && messageUnreadCount > 0) || (activeTab !== 'message' && unreadNoticeCount > 0)" text type="primary" size="small" @click="markAllRead">{{ $t('layout.allRead') }}</el-button>
      </div>
      <div class="notice-tabs">
        <div v-for="tab in tabs" :key="tab.key" :class="['notice-tab', { active: activeTab === tab.key }]" @click="activeTab = tab.key">
          {{ tab.label }}
          <span v-if="tab.count > 0" class="notice-tab-badge">{{ tab.count > 99 ? '99+' : tab.count }}</span>
        </div>
      </div>
      <el-scrollbar max-height="280px">
        <div v-if="loading" class="notice-loading">{{ $t('common.loading') }}</div>
        <div v-else-if="filteredList.length === 0" class="notice-empty">{{ $t('layout.noNotice') }}</div>
        <div v-for="notice in filteredList" :key="notice.category + '-' + notice.id" :class="['notice-item', { 'notice-unread': !notice._read }]" @click="handleNoticeClick(notice)">
          <div class="notice-item-header">
            <span class="notice-dot" v-if="!notice._read"></span>
            <span class="notice-title">{{ formatMsgText(notice.title, notice) }}</span>
            <el-tag size="small" :type="getTagType(notice)" effect="plain">{{ getTagLabel(notice) }}</el-tag>
          </div>
          <div class="notice-item-time">{{ notice.createTime }}</div>
        </div>
      </el-scrollbar>
      <div class="notice-popover-footer" @click="goToNoticePage">{{ activeTab === 'message' ? $t('layout.viewAllMessage') : $t('layout.viewAllNotice') }}</div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getNoticePageApi, getNoticeSummaryApi, getReadNoticeIdsApi, markNoticeReadApi, markAllNoticeReadApi } from '@/api/notice'
import { getMessagePageApi, getUnreadCountApi, markAsReadApi, markAllReadApi } from '@/api/message'
import { useUserStore } from '@/stores/user'
import { useNoticeBroadcast } from '@/composables/useNoticeBroadcast'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()

const visible = ref(false)
const list = ref([])
const loading = ref(false)
const readNoticeIds = ref([])
const noticeTimer = ref(null)
const activeTab = ref('unread')
const summary = ref({ noticeCount: 0, announcementCount: 0, todoCount: 0 })

// 消息中心数据
const messageList = ref([])
const messageUnreadCount = ref(0)

const tabs = computed(() => [
  { key: 'unread', label: t('layout.noticeTab.unread'), count: unreadNoticeCount.value + todoCount.value + messageUnreadCount.value },
  { key: 'all', label: t('layout.noticeTab.all'), count: 0 },
  { key: 'notice', label: t('layout.noticeType.notice'), count: unreadNoticeByCategory('notice') },
  { key: 'announcement', label: t('layout.noticeType.announcement'), count: unreadNoticeByCategory('announcement') },
  { key: 'todo', label: t('layout.noticeType.todo'), count: summary.value.todoCount || 0 },
  { key: 'message', label: t('layout.noticeType.message'), count: messageUnreadCount.value },
])

const unreadNoticeCount = computed(() => list.value.filter(n => n.category !== 'todo' && !n._read).length)
const todoCount = computed(() => list.value.filter(n => n.category === 'todo').length)
const totalBadgeCount = computed(() => unreadNoticeCount.value + todoCount.value + messageUnreadCount.value)

// 按分类统计未读数（通知/公告分别显示未读数，已读不显示数字）
function unreadNoticeByCategory(category) {
  return list.value.filter(n => n.category === category && !n._read).length
}

// 将消息中心数据归一化为和通知相同的结构
const normalizedMessages = computed(() => messageList.value.map(m => ({
  id: m.id,
  title: m.title,
  createTime: m.createTime,
  category: 'message',
  _read: m.isRead === 1,
  _messageType: m.messageType,
  _linkPath: m.linkPath,
  _raw: m
})))

const combinedList = computed(() => [...list.value, ...normalizedMessages.value])

const filteredList = computed(() => {
  if (activeTab.value === 'all') return combinedList.value
  if (activeTab.value === 'unread') return combinedList.value.filter(n =>
    (n.category !== 'todo' && n.category !== 'message' && !n._read) ||
    n.category === 'todo' ||
    (n.category === 'message' && !n._read)
  )
  if (activeTab.value === 'message') return combinedList.value.filter(n => n.category === 'message')
  return combinedList.value.filter(n => n.category === activeTab.value)
})

function getTagType(notice) {
  if (notice.category === 'todo') return 'danger'
  if (notice.category === 'announcement') return 'warning'
  if (notice.category === 'message') {
    if (notice._messageType === 'system') return 'danger'
    if (notice._messageType === 'notice') return 'warning'
    return 'info'
  }
  return 'primary'
}

function getTagLabel(notice) {
  if (notice.category === 'todo') return t('layout.noticeType.todo')
  if (notice.category === 'announcement') return t('layout.noticeType.announcement')
  if (notice.category === 'message') {
    if (notice._messageType === 'system') return t('content.message.typeSys')
    if (notice._messageType === 'notice') return t('content.message.typeNotice')
    return t('content.message.typeInfo')
  }
  return t('layout.noticeType.notice')
}

// 管理员视角：将消息中的"您"替换为实际接收人用户名
function formatMsgText(text, item) {
  if (!text) return ''
  if (userStore.hasRole('admin') && item._raw?.receiverUsername) {
    return text.replace(/您申请/g, item._raw.receiverUsername + '申请')
      .replace(/您的/g, item._raw.receiverUsername + '的')
      .replace(/^您(?=[，。、：])/g, item._raw.receiverUsername)
  }
  return text
}

async function fetchNotices() {
  loading.value = true
  try {
    const [listRes, summaryRes, readRes] = await Promise.all([
      getNoticePageApi({ page: 1, size: 50 }, { _skipNProgress: true }),
      getNoticeSummaryApi({ _skipNProgress: true }).catch(() => ({ data: {} })),
      getReadNoticeIdsApi({ _skipNProgress: true }).catch(() => ({ data: [] }))
    ])
    // 从后端获取已读ID（替代localStorage）
    readNoticeIds.value = (readRes?.data) ? readRes.data : []
    const records = (listRes.data && listRes.data.records) ? listRes.data.records : []
    records.forEach(n => {
      if (n.category === 'todo') { n._read = true }
      else { n._read = readNoticeIds.value.includes(n.id) }
    })
    list.value = records
    if (summaryRes.data) summary.value = summaryRes.data
  } catch {} finally { loading.value = false }
}

async function fetchMessages() {
  try {
    const [countRes, listRes] = await Promise.all([
      getUnreadCountApi({ _skipNProgress: true }),
      getMessagePageApi({ page: 1, size: 20 }, { _skipNProgress: true })
    ])
    // 兜底：优先使用 API 返回的未读数，若异常则从列表推导
    const apiCount = countRes?.data?.count
    messageUnreadCount.value = typeof apiCount === 'number' ? apiCount : 0
    const records = (listRes?.data?.records) ? listRes.data.records : []
    messageList.value = records
    // 如果 API 返回值无效，用实际未读记录数兜底
    if (!apiCount && records.length > 0) {
      messageUnreadCount.value = records.filter(m => m.isRead !== 1).length
    }
  } catch (e) {
    // fetchMessages 失败时静默处理，保持当前状态
  }
}

function handleNoticeClick(notice) {
  // 消息中心的消息：标记已读，保持弹窗打开以便继续查看其他消息
  if (notice.category === 'message') {
    if (!notice._read) {
      markAsReadApi(notice.id).catch(() => {})
      if (notice._raw) notice._raw.isRead = 1
      notice._read = true
      messageUnreadCount.value = Math.max(0, messageUnreadCount.value - 1)
    }
    return
  }
  // 待办项：管理员→用户管理页面审批，普通用户→权限申请页面查看状态
  if (notice.category === 'todo') {
    visible.value = false
    const isAdmin = userStore.hasRole('admin')
    router.push(isAdmin ? '/system/user' : '/permission/request')
    return
  }
  // 通知/公告：标记已读（持久化到数据库），保持弹窗打开
  if (!notice._read) {
    if (!readNoticeIds.value.includes(notice.id)) {
      readNoticeIds.value.push(notice.id)
    }
    markNoticeReadApi(notice.id).catch(() => {})
    notice._read = true
  }
}

function markAllRead() {
  // 当在消息 tab 时，全部已读针对消息中心的消息
  if (activeTab.value === 'message') {
    markAllReadApi().catch(() => {})
    messageList.value.forEach(m => { m.isRead = 1 })
    messageUnreadCount.value = 0
    ElMessage.success(t('layout.allReadDone'))
    return
  }
  // 通知公告的全部已读（调用后端API持久化）
  const unreadIds = list.value.filter(n => n.category !== 'todo' && !n._read).map(n => n.id)
  readNoticeIds.value = [...new Set([...readNoticeIds.value, ...unreadIds])]
  markAllNoticeReadApi().catch(() => {})
  list.value.forEach(n => { if (n.category !== 'todo') n._read = true })
  ElMessage.success(t('layout.allReadDone'))
}

function goToNoticePage() {
  visible.value = false
  if (activeTab.value === 'message') {
    router.push('/content/message')
  } else {
    router.push('/content/notice')
  }
}

// 监听广播事件，审批完成后立即刷新（不等待轮询）
const { refreshCounter } = useNoticeBroadcast()
watch(refreshCounter, () => {
  fetchNotices()
  fetchMessages()
})

onMounted(() => {
  fetchNotices()
  fetchMessages()
  // 自动刷新通知和消息，间隔通过 .env 文件 VITE_NOTICE_POLL_INTERVAL 配置
  const interval = Number(import.meta.env.VITE_NOTICE_POLL_INTERVAL) || 15000
  noticeTimer.value = setInterval(() => { fetchNotices(); fetchMessages() }, interval)
})

onUnmounted(() => {
  if (noticeTimer.value) {
    clearInterval(noticeTimer.value)
    noticeTimer.value = null
  }
})
</script>

<style scoped>
.notice-popover .notice-popover-header {
  display: flex; align-items: center; justify-content: space-between; padding-bottom: 10px; margin-bottom: 4px;
}
.notice-popover .notice-popover-header span { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.notice-popover .notice-tabs {
  display: flex; gap: 0; border-bottom: 1px solid var(--border-light); margin-bottom: 8px;
  overflow-x: auto; scrollbar-width: none; -ms-overflow-style: none;
}
.notice-popover .notice-tabs::-webkit-scrollbar { display: none; }
.notice-popover .notice-tab {
  position: relative; padding: 6px 10px; font-size: 13px; color: var(--text-secondary);
  cursor: pointer; transition: color 0.2s; white-space: nowrap; flex-shrink: 0;
}
.notice-popover .notice-tab:hover { color: var(--color-primary); }
.notice-popover .notice-tab.active { color: var(--color-primary); font-weight: 500; }
.notice-popover .notice-tab.active::after {
  content: ''; position: absolute; bottom: -1px; left: 8px; right: 8px;
  height: 2px; background: var(--color-primary); border-radius: 1px;
}
.notice-popover .notice-tab-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 14px; height: 14px; margin-left: 2px; padding: 0 3px;
  font-size: 10px; line-height: 1; color: #fff;
  background: var(--color-danger); border-radius: 7px;
}
.notice-popover .notice-loading,
.notice-popover .notice-empty { text-align: center; padding: 32px 0; font-size: 13px; color: var(--text-secondary); }
.notice-popover .notice-item {
  padding: 10px 12px; border-radius: 6px; cursor: pointer; transition: background 0.15s;
  border-bottom: 1px solid var(--border-lighter);
}
.notice-popover .notice-item:last-child { border-bottom: none; }
.notice-popover .notice-item:hover { background: var(--bg-hover); }
.notice-popover .notice-item.notice-unread { background: var(--notice-unread-bg); }
.notice-popover .notice-item.notice-unread:hover { background: var(--notice-unread-hover); }
.notice-popover .notice-item-header { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.notice-popover .notice-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--color-primary); flex-shrink: 0; }
.notice-popover .notice-title { flex: 1; font-size: 13px; font-weight: 500; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notice-popover .notice-item-time { font-size: 11px; color: var(--text-secondary); padding-left: 15px; }
.notice-popover .notice-popover-footer {
  text-align: center; padding: 10px 0 4px; border-top: 1px solid var(--border-light); margin-top: 8px;
  font-size: 13px; color: var(--color-primary); cursor: pointer;
}
.notice-popover .notice-popover-footer:hover { color: var(--color-primary-light); }
.header-action-btn {
  display: inline-flex; align-items: center; justify-content: center;
  width: 34px; height: 34px; border-radius: 6px;
  cursor: pointer; color: var(--text-regular); font-size: 18px;
  transition: all 0.2s;
}
.header-action-btn:hover {
  background: var(--bg-active); color: var(--color-primary);
}
</style>

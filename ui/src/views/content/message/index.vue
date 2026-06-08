<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600;">{{ $t('content.message.title') }}</span>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" style="margin-left: 8px;">
        <span style="color:#909399;font-size:12px;">{{ $t('content.message.unread') }}</span>
      </el-badge>
      <el-radio-group v-model="msgType" size="small" style="margin-left:16px;" @change="fetchData">
        <el-radio-button value="">{{ $t('content.message.all') }}</el-radio-button>
        <el-radio-button value="system">{{ $t('content.message.system') }}</el-radio-button>
        <el-radio-button value="notice">{{ $t('content.message.notification') }}</el-radio-button>
        <el-radio-button value="info">{{ $t('content.message.info') }}</el-radio-button>
      </el-radio-group>
      <div style="flex:1" />
      <el-button @click="handleMarkAllRead" :disabled="unreadCount === 0" type="warning" plain>{{ $t('content.message.allRead') }}</el-button>
    </div>

    <div class="table-container">
      <el-timeline v-if="tableData.length > 0" style="padding: 20px 40px;">
        <el-timeline-item v-for="msg in tableData" :key="msg.id"
          :timestamp="msg.createTime" placement="top"
          :color="msg.isRead ? '#909399' : '#409EFF'">
          <el-card shadow="hover" :body-style="{ padding: '12px 16px' }"
            :style="{ cursor: msg.linkPath ? 'pointer' : 'default', borderLeft: msg.isRead ? 'none' : '3px solid #409EFF' }"
            @click="handleClick(msg)">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <div>
                <span :style="{ fontWeight: msg.isRead ? 'normal' : 'bold', fontSize: '15px' }">{{ formatMsgText(msg.title, msg) }}</span>
                <el-tag size="small" style="margin-left:8px;" :type="tagType(msg.messageType)">{{ typeLabel(msg.messageType) }}</el-tag>
              </div>
              <div style="display:flex;align-items:center;gap:4px;">
                <el-button v-if="!msg.isRead" link type="primary" size="small" @click.stop="handleRead(msg)">{{ $t('content.message.markRead') }}</el-button>
                <el-button link type="danger" size="small" @click.stop="handleDelete(msg)">{{ $t('common.delete') }}</el-button>
              </div>
            </div>
            <div style="color:#606266;font-size:13px;margin-top:6px;line-height:1.5;" v-html="formatMsgText(msg.content?.substring(0, 200), msg)"></div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else :description="$t('content.message.noMessage')" />

      <el-pagination v-if="total > 0" class="page-pagination" v-model:current-page="page" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMessagePageApi, getUnreadCountApi, markAsReadApi, markAllReadApi, deleteMessageApi } from '@/api/message'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const tableData = ref([]); const loading = ref(false)
const page = ref(1); const size = ref(10); const total = ref(0)
const msgType = ref(''); const unreadCount = ref(0)

// 管理员视角：将消息中的"您"替换为实际接收人用户名
function formatMsgText(text, msg) {
  if (!text) return ''
  if (userStore.hasRole('admin') && msg.receiverUsername) {
    return text.replace(/您申请/g, msg.receiverUsername + '申请')
      .replace(/您的/g, msg.receiverUsername + '的')
      .replace(/^您(?=[，。、：])/g, msg.receiverUsername)
  }
  return text
}

const tagType = (type) => type === 'system' ? 'warning' : type === 'notice' ? 'danger' : 'info'
const typeLabel = (type) => type === 'system' ? t('content.message.typeSys') : type === 'notice' ? t('content.message.typeNotice') : t('content.message.typeInfo')

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
  } finally { loading.value = false }
}

const handleRead = async (msg) => { await markAsReadApi(msg.id); msg.isRead = 1; unreadCount.value--; ElMessage.success(t('content.message.markedRead')) }
const handleMarkAllRead = async () => { await markAllReadApi(); tableData.value.forEach(m => m.isRead = 1); unreadCount.value = 0; ElMessage.success(t('content.message.allMarkedRead')) }
const handleClick = (msg) => { if (!msg.isRead) handleRead(msg); if (msg.linkPath) router.push(msg.linkPath) }
const handleDelete = async (msg) => {
  try {
    await ElMessageBox.confirm(t('content.message.deleteConfirm'), t('common.warning'), { type: 'warning' })
    await deleteMessageApi(msg.id)
    tableData.value = tableData.value.filter(m => m.id !== msg.id)
    total.value--
    if (msg.isRead === 0) unreadCount.value--
    ElMessage.success(t('common.deleteSuccess'))
  } catch { /* cancelled */ }
}

fetchData()
</script>

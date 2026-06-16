<template>
  <el-dialog v-model="visible" title="系统公告" width="480px" :close-on-click-modal="false"
    @closed="showNext">
    <!-- ⚠️ XSS 防护：必须经 sanitizeHtml 过滤后渲染，禁止直接 v-html 用户内容 -->
    <div v-html="safeContent" style="line-height: 1.8; max-height: 300px; overflow-y: auto;"></div>
    <template #footer>
      <el-button type="primary" @click="dismiss">我知道了</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPopupAnnouncementsApi, markAnnouncementReadApi } from '@/api/announcement'
import { sanitizeHtml } from '@/utils/sanitize'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

const visible = ref(false)
const announcements = ref([])
const currentIndex = ref(0)

const currentAnnouncement = ref(null)

// 统一封装：避免散落 localStorage.xxx 调用，统一命名空间
const dismissedStore = useStorage(STORAGE_KEYS.ANNOUNCEMENT_DISMISSED, [])

// 实时过滤 XSS，避免恶意 <script>/onerror 注入
const safeContent = computed(() => sanitizeHtml(currentAnnouncement.value?.content))

const dismiss = async () => {
  const a = currentAnnouncement.value
  if (a) {
    try { await markAnnouncementReadApi(a.id) } catch (e) { /* */ }
    const dismissed = dismissedStore.get() || []
    dismissed.push(a.id)
    dismissedStore.set(dismissed)
  }
  visible.value = false
}

const showNext = () => {
  currentIndex.value++
  if (currentIndex.value < announcements.value.length) {
    currentAnnouncement.value = announcements.value[currentIndex.value]
    visible.value = true
  }
}

onMounted(async () => {
  try {
    const res = await getPopupAnnouncementsApi()
    announcements.value = res.data || []
    // 过滤已关闭的
    const dismissed = dismissedStore.get() || []
    announcements.value = announcements.value.filter(a => !dismissed.includes(a.id))

    if (announcements.value.length > 0) {
      currentAnnouncement.value = announcements.value[0]
      visible.value = true
    }
  } catch (e) { /* */ }
})
</script>

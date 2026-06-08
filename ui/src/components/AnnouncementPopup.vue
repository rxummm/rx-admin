<template>
  <el-dialog v-model="visible" title="系统公告" width="480px" :close-on-click-modal="false"
    @closed="showNext">
    <div v-html="currentAnnouncement?.content" style="line-height: 1.8; max-height: 300px; overflow-y: auto;"></div>
    <template #footer>
      <el-button type="primary" @click="dismiss">我知道了</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPopupAnnouncementsApi, markAnnouncementReadApi } from '@/api/announcement'

const visible = ref(false)
const announcements = ref([])
const currentIndex = ref(0)

const currentAnnouncement = ref(null)

const dismiss = async () => {
  const a = currentAnnouncement.value
  if (a) {
    try { await markAnnouncementReadApi(a.id) } catch (e) { /* */ }
    const dismissed = JSON.parse(localStorage.getItem('announcement_dismissed') || '[]')
    dismissed.push(a.id)
    localStorage.setItem('announcement_dismissed', JSON.stringify(dismissed))
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
    const dismissed = JSON.parse(localStorage.getItem('announcement_dismissed') || '[]')
    announcements.value = announcements.value.filter(a => !dismissed.includes(a.id))

    if (announcements.value.length > 0) {
      currentAnnouncement.value = announcements.value[0]
      visible.value = true
    }
  } catch (e) { /* */ }
})
</script>

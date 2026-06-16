<template>
  <div class="video-player-page">
    <div class="player-body">
      <div class="video-list-panel" :class="{ collapsed: listCollapsed }">
        <div class="list-toolbar">
          <div class="toolbar-left">
            <el-tooltip :content="listCollapsed ? $t('tool.videoPlayer.expandList') : $t('tool.videoPlayer.collapseList')" placement="right">
              <el-button :icon="listCollapsed ? DArrowRight : DArrowLeft" text size="small" @click="listCollapsed = !listCollapsed" />
            </el-tooltip>
            <template v-if="!listCollapsed">
              <span class="brand-icon">🎬</span>
              <span class="toolbar-title">{{ $t('tool.videoPlayer.videoList') }}</span>
              <span class="video-count">{{ videos.length }}</span>
            </template>
          </div>
        </div>

        <template v-if="!listCollapsed">
          <div class="list-tools">
            <div class="search-row">
              <el-input
                v-model="searchKeyword"
                :placeholder="$t('tool.videoPlayer.searchPlaceholder')"
                :prefix-icon="Search"
                clearable
                @input="handleSearch"
                size="small"
                class="search-input"
              />
              <el-tooltip :content="$t('tool.videoPlayer.scanFolder')" placement="bottom">
                <el-button :icon="Refresh" circle size="small" :loading="scanning" @click="handleScan" />
              </el-tooltip>
              <el-tooltip :content="$t('tool.videoPlayer.playStats')" placement="bottom">
                <el-button :icon="DataAnalysis" circle size="small" @click="showStats = true" />
              </el-tooltip>
            </div>
          </div>

        <div class="list-header">
          <span class="col-play"></span>
          <span class="col-index">#</span>
          <span class="col-title">{{ $t('tool.videoPlayer.title') }}</span>
          <span class="col-type">{{ $t('tool.videoPlayer.format') }}</span>
          <span class="col-duration">{{ $t('tool.videoPlayer.duration') }}</span>
        </div>

        <div class="video-list" ref="videoListRef">
          <div
            v-for="(video, idx) in videos"
            :key="video.id"
            class="video-row"
            :class="{ active: currentVideo?.id === video.id }"
            @click="selectVideo(video)"
          >
            <span class="col-play">
              <el-button
                :icon="currentVideo?.id === video.id && isPlaying ? VideoPause : VideoPlay"
                text
                size="small"
                class="row-play-btn"
                @click.stop="togglePlay(video)"
              />
            </span>
            <span class="col-index">
              <span v-if="currentVideo?.id === video.id && isPlaying" class="playing-bars"><i></i><i></i><i></i></span>
              <span v-else>{{ idx + 1 }}</span>
            </span>
            <span class="col-title">
              <span class="title-text">{{ video.title }}</span>
            </span>
            <span class="col-type">
              <el-tag size="small" type="info">{{ video.videoType?.toUpperCase() }}</el-tag>
            </span>
            <span class="col-duration">{{ video.duration > 0 ? formatTime(video.duration) : '--:--' }}</span>
          </div>
          <el-empty v-if="videos.length === 0 && !loading" :description="$t('tool.videoPlayer.noVideos')" :image-size="80" />
          <div v-if="loading" class="loading-mask">
            <el-icon class="is-loading" :size="24"><Refresh /></el-icon>
            <span>{{ $t('tool.videoPlayer.loadingText') }}</span>
          </div>
        </div>

        <div class="video-info" v-if="currentVideo">
          <div class="info-row">
            <span class="info-label">{{ $t('tool.videoPlayer.fileName') }}:</span>
            <span class="info-value">{{ currentVideo.fileName }}</span>
          </div>
          <div class="info-row" v-if="currentVideo.fileSize">
            <span class="info-label">{{ $t('tool.videoPlayer.fileSize') }}:</span>
            <span class="info-value">{{ formatFileSize(currentVideo.fileSize) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">{{ $t('tool.videoPlayer.playCount') }}:</span>
            <span class="info-value">{{ currentVideo.playCount || 0 }}</span>
          </div>
        </div>
      </template>
      </div>

      <div class="detail-panel">
        <div class="player-area">
          <div ref="artRef" class="artplayer-container"></div>
        </div>

        <div class="url-play-area">
          <div class="url-section">
            <h4>{{ $t('tool.videoPlayer.urlPlay') }}</h4>
            <div class="url-input-row">
              <el-input
                v-model="videoUrl"
                :placeholder="$t('tool.videoPlayer.urlPlaceholder')"
                clearable
                size="default"
                @keyup.enter="playUrl"
              >
                <template #prefix>
                  <el-icon><Link /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="playUrl" :disabled="!videoUrl.trim()">
                {{ $t('tool.videoPlayer.playUrl') }}
              </el-button>
            </div>
            <div class="url-hints">
              <el-tag size="small" type="info" v-for="hint in urlHints" :key="hint" class="url-hint-tag">{{ hint }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showStats" :title="$t('tool.videoPlayer.playStats')" width="400px" destroy-on-close>
      <div class="stats-dialog">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalVideos || 0 }}</div>
          <div class="stat-label">{{ $t('tool.videoPlayer.totalVideos') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalPlays || 0 }}</div>
          <div class="stat-label">{{ $t('tool.videoPlayer.totalPlays') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.todayPlays || 0 }}</div>
          <div class="stat-label">{{ $t('tool.videoPlayer.todayPlays') }}</div>
        </div>
      </div>
      <div class="recent-plays" v-if="recentPlays.length > 0">
        <h4>{{ $t('tool.videoPlayer.recentPlays') }}</h4>
        <div v-for="record in recentPlays" :key="record.id" class="recent-item">
          <span class="recent-title">{{ record.videoTitle }}</span>
          <span class="recent-user">{{ record.username }}</span>
          <span class="recent-time">{{ formatDateTime(record.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, DataAnalysis, VideoPlay, VideoPause, Link, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import Artplayer from 'artplayer'
import Hls from 'hls.js'
import {
  scanVideoApi,
  getVideoListApi,
  getVideoDetailApi,
  recordVideoPlayApi,
  getVideoStatsApi,
  getVideoRecentApi
} from '@/api/video'
import { API } from '@/api/routes'

defineOptions({ name: 'VideoPlayer' })

const searchKeyword = ref('')
const videos = ref([])
const currentVideo = ref(null)
const loading = ref(false)
const scanning = ref(false)
const showStats = ref(false)
const stats = ref({})
const recentPlays = ref([])
const videoUrl = ref('')
const artRef = ref(null)
const videoListRef = ref(null)
const listCollapsed = ref(false)
let artInstance = null
let searchTimer = null
let isPlaying = ref(false)

const urlHints = ['mp4', 'm3u8', 'webm', 'Bilibili', 'YouTube', 'iframe']

onMounted(async () => {
  loading.value = true
  try {
    const res = await getVideoListApi()
    videos.value = res.data || []
  } finally {
    loading.value = false
  }
  if (videos.value.length === 0) {
    handleScan()
  }
})

onBeforeUnmount(() => {
  destroyArt()
  if (searchTimer) clearTimeout(searchTimer)
})

function destroyArt() {
  if (artInstance) {
    try { artInstance.destroy() } catch {}
    artInstance = null
  }
}

async function loadVideos() {
  loading.value = true
  try {
    const res = await getVideoListApi()
    videos.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    loading.value = true
    try {
      const res = await getVideoListApi(searchKeyword.value)
      videos.value = res.data || []
    } finally {
      loading.value = false
    }
  }, 300)
}

async function handleScan() {
  scanning.value = true
  try {
    const res = await scanVideoApi()
    videos.value = res.data || []
    ElMessage.success(res.message || '扫描完成')
  } catch (e) {
    ElMessage.error(e.message || '扫描失败')
  } finally {
    scanning.value = false
  }
}

function selectVideo(video) {
  currentVideo.value = video
}

function togglePlay(video) {
  if (currentVideo.value?.id === video.id && artInstance) {
    if (artInstance.playing) {
      artInstance.pause()
    } else {
      artInstance.play()
    }
  } else {
    playVideo(video)
  }
}

async function playVideo(video) {
  currentVideo.value = video
  const url = API.VIDEO.PLAYER.STREAM(video.id)
  initArtPlayer(url)
  try {
    await recordVideoPlayApi(video.id, 0)
  } catch {}
}

function playUrl() {
  const url = videoUrl.value.trim()
  if (!url) return
  currentVideo.value = null

  if (isDirectVideoUrl(url)) {
    initArtPlayer(url)
  } else if (isBilibiliUrl(url)) {
    const bvid = extractBilibiliBvid(url)
    if (bvid) {
      initIframePlayer(`https://player.bilibili.com/player.html?bvid=${bvid}&autoplay=1`)
    } else {
      ElMessage.warning('无法解析 Bilibili 视频 ID')
    }
  } else if (isYouTubeUrl(url)) {
    const vid = extractYouTubeId(url)
    if (vid) {
      initIframePlayer(`https://www.youtube.com/embed/${vid}?autoplay=1`)
    } else {
      ElMessage.warning('无法解析 YouTube 视频 ID')
    }
  } else {
    ElMessage.info('支持直接视频链接（mp4/m3u8/webm）或 Bilibili/YouTube 链接')
  }
}

function isDirectVideoUrl(url) {
  return /\.(mp4|webm|ogg|mkv|avi|flv|mov|m3u8|m4v|ts)(\?|$)/i.test(url)
    || url.includes('.m3u8')
    || url.includes('video.m3u8')
}

function isBilibiliUrl(url) {
  return /bilibili\.com|b23\.tv|bili2233\.cn/.test(url)
}

function isYouTubeUrl(url) {
  return /youtube\.com|youtu\.be/.test(url)
}

function extractBilibiliBvid(url) {
  const match = url.match(/BV[\w]+/i)
  return match ? match[0] : null
}

function extractYouTubeId(url) {
  const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([^&?\s]+)/)
  return match ? match[1] : null
}

function initIframePlayer(url) {
  destroyArt()
  nextTick(() => {
    if (!artRef.value) return
    artRef.value.innerHTML = `
      <iframe src="${url}"
        style="width:100%;height:100%;border:none;"
        allowfullscreen
        allow="autoplay; encrypted-media"
        sandbox="allow-same-origin allow-scripts allow-popups allow-forms">
      </iframe>
    `
  })
}

function initArtPlayer(url) {
  destroyArt()
  nextTick(() => {
    if (!artRef.value) return

    const isHls = url.includes('.m3u8')
    const artConfig = {
      container: artRef.value,
      url: url,
      volume: 0.7,
      isLive: false,
      autoPlay: true,
      playbackRate: true,
      fullscreen: true,
      fullscreenWeb: true,
      pip: true,
      autoSize: false,
      autoMini: true,
      screenshot: true,
      lock: true,
      flip: true,
      aspectRatio: true,
      subtitleOffset: false,
      miniProgressBar: true,
      mutex: true,
      backdrop: true,
      playsInline: true,
      autoPlayback: true,
      airplay: true,
      theme: '#6366f1',
      lang: 'zh-cn',
      moreVideoProps: {
        crossOrigin: 'anonymous'
      }
    }

    if (isHls && Hls.isSupported()) {
      artConfig.customType = {
        m3u8: function (video, url) {
          const hls = new Hls()
          hls.loadSource(url)
          hls.attachMedia(video)
          hls.on(Hls.Events.ERROR, function (event, data) {
            if (data.fatal) {
              hls.destroy()
            }
          })
          artInstance._hls = hls
        }
      }
    }

    artInstance = new Artplayer(artConfig)

    artInstance.on('video:timeupdate', () => {
      isPlaying.value = true
    })

    artInstance.on('pause', () => {
      isPlaying.value = false
    })

    artInstance.on('play', () => {
      isPlaying.value = true
    })

    artInstance.on('error', (error) => {
      ElMessage.error('视频播放失败：' + (error?.message || '未知错误'))
      isPlaying.value = false
    })

    artInstance.on('video:ended', () => {
      isPlaying.value = false
      if (currentVideo.value) {
        const idx = videos.value.findIndex(v => v.id === currentVideo.value.id)
        if (idx >= 0 && idx < videos.value.length - 1) {
          playVideo(videos.value[idx + 1])
        }
      }
    })
  })
}

async function loadStats() {
  try {
    const res = await getVideoStatsApi()
    stats.value = res.data || {}
  } catch {}
}

async function loadRecentPlays() {
  try {
    const res = await getVideoRecentApi(10)
    recentPlays.value = res.data || []
  } catch {}
}

watch(showStats, (val) => {
  if (val) {
    loadStats()
    loadRecentPlays()
  }
})

function formatTime(seconds) {
  if (!seconds || seconds <= 0) return '00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  if (h > 0) {
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i === 0 ? 0 : 1) + ' ' + units[i]
}

function formatDateTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hours = d.getHours().toString().padStart(2, '0')
  const minutes = d.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped>
.video-player-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.player-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}

.video-list-panel {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-page);
  max-height: 100%;
  transition: width 0.3s ease;
}

.video-list-panel.collapsed {
  width: 48px;
}

.list-tools {
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-lighter);
}

.search-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-row .search-input {
  flex: 1;
  min-width: 0;
}

.toolbar-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-lighter);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.video-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.detail-brand {
  display: none;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  font-size: 12px;
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--border-lighter);
  background: var(--bg-page);
}

.col-play { width: 36px; text-align: center; }
.col-index { width: 36px; text-align: center; }
.col-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-type { width: 60px; text-align: center; }
.col-duration { width: 60px; text-align: right; }

.video-list {
  flex: 1;
  overflow-y: auto;
}

.video-row {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid var(--border-lighter);
}

.video-row:hover {
  background: var(--bg-hover);
}

.video-row.active {
  background: var(--color-primary-light-9);
}

.video-row.playing .title-text {
  color: var(--color-primary);
}

.row-play-btn {
  color: var(--color-text-secondary);
}

.video-row.active .row-play-btn {
  color: var(--color-primary);
}

.playing-bars {
  display: inline-flex;
  align-items: flex-end;
  gap: 2px;
  height: 14px;
}

.playing-bars i {
  display: block;
  width: 3px;
  background: var(--color-primary);
  border-radius: 1px;
  animation: barBounce 0.8s ease-in-out infinite;
}

.playing-bars i:nth-child(1) { height: 60%; animation-delay: 0s; }
.playing-bars i:nth-child(2) { height: 100%; animation-delay: 0.15s; }
.playing-bars i:nth-child(3) { height: 40%; animation-delay: 0.3s; }

@keyframes barBounce {
  0%, 100% { transform: scaleY(1); }
  50% { transform: scaleY(0.4); }
}

.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.player-area {
  flex: 1;
  min-height: 300px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.artplayer-container {
  width: 100%;
  height: 100%;
}

.url-play-area {
  flex-shrink: 0;
}

.url-section {
  padding: 16px;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-page);
}

.url-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.url-input-row {
  display: flex;
  gap: 8px;
}

.url-input-row .el-input {
  flex: 1;
}

.url-hints {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.url-hint-tag {
  cursor: default;
}

.video-info {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid var(--border-lighter);
  background: var(--bg-page);
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
}

.info-label {
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.info-value {
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loading-mask {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.stats-dialog {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.recent-plays {
  margin-top: 16px;
  border-top: 1px solid var(--border-lighter);
  padding-top: 16px;
}

.recent-plays h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid var(--border-lighter);
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text-primary);
}

.recent-user {
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.recent-time {
  color: var(--color-text-secondary);
  flex-shrink: 0;
}
</style>

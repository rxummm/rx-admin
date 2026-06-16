<template>
  <div class="music-player-page">
    <!-- ========== 顶部区域：Logo + 搜索栏 + 工具按钮 ========== -->
    <div class="player-header">
      <div class="header-left">
        <div class="brand">
          <span class="brand-icon">🎵</span>
          <span class="brand-name">{{ $t('tool.musicPlayer.title') }}</span>
        </div>
      </div>
      <div class="header-center">
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('tool.musicPlayer.searchPlaceholder')"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
          size="default"
          class="search-input"
        />
      </div>
      <div class="header-right">
        <el-tooltip :content="$t('tool.musicPlayer.scanMusicFolder')" placement="bottom">
          <el-button :icon="Refresh" circle size="small" :loading="scanning" @click="handleScan" />
        </el-tooltip>
        <el-tooltip :content="$t('tool.musicPlayer.playStats')" placement="bottom">
          <el-button :icon="DataAnalysis" circle size="small" @click="showStats = true" />
        </el-tooltip>
      </div>
    </div>

    <!-- ========== 中间主体区域 ========== -->
    <div class="player-body">
      <!-- 左侧：歌曲列表 -->
      <div class="song-list-panel">
        <!-- 列表工具栏 -->
        <div class="list-toolbar">
          <div class="toolbar-left">
            <h3>{{ $t('tool.musicPlayer.playlist') }}</h3>
            <span class="song-count">{{ songs.length }} {{ $t('tool.musicPlayer.playCount') }}</span>
          </div>
          <el-button type="primary" size="small" plain @click="playAll">
            {{ $t('tool.musicPlayer.playAll') }}
          </el-button>
        </div>

        <!-- 表头 -->
        <div class="list-header">
          <span class="col-play"> </span>
          <span class="col-index">#</span>
          <span class="col-title">歌曲</span>
          <span class="col-artist">歌手</span>
          <span class="col-duration">时长</span>
        </div>

        <!-- 歌曲列表 -->
        <div class="song-list" ref="songListRef">
          <div
            v-for="(song, idx) in songs"
            :key="song.id"
            class="song-row"
            :class="{ active: currentSong?.id === song.id, playing: currentSong?.id === song.id && isPlaying }"
            @contextmenu.prevent="showSongMenu($event, song)"
          >
            <span class="col-play">
              <el-button
                :icon="currentSong?.id === song.id && isPlaying ? VideoPause : VideoPlay"
                text
                size="small"
                class="row-play-btn"
                @click.stop="playSong(song)"
              />
            </span>
            <span class="col-index">
              <span v-if="currentSong?.id === song.id && isPlaying" class="playing-bars"><i></i><i></i><i></i></span>
              <span v-else>{{ idx + 1 }}</span>
            </span>
            <span class="col-title">
              <span class="title-text">{{ song.title }}</span>
            </span>
            <span class="col-artist">{{ song.artist || '-' }}</span>
            <span class="col-duration">{{ song.duration > 0 ? formatTime(song.duration) : '--:--' }}</span>
          </div>
          <el-empty v-if="songs.length === 0 && !loading" description="暂无歌曲，请点击扫描按钮加载" :image-size="80" />
          <div v-if="loading" class="loading-mask">
            <el-icon class="is-loading" :size="24"><Refresh /></el-icon>
            <span>加载中...</span>
          </div>
        </div>
      </div>

      <!-- 右侧：封面 + 歌词 -->
      <div class="detail-panel">
        <!-- 封面区域 -->
        <div class="cover-area">
          <div class="cover-disc" :class="{ spinning: isPlaying }">
            <div class="cover-inner">
              <span class="cover-letter">{{ currentSong ? currentSong.title?.charAt(0) : '♪' }}</span>
            </div>
          </div>
          <div class="cover-glow"></div>
        </div>

        <!-- 歌曲信息 -->
        <div class="song-info-bar" v-if="currentSong">
          <div class="info-title">{{ currentSong.title }}</div>
          <div class="info-artist">{{ currentSong.artist || '未知艺术家' }}</div>
        </div>
        <div class="song-info-bar empty" v-else>
          <div class="info-title">未选择歌曲</div>
        </div>

        <!-- 歌词面板 -->
        <div class="lyric-wrapper" ref="lyricPanelRef">
          <div class="lyric-scroll" v-if="lyrics.length > 0">
            <div
              v-for="(line, index) in lyrics"
              :key="index"
              class="lyric-line"
              :class="{ active: index === currentLyricIndex }"
              :ref="el => { if (el) lyricLineRefs[index] = el }"
              @click="seekToLyric(line.time)"
            >
              {{ line.text }}
            </div>
          </div>
          <div class="no-lyric" v-else>
            <p>{{ currentSong ? '暂无歌词' : '选择歌曲开始播放' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 底部播放控制栏（固定） ========== -->
    <div class="player-footer">
      <!-- 左：当前播放信息 -->
      <div class="footer-left">
        <div class="footer-thumb" :class="{ spinning: isPlaying }">
          <span class="thumb-letter">{{ currentSong ? currentSong.title?.charAt(0) : '♪' }}</span>
        </div>
        <div class="footer-song-info">
          <div class="footer-song-title">{{ currentSong ? currentSong.title : '未选择歌曲' }}</div>
          <div class="footer-song-artist">{{ currentSong ? (currentSong.artist || '-') : '' }}</div>
        </div>
        <div class="footer-actions" v-if="currentSong">
          <el-tooltip content="收藏" placement="top">
            <el-button :icon="Star" text size="small" :type="isFavorited ? 'warning' : 'default'" @click="toggleFavorite" />
          </el-tooltip>
        </div>
      </div>

      <!-- 中：进度条 + 播放控制 -->
      <div class="footer-center">
        <div class="center-controls">
          <el-tooltip :content="playModeTip" placement="top">
            <span class="mode-icon" :class="{ active: playMode !== 0 }" @click="togglePlayMode">
              <FontAwesomeIcon :icon="playModeFaIcon" />
            </span>
          </el-tooltip>
          <el-tooltip content="上一首" placement="top">
            <el-button :icon="DArrowLeft" text size="default" class="skip-btn" @click="playPrev" />
          </el-tooltip>
          <el-button
            :icon="isPlaying ? VideoPause : VideoPlay"
            circle
            size="large"
            class="main-play-btn"
            :type="'primary'"
            @click="togglePlay"
          />
          <el-tooltip content="下一首" placement="top">
            <el-button :icon="DArrowRight" text size="default" class="skip-btn" @click="playNext" />
          </el-tooltip>
          <el-tooltip content="歌词" placement="top">
            <el-button :icon="Document" text size="small" :type="showLyricPanel ? 'primary' : 'default'" @click="toggleLyricPanel" />
          </el-tooltip>
        </div>
        <div class="progress-row">
          <span class="time-label">{{ formatTime(currentTime) }}</span>
          <div class="progress-track-wrap" ref="progressBarRef" @click="seekTo">
            <div class="progress-track">
              <div class="progress-buffered"></div>
              <div class="progress-played" :style="{ width: progressPercent + '%' }"></div>
              <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
            </div>
          </div>
          <span class="time-label">{{ formatTime(duration) }}</span>
        </div>
      </div>

      <!-- 右：音量控制 -->
      <div class="footer-right">
        <div class="volume-control">
          <el-icon :size="18" class="vol-icon" @click="toggleMute">
            <VideoPause v-if="muted || volume === 0" />
            <VideoPlay v-else />
          </el-icon>
          <el-slider v-model="volume" :min="0" :max="100" size="small" class="vol-slider" @input="setVolume" :show-tooltip="false" />
        </div>
      </div>
    </div>

    <!-- 播放统计弹窗 -->
    <el-dialog v-model="showStats" title="播放统计" width="520px" destroy-on-close>
      <div class="stats-grid" v-if="stats">
        <div class="stat-card">
          <div class="stat-num">{{ stats.totalSongs }}</div>
          <div class="stat-label">歌曲总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ stats.totalPlays }}</div>
          <div class="stat-label">总播放次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ stats.todayPlays }}</div>
          <div class="stat-label">今日播放</div>
        </div>
      </div>
      <div class="top-songs-section" v-if="topSongs.length > 0">
        <h4>热门歌曲 TOP{{ topSongs.length }}</h4>
        <div class="top-song-item" v-for="(s, i) in topSongs" :key="s.id">
          <span class="top-rank" :class="{ 'top-3': i < 3 }">{{ i + 1 }}</span>
          <span class="top-title">{{ s.title }}</span>
          <span class="top-count">{{ s.playCount }}次</span>
        </div>
      </div>
      <div class="recent-section" v-if="recentPlays.length > 0">
        <h4>最近播放</h4>
        <div class="recent-item" v-for="r in recentPlays.slice(0, 10)" :key="r.id">
          <span>{{ r.songTitle }}</span>
          <span class="recent-time">{{ r.createTime }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh, Search, VideoPlay, VideoPause, DArrowLeft, DArrowRight,
  DataAnalysis, Star, Document
} from '@element-plus/icons-vue'
import {
  scanMusicApi, getSongsApi, getSongDetailApi, recordPlayApi,
  getStatsApi, getRecentApi, getTopSongsApi
} from '@/api/music'

defineOptions({ name: 'ToolMusicPlayer' })

// 状态
const songs = ref([])
const currentSong = ref(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(70)
const muted = ref(false)
const loading = ref(false)
const scanning = ref(false)
const searchKeyword = ref('')
const playMode = ref(0) // 0=顺序播放, 1=单曲循环, 2=随机播放
const lyrics = ref([])
const currentLyricIndex = ref(-1)
const showStats = ref(false)
const showLyricPanel = ref(true)
const isFavorited = ref(false)
const stats = ref(null)
const topSongs = ref([])
const recentPlays = ref([])
const lyricLineRefs = reactive({})
const progressBarRef = ref(null)
const lyricPanelRef = ref(null)

// 音频元素
const audio = new Audio()

// 计算属性
const progressPercent = computed(() => {
  if (duration.value === 0) return 0
  return (currentTime.value / duration.value) * 100
})

// 播放模式配置 — 使用 FontAwesome 图标
const playModeConfig = [
  { faIcon: 'list', tip: '顺序播放' },
  { faIcon: 'repeat', tip: '单曲循环' },
  { faIcon: 'shuffle', tip: '随机播放' }
]
const playModeFaIcon = computed(() => playModeConfig[playMode.value].faIcon)
const playModeTip = computed(() => playModeConfig[playMode.value].tip)

// 初始化
onMounted(() => {
  audio.volume = volume.value / 100
  audio.addEventListener('timeupdate', onTimeUpdate)
  audio.addEventListener('loadedmetadata', onLoaded)
  audio.addEventListener('ended', onEnded)
  audio.addEventListener('error', onAudioError)
  loadSongs()
})

onBeforeUnmount(() => {
  audio.pause()
  audio.removeEventListener('timeupdate', onTimeUpdate)
  audio.removeEventListener('loadedmetadata', onLoaded)
  audio.removeEventListener('ended', onEnded)
  audio.removeEventListener('error', onAudioError)
  audio.src = ''
})

// 加载歌曲列表
async function loadSongs() {
  loading.value = true
  try {
    const res = await getSongsApi(searchKeyword.value || undefined)
    if (res.code === 200) {
      songs.value = res.data || []
    }
  } catch (e) {
    console.error('加载歌曲失败', e)
  } finally {
    loading.value = false
  }
}

// 扫描音乐文件夹
async function handleScan() {
  scanning.value = true
  try {
    const res = await scanMusicApi()
    if (res.code === 200) {
      ElMessage.success(res.msg || '扫描完成')
      songs.value = res.data || []
    } else {
      ElMessage.error(res.msg || '扫描失败')
    }
  } catch (e) {
    ElMessage.error('扫描失败')
  } finally {
    scanning.value = false
  }
}

// 搜索
let searchTimer = null
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadSongs(), Number(import.meta.env.VITE_MUSIC_SEARCH_DEBOUNCE_MS) || 300)
}

// 播放全部
function playAll() {
  if (songs.value.length > 0) {
    playSong(songs.value[0])
  }
}

// 播放歌曲
async function playSong(song) {
  if (currentSong.value?.id === song.id) {
    togglePlay()
    return
  }
  reportPlay()
  currentSong.value = song

  // 加载歌词
  if (song.lrcContent) {
    lyrics.value = parseLrc(song.lrcContent)
  } else {
    try {
      const res = await getSongDetailApi(song.id)
      if (res.code === 200 && res.data?.lrcContent) {
        lyrics.value = parseLrc(res.data.lrcContent)
      } else {
        lyrics.value = []
      }
    } catch (e) {
      lyrics.value = []
    }
  }
  currentLyricIndex.value = -1

  // 设置音频源
  audio.src = `/api/music/stream/${song.id}`
  audio.play().then(() => {
    isPlaying.value = true
  }).catch(e => {
    ElMessage.warning('流式播放失败，尝试静态文件')
    console.warn('流式播放失败，尝试静态路径', e)
    const fileName = song.title + '.mp3'
    audio.src = `/shareddocs/${fileName}`
    audio.play().then(() => {
      isPlaying.value = true
    }).catch(err => {
      ElMessage.error('无法播放该文件')
      isPlaying.value = false
    })
  })
}

// 暂停/播放
function togglePlay() {
  if (!currentSong.value) {
    if (songs.value.length > 0) playSong(songs.value[0])
    return
  }
  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
  } else {
    audio.play().then(() => {
      isPlaying.value = true
    }).catch(() => {
      ElMessage.error('播放失败')
    })
  }
}

// 上一首
function playPrev() {
  if (songs.value.length === 0) return
  const idx = songs.value.findIndex(s => s.id === currentSong.value?.id)
  const prevIdx = playMode.value === 2
    ? Math.floor(Math.random() * songs.value.length)
    : idx <= 0 ? songs.value.length - 1 : idx - 1
  playSong(songs.value[prevIdx])
}

// 下一首
function playNext() {
  if (songs.value.length === 0) return
  const idx = songs.value.findIndex(s => s.id === currentSong.value?.id)
  const nextIdx = playMode.value === 2
    ? Math.floor(Math.random() * songs.value.length)
    : idx >= songs.value.length - 1 ? 0 : idx + 1
  playSong(songs.value[nextIdx])
}

// 播放模式切换
function togglePlayMode() {
  playMode.value = (playMode.value + 1) % 3
}

// 静音
function toggleMute() {
  muted.value = !muted.value
  audio.muted = muted.value
}

// 音量
function setVolume(val) {
  audio.volume = val / 100
  muted.value = false
  audio.muted = false
}

// 进度条点击
function seekTo(e) {
  if (!progressBarRef.value) return
  const rect = progressBarRef.value.getBoundingClientRect()
  const ratio = (e.clientX - rect.left) / rect.width
  audio.currentTime = ratio * duration.value
}

// 点击歌词跳转播放
function seekToLyric(time) {
  if (audio.src) {
    audio.currentTime = time
    if (!isPlaying.value) {
      audio.play().then(() => { isPlaying.value = true }).catch(() => {
  ElMessage.error('播放失败')
})
    }
  }
}

// 切换歌词面板显示
function toggleLyricPanel() {
  showLyricPanel.value = !showLyricPanel.value
}

// 收藏切换
function toggleFavorite() {
  isFavorited.value = !isFavorited.value
  ElMessage.success(isFavorited.value ? '已添加到收藏' : '已取消收藏')
}

// 时间格式化
function formatTime(seconds) {
  if (!seconds || isNaN(seconds)) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 音频事件
function onTimeUpdate() {
  currentTime.value = audio.currentTime
  if (lyrics.value.length > 0) {
    const time = audio.currentTime
    let newIndex = -1
    for (let i = 0; i < lyrics.value.length; i++) {
      if (time >= lyrics.value[i].time) {
        newIndex = i
      } else {
        break
      }
    }
    if (newIndex !== currentLyricIndex.value) {
      currentLyricIndex.value = newIndex
      nextTick(() => {
        const el = lyricLineRefs[newIndex]
        if (el && lyricPanelRef.value) {
          const container = lyricPanelRef.value
          const elTop = el.offsetTop
          const containerHeight = container.clientHeight
          const elHeight = el.offsetHeight
          container.scrollTo({ top: elTop - containerHeight / 2 + elHeight / 2, behavior: 'smooth' })
        }
      })
    }
  }
}

function onLoaded() {
  duration.value = audio.duration
}

function onEnded() {
  if (playMode.value === 1) {
    audio.currentTime = 0
    audio.play()
  } else {
    reportPlay()
    playNext()
  }
}

function onAudioError() {
  ElMessage.error('音频加载失败')
  isPlaying.value = false
}

// 上报播放记录
function reportPlay() {
  if (!currentSong.value) return
  recordPlayApi(currentSong.value.id, Math.floor(audio.currentTime)).catch(() => {
  ElMessage.error('上报播放记录失败')
})
}

// 右键菜单
function showSongMenu(e, song) {
  // 预留右键菜单功能
}

// 歌词解析
function parseLrc(lrcText) {
  if (!lrcText) return []
  const lines = lrcText.split('\n')
  const result = []
  const timeReg = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/
  for (const line of lines) {
    const match = line.match(timeReg)
    if (match) {
      const minutes = parseInt(match[1])
      const seconds = parseInt(match[2])
      const millis = parseInt(match[3])
      const time = minutes * 60 + seconds + millis / (match[3].length === 2 ? 100 : 1000)
      const text = line.replace(timeReg, '').trim()
      if (text) {
        result.push({ time, text })
      }
    }
  }
  result.sort((a, b) => a.time - b.time)
  return result
}

// 加载统计数据
async function loadStats() {
  try {
    const [statsRes, topRes, recentRes] = await Promise.all([
      getStatsApi(), getTopSongsApi(20), getRecentApi(20)
    ])
    if (statsRes.code === 200) stats.value = statsRes.data
    if (topRes.code === 200) topSongs.value = topRes.data || []
    if (recentRes.code === 200) recentPlays.value = recentRes.data || []
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

watch(showStats, (val) => {
  if (val) loadStats()
})
</script>

<style scoped>
/* ==================== 页面整体布局 ==================== */
.music-player-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--layout-content-offset, 107px));
  background: var(--el-bg-color);
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
}

/* ==================== 顶部 Header ==================== */
.player-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  flex-shrink: 0;
}
.header-left .brand {
  display: flex;
  align-items: center;
  gap: 8px;
}
.brand-icon {
  font-size: 22px;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.header-center .search-input {
  width: 360px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ==================== 主体区域 ==================== */
.player-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ---- 左侧：歌曲列表 ---- */
.song-list-panel {
  width: 480px;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}
.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px 10px;
}
.toolbar-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.toolbar-left h3 {
  margin: 0;
  font-size: 15px;
  color: var(--el-text-color-primary);
}
.song-count {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* 表头 */
.list-header {
  display: flex;
  align-items: center;
  padding: 6px 20px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  background: var(--el-fill-color-extra-light);
  user-select: none;
}
.col-play { width: 40px; flex-shrink: 0; text-align: center; }
.col-index { width: 48px; text-align: center; flex-shrink: 0; }
.col-title { flex: 1; min-width: 0; }
.col-artist { width: 120px; flex-shrink: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-duration { width: 56px; text-align: right; flex-shrink: 0; }

.row-play-btn { opacity: 0; transition: opacity 0.15s; }
.song-row:hover .row-play-btn { opacity: 1; }
.song-row.active .row-play-btn { opacity: 1; }

/* 列表 */
.song-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}
.song-list::-webkit-scrollbar { width: 6px; }
.song-list::-webkit-scrollbar-thumb { background: var(--el-border-color); border-radius: 3px; }

/* 行 */
.song-row {
  display: flex;
  align-items: center;
  padding: 8px 20px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 13px;
  user-select: none;
}
.song-row:hover {
  background: var(--el-fill-color-light);
}
.song-row.active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.song-row.playing .col-title .title-text {
  color: var(--el-color-primary);
  font-weight: 600;
}
.song-row .col-index {
  color: var(--el-text-color-placeholder);
}

/* 动态音波图标 */
.playing-bars {
  display: inline-flex;
  align-items: flex-end;
  gap: 2px;
  height: 14px;
}
.playing-bars i {
  display: inline-block;
  width: 3px;
  height: 6px;
  background: var(--el-color-primary);
  animation: bars 0.8s ease-in-out infinite;
  border-radius: 1px;
}
.playing-bars i:nth-child(2) { animation-delay: 0.2s; height: 10px; }
.playing-bars i:nth-child(3) { animation-delay: 0.4s; height: 5px; }
@keyframes bars {
  0%, 100% { height: 4px; }
  50% { height: 14px; }
}

.col-title .title-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}
.col-artist {
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
}
.col-duration {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

.loading-mask {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

/* ---- 右侧：封面 + 歌词 ---- */
.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 32px;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

/* 封面 */
.cover-area {
  position: relative;
  margin-bottom: 16px;
  flex-shrink: 0;
}
.cover-disc {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #4facfe 75%, #667eea 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.35), 0 0 80px rgba(118, 75, 162, 0.15);
  position: relative;
  z-index: var(--z-decor, 1);
  transition: transform 0.3s;
}
.cover-disc::after {
  content: '';
  position: absolute;
  width: 44px;
  height: 44px;
  background: var(--el-bg-color);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  z-index: 2;
}
.spinning.cover-disc {
  animation: spin 12s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.cover-inner {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  background: rgba(255,255,255,0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-audio-control, 3);
  position: relative;
}
.cover-letter {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.cover-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(102,126,234,0.12) 0%, transparent 70%);
  z-index: var(--z-base, 0);
  pointer-events: none;
}

/* 歌曲信息 */
.song-info-bar {
  text-align: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}
.info-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 320px;
}
.info-artist {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.song-info-bar.empty .info-title {
  color: var(--el-text-color-placeholder);
  font-weight: 400;
  font-size: 14px;
}

/* 歌词面板 */
.lyric-wrapper {
  flex: 1;
  width: 100%;
  min-height: 0;
  overflow-y: auto;
  position: relative;
}
.lyric-wrapper::-webkit-scrollbar { width: 0; }
.lyric-scroll {
  text-align: center;
  padding: 8px 0;
}
.lyric-line {
  padding: 6px 16px;
  font-size: 14px;
  color: var(--el-text-color-placeholder);
  transition: all 0.25s;
  cursor: pointer;
  border-radius: 4px;
  line-height: 1.6;
}
.lyric-line:hover {
  color: var(--el-text-color-primary);
  background: var(--el-fill-color-light);
}
.lyric-line.active {
  color: var(--el-color-primary);
  font-size: 16px;
  font-weight: 600;
}
.no-lyric {
  text-align: center;
  color: var(--el-text-color-placeholder);
  padding: 40px 0;
  font-size: 14px;
}

/* ==================== 底部 Footer（固定高度） ==================== */
.player-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 28px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  flex-shrink: 0;
  min-height: 72px;
  z-index: var(--z-content, 10);
}

/* -- 左：当前播放 -- */
.footer-left {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 240px;
  min-width: 180px;
}
.footer-thumb {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}
.footer-thumb::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.thumb-letter {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  z-index: var(--z-decor, 1);
}
.footer-song-info {
  min-width: 0;
  flex: 1;
}
.footer-song-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.footer-song-artist {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 1px;
}
.footer-actions {
  flex-shrink: 0;
}

/* -- 中：播放控制 + 进度条 -- */
.footer-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  max-width: 520px;
  flex: 1;
}
.center-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}
.mode-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; cursor: pointer; border-radius: 50%;
  transition: all 0.2s; color: var(--el-text-color-secondary); font-size: 15px;
}
.mode-icon:hover { color: var(--el-color-primary); background: var(--el-fill-color-light); }
.mode-icon.active { color: var(--el-color-primary); }
.skip-btn { font-size: 20px !important; }
.main-play-btn {
  width: 38px !important;
  height: 38px !important;
  margin: 0 6px;
}

.progress-row {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 8px;
}
.time-label {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  width: 42px;
  text-align: center;
  flex-shrink: 0;
  user-select: none;
}
.progress-track-wrap {
  flex: 1;
  cursor: pointer;
  padding: 6px 0;
}
.progress-track {
  height: 3px;
  background: var(--el-fill-color);
  border-radius: 2px;
  position: relative;
}
.progress-buffered {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 30%;
  background: var(--el-fill-color-dark);
  border-radius: 2px;
}
.progress-played {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: var(--el-color-primary);
  border-radius: 2px;
  transition: width 0.08s linear;
}
.progress-thumb {
  position: absolute;
  top: 50%;
  width: 10px;
  height: 10px;
  background: var(--el-color-primary);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.15s;
}
.progress-track-wrap:hover .progress-track {
  height: 5px;
}
.progress-track-wrap:hover .progress-thumb {
  transform: translate(-50%, -50%) scale(1);
}

/* -- 右：音量 -- */
.footer-right {
  width: 180px;
  min-width: 140px;
  display: flex;
  justify-content: flex-end;
}
.volume-control {
  display: flex;
  align-items: center;
  gap: 6px;
}
.vol-icon {
  cursor: pointer;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}
.vol-slider {
  width: 90px;
}

/* ==================== 统计弹窗 ==================== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}
.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
}
.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.top-songs-section, .recent-section {
  margin-top: 16px;
}
.top-songs-section h4, .recent-section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--el-text-color-primary);
}
.top-song-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.top-rank {
  width: 24px;
  text-align: center;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}
.top-rank.top-3 { color: var(--el-color-danger); }
.top-title { flex: 1; }
.top-count { color: var(--el-text-color-placeholder); font-size: 12px; }
.recent-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
}
.recent-time { color: var(--el-text-color-placeholder); font-size: 12px; }
</style>

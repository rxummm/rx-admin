import request from '@/utils/request'

// 扫描音乐文件夹
export function scanMusicApi() {
  return request({ url: '/music/scan', method: 'post' })
}

// 获取歌曲列表
export function getSongsApi(keyword) {
  return request({ url: '/music/songs', method: 'get', params: { keyword } })
}

// 获取歌曲详情(含歌词)
export function getSongDetailApi(id) {
  return request({ url: `/music/song/${id}`, method: 'get' })
}

// 记录播放
export function recordPlayApi(id, playedSeconds) {
  return request({ url: `/music/play/${id}`, method: 'post', params: { playedSeconds } })
}

// 播放统计
export function getStatsApi() {
  return request({ url: '/music/stats', method: 'get' })
}

// 最近播放记录
export function getRecentApi(limit = 20) {
  return request({ url: '/music/recent', method: 'get', params: { limit } })
}

// 热门歌曲排行
export function getTopSongsApi(limit = 20) {
  return request({ url: '/music/top', method: 'get', params: { limit } })
}

// 获取音乐文件夹路径
export function getMusicFolderApi() {
  return request({ url: '/music/folder', method: 'get' })
}

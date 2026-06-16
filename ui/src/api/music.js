import request from '@/utils/request'
import { API } from './routes'

// 扫描音乐文件夹
export function scanMusicApi() {
  return request({ url: API.MUSIC.SCAN, method: 'post' })
}

// 获取歌曲列表
export function getSongsApi(keyword) {
  return request({ url: API.MUSIC.SONGS, method: 'get', params: { keyword } })
}

// 获取歌曲详情(含歌词)
export function getSongDetailApi(id) {
  return request({ url: API.MUSIC.SONG_DETAIL(id), method: 'get' })
}

// 记录播放
export function recordPlayApi(id, playedSeconds) {
  return request({ url: API.MUSIC.PLAY(id), method: 'post', params: { playedSeconds } })
}

// 播放统计
export function getStatsApi() {
  return request({ url: API.MUSIC.STATS, method: 'get' })
}

// 最近播放记录
export function getRecentApi(limit = 20) {
  return request({ url: API.MUSIC.RECENT, method: 'get', params: { limit } })
}

// 热门歌曲排行
export function getTopSongsApi(limit = 20) {
  return request({ url: API.MUSIC.TOP, method: 'get', params: { limit } })
}

// 获取音乐文件夹路径
export function getMusicFolderApi() {
  return request({ url: API.MUSIC.FOLDER, method: 'get' })
}

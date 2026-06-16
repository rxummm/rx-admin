import request from '@/utils/request'
import { API } from './routes'

// 扫描视频文件夹
export function scanVideoApi() {
  return request({ url: API.VIDEO.PLAYER.SCAN, method: 'post' })
}

// 获取视频列表
export function getVideoListApi(keyword) {
  return request({ url: API.VIDEO.PLAYER.LIST, method: 'get', params: { keyword } })
}

// 获取视频详情
export function getVideoDetailApi(id) {
  return request({ url: API.VIDEO.PLAYER.DETAIL(id), method: 'get' })
}

// 记录播放
export function recordVideoPlayApi(videoId, playedSeconds) {
  return request({ url: API.VIDEO.PLAYER.RECORD, method: 'post', params: { videoId, playedSeconds } })
}

// 播放统计
export function getVideoStatsApi() {
  return request({ url: API.VIDEO.PLAYER.STATS, method: 'get' })
}

// 最近播放记录
export function getVideoRecentApi(limit = 20) {
  return request({ url: API.VIDEO.PLAYER.RECENT, method: 'get', params: { limit } })
}

// 删除视频记录
export function deleteVideoApi(id) {
  return request({ url: API.VIDEO.PLAYER.DELETE(id), method: 'delete' })
}

// 获取视频文件夹路径
export function getVideoFolderApi() {
  return request({ url: API.VIDEO.PLAYER.FOLDER, method: 'get' })
}

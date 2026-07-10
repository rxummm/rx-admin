import request from '@/utils/request'
import { API } from './routes'

// 定时任务分页列表
export function getJobPageApi(params) {
  return request({
    url: API.MONITOR.JOB.PAGE,
    method: 'get',
    params
  })
}

// 新增定时任务
export function addJobApi(data) {
  return request({
    url: API.MONITOR.JOB.CRUD,
    method: 'post',
    data
  })
}

// 修改定时任务
export function updateJobApi(data) {
  return request({
    url: API.MONITOR.JOB.CRUD,
    method: 'put',
    data
  })
}

// 删除定时任务
export function deleteJobApi(id) {
  return request({
    url: API.MONITOR.JOB.BY_ID(id),
    method: 'delete'
  })
}

// 切换状态
export function toggleJobStatusApi(id) {
  return request({
    url: API.MONITOR.JOB.TOGGLE_STATUS(id),
    method: 'put'
  })
}

// 执行一次
export function runOnceApi(id) {
  return request({
    url: API.MONITOR.JOB.RUN_ONCE(id),
    method: 'put'
  })
}

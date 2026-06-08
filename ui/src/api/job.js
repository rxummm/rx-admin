import request from '@/utils/request'

// 定时任务分页列表
export function getJobPageApi(params) {
  return request({
    url: '/monitor/job/page',
    method: 'get',
    params
  })
}

// 新增定时任务
export function addJobApi(data) {
  return request({
    url: '/monitor/job',
    method: 'post',
    data
  })
}

// 修改定时任务
export function updateJobApi(data) {
  return request({
    url: '/monitor/job',
    method: 'put',
    data
  })
}

// 删除定时任务
export function deleteJobApi(id) {
  return request({
    url: `/monitor/job/${id}`,
    method: 'delete'
  })
}

// 切换状态
export function toggleJobStatusApi(id) {
  return request({
    url: `/monitor/job/status/${id}`,
    method: 'put'
  })
}

// 执行一次
export function runOnceApi(id) {
  return request({
    url: `/monitor/job/run/${id}`,
    method: "put"
  })
}


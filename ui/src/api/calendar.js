import request from '@/utils/request'
import { API } from './routes'

export function getCalendarEventsByMonthApi(year, month) {
  return request({
    url: API.CALENDAR.MONTH,
    method: 'get',
    params: { year, month },
    _skipCancel: true,
    _skipNProgress: true
  })
}

export function getTodayEventsApi() {
  return request({
    url: API.CALENDAR.TODAY,
    method: 'get',
    _skipCancel: true,
    _skipNProgress: true
  })
}

export function getEventByIdApi(id) {
  return request({
    url: API.CALENDAR.BY_ID(id),
    method: 'get'
  })
}

export function createEventApi(data) {
  return request({
    url: API.CALENDAR.CRUD,
    method: 'post',
    data
  })
}

export function updateEventApi(data) {
  return request({
    url: API.CALENDAR.CRUD,
    method: 'put',
    data
  })
}

export function deleteEventApi(id) {
  return request({
    url: API.CALENDAR.BY_ID(id),
    method: 'delete'
  })
}

export function getCalendarEventsByRangeApi(startDate, endDate) {
  return request({
    url: API.CALENDAR.RANGE,
    method: 'get',
    params: { startDate, endDate },
    _skipCancel: true,
    _skipNProgress: true
  })
}

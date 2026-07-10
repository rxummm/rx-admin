import request from '@/utils/request'
import { API } from './routes'

export function loginApi(username, password, captchaUuid, captchaCode) {
  return request({
    url: API.AUTH.LOGIN,
    method: 'post',
    headers: {
      'X-Timestamp': Date.now().toString(),
      'X-Nonce': crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36)
    },
    data: { username, password, captchaUuid, captchaCode }
  })
}

export function registerApi(username, password, nickname) {
  return request({
    url: API.AUTH.REGISTER,
    method: 'post',
    data: { username, password, nickname }
  })
}

export function logoutApi() {
  return request({
    url: API.AUTH.LOGOUT,
    method: 'post'
  })
}

export function getUserInfoApi() {
  return request({
    url: API.AUTH.USER_INFO,
    method: 'get'
  })
}

export function getRoutersApi() {
  return request({
    url: API.AUTH.ROUTERS,
    method: 'get'
  })
}

export function getCaptchaApi() {
  return request({
    url: API.AUTH.CAPTCHA,
    method: 'get'
  })
}

export function updateProfileApi(data) {
  return request({
    url: API.AUTH.UPDATE_PROFILE,
    method: 'put',
    data
  })
}

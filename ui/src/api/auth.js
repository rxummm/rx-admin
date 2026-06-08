import request from '@/utils/request'

export function loginApi(username, password, captchaUuid, captchaCode) {
  return request({
    url: '/auth/login',
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
    url: '/auth/register',
    method: 'post',
    data: { username, password, nickname }
  })
}

export function logoutApi() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function getUserInfoApi() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

export function getRoutersApi() {
  return request({
    url: '/auth/routers',
    method: 'get'
  })
}

export function getCaptchaApi() {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}

export function updateProfileApi(data) {
  return request({
    url: '/auth/update-profile',
    method: 'put',
    data
  })
}
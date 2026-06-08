import request from '@/utils/request'

// ====== 作者 ======

export function getAuthorPageApi(params) {
  return request({ url: '/classics/literature/author/page', method: 'get', params })
}

export function getAuthorDetailApi(id) {
  return request({ url: `/classics/literature/author/${id}`, method: 'get' })
}

export function getAllAuthorsApi() {
  return request({ url: '/classics/literature/author/all', method: 'get' })
}

export function addAuthorApi(data) {
  return request({ url: '/classics/literature/author', method: 'post', data })
}

export function updateAuthorApi(data) {
  return request({ url: '/classics/literature/author', method: 'put', data })
}

export function deleteAuthorApi(id) {
  return request({ url: `/classics/literature/author/${id}`, method: 'delete' })
}

export function batchDeleteAuthorApi(ids) {
  return request({ url: '/classics/literature/author/batch', method: 'delete', data: ids })
}

// ====== 朝代 ======

export function getDynastyPageApi(params) {
  return request({ url: '/classics/literature/dynasty/page', method: 'get', params })
}

export function getDynastyDetailApi(id) {
  return request({ url: `/classics/literature/dynasty/${id}`, method: 'get' })
}

export function getAllDynastiesApi() {
  return request({ url: '/classics/literature/dynasty/all', method: 'get' })
}

export function addDynastyApi(data) {
  return request({ url: '/classics/literature/dynasty', method: 'post', data })
}

export function updateDynastyApi(data) {
  return request({ url: '/classics/literature/dynasty', method: 'put', data })
}

export function deleteDynastyApi(id) {
  return request({ url: `/classics/literature/dynasty/${id}`, method: 'delete' })
}

export function batchDeleteDynastyApi(ids) {
  return request({ url: '/classics/literature/dynasty/batch', method: 'delete', data: ids })
}

// ====== 体裁 ======

export function getGenrePageApi(params) {
  return request({ url: '/classics/literature/genre/page', method: 'get', params })
}

export function getGenreDetailApi(id) {
  return request({ url: `/classics/literature/genre/${id}`, method: 'get' })
}

export function getAllGenresApi() {
  return request({ url: '/classics/literature/genre/all', method: 'get' })
}

export function addGenreApi(data) {
  return request({ url: '/classics/literature/genre', method: 'post', data })
}

export function updateGenreApi(data) {
  return request({ url: '/classics/literature/genre', method: 'put', data })
}

export function deleteGenreApi(id) {
  return request({ url: `/classics/literature/genre/${id}`, method: 'delete' })
}

export function batchDeleteGenreApi(ids) {
  return request({ url: '/classics/literature/genre/batch', method: 'delete', data: ids })
}

// ====== 内容分类 ======

export function getCategoryPageApi(params) {
  return request({ url: '/classics/literature/category/page', method: 'get', params })
}

export function getCategoryDetailApi(id) {
  return request({ url: `/classics/literature/category/${id}`, method: 'get' })
}

export function getAllCategoriesApi() {
  return request({ url: '/classics/literature/category/all', method: 'get' })
}

export function addCategoryApi(data) {
  return request({ url: '/classics/literature/category', method: 'post', data })
}

export function updateCategoryApi(data) {
  return request({ url: '/classics/literature/category', method: 'put', data })
}

export function deleteCategoryApi(id) {
  return request({ url: `/classics/literature/category/${id}`, method: 'delete' })
}

export function batchDeleteCategoryApi(ids) {
  return request({ url: '/classics/literature/category/batch', method: 'delete', data: ids })
}

// ====== 文学作品 ======

export function getWorkPageApi(params) {
  return request({ url: '/classics/literature/work/page', method: 'get', params })
}

export function getWorkDetailApi(id) {
  return request({ url: `/classics/literature/work/${id}`, method: 'get' })
}

export function getAllWorksApi() {
  return request({ url: '/classics/literature/work/all', method: 'get' })
}

export function addWorkApi(data) {
  return request({ url: '/classics/literature/work', method: 'post', data })
}

export function updateWorkApi(data) {
  return request({ url: '/classics/literature/work', method: 'put', data })
}

export function deleteWorkApi(id) {
  return request({ url: `/classics/literature/work/${id}`, method: 'delete' })
}

export function batchDeleteWorkApi(ids) {
  return request({ url: '/classics/literature/work/batch', method: 'delete', data: ids })
}

import request from '@/utils/request'
import { API } from './routes'

// ====== 作者 ======

export function getAuthorPageApi(params) {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.PAGE, method: 'get', params })
}

export function getAuthorDetailApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.BY_ID(id), method: 'get' })
}

export function getAllAuthorsApi() {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.ALL, method: 'get' })
}

export function addAuthorApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.CRUD, method: 'post', data })
}

export function updateAuthorApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.CRUD, method: 'put', data })
}

export function deleteAuthorApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.BY_ID(id), method: 'delete' })
}

export function batchDeleteAuthorApi(ids) {
  return request({ url: API.CLASSICS.LITERATURE.AUTHOR.BATCH, method: 'delete', data: ids })
}

// ====== 朝代 ======

export function getDynastyPageApi(params) {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.PAGE, method: 'get', params })
}

export function getDynastyDetailApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.BY_ID(id), method: 'get' })
}

export function getAllDynastiesApi() {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.ALL, method: 'get' })
}

export function addDynastyApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.CRUD, method: 'post', data })
}

export function updateDynastyApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.CRUD, method: 'put', data })
}

export function deleteDynastyApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.BY_ID(id), method: 'delete' })
}

export function batchDeleteDynastyApi(ids) {
  return request({ url: API.CLASSICS.LITERATURE.DYNASTY.BATCH, method: 'delete', data: ids })
}

// ====== 体裁 ======

export function getGenrePageApi(params) {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.PAGE, method: 'get', params })
}

export function getGenreDetailApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.BY_ID(id), method: 'get' })
}

export function getAllGenresApi() {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.ALL, method: 'get' })
}

export function addGenreApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.CRUD, method: 'post', data })
}

export function updateGenreApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.CRUD, method: 'put', data })
}

export function deleteGenreApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.BY_ID(id), method: 'delete' })
}

export function batchDeleteGenreApi(ids) {
  return request({ url: API.CLASSICS.LITERATURE.GENRE.BATCH, method: 'delete', data: ids })
}

// ====== 内容分类 ======

export function getCategoryPageApi(params) {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.PAGE, method: 'get', params })
}

export function getCategoryDetailApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.BY_ID(id), method: 'get' })
}

export function getAllCategoriesApi() {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.ALL, method: 'get' })
}

export function addCategoryApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.CRUD, method: 'post', data })
}

export function updateCategoryApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.CRUD, method: 'put', data })
}

export function deleteCategoryApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.BY_ID(id), method: 'delete' })
}

export function batchDeleteCategoryApi(ids) {
  return request({ url: API.CLASSICS.LITERATURE.CATEGORY.BATCH, method: 'delete', data: ids })
}

// ====== 文学作品 ======

export function getWorkPageApi(params) {
  return request({ url: API.CLASSICS.LITERATURE.WORK.PAGE, method: 'get', params })
}

export function getWorkDetailApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.WORK.BY_ID(id), method: 'get' })
}

export function getAllWorksApi() {
  return request({ url: API.CLASSICS.LITERATURE.WORK.ALL, method: 'get' })
}

export function addWorkApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.WORK.CRUD, method: 'post', data })
}

export function updateWorkApi(data) {
  return request({ url: API.CLASSICS.LITERATURE.WORK.CRUD, method: 'put', data })
}

export function deleteWorkApi(id) {
  return request({ url: API.CLASSICS.LITERATURE.WORK.BY_ID(id), method: 'delete' })
}

export function batchDeleteWorkApi(ids) {
  return request({ url: API.CLASSICS.LITERATURE.WORK.BATCH, method: 'delete', data: ids })
}

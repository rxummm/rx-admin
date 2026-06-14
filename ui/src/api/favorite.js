import request from '@/utils/request'
import { API } from './routes'

export const getFavoritesApi = () => request.get(API.SYSTEM.FAVORITE.LIST)
export const addFavoriteApi = (data) => request.post(API.SYSTEM.FAVORITE.CRUD, data)
export const toggleFavoriteApi = (data) => request.post(API.SYSTEM.FAVORITE.TOGGLE, data)
export const deleteFavoriteApi = (id) => request.delete(API.SYSTEM.FAVORITE.BY_ID(id))
export const sortFavoritesApi = (ids) => request.put(API.SYSTEM.FAVORITE.SORT, { ids })

import request from '@/utils/request'

export const getFavoritesApi = () => request.get('/system/favorite/list')
export const addFavoriteApi = (data) => request.post('/system/favorite', data)
export const toggleFavoriteApi = (data) => request.post('/system/favorite/toggle', data)
export const deleteFavoriteApi = (id) => request.delete(`/system/favorite/${id}`)
export const sortFavoritesApi = (ids) => request.put('/system/favorite/sort', { ids })

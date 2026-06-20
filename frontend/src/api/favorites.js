import { del, get, post } from './request'

export function favoriteItem(itemId) {
  return post(`/items/${itemId}/favorite`)
}

export function unfavoriteItem(itemId) {
  return del(`/items/${itemId}/favorite`)
}

export function listFavoriteItems() {
  return get('/favorites')
}

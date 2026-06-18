import { del, get, post, put } from './request'

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      searchParams.set(key, String(value).trim())
    }
  })
  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

export function listItems(params) {
  return get(`/items${buildQuery(params)}`)
}

export function getItemDetail(id) {
  return get(`/items/${id}`)
}

export function listMyItems() {
  return get('/items/mine')
}

export function createItem(payload) {
  return post('/items', payload)
}

export function updateItem(id, payload) {
  return put(`/items/${id}`, payload)
}

export function deleteItem(id) {
  return del(`/items/${id}`)
}

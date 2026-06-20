import { get, post } from './request'

export function reserveItem(itemId) {
  return post(`/items/${itemId}/reserve`)
}

export function cancelOrder(orderId) {
  return post(`/orders/${orderId}/cancel`)
}

export function completeOrder(orderId) {
  return post(`/orders/${orderId}/complete`)
}

export function listReservedItems() {
  return get('/orders/reserved')
}

export function listBoughtItems() {
  return get('/orders/bought')
}

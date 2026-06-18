import { get } from './request'

function normalizeCascaderOptions(options = []) {
  return options.map((option) => {
    const normalized = { ...option }
    if (Array.isArray(normalized.children) && normalized.children.length > 0) {
      normalized.children = normalizeCascaderOptions(normalized.children)
    } else {
      delete normalized.children
    }
    return normalized
  })
}

export async function getLocationTree() {
  const options = await get('/locations/tree')
  return normalizeCascaderOptions(options)
}

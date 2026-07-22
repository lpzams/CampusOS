const BASE_URL = 'http://localhost:8080/api'
const API_ORIGIN = BASE_URL.replace(/\/api\/?$/, '')

function headers(extra = {}) {
  const token = uni.getStorageSync('token')
  return { ...extra, ...(token ? { Authorization: `Bearer ${token}` } : {}) }
}

function unwrap(result, resolve, reject) {
  if (result && (result.code === 200 || result.code === 0)) {
    resolve(result.data)
    return
  }
  const message = result?.msg || '请求失败'
  if (result?.code === 401 || result?.code === 2001) uni.removeStorageSync('token')
  uni.showToast({ title: message, icon: 'none' })
  reject(new Error(message))
}

export function request({ url, method = 'GET', data = {}, header = {} }) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: headers(header),
      success: (res) => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          if (res.statusCode === 401) uni.removeStorageSync('token')
          const message = res.data?.msg || `服务异常(${res.statusCode})`
          uni.showToast({ title: message, icon: 'none' })
          reject(new Error(message))
          return
        }
        unwrap(res.data, resolve, reject)
      },
      fail: (error) => {
        uni.showToast({ title: '网络异常，请确认接口服务已启动', icon: 'none' })
        reject(error)
      },
    })
  })
}

export function upload(url, filePath, name = 'file') {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name,
      header: headers(),
      success: (res) => {
        try {
          unwrap(JSON.parse(res.data), resolve, reject)
        } catch (error) {
          reject(error)
        }
      },
      fail: reject,
    })
  })
}

export const get = (url, data) => request({ url, data })
export const post = (url, data) => request({ url, data, method: 'POST' })
export const put = (url, data) => request({ url, data, method: 'PUT' })
export const del = (url, data) => request({ url, data, method: 'DELETE' })
export const assetUrl = (path) => path && !/^https?:\/\//.test(path) ? API_ORIGIN + path : path

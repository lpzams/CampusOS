/**
 * uni.request 统一封装 —— 小程序端所有接口都走这里（作用等同网站端的 utils/request.ts）。
 *
 * 【它做了三件事】
 *  1. 统一 BASE_URL；
 *  2. 把回调风格的 uni.request 包成 Promise，页面里可以用 async/await；
 *  3. 统一"拆包"后端的 Result{code,msg,data}：成功只把 data 往下传，
 *     失败统一 uni.showToast 提示，页面里不用重复写错误处理。
 *
 * 【开发调试须知】
 *  - 微信开发者工具请求 http://localhost 需要勾选：详情 -> 本地设置 -> 「不校验合法域名…」
 *    （manifest.json 里 urlCheck:false 已帮你默认关掉校验）。
 *  - 真机预览时手机访问不到你电脑的 localhost，把下面 BASE_URL 换成
 *    电脑的局域网 IP，如 http://192.168.1.5:8080/api（手机和电脑要连同一个 WiFi）。
 *  - 正式发布必须用 https 域名，并在微信公众平台配置 request 合法域名。
 *
 * 【新增功能时】不用改这里，直接在 api/你的功能.js 里 import { get, post } 使用。
 */
const BASE_URL = 'http://localhost:8080/api'

export function request(options) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: options.header || {},
      success: (res) => {
        // HTTP 层失败（404 / 500 等）
        if (res.statusCode !== 200) {
          uni.showToast({ title: `服务异常(${res.statusCode})`, icon: 'none' })
          reject(new Error(`HTTP ${res.statusCode}`))
          return
        }
        // 业务层：按后端约定 code=200 才算成功
        const result = res.data
        if (result && result.code === 200) {
          resolve(result.data)
        } else {
          const msg = (result && result.msg) || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        // 连不上服务器：后端没启动 / 没关域名校验 / BASE_URL 配错
        uni.showToast({ title: '网络异常，请确认后端已启动', icon: 'none' })
        reject(err)
      },
    })
  })
}

/** GET：data 会被 uni.request 拼成 query string */
export const get = (url, data) => request({ url, data, method: 'GET' })

/** POST：data 以 JSON body 发送 */
export const post = (url, data) => request({ url, data, method: 'POST' })

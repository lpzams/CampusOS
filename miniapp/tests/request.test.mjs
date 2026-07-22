import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = await readFile(new URL('../utils/request.js', import.meta.url), 'utf8')
const { request } = await import(`data:text/javascript,${encodeURIComponent(source)}`)

let response
let statusCode = 200
let tokenRemoved = false
globalThis.uni = {
  getStorageSync: () => '',
  removeStorageSync: () => { tokenRemoved = true },
  showToast: () => {},
  request: ({ success }) => success({ statusCode, data: response }),
}

response = { code: 0, data: 'backend' }
assert.equal(await request({ url: '/test' }), 'backend')

response = { code: 200, data: 'document' }
assert.equal(await request({ url: '/test' }), 'document')

response = { code: 2001, msg: '登录过期' }
await assert.rejects(request({ url: '/test' }), /登录过期/)
assert.equal(tokenRemoved, true)

tokenRemoved = false
statusCode = 401
response = { msg: '未授权' }
await assert.rejects(request({ url: '/test' }), /未授权/)
assert.equal(tokenRemoved, true)

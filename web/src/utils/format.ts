/**
 * 展示格式化小工具。
 *
 * 后端的 LocalDateTime 经 Jackson 序列化后是 "2026-07-16T10:30:00" 这种 ISO 字符串，
 * 直接展示不好看，这里统一转成 "2026-07-16 10:30"。
 */

/** ISO 时间字符串 -> "yyyy-MM-dd HH:mm"；空值返回 "-" */
export function formatDateTime(value?: string | null): string {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

/** 取正文前 n 个字作为列表页摘要 */
export function summary(content?: string | null, n = 80): string {
  if (!content) return ''
  const plain = content.replace(/\s+/g, ' ').trim()
  return plain.length > n ? plain.slice(0, n) + '…' : plain
}

/** 金额展示："¥5,200.00"；空值返回 "-"（金额字段后端可能是 number 或字符串） */
export function formatMoney(value?: number | string | null): string {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  if (Number.isNaN(num)) return String(value)
  return `¥${num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

/**
 * 后端返回的文件路径（如头像 /uploads/avatars/xx.png）转成可访问的 URL。
 * 开发环境 /uploads 由 vite 代理转发到后端（见 vite.config.ts）；
 * 生产环境由 nginx 做同样的转发。完整 http(s) 地址原样返回。
 */
export function fileUrl(path?: string | null): string {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  return path
}

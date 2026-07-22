/**
 * 展示格式化小工具（与网站端 web/src/utils/format.ts 逻辑一致）。
 */

/** 后端时间 "2026-07-16T10:30:00" -> "2026-07-16 10:30"；空值返回 "-" */
export function formatDateTime(value) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

/** 取正文前 n 个字作为列表摘要 */
export function summary(content, n = 60) {
  if (!content) return ''
  const plain = content.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  return plain.length > n ? plain.slice(0, n) + '…' : plain
}

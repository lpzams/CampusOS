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

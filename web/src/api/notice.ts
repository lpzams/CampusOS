/**
 * 校园公告通知 API（功能 4）。
 *
 * 对应后端 NoticeController（/api/notice/**）。
 * 注意：本模块分页参数是 page/size（不是新闻模块的 pageNum/pageSize），
 * 以后端 Controller 的 @RequestParam 为准。
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/api/types'

/** 公告类型的固定取值（与后端存的字符串一致） */
export const NOTICE_TYPES = [
  { code: 'SCHOOL', name: '学校通知' },
  { code: 'DEPT', name: '院系通知' },
] as const

/** 一条公告（后端是动态 Map，字段以种子数据为准） */
export interface NoticeItem {
  id: number
  title: string
  content: string
  /** SCHOOL-学校 DEPT-院系 */
  type?: string
  department?: string
  createTime?: string
  [key: string]: unknown
}

/** 分页查询公告：GET /api/notice/list?type=&department=&page=1&size=10 */
export function pageNotices(query: { type?: string; department?: string; page?: number; size?: number }) {
  return get<PageResult<NoticeItem>>('/notice/list', query)
}

/** 公告详情：GET /api/notice/detail/{id} */
export function getNoticeDetail(id: number | string) {
  return get<NoticeItem>(`/notice/detail/${id}`)
}

/** 标记公告已读（需登录）：POST /api/notice/read/{id} */
export function markNoticeRead(id: number | string) {
  return post<void>(`/notice/read/${id}`)
}

/** 未读公告数量（需登录）：GET /api/notice/unread/count */
export function getUnreadCount() {
  return get<{ count: number }>('/notice/unread/count')
}

export function publishNotice(data: Pick<NoticeItem, 'title' | 'content'> & { type: string; department: string }) {
  return post<number>('/admin/notice', data)
}

/**
 * 公告通知模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  NoticeListParams,
  NoticeListResponse,
  NoticeDetail,
  NoticeReadResponse,
  UnreadCountResponse,
} from './types'

// ============================================================
// ===== 4.1 获取公告列表 =====
// ============================================================

export function getNoticeList(params: NoticeListParams) {
  return get<Result<NoticeListResponse>>('/notice/list', params)
}

// ============================================================
// ===== 4.2 获取公告详情 =====
// ============================================================

export function getNoticeDetail(id: number | string) {
  return get<Result<NoticeDetail>>(`/notice/detail/${id}`)
}

// ============================================================
// ===== 4.3 标记公告已读 =====
// ============================================================

export function markNoticeRead(id: number) {
  return post<Result<NoticeReadResponse>>(`/notice/read/${id}`)
}

// ============================================================
// ===== 4.4 获取未读公告数量 =====
// ============================================================

export function getUnreadCount() {
  return get<Result<UnreadCountResponse>>('/notice/unread/count')
}
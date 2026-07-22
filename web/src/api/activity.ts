/**
 * 校园活动 API（功能 13）。
 *
 * 对应后端 ActivityController（/api/activity/**）。
 * 浏览公开；报名/取消/签到/我的活动需要登录。
 * 注意：取消报名传的是「报名记录 id」（/activity/my 里每条的 id），不是活动 id。
 */
import { del, get, post } from '@/utils/request'
import type { PageResult } from '@/api/types'

/** 活动分类（code 与后端 categoryCode 一致） */
export const ACTIVITY_CATEGORIES = [
  { code: 'SPORTS', name: '体育' },
  { code: 'CULTURE', name: '文艺' },
  { code: 'ACADEMIC', name: '学术' },
  { code: 'VOLUNTEER', name: '志愿' },
] as const

/** 一场活动 */
export interface ActivityItem {
  id: number
  title: string
  category?: string
  categoryCode?: string
  coverImage?: string
  startTime?: string
  endTime?: string
  location?: string
  maxParticipants?: number
  currentParticipants?: number
  /** 如"报名中" */
  status?: string
  content?: string
  [key: string]: unknown
}

/** 我的一条报名记录 */
export interface ActivityRegistration {
  id: number
  activityId: number
  status?: string
  checkedIn?: boolean
  createTime?: string
  [key: string]: unknown
}

/** 分页查询活动：GET /api/activity/list?category=&status=&page=&size= */
export function pageActivities(query: { category?: string; status?: string; page?: number; size?: number }) {
  return get<PageResult<ActivityItem>>('/activity/list', query)
}

/** 活动详情：GET /api/activity/detail/{id} */
export function getActivityDetail(id: number | string) {
  return get<ActivityItem>(`/activity/detail/${id}`)
}

/** 报名活动（需登录）：POST /api/activity/register */
export function registerActivity(activityId: number) {
  return post<ActivityRegistration>('/activity/register', { activityId })
}

/** 取消报名（需登录，id 是报名记录 id）：DELETE /api/activity/register/{id} */
export function cancelRegistration(registrationId: number) {
  return del<void>(`/activity/register/${registrationId}`)
}

/** 活动签到（需登录，code 为签到码）：POST /api/activity/checkin */
export function checkinActivity(data: { activityId: number; code: string }) {
  return post<ActivityRegistration>('/activity/checkin', data)
}

/** 我的报名列表（需登录）：GET /api/activity/my */
export function getMyActivities() {
  return get<ActivityRegistration[]>('/activity/my')
}

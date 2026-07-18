/**
 * 校园活动模块 API
 */
import { get, post, del } from '../utils/request'
import type {
  Result,
  ActivityListParams,
  ActivityListResponse,
  ActivityDetail,
  ActivityRegisterParams,
  ActivityRegisterResponse,
  ActivityCheckinParams,
  ActivityCheckinResponse,
  MyActivityItem,
} from './types'

// ============================================================
// ===== 13.1 获取活动列表 =====
// ============================================================

export function getActivityList(params: ActivityListParams) {
  return get<Result<ActivityListResponse>>('/activity/list', params)
}

// ============================================================
// ===== 13.2 获取活动详情 =====
// ============================================================

export function getActivityDetail(id: number | string) {
  return get<Result<ActivityDetail>>(`/activity/detail/${id}`)
}

// ============================================================
// ===== 13.3 报名活动 =====
// ============================================================

export function registerActivity(data: ActivityRegisterParams) {
  return post<Result<ActivityRegisterResponse>>('/activity/register', data)
}

// ============================================================
// ===== 13.4 取消报名 =====
// ============================================================

export function cancelActivityRegister(id: number) {
  return del<Result<null>>(`/activity/register/${id}`)
}

// ============================================================
// ===== 13.5 活动签到 =====
// ============================================================

export function checkinActivity(data: ActivityCheckinParams) {
  return post<Result<ActivityCheckinResponse>>('/activity/checkin', data)
}

// ============================================================
// ===== 13.6 获取我的活动 =====
// ============================================================

export function getMyActivities() {
  return get<Result<MyActivityItem[]>>('/activity/my')
}
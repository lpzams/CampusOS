/**
 * 宿舍管理 API（功能 10）。
 *
 * 对应后端 DormitoryController（/api/dormitory/**）。
 * 宿舍公告是公开接口，宿舍信息/水电余额需要登录。
 */
import { get } from '@/utils/request'

/** 宿舍成员 */
export interface DormMember {
  userId?: number
  realName?: string
  phone?: string
  isRoomLeader?: boolean
}

/** 宿舍信息 */
export interface DormitoryInfo {
  id: number
  building: string
  room: string
  /** 如"四人间" */
  type?: string
  members?: DormMember[]
  facilities?: string[]
  electricityBalance?: number
  waterBalance?: number
  [key: string]: unknown
}

/** 一条宿舍公告 */
export interface DormNotice {
  id: number
  title: string
  content?: string
  createTime?: string
  [key: string]: unknown
}

/** 我的宿舍信息（需登录）：GET /api/dormitory/info */
export function getDormitoryInfo() {
  return get<DormitoryInfo>('/dormitory/info')
}

/** 宿舍公告：GET /api/dormitory/notice */
export function getDormitoryNotices() {
  return get<DormNotice[]>('/dormitory/notice')
}

/** 水电余额（需登录）：GET /api/dormitory/utility */
export function getUtility() {
  return get<{ electricityBalance: number; waterBalance: number }>('/dormitory/utility')
}

/**
 * 宿舍管理模块 API
 */
import { get } from '../utils/request'
import type {
  Result,
  DormitoryInfo,
  DormitoryNotice,
  UtilityResponse,
} from './types'

// ============================================================
// ===== 10.1 获取宿舍信息 =====
// ============================================================

export function getDormitoryInfo() {
  return get<Result<DormitoryInfo>>('/dormitory/info')
}

// ============================================================
// ===== 10.2 获取宿舍公告 =====
// ============================================================

export function getDormitoryNotices() {
  return get<Result<DormitoryNotice[]>>('/dormitory/notice')
}

// ============================================================
// ===== 10.3 查询水电余额 =====
// ============================================================

export function getUtilityInfo() {
  return get<Result<UtilityResponse>>('/dormitory/utility')
}
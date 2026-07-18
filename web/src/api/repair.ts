/**
 * 校园报修模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  SubmitRepairParams,
  SubmitRepairResponse,
  RepairListItem,
  RepairDetail,
  EvaluateParams,
  EvaluateResponse,
} from './types'

// ============================================================
// ===== 11.1 提交报修 =====
// ============================================================

export function submitRepair(data: SubmitRepairParams) {
  return post<Result<SubmitRepairResponse>>('/repair/submit', data)
}

// ============================================================
// ===== 11.2 获取报修列表 =====
// ============================================================

export function getRepairList() {
  return get<Result<RepairListItem[]>>('/repair/list')
}

// ============================================================
// ===== 11.3 获取报修详情 =====
// ============================================================

export function getRepairDetail(id: number | string) {
  return get<Result<RepairDetail>>(`/repair/detail/${id}`)
}

// ============================================================
// ===== 11.4 评价维修服务 =====
// ============================================================

export function evaluateRepair(data: EvaluateParams) {
  return post<Result<EvaluateResponse>>('/repair/evaluate', data)
}
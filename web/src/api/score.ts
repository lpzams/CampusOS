/**
 * 成绩查询模块 API
 */
import { get } from '../utils/request'
import type {
  Result,
  ScoreListParams,
  ScoreListResponse,
  GpaResponse,
  ScoreStatisticsResponse,
} from './types'

// ============================================================
// ===== 6.1 获取成绩列表 =====
// ============================================================

export function getScoreList(params?: ScoreListParams) {
  return get<Result<ScoreListResponse>>('/score/list', params)
}

// ============================================================
// ===== 6.2 获取 GPA =====
// ============================================================

export function getGpa() {
  return get<Result<GpaResponse>>('/score/gpa')
}

// ============================================================
// ===== 6.3 成绩统计分析 =====
// ============================================================

export function getScoreStatistics() {
  return get<Result<ScoreStatisticsResponse>>('/score/statistics')
}
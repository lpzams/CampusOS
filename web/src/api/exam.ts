/**
 * 考试安排模块 API
 */
import { get } from '../utils/request'
import type {
  Result,
  ExamItem,
  ExamCalendarParams,
  ExamCalendarResponse,
} from './types'

// ============================================================
// ===== 7.1 获取考试安排 =====
// ============================================================

export function getExamList() {
  return get<Result<ExamItem[]>>('/exam/list')
}

// ============================================================
// ===== 7.2 获取考试日历 =====
// ============================================================

export function getExamCalendar(params: ExamCalendarParams) {
  return get<Result<ExamCalendarResponse>>('/exam/calendar', params)
}
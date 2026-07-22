/**
 * 考试安排 API（功能 7）。
 *
 * 对应后端 ExamController（/api/exam/**，全部需要登录）。
 */
import { get, post } from '@/utils/request'

/** 一场考试 */
export interface ExamItem {
  id: number
  courseName: string
  courseCode?: string
  /** 如 "2026-07-20" */
  examDate: string
  /** 如 "14:00-16:00" */
  examTime?: string
  building?: string
  classroom?: string
  seatNumber?: string
  /** 如"待考试" */
  status?: string
  [key: string]: unknown
}

/** 全部考试安排（需登录）：GET /api/exam/list */
export function getExams() {
  return get<ExamItem[]>('/exam/list')
}

/** 按月查询考试（需登录）：GET /api/exam/calendar?month=2026-07 */
export function getExamCalendar(month: string) {
  return get<ExamItem[]>('/exam/calendar', { month })
}

export function publishExam(data: Omit<ExamItem, 'id' | 'status'> & { userId?: number }) {
  return post<number>('/admin/exam', data)
}

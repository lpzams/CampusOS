/**
 * 成绩查询 API（功能 6）。
 *
 * 对应后端 ScoreController（/api/score/**，全部需要登录）。
 * /score/list 一次性带回统计值（GPA/学分/平均分）和成绩明细，页面直接用。
 */
import { get } from '@/utils/request'

/** 一条成绩记录 */
export interface ScoreItem {
  id?: number
  courseName: string
  courseCode?: string
  credit: number
  score: number
  /** 如"优秀"、"良好" */
  grade?: string
  /** 考试 / 平时 */
  type?: string
  semester?: string
  examTime?: string
  [key: string]: unknown
}

/** 成绩列表（带统计）返回结构 */
export interface ScoreSummary {
  gpa: number
  totalCredits: number
  average: number
  count: number
  semester?: string | null
  scores: ScoreItem[]
}

/** 成绩列表 + 统计（需登录）：GET /api/score/list?semester=&type= */
export function getScores(params?: { semester?: string; type?: string }) {
  return get<ScoreSummary>('/score/list', params)
}

/** 仅查 GPA：GET /api/score/gpa */
export function getGpa(params?: { semester?: string }) {
  return get<{ gpa: number; totalCredits: number }>('/score/gpa', params)
}

/** 成绩统计分析：GET /api/score/statistics */
export function getScoreStatistics(params?: { semester?: string }) {
  return get<{ gpa: number; totalCredits: number; average: number; count: number }>('/score/statistics', params)
}

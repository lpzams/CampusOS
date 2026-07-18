/**
 * 课程管理模块 API
 */
import { get } from '../utils/request'
import type {
  Result,
  ScheduleResponse,
  TodayCoursesResponse,
  FreeClassroomsParams,
  FreeClassroomsResponse,
  TeacherCoursesResponse,
} from './types'

// ============================================================
// ===== 5.1 获取个人课表 =====
// ============================================================

export function getSchedule(params?: { semester?: string; week?: number }) {
  return get<Result<ScheduleResponse>>('/course/schedule', params)
}

// ============================================================
// ===== 5.2 获取今日课程 =====
// ============================================================

export function getTodayCourses() {
  return get<Result<TodayCoursesResponse>>('/course/today')
}

// ============================================================
// ===== 5.3 获取空闲教室 =====
// ============================================================

export function getFreeClassrooms(params: FreeClassroomsParams) {
  return get<Result<FreeClassroomsResponse>>('/course/free-classrooms', params)
}

// ============================================================
// ===== 5.4 获取教师授课列表 =====
// ============================================================

export function getTeacherCourses(teacherId: number | string) {
  return get<Result<TeacherCoursesResponse>>(`/course/teacher/${teacherId}`)
}
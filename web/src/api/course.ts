/**
 * 课程查询 API（功能 5）。
 *
 * 对应后端 CourseController（/api/course/**）。
 * 课表数据一行是「星期几 + 时间段」，courses 里才是具体课程
 * （同一时段可能有多门课，按周次错开）。
 */
import { get } from '@/utils/request'

/** 一门课（课表格子里展示的内容） */
export interface CourseInfo {
  id: number
  name: string
  teacher?: string
  classroom?: string
  building?: string
  /** 如 "1-16周" */
  weeks?: string
  /** 课表色块颜色，如 "#2F7D66" */
  color?: string
}

/** 课表的一行：某天某时段的课程集合 */
export interface CourseSlot {
  id?: number
  /** 1-周一 … 7-周日 */
  dayOfWeek: number
  /** 如 "08:00-09:35" */
  timeSlot: string
  courses: CourseInfo[]
  [key: string]: unknown
}

/** 个人课表返回结构 */
export interface ScheduleResult {
  semester: string
  week: number
  schedule: CourseSlot[]
}

/** 教室（空闲教室查询用） */
export interface Classroom {
  id: number
  name: string
  building?: string
  capacity?: number
  timeSlot?: string
  available?: boolean
  [key: string]: unknown
}

/** 个人课表（需登录）：GET /api/course/schedule?semester=2026-2027-1&week=1 */
export function getSchedule(params?: { semester?: string; week?: number }) {
  return get<ScheduleResult>('/course/schedule', params)
}

/** 今日课程（需登录）：GET /api/course/today */
export function getTodayCourses() {
  return get<CourseSlot[]>('/course/today')
}

/** 空闲教室查询：GET /api/course/free-classrooms?building=&date=&timeSlot= */
export function getFreeClassrooms(params?: { building?: string; date?: string; timeSlot?: string }) {
  return get<Classroom[]>('/course/free-classrooms', params)
}

/** 教师授课列表：GET /api/course/teacher/{teacherId} */
export function getTeacherCourses(teacherId: number | string) {
  return get<CourseSlot[]>(`/course/teacher/${teacherId}`)
}

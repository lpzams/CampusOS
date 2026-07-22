import { del, get, post, put } from '@/utils/request'

export interface TeacherOption { userId: number; realName: string; department?: string }
export interface AdminTeachingCourse { id: number; courseName: string; courseCode?: string; teacherId: number; teacherName: string; dayOfWeek: number; timeSlot: string; classroom: string; building?: string; semester?: string; credit?: number; weeks?: string; color?: string; catalogSourceId?: string; reviewCount?: number; avgOverall?: number }
export const getTeachingCourses = () => get<AdminTeachingCourse[]>('/admin/teaching/courses')
export const getTeachingTeachers = () => get<TeacherOption[]>('/admin/teaching/teachers')
export const arrangeTeachingCourse = (data: Omit<AdminTeachingCourse, 'id' | 'teacherName'>) => post<AdminTeachingCourse>('/admin/teaching/courses', data)
export const updateTeachingCourse = (id: number, data: Partial<Omit<AdminTeachingCourse, 'id' | 'teacherName'>>) => put<AdminTeachingCourse>(`/admin/teaching/courses/${id}`, data)
export const deleteTeachingCourse = (id: number) => del<void>(`/admin/teaching/courses/${id}`)

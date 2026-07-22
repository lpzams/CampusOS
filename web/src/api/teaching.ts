import { get, put } from '@/utils/request'

export interface TeachingCourse { id: number; courseName: string; courseCode?: string; classroom?: string; dayOfWeek?: number; timeSlot?: string; semester?: string }
export interface EnrolledStudent { studentId: number; username: string; realName?: string; courseId: number; courseName: string; score?: number | null }
export interface CourseFeedback { id: number; overall?: number; comment: string; createdTime?: string }
export const getMyTeachingCourses = () => get<TeachingCourse[]>('/teaching/courses/my')
export const getEnrolledStudents = (courseId: number) => get<EnrolledStudent[]>(`/teaching/courses/${courseId}/students`)
export const getTeachingReviews = (courseId: number) => get<CourseFeedback[]>(`/teaching/courses/${courseId}/reviews`)
export const saveStudentScore = (courseId: number, studentId: number, score: number) => put<void>(`/teaching/courses/${courseId}/students/${studentId}/score`, { score })

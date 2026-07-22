import { del, get, post } from '@/utils/request'

export interface CourseCatalogItem {
  /** Formal teaching offering id when returned by /teaching/catalog. */
  sourceId: string
  /** Crawler catalog id: the only id accepted by the review API. */
  catalogSourceId?: string
  courseName: string
  professor: string
  reviewCount: number
  avgOverall?: number
  avgRate1?: number
  avgRate2?: number
  avgRate3?: number
}

export interface CourseReviewItem {
  sourceId: string
  courseName: string
  professor: string
  comment: string
  overall?: number
  rate1?: number
  rate2?: number
  rate3?: number
  tags?: string
  attendance?: string
  bird?: string
  homework?: string
  upVote?: number
  downVote?: number
  sourceCreatedAt?: string
}

export interface SelectedCourse extends CourseCatalogItem {
  selectedAt: string
}

export function getTeachingCatalog(params?: { keyword?: string; page?: number; size?: number }) {
  return get<CourseCatalogItem[]>('/teaching/catalog', params)
}

export function getReviewCatalog(params?: { keyword?: string; page?: number; size?: number }) {
  return get<CourseCatalogItem[]>('/course-reviews/catalog', params)
}

export function getCourseReviews(courseSourceId: string, params?: { page?: number; size?: number }) {
  return get<CourseReviewItem[]>(`/course-reviews/${encodeURIComponent(courseSourceId)}`, params)
}

export function getSelectedCourses() {
  return get<SelectedCourse[]>('/course-selection/my')
}

export function selectCourse(courseSourceId: string) {
  return post<void>('/course-selection', { courseSourceId })
}

export function cancelCourseSelection(courseSourceId: string) {
  return del<void>('/course-selection', { courseSourceId })
}

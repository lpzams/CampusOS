import { del, get, put } from '@/utils/request'
import type { PageQuery, PageResult } from '@/api/types'

export interface ManagedUser {
  userId: number
  username: string
  realName?: string
  phone?: string
  email?: string
  department?: string
  studentId?: string
  userType: 1 | 2 | 3
  roleCode: string
  status: 0 | 1 | 2 | 3
  statusRemark?: string
  createdTime?: string
}

export interface UserQuery extends PageQuery {
  keyword?: string
  status?: number
  userType?: number
}

export const pageUsers = (query: UserQuery) => get<PageResult<ManagedUser>>('/admin/users', query)
export const enableAllUsers = () => put<{ enabledCount: number }>('/admin/users/enable-all')
export const auditUser = (id: number, status: 0 | 1, remark = '') => put<ManagedUser>(`/admin/user/${id}/audit`, { status, remark })
export const updateUserStatus = (id: number, status: 0 | 1 | 2 | 3, remark = '') => put<ManagedUser>(`/admin/user/${id}/status`, { status, remark })
export const updateUserRole = (id: number, userType: 1 | 2 | 3) => put<ManagedUser>(`/admin/user/${id}/role`, { userType })
export const deactivateUser = (id: number) => del<void>(`/admin/user/${id}`)

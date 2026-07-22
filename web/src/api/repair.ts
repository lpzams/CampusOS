/**
 * 校园报修 API（功能 11）。
 *
 * 对应后端 RepairController（/api/repair/**，全部需要登录）。
 * 流程：提交（待处理）-> 维修人员接单（处理中）-> 已完成 -> 评价（已评价）。
 * 演示环境没有维修端，种子数据里有一条"已完成"的工单可用来体验评价。
 */
import { get, post, put } from '@/utils/request'

/** 报修类型的固定取值（与后端/小程序保持一致） */
export const REPAIR_TYPES = ['水电', '家具', '设备', '网络', '其他'] as const

/** 报修状态 -> 标签颜色（页面展示用） */
export const REPAIR_STATUS_TAGS: Record<string, 'info' | 'warning' | 'success' | 'primary'> = {
  待处理: 'info',
  处理中: 'warning',
  已完成: 'success',
  已评价: 'primary',
}

/** 一条报修工单 */
export interface RepairItem {
  id: number
  title: string
  type?: string
  description?: string
  building?: string
  room?: string
  contactPhone?: string
  images?: string[]
  /** 待处理/处理中/已完成/已评价 */
  status?: string
  statusDesc?: string
  /** 评价分（1-5，评价后才有） */
  score?: number
  evaluation?: string
  createTime?: string
  [key: string]: unknown
}

/** 提交报修表单 */
export interface RepairForm {
  type: string
  title: string
  description: string
  building: string
  room: string
  contactPhone: string
  images?: string[]
}

/** 提交报修：POST /api/repair/submit */
export function submitRepair(data: RepairForm) {
  return post<RepairItem>('/repair/submit', data)
}

/** 我的报修列表：GET /api/repair/list */
export function getRepairList() {
  return get<RepairItem[]>('/repair/list')
}

/** 报修详情：GET /api/repair/detail/{id} */
export function getRepairDetail(id: number | string) {
  return get<RepairItem>(`/repair/detail/${id}`)
}

/** 评价维修服务（仅"已完成"的工单可评价）：POST /api/repair/evaluate */
export function evaluateRepair(data: { repairId: number; score: number; content: string }) {
  return post<RepairItem>('/repair/evaluate', data)
}

export function getAdminRepairs() { return get<RepairItem[]>('/admin/repair') }
export function updateRepairProgress(id: number, data: { status: '处理中' | '已完成'; statusDesc: string }) { return put<RepairItem>(`/admin/repair/${id}/progress`, data) }

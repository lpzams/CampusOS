/**
 * 校园地图导航 API（功能 14）。
 *
 * 对应后端 LocationController（/api/location/**，全部公开）。
 * 路径规划是后端按经纬度直线距离估算的演示实现，不接真实地图服务。
 */
import { get, post } from '@/utils/request'

/** 地点分类（code 与后端 categoryCode 一致） */
export const LOCATION_CATEGORIES = [
  { code: 'GATE', name: '校门' },
  { code: 'BUILDING', name: '教学楼' },
  { code: 'LIBRARY', name: '图书馆' },
  { code: 'CANTEEN', name: '食堂' },
  { code: 'DORMITORY', name: '宿舍' },
  { code: 'GYM', name: '体育馆' },
] as const

/** 一个校园地点 */
export interface LocationItem {
  id: number
  name: string
  category?: string
  categoryCode?: string
  longitude?: number
  latitude?: number
  address?: string
  building?: string
  image?: string
  [key: string]: unknown
}

/** 路径规划结果 */
export interface RouteResult {
  /** 距离（米） */
  distance: number
  /** 时长（分钟） */
  duration: number
  steps: { instruction: string; distance: number; direction: string }[]
}

/** 地点列表（可按分类筛）：GET /api/location/list?category=BUILDING */
export function getLocations(category?: string) {
  return get<LocationItem[]>('/location/list', category ? { category } : undefined)
}

/** 地点详情：GET /api/location/detail/{id} */
export function getLocationDetail(id: number | string) {
  return get<LocationItem>(`/location/detail/${id}`)
}

/** 按名称搜索地点：GET /api/location/search?keyword=图书馆 */
export function searchLocations(keyword: string) {
  return get<LocationItem[]>('/location/search', { keyword })
}

/** 路径规划：POST /api/location/route */
export function planRoute(data: { fromLng: number; fromLat: number; toLng: number; toLat: number; mode?: string }) {
  return post<RouteResult>('/location/route', data)
}

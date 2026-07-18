/**
 * 校园地图导航模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  LocationListParams,
  LocationListResponse,
  LocationDetail,
  LocationSearchResponse,
  RoutePlanParams,
  RoutePlanResponse,
} from './types'

// ============================================================
// ===== 14.1 获取校园地点列表 =====
// ============================================================

export function getLocationList(params?: LocationListParams) {
  return get<Result<LocationListResponse>>('/location/list', params)
}

// ============================================================
// ===== 14.2 获取地点详情 =====
// ============================================================

export function getLocationDetail(id: number | string) {
  return get<Result<LocationDetail>>(`/location/detail/${id}`)
}

// ============================================================
// ===== 14.3 搜索地点 =====
// ============================================================

export function searchLocation(keyword: string) {
  return get<Result<LocationSearchResponse>>('/location/search', { keyword })
}

// ============================================================
// ===== 14.4 获取路径规划 =====
// ============================================================

export function planRoute(data: RoutePlanParams) {
  return post<Result<RoutePlanResponse>>('/location/route', data)
}
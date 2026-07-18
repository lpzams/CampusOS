/**
 * 二手交易模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  ProductListParams,
  ProductListResponse,
  ProductDetail,
  PublishProductParams,
  PublishProductResponse,
  ProductFavoriteResponse,
  CreateProductOrderParams,
  CreateProductOrderResponse,
} from './types'

// ============================================================
// ===== 12.1 获取商品列表 =====
// ============================================================

export function getProductList(params: ProductListParams) {
  return get<Result<ProductListResponse>>('/product/list', params)
}

// ============================================================
// ===== 12.2 获取商品详情 =====
// ============================================================

export function getProductDetail(id: number | string) {
  return get<Result<ProductDetail>>(`/product/detail/${id}`)
}

// ============================================================
// ===== 12.3 发布商品 =====
// ============================================================

export function publishProduct(data: PublishProductParams) {
  return post<Result<PublishProductResponse>>('/product', data)
}

// ============================================================
// ===== 12.4 收藏商品 =====
// ============================================================

export function favoriteProduct(id: number) {
  return post<Result<ProductFavoriteResponse>>(`/product/favorite/${id}`)
}

// ============================================================
// ===== 12.5 创建订单 =====
// ============================================================

export function createProductOrder(data: CreateProductOrderParams) {
  return post<Result<CreateProductOrderResponse>>('/product/order', data)
}
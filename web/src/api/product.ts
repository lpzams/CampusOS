/**
 * 二手交易 API（功能 12）。
 *
 * 对应后端 ProductController（/api/product/**）。
 * 浏览公开；发布/收藏/下单需要登录。分页参数是 page/size。
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/api/types'

/** 商品分类（后端没有分类表，前后端约定这份固定清单，categoryId 对号入座） */
export const PRODUCT_CATEGORIES = [
  { id: 1, name: '电子产品' },
  { id: 2, name: '图书教材' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '运动器材' },
  { id: 5, name: '其他' },
] as const

/** 一件商品 */
export interface ProductItem {
  id: number
  title: string
  price: number
  description?: string
  coverImage?: string
  category?: string
  categoryId?: number
  /** 在售 / 已售 */
  status?: string
  viewCount?: number
  sellerId?: number
  contactPhone?: string
  wechat?: string
  createTime?: string
  [key: string]: unknown
}

/** 商品列表查询条件 */
export interface ProductQuery {
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
  /** 0-在售 1-已售 */
  status?: number
  page?: number
  size?: number
}

/** 发布商品表单 */
export interface ProductForm {
  title: string
  price: number
  description: string
  categoryId: number
  contactPhone: string
  wechat?: string
  images?: string[]
}

/** 分页查询商品：GET /api/product/list */
export function pageProducts(query: ProductQuery) {
  return get<PageResult<ProductItem>>('/product/list', query)
}

/** 商品详情：GET /api/product/detail/{id} */
export function getProductDetail(id: number | string) {
  return get<ProductItem>(`/product/detail/${id}`)
}

/** 发布商品（需登录）：POST /api/product */
export function createProduct(data: ProductForm) {
  return post<ProductItem>('/product', data)
}

/** 收藏商品（需登录，重复收藏后端会忽略）：POST /api/product/favorite/{id} */
export function favoriteProduct(id: number | string) {
  return post<void>(`/product/favorite/${id}`)
}

/** 对商品下单/留言（需登录）：POST /api/product/order */
export function orderProduct(data: { productId: number; message?: string }) {
  return post<Record<string, unknown>>('/product/order', data)
}

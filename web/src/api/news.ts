/**
 * 新闻模块 API —— 一个功能一个文件，集中放该功能的接口函数和类型。
 */
import { get, post, put, del } from '../utils/request'
import type {
  Result,
  NewsCategory,
  NewsListParams,
  NewsListResponse,
  NewsDetail,
  FavoriteNewsItem,
  FavoriteListResponse,
  FavoriteResponse,
  PublishNewsParams,
  PublishNewsResponse,
} from './types'

// ============================================================
// ===== 3.1 获取新闻列表 =====
// ============================================================

export function getNewsList(params: NewsListParams) {
  return get<Result<NewsListResponse>>('/news/list', params)
}

// ============================================================
// ===== 3.2 获取新闻详情 =====
// ============================================================

export function getNewsDetail(id: number | string) {
  return get<Result<NewsDetail>>(`/news/detail/${id}`)
}

// ============================================================
// ===== 3.3 获取新闻分类 =====
// ============================================================

export function getNewsCategories() {
  return get<Result<NewsCategory[]>>('/news/categories')
}

// ============================================================
// ===== 3.4 收藏新闻 =====
// ============================================================

export function favoriteNews(id: number) {
  return post<Result<FavoriteResponse>>(`/news/favorite/${id}`)
}

// ============================================================
// ===== 3.5 取消收藏 =====
// ============================================================

export function unfavoriteNews(id: number) {
  return del<Result<null>>(`/news/favorite/${id}`)
}

// ============================================================
// ===== 3.6 获取收藏列表 =====
// ============================================================

export function getFavoriteList(params: { page?: number; size?: number }) {
  return get<Result<FavoriteListResponse>>('/news/favorites', params)
}

// ============================================================
// ===== 3.7 后台发布新闻（管理员） =====
// ============================================================

export function publishNews(data: PublishNewsParams) {
  return post<Result<PublishNewsResponse>>('/admin/news', data)
}

// ============================================================
// ===== 下线新闻 =====
// ============================================================

export function offlineNews(id: number) {
  return put<Result<null>>(`/news/${id}/offline`)
}

// ============================================================
// ===== 删除新闻 =====
// ============================================================

export function deleteNews(id: number) {
  return del<Result<null>>(`/news/${id}`)
}
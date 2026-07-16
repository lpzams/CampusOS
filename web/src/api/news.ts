/**
 * 新闻模块 API —— 一个功能一个文件，集中放该功能的接口函数和类型。
 *
 * 每个函数对应后端 NewsController 的一个端点，类型对应后端的 DTO / Query / Command，
 * 改后端字段时记得同步这里。
 *
 * 【新增功能时】照抄本文件：建 api/你的功能.ts，
 * 把 URL 前缀换成 /你的功能，类型对着你后端的 DTO 写一遍。
 */
import { del, get, post, put } from '@/utils/request'
import type { PageQuery, PageResult } from '@/api/types'

/** 新闻栏目的固定取值（与后端、数据库中存的字符串一致，别改字面量） */
export const NEWS_CATEGORIES = ['校园新闻', '学院动态', '通知公告', '政策文件'] as const

/** 一条新闻（对应后端 NewsDTO.java） */
export interface NewsItem {
  id: number
  title: string
  content: string
  author: string
  category: string
  viewCount: number
  published: boolean
  /** ISO 时间字符串，如 "2026-07-16T10:30:00"，展示用 utils/format 转换 */
  createdAt: string | null
  publishedAt: string | null
}

/** 分页查询条件（对应后端 NewsPageQuery.java），两个筛选项都可不传 */
export interface NewsPageQuery extends PageQuery {
  /** 标题关键字，模糊匹配 */
  keyword?: string
  /** 栏目，精确匹配 */
  category?: string
}

/** 发布新闻表单（对应后端 CreateNewsCommand.java，校验规则见后端注解） */
export interface CreateNewsForm {
  title: string
  content: string
  author: string
  category: string
}

/** 分页查询已发布新闻：GET /api/news?pageNum=1&pageSize=10&keyword=&category= */
export function pageNews(query: NewsPageQuery) {
  return get<PageResult<NewsItem>>('/news', query)
}

/** 新闻详情（后端会自动 +1 浏览量）：GET /api/news/{id} */
export function getNewsDetail(id: number | string) {
  return get<NewsItem>(`/news/${id}`)
}

/** 发布新闻（后台管理用），返回新闻 id：POST /api/news */
export function createNews(data: CreateNewsForm) {
  return post<number>('/news', data)
}

/** 下线新闻（后台管理用）：PUT /api/news/{id}/offline */
export function offlineNews(id: number) {
  return put<void>(`/news/${id}/offline`)
}

/** 删除新闻（后端为逻辑删除）：DELETE /api/news/{id} */
export function deleteNews(id: number) {
  return del<void>(`/news/${id}`)
}

/**
 * 新闻模块 API（小程序端）—— 与网站端 web/src/api/news.ts 调同一批后端接口。
 *
 * 小程序端只做浏览（列表 + 详情）；发布/下线/删除是管理操作，只在网站后台做。
 *
 * 【新增功能时】照抄本文件：建 api/你的功能.js，URL 前缀换成 /你的功能。
 */
import { get } from '@/utils/request'

/**
 * 分页查询新闻：GET /api/news/list
 * @param {{page?:number, size?:number, keyword?:string, categoryId?:number|string}} query
 * @returns {Promise<{total:number,list:Array}>}
 */
export function pageNews(query) {
  return get('/news/list', query)
}

/**
 * 新闻详情：GET /api/news/detail/{id}
 * @param {number|string} id
 */
export function getNewsDetail(id) {
  return get(`/news/detail/${id}`)
}

/** 新闻分类：GET /api/news/categories */
export function getNewsCategories() {
  return get('/news/categories')
}

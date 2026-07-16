/**
 * 新闻模块 API（小程序端）—— 与网站端 web/src/api/news.ts 调同一批后端接口。
 *
 * 小程序端只做浏览（列表 + 详情）；发布/下线/删除是管理操作，只在网站后台做。
 *
 * 【新增功能时】照抄本文件：建 api/你的功能.js，URL 前缀换成 /你的功能。
 */
import { get } from '@/utils/request'

/** 新闻栏目固定取值（与后端、数据库存的字符串一致） */
export const NEWS_CATEGORIES = ['校园新闻', '学院动态', '通知公告', '政策文件']

/**
 * 分页查询已发布新闻：GET /api/news
 * @param {{pageNum?:number, pageSize?:number, keyword?:string, category?:string}} query
 * @returns {Promise<{pageNum:number,pageSize:number,total:number,pages:number,list:Array}>}
 */
export function pageNews(query) {
  return get('/news', query)
}

/**
 * 新闻详情（后端会自动 +1 浏览量）：GET /api/news/{id}
 * @param {number|string} id
 */
export function getNewsDetail(id) {
  return get(`/news/${id}`)
}

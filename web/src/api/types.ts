/**
 * 与后端约定的「通用返回结构」类型定义。
 *
 * 一一对应 backend/campus-common 里的三个类：
 *   Result.java / PageResult.java / PageQuery.java
 *
 * 【新增功能时】不用改这里；你功能自己的 DTO 类型写在 api/你的功能.ts 里。
 */

/** 统一返回结构（对应 Result.java）。code=200 成功，其余失败，msg 为提示语。 */
export interface Result<T = unknown> {
  code: number
  msg: string
  data: T
}

/** 统一分页结构（对应 PageResult.java）。所有分页接口都长这样。 */
export interface PageResult<T> {
  /** 当前页码，从 1 开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
  /** 当前页数据 */
  list: T[]
}

/** 分页查询参数基类（对应 PageQuery.java）。各功能的查询参数 extends 它。 */
export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

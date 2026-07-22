/**
 * AI 智慧校园助手 API（功能 15）。
 *
 * 对应后端 AssistantController（/api/ai/**）。
 * 问答/推荐需要登录；热门问题、办事流程公开。
 * 后端目前是本地 FAQ 规则回答（不接大模型），前端按普通接口调用即可。
 */
import { get, post } from '@/utils/request'

/** 办事流程类型（与后端约定的 type 取值） */
export const PROCESS_TYPES = [
  { code: 'LIBRARY', name: '图书馆' },
  { code: 'CARD', name: '校园卡' },
  { code: 'DORMITORY', name: '宿舍' },
  { code: 'EXAM', name: '考试' },
] as const

/** 智能推荐类型 */
export const RECOMMEND_TYPES = [
  { code: 'COURSE', name: '课程' },
  { code: 'ACTIVITY', name: '活动' },
  { code: 'CANTEEN', name: '食堂' },
] as const

/** 一次问答的回复 */
export interface ChatReply {
  answer: string
  relatedQuestions?: string[]
}

export interface ChatImage { mediaType: string; data: string }
export interface ChatMessage { role: 'user' | 'assistant'; content: string }

/** 热门问题 */
export interface HotQuestion {
  id: number
  question: string
  answer: string
  category?: string
  [key: string]: unknown
}

/** 办事流程 */
export interface ProcessGuide {
  title: string
  steps: string[]
}

/** 一条推荐 */
export interface RecommendItem {
  id: number
  type?: string
  title?: string
  reason?: string
  [key: string]: unknown
}

/** AI 问答（需登录）：POST /api/ai/chat */
export function chat(question: string, history: ChatMessage[] = [], images: ChatImage[] = []) {
  return post<ChatReply>('/ai/chat', { question, history, images }, { timeout: 100000 })
}

/** 热门问题：GET /api/ai/hot-questions */
export function getHotQuestions() {
  return get<HotQuestion[]>('/ai/hot-questions')
}

/** 办事流程查询：POST /api/ai/process */
export function getProcess(data: { type: string; action: string }) {
  return post<ProcessGuide>('/ai/process', data)
}

/** 智能推荐（需登录）：GET /api/ai/recommend?type=ACTIVITY */
export function getRecommend(type: string) {
  return get<RecommendItem[]>('/ai/recommend', { type })
}

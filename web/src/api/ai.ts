/**
 * AI智慧助手模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  AIChatParams,
  AIChatResponse,
  HotQuestionsResponse,
} from './types'

// ============================================================
// ===== 15.1 AI问答 =====
// ============================================================

export function aiChat(data: AIChatParams) {
  return post<Result<AIChatResponse>>('/ai/chat', data)
}

// ============================================================
// ===== 15.2 获取热门问题 =====
// ============================================================

export function getHotQuestions() {
  return get<Result<HotQuestionsResponse>>('/ai/hot-questions')
}
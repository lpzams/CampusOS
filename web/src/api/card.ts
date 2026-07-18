/**
 * 校园卡服务模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  CardInfo,
  TransactionListParams,
  TransactionListResponse,
  CardLossResponse,
  CardRechargeParams,
  CardRechargeResponse,
} from './types'

// ============================================================
// ===== 9.1 获取校园卡信息 =====
// ============================================================

export function getCardInfo() {
  return get<Result<CardInfo>>('/card/info')
}

// ============================================================
// ===== 9.2 获取消费记录 =====
// ============================================================

export function getTransactions(params: TransactionListParams) {
  return get<Result<TransactionListResponse>>('/card/transactions', params)
}

// ============================================================
// ===== 9.3 校园卡挂失 =====
// ============================================================

export function lossCard() {
  return post<Result<CardLossResponse>>('/card/loss')
}

// ============================================================
// ===== 9.4 校园卡解挂 =====
// ============================================================

export function unlossCard() {
  return post<Result<CardLossResponse>>('/card/unloss')
}

// ============================================================
// ===== 9.5 校园卡充值 =====
// ============================================================

export function rechargeCard(params: CardRechargeParams) {
  return post<Result<CardRechargeResponse>>('/card/recharge', params)
}
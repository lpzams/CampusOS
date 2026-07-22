/**
 * 校园卡服务 API（功能 9）。
 *
 * 对应后端 CardController（/api/card/**，全部需要登录）。
 * 挂失/解挂/充值的状态规则在后端领域对象 CampusCard 里（如挂失状态不能充值）。
 */
import { get, post } from '@/utils/request'

/** 校园卡信息 */
export interface CardInfo {
  id: number
  cardId: string
  userId?: number
  realName?: string
  balance: number
  /** 正常 / 挂失 / 冻结 */
  status: string
  expireTime?: string
  [key: string]: unknown
}

/** 一条消费/充值流水 */
export interface CardTransaction {
  id: number
  /** 如"食堂消费"、"充值" */
  type?: string
  /** 消费为负数、充值为正数 */
  amount?: number
  merchant?: string
  payMethod?: string
  status?: string
  createTime?: string
  [key: string]: unknown
}

/** 校园卡信息：GET /api/card/info */
export function getCardInfo() {
  return get<CardInfo>('/card/info')
}

/** 消费记录：GET /api/card/transactions */
export function getCardTransactions() {
  return get<CardTransaction[]>('/card/transactions')
}

/** 挂失（返回更新后的卡信息）：POST /api/card/loss */
export function reportLoss() {
  return post<CardInfo>('/card/loss')
}

/** 解挂：POST /api/card/unloss */
export function cancelLoss() {
  return post<CardInfo>('/card/unloss')
}

/** 充值（payMethod 取值见 api/payment.ts 的 PAY_METHODS）：POST /api/card/recharge */
export function rechargeCard(data: { amount: number; payMethod: string }) {
  return post<CardInfo>('/card/recharge', data)
}

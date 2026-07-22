/**
 * 校园缴费 API（功能 8）。
 *
 * 对应后端 PaymentController（/api/payment/**，全部需要登录）。
 * 支付是演示流程：创建订单即返回结果，不接真实的微信/支付宝。
 */
import { get, post } from '@/utils/request'

/** 支付方式的固定取值（与后端 PaymentOrder.Method 枚举一致） */
export const PAY_METHODS = [
  { code: 'WECHAT', name: '微信支付' },
  { code: 'ALIPAY', name: '支付宝' },
] as const

/** 一条待缴费项目 */
export interface PaymentItem {
  id: number
  /** 学费/住宿费… */
  type?: string
  amount?: number
  deadline?: string
  status?: string
  description?: string
  [key: string]: unknown
}

/** 一条缴费订单（缴费记录） */
export interface PaymentOrder {
  id: number
  paymentId?: number
  type?: string
  dormitoryId?: string
  amount?: number
  /** WECHAT / ALIPAY */
  payMethod?: string
  status?: string
  createTime?: string
  [key: string]: unknown
}

/** 待缴费列表：GET /api/payment/pending */
export function getPendingPayments() {
  return get<PaymentItem[]>('/payment/pending')
}

/** 创建缴费订单：POST /api/payment/order */
export function createPaymentOrder(data: { paymentId: number; payMethod: string }) {
  return post<PaymentOrder>('/payment/order', data)
}

/** 缴费记录：GET /api/payment/records */
export function getPaymentRecords() {
  return get<PaymentOrder[]>('/payment/records')
}

/** 电费充值：POST /api/payment/electricity */
export function rechargeElectricity(data: { dormitoryId: string; amount: number; payMethod: string }) {
  return post<PaymentOrder>('/payment/electricity', data)
}

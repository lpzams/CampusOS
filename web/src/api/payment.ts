/**
 * 校园缴费模块 API
 */
import { get, post } from '../utils/request'
import type {
  Result,
  PendingPayment,
  CreateOrderParams,
  CreateOrderResponse,
  PaymentRecordsResponse,
  ElectricityRechargeParams,
  ElectricityRechargeResponse,
} from './types'

// ============================================================
// ===== 8.1 获取待缴费列表 =====
// ============================================================

export function getPendingPayments() {
  return get<Result<PendingPayment[]>>('/payment/pending')
}

// ============================================================
// ===== 8.2 创建缴费订单 =====
// ============================================================

export function createOrder(params: CreateOrderParams) {
  return post<Result<CreateOrderResponse>>('/payment/order', params)
}

// ============================================================
// ===== 8.3 获取缴费记录 =====
// ============================================================

export function getPaymentRecords(params?: { page?: number; size?: number }) {
  return get<Result<PaymentRecordsResponse>>('/payment/records', params)
}

// ============================================================
// ===== 8.4 电费充值 =====
// ============================================================

export function rechargeElectricity(params: ElectricityRechargeParams) {
  return post<Result<ElectricityRechargeResponse>>('/payment/electricity', params)
}
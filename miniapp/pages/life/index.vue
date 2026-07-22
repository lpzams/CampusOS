<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { cancelCardLoss, createPaymentOrder, getCardInfo, getCardTransactions, getDormitoryInfo, getDormitoryNotices, getDormitoryUtility, getPaymentRecords, getPendingPayments, rechargeCard, rechargeElectricity, reportCardLoss } from '@/api/campus'

const tab = ref('payment'); const primary = ref(null); const secondary = ref(null)
const form = reactive({ paymentId: '', amount: '', dormitoryId: '', payMethod: 'WECHAT' })
const tabs = [['payment','缴费'],['card','校园卡'],['dorm','宿舍']]
async function load() {
  if (!uni.getStorageSync('token')) return uni.navigateTo({ url: '/pages/auth/index' })
  secondary.value = null
  if (tab.value === 'payment') primary.value = await getPendingPayments()
  if (tab.value === 'card') primary.value = await getCardInfo()
  if (tab.value === 'dorm') primary.value = await getDormitoryInfo()
}
async function action(name, id) {
  if (name === 'pay') secondary.value = await createPaymentOrder({ paymentId: id || form.paymentId, payMethod: form.payMethod })
  if (name === 'records') secondary.value = await getPaymentRecords()
  if (name === 'electricity') secondary.value = await rechargeElectricity({ dormitoryId: form.dormitoryId, amount: Number(form.amount), payMethod: form.payMethod })
  if (name === 'transactions') secondary.value = await getCardTransactions()
  if (name === 'recharge') secondary.value = await rechargeCard({ amount: Number(form.amount), payMethod: form.payMethod })
  if (name === 'loss') secondary.value = await reportCardLoss()
  if (name === 'unloss') secondary.value = await cancelCardLoss()
  if (name === 'utility') secondary.value = await getDormitoryUtility()
  if (name === 'notices') secondary.value = await getDormitoryNotices()
  uni.showToast({ title: '操作成功' }); if (['pay','recharge','loss','unloss'].includes(name)) load()
}
function select(value){tab.value=value;load()}
onLoad((options)=>{if(options.tab)tab.value=options.tab;load()})
</script>
<template>
  <view class="screen">
    <view class="tabs"><view v-for="item in tabs" :key="item[0]" class="tab" :class="{active:tab===item[0]}" @click="select(item[0])">{{ item[1] }}</view></view>
    <picker v-if="tab==='payment' || tab==='card'" class="pay-method" :range="['微信支付','支付宝']" @change="form.payMethod=['WECHAT','ALIPAY'][$event.detail.value]"><view class="field picker"><text class="muted">支付方式</text><text>{{ form.payMethod==='ALIPAY'?'支付宝':'微信支付' }}</text></view></picker>
    <template v-if="tab==='payment'"><view v-for="item in (primary||[])" :key="item.id" class="list-item"><view class="item-title">{{ item.description || item.type }}</view><view class="amount">¥{{ item.amount }}</view><view class="item-meta"><text>{{ item.status }}</text><text>截止 {{ item.deadline }}</text></view><button class="primary-btn mini" @click="action('pay',item.id)">立即缴费</button></view><view class="panel"><input v-model="form.dormitoryId" class="field" placeholder="宿舍号，如 6-302" /><input v-model="form.amount" class="field" type="digit" placeholder="充值金额" /><view class="row"><button class="secondary-btn" @click="action('electricity')">电费充值</button><button class="secondary-btn" @click="action('records')">缴费记录</button></view></view></template>
    <template v-if="tab==='card' && primary"><view class="card"><text class="card-name">校园一卡通</text><text class="balance">¥{{ primary.balance }}</text><text>{{ primary.realName }} · {{ primary.cardId }}</text><text>{{ primary.status }}</text></view><view class="panel"><input v-model="form.amount" class="field" type="digit" placeholder="充值金额" /><button class="primary-btn" @click="action('recharge')">校园卡充值</button><view class="row extra"><button class="secondary-btn" @click="action('transactions')">消费记录</button><button class="danger-btn" @click="action('loss')">挂失</button><button class="secondary-btn" @click="action('unloss')">解挂</button></view></view></template>
    <template v-if="tab==='dorm' && primary"><view class="panel"><view class="section-title">{{ primary.building }}{{ primary.room }}</view><view class="item-meta"><text>{{ primary.type }}</text><text>电费 {{ primary.electricityBalance }}</text><text>水费 {{ primary.waterBalance }}</text></view><view v-for="member in primary.members" :key="member.userId" class="member">{{ member.realName }} <text class="muted">{{ member.phone }} {{ member.isRoomLeader ? '寝室长' : '' }}</text></view></view><view class="row"><button class="secondary-btn" @click="action('utility')">水电余额</button><button class="secondary-btn" @click="action('notices')">宿舍公告</button></view></template>
    <view v-if="secondary" class="panel output"><view class="section-title">查询结果</view><view v-for="(item,index) in (Array.isArray(secondary)?secondary:[secondary])" :key="index" class="member">{{ item.description || item.type || item.title || item.orderNo || item.status || '操作成功' }} <text class="muted">{{ item.amount || item.balance || item.createTime || '' }}</text></view></view>
  </view>
</template>
<style scoped lang="scss">.pay-method{display:block;margin-bottom:20rpx}.pay-method .picker{display:flex;align-items:center;justify-content:space-between;margin:0;line-height:76rpx}.amount{margin:12rpx 0;color:#f56c6c;font-size:38rpx;font-weight:700}.mini{margin-top:18rpx}.card{display:flex;flex-direction:column;min-height:270rpx;margin-bottom:20rpx;padding:30rpx;border-radius:24rpx;background:linear-gradient(135deg,#241457,#7c5cd6 65%,#b06fd0);color:#fff;gap:10rpx;box-shadow:0 12rpx 32rpx rgba(36,20,87,.25)}.card-name{color:#e3d8f7}.balance{margin:12rpx 0;font-size:52rpx;font-weight:700}.extra{margin-top:16rpx}.member{padding:16rpx 0;border-bottom:1rpx solid #ebeef5}</style>

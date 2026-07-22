<script setup>
import { reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getRepairs, submitRepair } from '@/api/campus'
import { formatDateTime } from '@/utils/format'
const tab=ref('list');const list=ref([]);const form=reactive({type:'水电',title:'',description:'',building:'',room:'',contactPhone:'',imageUrls:''})
async function load(){if(!uni.getStorageSync('token'))return uni.navigateTo({url:'/pages/auth/index'});list.value=await getRepairs()}
async function submit(){await submitRepair({...form,images:form.imageUrls.split(',').map(v=>v.trim()).filter(Boolean)});uni.showToast({title:'报修已提交'});tab.value='list';load()}
onLoad(load);onShow(()=>{if(uni.getStorageSync('token'))load()})
</script>
<template><view class="screen"><view class="tabs"><view class="tab" :class="{active:tab==='list'}" @click="tab='list'">我的报修</view><view class="tab" :class="{active:tab==='submit'}" @click="tab='submit'">新建报修</view></view><template v-if="tab==='list'"><view v-for="item in list" :key="item.id" class="list-item" @click="uni.navigateTo({url:`/pages/detail/index?type=repair&id=${item.id}`})"><view class="item-title">{{ item.title }}</view><view class="item-meta"><text>{{ item.type }}</text><text>{{ item.status }}</text><text>{{ formatDateTime(item.createTime) }}</text></view><view class="muted note">{{ item.statusDesc }}</view></view><view v-if="!list.length" class="empty-state">暂无报修记录</view></template><view v-else class="panel"><picker :range="['水电','家具','设备','网络','其他']" @change="form.type=['水电','家具','设备','网络','其他'][$event.detail.value]"><view class="field picker">类型：{{ form.type }}</view></picker><input v-model="form.title" class="field" placeholder="故障标题" /><textarea v-model="form.description" class="textarea" placeholder="详细描述"/><input v-model="form.building" class="field" placeholder="楼栋"/><input v-model="form.room" class="field" placeholder="房间"/><input v-model="form.contactPhone" class="field" type="number" placeholder="联系电话"/><input v-model="form.imageUrls" class="field" placeholder="图片 URL，多个用逗号分隔（可选）"/><button class="primary-btn submit" @click="submit">提交报修</button></view></view></template>
<style scoped lang="scss">.picker{line-height:76rpx}.note{margin-top:12rpx}.submit{margin-top:18rpx}</style>

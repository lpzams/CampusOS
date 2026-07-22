<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ChatDotRound, Search, Star, User } from '@element-plus/icons-vue'
import { getReviewCatalog, getCourseReviews } from '@/api/courseReview'
import type { CourseCatalogItem, CourseReviewItem } from '@/api/courseReview'

const route = useRoute()
const loading = ref(false)
const reviewLoading = ref(false)
const keyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const sortBy = ref<'rating' | 'reviews'>('reviews')
const page = ref(1)
const pageSize = 20
const catalog = ref<CourseCatalogItem[]>([])
const current = ref<CourseCatalogItem | null>(null)
const reviews = ref<CourseReviewItem[]>([])
const dialogVisible = ref(false)
const reviewFilter = ref<'all' | 'positive' | 'negative'>('all')
const displayedCatalog = computed(() => [...catalog.value].sort((a, b) => sortBy.value === 'rating'
  ? (b.avgOverall ?? 0) - (a.avgOverall ?? 0) : b.reviewCount - a.reviewCount))
const positiveCount = computed(() => reviews.value.filter(review => (review.overall ?? 0) >= 12).length)
const negativeCount = computed(() => reviews.value.filter(review => review.overall != null && review.overall < 12).length)
const displayedReviews = computed(() => reviews.value.filter(review => reviewFilter.value === 'all'
  || (reviewFilter.value === 'positive' ? (review.overall ?? 0) >= 12 : review.overall != null && review.overall < 12)))

function ratingPercent(value?: number) { return Math.round(((value ?? 0) / 5) * 100) }
function formatTags(tags?: string) { return tags?.split(/[，,、|]/).map(tag => tag.trim()).filter(Boolean) ?? [] }

async function loadCatalog(reset = false) {
  if (reset) page.value = 1
  loading.value = true
  try { catalog.value = await getReviewCatalog({ keyword: keyword.value || undefined, page: page.value, size: pageSize }) } finally { loading.value = false }
}

async function openReviews(course: CourseCatalogItem) {
  current.value = course
  dialogVisible.value = true
  reviewLoading.value = true
  reviewFilter.value = 'all'
  reviews.value = []
  try {
    const pages = Math.max(1, Math.ceil(course.reviewCount / 100))
    reviews.value = (await Promise.all(Array.from({ length: pages }, (_, index) =>
      getCourseReviews(course.sourceId, { page: index + 1, size: 100 })))).flat()
  } catch { /* request.ts 已统一提示 */ } finally { reviewLoading.value = false }
}

async function changePage(delta: number) {
  const next = page.value + delta
  if (next < 1) return
  page.value = next
  await loadCatalog()
  if (!catalog.value.length && page.value > 1) { page.value -= 1; await loadCatalog() }
}

watch(() => route.query.keyword, (value) => { if (typeof value === 'string' && value !== keyword.value) { keyword.value = value; loadCatalog(true) } })
onMounted(loadCatalog)
</script>

<template>
  <main class="review-workspace">
    <header class="workspace-header"><div><p class="kicker">COURSE INTELLIGENCE</p><h1>课程评价</h1><p class="intro">用聚合评分和匿名评价，快速建立对课程与教师的第一判断。</p></div><router-link to="/course-selection" class="text-link">进入课程选择</router-link></header>
    <section class="search-console"><el-icon><Search /></el-icon><el-input v-model="keyword" aria-label="搜索课程或教师" clearable placeholder="搜索课程名或教师" @keyup.enter="loadCatalog(true)" @clear="loadCatalog(true)" /><el-select v-model="sortBy" aria-label="排序方式" class="sort-select"><el-option label="评价数量优先" value="reviews" /><el-option label="综合评分优先" value="rating" /></el-select><el-button type="primary" :loading="loading" @click="loadCatalog(true)">检索评价</el-button></section>
    <div class="result-bar"><span>课程情报目录</span><small>评分满分 15 分，由三项维度聚合</small></div>
    <div v-loading="loading" class="review-grid"><el-empty v-if="!loading && !displayedCatalog.length" description="没有匹配的课程，请尝试其他关键词" /><article v-for="course in displayedCatalog" :key="course.sourceId" class="review-card"><div class="card-top"><div class="course-identity"><span>{{ course.courseName.slice(0, 1) }}</span><div><h2>{{ course.courseName }}</h2><p><el-icon><User /></el-icon>{{ course.professor }}</p></div></div><div class="overall"><strong>{{ course.avgOverall?.toFixed(2) ?? '--' }}</strong><small>/ 15</small></div></div><div class="dimension-grid"><div><span>讲授</span><el-progress :percentage="ratingPercent(course.avgRate1)" :show-text="false" :stroke-width="6" color="#0b8f82" /></div><div><span>收获</span><el-progress :percentage="ratingPercent(course.avgRate2)" :show-text="false" :stroke-width="6" color="#2d7aae" /></div><div><span>体验</span><el-progress :percentage="ratingPercent(course.avgRate3)" :show-text="false" :stroke-width="6" color="#c56b43" /></div></div><div class="card-bottom"><span><el-icon><ChatDotRound /></el-icon>{{ course.reviewCount }} 条已审核评价</span><el-button type="primary" plain @click="openReviews(course)">阅读评价</el-button></div></article></div>
    <nav class="pager" aria-label="课程页码"><el-button :disabled="page === 1 || loading" @click="changePage(-1)">上一页</el-button><span>第 {{ page }} 页</span><el-button :disabled="catalog.length < pageSize || loading" @click="changePage(1)">下一页</el-button></nav>
    <el-dialog v-model="dialogVisible" class="review-dialog" :title="current ? `${current.courseName} · ${current.professor}` : '课程评价'" width="min(820px, 94vw)"><section v-if="current" class="dialog-summary"><div><span>综合评分</span><strong>{{ current.avgOverall?.toFixed(2) ?? '--' }} <small>/ 15</small></strong></div><div><span>已审核评价</span><strong>{{ current.reviewCount }} <small>条</small></strong></div><p>评价仅用于课程选择参考，已隐藏所有作者身份信息。</p></section><nav class="review-filter" aria-label="评价筛选"><button :class="{ active: reviewFilter === 'all' }" @click="reviewFilter = 'all'">全部 <span>{{ reviews.length }}</span></button><button :class="{ active: reviewFilter === 'positive' }" @click="reviewFilter = 'positive'">好评 <span>{{ positiveCount }}</span></button><button :class="{ active: reviewFilter === 'negative' }" @click="reviewFilter = 'negative'">差评 <span>{{ negativeCount }}</span></button><small>12分及以上为好评</small></nav><div v-loading="reviewLoading" class="review-list"><el-empty v-if="!reviewLoading && !displayedReviews.length" :description="reviews.length ? '该分类下暂无评价' : '暂无评价'" /><article v-for="review in displayedReviews" :key="review.sourceId" class="review-item" :class="(review.overall ?? 0) >= 12 ? 'positive' : 'negative'"><div class="review-top"><span class="score-badge"><el-icon><Star /></el-icon><strong>{{ review.overall ?? '--' }}</strong><small>/ 15</small></span><time>{{ review.sourceCreatedAt?.slice(0, 10) || '历史评价' }}</time></div><p>{{ review.comment }}</p><div v-if="formatTags(review.tags).length" class="tag-list"><el-tag v-for="tag in formatTags(review.tags)" :key="tag" effect="plain" size="small">{{ tag }}</el-tag></div></article></div></el-dialog>
  </main>
</template>

<style scoped>
.review-workspace { display: grid; gap: 18px; color: #26343b; }.workspace-header { display: flex; align-items: end; justify-content: space-between; gap: 20px; padding: 4px 2px; }.kicker { margin: 0 0 7px; color: #c26743; font-size: 12px; font-weight: 700; letter-spacing: 1.6px; }h1 { margin: 0; font-size: 28px; line-height: 1.15; }.intro { margin: 8px 0 0; color: #69777e; }.text-link { color: #0b766d; font-weight: 600; text-decoration: none; white-space: nowrap; }.search-console { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border: 1px solid #dbe6e5; background: #fff; }.search-console > .el-icon { color: #c26743; font-size: 19px; }.search-console :deep(.el-input__wrapper) { box-shadow: none; }.sort-select { width: 155px; flex-shrink: 0; }.result-bar { display: flex; align-items: baseline; justify-content: space-between; }.result-bar span { font-size: 16px; font-weight: 700; }.result-bar small { color: #77858b; }.review-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; min-height: 350px; }.review-card { display: grid; gap: 18px; padding: 17px; border: 1px solid #dbe6e5; background: #fff; transition: border-color .2s, transform .2s, box-shadow .2s; }.review-card:hover { border-color: #d7a087; transform: translateY(-2px); box-shadow: 0 10px 22px #c2674312; }.card-top { display: flex; justify-content: space-between; gap: 12px; }.course-identity { display: flex; gap: 10px; min-width: 0; }.course-identity > span { display: grid; place-items: center; flex: 0 0 auto; width: 38px; height: 38px; color: #a15031; background: #faeee8; font-weight: 700; }.course-identity h2 { overflow: hidden; margin: 0; font-size: 16px; text-overflow: ellipsis; white-space: nowrap; }.course-identity p { display: flex; align-items: center; gap: 4px; margin: 5px 0 0; color: #738188; font-size: 13px; }.overall { text-align: right; white-space: nowrap; }.overall strong { color: #b65e3c; font-size: 23px; }.overall small { color: #7d8a90; font-size: 11px; }.dimension-grid { display: grid; gap: 9px; }.dimension-grid > div { display: grid; grid-template-columns: 31px 1fr; align-items: center; gap: 8px; }.dimension-grid span { color: #75838a; font-size: 12px; }.card-bottom { display: flex; align-items: center; justify-content: space-between; gap: 8px; }.card-bottom > span { display: flex; align-items: center; gap: 4px; color: #748187; font-size: 12px; }.pager { display: flex; justify-content: flex-end; align-items: center; gap: 12px; color: #6d7b81; font-size: 13px; }.dialog-summary { display: grid; grid-template-columns: 140px 140px 1fr; gap: 16px; align-items: center; padding: 18px; border-radius: 12px; background: linear-gradient(120deg, #f8f4ff, #fff8f4); }.dialog-summary span, .dialog-summary strong { display: block; }.dialog-summary span { color: #748187; font-size: 12px; }.dialog-summary strong { margin-top: 3px; color: #342654; font-size: 24px; }.dialog-summary strong small { color: #7e8b90; font-size: 11px; font-weight: 500; }.dialog-summary p { margin: 0; color: #718087; font-size: 12px; line-height: 1.6; }.review-filter { display: flex; align-items: center; gap: 8px; padding-top: 16px; }.review-filter button { padding: 7px 13px; border: 1px solid #e3dceb; border-radius: 999px; color: #696177; background: #fff; cursor: pointer; }.review-filter button.active { border-color: #7c5cd6; color: #fff; background: #7c5cd6; }.review-filter button span { margin-left: 3px; opacity: .75; }.review-filter > small { margin-left: auto; color: #9a929f; }.review-list { display: grid; gap: 12px; min-height: 180px; max-height: min(58vh, 620px); padding-top: 14px; overflow-y: auto; }.review-item { padding: 16px 18px; border: 1px solid #e5e0e9; border-left: 4px solid #52a88d; border-radius: 10px; background: #fff; }.review-item.negative { border-left-color: #d77964; }.review-top { display: flex; align-items: center; justify-content: space-between; color: #918a98; font-size: 12px; }.score-badge { display: inline-flex; align-items: baseline; gap: 4px; color: #21806b; }.negative .score-badge { color: #c35d49; }.score-badge strong { font-size: 19px; }.score-badge small { font-size: 11px; }.review-item p { margin: 12px 0 2px; color: #37323e; font-size: 14px; line-height: 1.85; white-space: pre-wrap; }.tag-list { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 12px; }@media (max-width: 900px) { .review-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }}@media (max-width: 640px) { .workspace-header { align-items: start; flex-direction: column; }.search-console { align-items: stretch; flex-wrap: wrap; }.search-console :deep(.el-input) { width: calc(100% - 30px); }.sort-select { flex: 1; }.review-grid { grid-template-columns: 1fr; }.result-bar { align-items: start; flex-direction: column; gap: 4px; }.dialog-summary { grid-template-columns: 1fr 1fr; }.dialog-summary p { grid-column: 1 / -1; }.review-filter { flex-wrap: wrap; }.review-filter > small { width: 100%; margin: 0; }}
</style>

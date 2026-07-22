<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, ChatDotRound, Search, Star, User } from '@element-plus/icons-vue'
import { getReviewCatalog, getSelectedCourses, selectCourse, cancelCourseSelection } from '@/api/courseReview'
import type { CourseCatalogItem, SelectedCourse } from '@/api/courseReview'

const loading = ref(false)
const keyword = ref('')
const sortBy = ref<'rating' | 'reviews'>('rating')
const courses = ref<CourseCatalogItem[]>([])
const selected = ref<SelectedCourse[]>([])
const page = ref(1)
const pageSize = 20

const selectedIds = computed(() => new Set(selected.value.map(item => item.sourceId)))
const displayedCourses = computed(() => [...courses.value].sort((a, b) => sortBy.value === 'rating'
  ? (b.avgOverall ?? 0) - (a.avgOverall ?? 0)
  : b.reviewCount - a.reviewCount))
const averageRating = computed(() => {
  if (!courses.value.length) return '--'
  return (courses.value.reduce((sum, course) => sum + (course.avgOverall ?? 0), 0) / courses.value.length).toFixed(1)
})

function ratingPercent(course: CourseCatalogItem) { return Math.round(((course.avgOverall ?? 0) / 15) * 100) }
function initials(course: CourseCatalogItem) { return course.courseName.slice(0, 1) || '课' }

async function loadCatalog(reset = false) {
  if (reset) page.value = 1
  loading.value = true
  try {
    courses.value = await getReviewCatalog({ keyword: keyword.value || undefined, page: page.value, size: pageSize })
  } finally {
    loading.value = false
  }
}

async function loadSelected() { selected.value = await getSelectedCourses() }

async function select(item: CourseCatalogItem) {
  await selectCourse(item.sourceId)
  await loadSelected()
  ElMessage.success(`已加入 ${item.courseName}`)
}

async function cancel(item: SelectedCourse) {
  await cancelCourseSelection(item.sourceId)
  await loadSelected()
  ElMessage.success(`已取消 ${item.courseName}`)
}

async function changePage(delta: number) {
  const next = page.value + delta
  if (next < 1) return
  page.value = next
  await loadCatalog()
  if (!courses.value.length && page.value > 1) { page.value -= 1; await loadCatalog() }
}

onMounted(async () => { await Promise.all([loadCatalog(), loadSelected()]) })
</script>

<template>
  <main class="selection-workspace">
    <header class="workspace-header">
      <div>
        <p class="kicker">ACADEMIC DECISION DESK</p>
        <h1>课程选择</h1>
        <p class="intro">用公开评价数据，快速找到值得投入时间的课程与教师。</p>
      </div>
      <router-link to="/course-reviews" class="text-link">进入评价库</router-link>
    </header>

    <section class="metrics" aria-label="课程目录概览">
      <div class="metric"><span>当前结果</span><strong>{{ courses.length }}</strong><small>门课程</small></div>
      <div class="metric"><span>我的选择</span><strong>{{ selected.length }}</strong><small>门课程</small></div>
      <div class="metric"><span>平均评分</span><strong>{{ averageRating }}</strong><small>/ 15</small></div>
      <div class="metric source"><span>数据状态</span><strong>已同步</strong><small>公开审核评价</small></div>
    </section>

    <section class="search-console">
      <el-icon><Search /></el-icon>
      <el-input v-model="keyword" aria-label="搜索课程或教师" clearable placeholder="按课程名或教师搜索" @keyup.enter="loadCatalog(true)" @clear="loadCatalog(true)" />
      <el-select v-model="sortBy" aria-label="排序方式" class="sort-select">
        <el-option label="综合评分优先" value="rating" />
        <el-option label="评价数量优先" value="reviews" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="loadCatalog(true)">查找课程</el-button>
    </section>

    <div class="workspace-grid">
      <section class="catalog-section">
        <div class="section-label"><span>课程目录</span><small>第 {{ page }} 页 · 每页 {{ pageSize }} 条</small></div>
        <div v-loading="loading" class="course-grid">
          <el-empty v-if="!loading && !displayedCourses.length" description="没有匹配的课程，请尝试其他关键词" />
          <article v-for="course in displayedCourses" :key="course.sourceId" class="course-tile">
            <div class="tile-top">
              <span class="course-mark">{{ initials(course) }}</span>
              <div class="course-title"><h2>{{ course.courseName }}</h2><p><el-icon><User /></el-icon>{{ course.professor }}</p></div>
              <span v-if="selectedIds.has(course.sourceId)" class="selected-badge"><el-icon><Check /></el-icon>已选</span>
            </div>
            <div class="rating-row"><div><span>综合口碑</span><strong>{{ course.avgOverall?.toFixed(2) ?? '--' }}<small>/15</small></strong></div><el-progress :percentage="ratingPercent(course)" :show-text="false" :stroke-width="7" color="#0b8f82" /></div>
            <div class="tile-meta"><span><el-icon><Star /></el-icon>三项评分已聚合</span><span><el-icon><ChatDotRound /></el-icon>{{ course.reviewCount }} 条评价</span></div>
            <div class="tile-actions"><router-link :to="{ path: '/course-reviews', query: { keyword: course.courseName } }">查看关联评价</router-link><el-button v-if="selectedIds.has(course.sourceId)" disabled>已选课</el-button><el-button v-else type="primary" @click="select(course)">加入意向课</el-button></div>
          </article>
        </div>
        <nav class="pager" aria-label="课程页码"><el-button :disabled="page === 1 || loading" @click="changePage(-1)">上一页</el-button><span>第 {{ page }} 页</span><el-button :disabled="courses.length < pageSize || loading" @click="changePage(1)">下一页</el-button></nav>
      </section>

      <aside class="selection-sidebar">
        <div class="sidebar-head"><div><span>我的选择</span><small>登录账户内实时保存</small></div><strong>{{ selected.length }}</strong></div>
        <el-empty v-if="!selected.length" :image-size="64" description="还没有加入课程" />
        <div v-else class="picked-list">
          <article v-for="course in selected" :key="course.sourceId" class="picked-course"><span class="picked-mark">{{ initials(course) }}</span><div><strong>{{ course.courseName }}</strong><small>{{ course.professor }} · {{ course.avgOverall?.toFixed(1) ?? '--' }}/15</small></div><el-button text type="danger" aria-label="取消选择" @click="cancel(course)">移除</el-button></article>
        </div>
        <p class="sidebar-note">此清单基于公开评价目录，供课程决策参考；不替代学校教务系统的正式选课结果。</p>
      </aside>
    </div>
  </main>
</template>

<style scoped>
.selection-workspace { display: grid; gap: 18px; color: #26343b; }.workspace-header { display: flex; align-items: end; justify-content: space-between; gap: 20px; padding: 4px 2px; }.kicker { margin: 0 0 7px; color: #0b8f82; font-size: 12px; font-weight: 700; letter-spacing: 1.6px; }h1 { margin: 0; font-size: 28px; line-height: 1.15; letter-spacing: 0; }.intro { margin: 8px 0 0; color: #69777e; }.text-link { color: #0b766d; font-weight: 600; text-decoration: none; white-space: nowrap; }.metrics { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #dbe6e5; background: #fff; }.metric { display: grid; gap: 2px; min-height: 92px; padding: 16px 18px; border-right: 1px solid #e5eceb; }.metric:last-child { border: 0; }.metric span, .metric small { color: #77858b; font-size: 12px; }.metric strong { color: #26343b; font-size: 24px; }.metric.source strong { color: #0b8f82; font-size: 18px; padding-top: 4px; }.search-console { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border: 1px solid #dbe6e5; background: #fff; }.search-console > .el-icon { color: #0b8f82; font-size: 19px; }.search-console :deep(.el-input__wrapper) { box-shadow: none; }.sort-select { width: 155px; flex-shrink: 0; }.workspace-grid { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 18px; align-items: start; }.catalog-section { min-width: 0; }.section-label { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 10px; }.section-label span { font-size: 16px; font-weight: 700; }.section-label small { color: #7b888e; }.course-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; min-height: 360px; }.course-tile { display: grid; gap: 16px; padding: 17px; border: 1px solid #dbe6e5; background: #fff; transition: border-color .2s, transform .2s, box-shadow .2s; }.course-tile:hover { border-color: #81bcb4; transform: translateY(-2px); box-shadow: 0 10px 22px #0b8f8212; }.tile-top { display: flex; gap: 11px; align-items: start; }.course-mark, .picked-mark { display: grid; place-items: center; flex: 0 0 auto; width: 38px; height: 38px; background: #e5f4f1; color: #08766d; font-weight: 700; }.course-title { min-width: 0; flex: 1; }.course-title h2 { overflow: hidden; margin: 0; color: #26343b; font-size: 16px; text-overflow: ellipsis; white-space: nowrap; }.course-title p { display: flex; align-items: center; gap: 4px; margin: 5px 0 0; color: #718087; font-size: 13px; }.selected-badge { display: flex; align-items: center; gap: 2px; color: #08766d; font-size: 12px; }.rating-row { display: grid; grid-template-columns: 118px 1fr; align-items: center; gap: 12px; }.rating-row span { display: block; color: #7a888d; font-size: 12px; }.rating-row strong { color: #08766d; font-size: 20px; }.rating-row small { color: #7a888d; font-size: 11px; font-weight: 500; }.tile-meta, .tile-actions { display: flex; justify-content: space-between; gap: 8px; }.tile-meta { color: #748288; font-size: 12px; }.tile-meta span { display: flex; align-items: center; gap: 4px; }.tile-actions { align-items: center; }.tile-actions a { color: #0b766d; font-size: 13px; text-decoration: none; }.tile-actions :deep(.el-button) { min-width: 104px; }.pager { display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-top: 14px; color: #6d7b81; font-size: 13px; }.selection-sidebar { position: sticky; top: 82px; border: 1px solid #dbe6e5; background: #fff; }.sidebar-head { display: flex; align-items: start; justify-content: space-between; padding: 16px; border-bottom: 1px solid #e7eeee; }.sidebar-head span, .sidebar-head small { display: block; }.sidebar-head span { font-weight: 700; }.sidebar-head small { margin-top: 4px; color: #75838a; font-size: 12px; }.sidebar-head strong { color: #0b8f82; font-size: 22px; }.picked-list { display: grid; }.picked-course { display: grid; grid-template-columns: 32px minmax(0, 1fr) auto; gap: 9px; align-items: center; padding: 12px 14px; border-bottom: 1px solid #edf2f1; }.picked-mark { width: 32px; height: 32px; font-size: 13px; }.picked-course strong, .picked-course small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.picked-course small { margin-top: 3px; color: #76848a; font-size: 11px; }.picked-course :deep(.el-button) { padding: 4px; }.sidebar-note { margin: 0; padding: 14px; background: #f5f9f8; color: #708087; font-size: 12px; line-height: 1.65; }@media (max-width: 900px) { .workspace-grid { grid-template-columns: 1fr; }.selection-sidebar { position: static; }.course-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }}@media (max-width: 640px) { .workspace-header { align-items: start; flex-direction: column; }.metrics { grid-template-columns: repeat(2, 1fr); }.metric:nth-child(2) { border-right: 0; }.metric:nth-child(-n+2) { border-bottom: 1px solid #e5eceb; }.search-console { align-items: stretch; flex-wrap: wrap; }.search-console :deep(.el-input) { width: calc(100% - 30px); }.sort-select { flex: 1; }.course-grid { grid-template-columns: 1fr; }.tile-meta { flex-direction: column; }.rating-row { grid-template-columns: 104px 1fr; }}
</style>

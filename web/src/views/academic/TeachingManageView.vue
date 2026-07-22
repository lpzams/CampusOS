<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnrolledStudents, getMyTeachingCourses, getTeachingReviews, saveStudentScore } from '@/api/teaching'
import type { CourseFeedback, EnrolledStudent, TeachingCourse } from '@/api/teaching'

const courses = ref<TeachingCourse[]>([])
const current = ref<TeachingCourse | null>(null)
const students = ref<EnrolledStudent[]>([])
const reviews = ref<CourseFeedback[]>([])
const loading = ref(false)
async function selectCourse(course: TeachingCourse) { current.value = course; loading.value = true; try { [students.value, reviews.value] = await Promise.all([getEnrolledStudents(course.id), getTeachingReviews(course.id)]) } finally { loading.value = false } }
async function save(row: EnrolledStudent) { const value = Number(row.score); if (!Number.isFinite(value)) return ElMessage.warning('请输入 0 到 100 的成绩'); await saveStudentScore(row.courseId, row.studentId, value); ElMessage.success('成绩已保存') }
onMounted(async () => { courses.value = await getMyTeachingCourses(); if (courses.value[0]) await selectCourse(courses.value[0]) })
</script>
<template><div class="teaching-page"><el-card shadow="never"><div class="heading"><div><h1>教学管理</h1><p>仅展示您本人承担的教学课程及已报名学生。</p></div></div><el-empty v-if="!courses.length" description="当前没有分配的教学课程" /><div v-else class="course-list"><el-button v-for="course in courses" :key="course.id" :type="current?.id === course.id ? 'primary' : 'default'" @click="selectCourse(course)">{{ course.courseName }} · {{ course.timeSlot }}</el-button></div></el-card><el-card v-if="current" v-loading="loading" shadow="never"><template #header>{{ current.courseName }} · 已选学生与成绩</template><el-table :data="students" empty-text="暂无学生选课"><el-table-column prop="studentId" label="学生 ID" width="100" /><el-table-column prop="realName" label="姓名" min-width="120" /><el-table-column prop="username" label="账号" min-width="130" /><el-table-column label="课程成绩" width="190"><template #default="{ row }"><el-input-number v-model="row.score" :min="0" :max="100" :precision="0" controls-position="right" /><el-button link type="primary" @click="save(row as EnrolledStudent)">保存</el-button></template></el-table-column></el-table></el-card><el-card v-if="current" shadow="never"><template #header>本课程评价</template><el-empty v-if="!reviews.length" description="暂无本课程评价" /><div v-for="review in reviews" :key="review.id" class="review"><el-tag type="warning">{{ review.overall ?? '-' }} 分</el-tag><p>{{ review.comment }}</p></div></el-card></div></template>
<style scoped>.teaching-page{display:grid;gap:16px}.heading h1{margin:0;font-size:20px}.heading p{margin:8px 0 0;color:#7a8490}.course-list{display:flex;gap:10px;flex-wrap:wrap;margin-top:16px}.review{padding:12px 0;border-bottom:1px solid #ebeef5}.review p{margin:8px 0 0;color:#505866}</style>

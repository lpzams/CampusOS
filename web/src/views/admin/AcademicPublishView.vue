<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { publishNotice, NOTICE_TYPES } from '@/api/notice'
import { publishExam } from '@/api/exam'

const savingNotice = ref(false)
const savingExam = ref(false)
const notice = reactive({ title: '', content: '', type: 'SCHOOL', department: '教务处' })
const exam = reactive({ courseName: '', courseCode: '', examDate: '', examTime: '', building: '', classroom: '', seatNumber: '', userId: undefined as number | undefined })

async function submitNotice() {
  if (!notice.title.trim() || !notice.content.trim() || !notice.department.trim()) return ElMessage.warning('请填写通知标题、发布部门和正文')
  savingNotice.value = true
  try { await publishNotice(notice); ElMessage.success('通知已发布，学生端公告列表会立即显示'); Object.assign(notice, { title: '', content: '', type: 'SCHOOL', department: '教务处' }) } finally { savingNotice.value = false }
}
async function submitExam() {
  if (!exam.courseName.trim() || !exam.examDate || !exam.examTime.trim() || !exam.building.trim() || !exam.classroom.trim()) return ElMessage.warning('请填写课程、日期、时间和考场信息')
  savingExam.value = true
  try { await publishExam(exam); ElMessage.success(exam.userId ? '定向考试安排已发布' : '全校考试安排已发布'); Object.assign(exam, { courseName: '', courseCode: '', examDate: '', examTime: '', building: '', classroom: '', seatNumber: '', userId: undefined }) } finally { savingExam.value = false }
}
</script>

<template>
  <div class="grid">
    <el-card shadow="never"><template #header>发布通知公告</template>
      <el-form label-width="82px"><el-form-item label="标题"><el-input v-model="notice.title" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="notice.type"><el-option v-for="type in NOTICE_TYPES" :key="type.code" :label="type.name" :value="type.code" /></el-select></el-form-item>
        <el-form-item label="发布部门"><el-input v-model="notice.department" /></el-form-item><el-form-item label="正文"><el-input v-model="notice.content" type="textarea" :rows="7" /></el-form-item>
        <el-button type="primary" :loading="savingNotice" @click="submitNotice">发布通知</el-button></el-form>
    </el-card>
    <el-card shadow="never"><template #header>发布考试安排</template>
      <el-form label-width="82px"><el-form-item label="课程名称"><el-input v-model="exam.courseName" /></el-form-item><el-form-item label="课程代码"><el-input v-model="exam.courseCode" /></el-form-item>
        <el-form-item label="考试日期"><el-date-picker v-model="exam.examDate" value-format="YYYY-MM-DD" type="date" /></el-form-item><el-form-item label="考试时间"><el-input v-model="exam.examTime" placeholder="如 14:00-16:00" /></el-form-item>
        <el-form-item label="楼宇 / 教室"><div class="pair"><el-input v-model="exam.building" placeholder="教学楼" /><el-input v-model="exam.classroom" placeholder="教室" /></div></el-form-item><el-form-item label="座位号"><el-input v-model="exam.seatNumber" /></el-form-item><el-form-item label="学生 ID"><el-input-number v-model="exam.userId" :min="1" placeholder="留空为全校" /></el-form-item>
        <el-button type="primary" :loading="savingExam" @click="submitExam">发布考试安排</el-button></el-form>
    </el-card>
  </div>
</template>

<style scoped>.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}.pair{display:flex;gap:8px;width:100%}@media(max-width:760px){.grid{grid-template-columns:1fr}}</style>

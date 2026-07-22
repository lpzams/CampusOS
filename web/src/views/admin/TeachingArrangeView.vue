<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arrangeTeachingCourse, deleteTeachingCourse, getTeachingCourses, getTeachingTeachers, updateTeachingCourse } from '@/api/adminTeaching'
import { getReviewCatalog } from '@/api/courseReview'
import type { CourseCatalogItem } from '@/api/courseReview'
import type { AdminTeachingCourse, TeacherOption } from '@/api/adminTeaching'

const courses = ref<AdminTeachingCourse[]>([])
const teachers = ref<TeacherOption[]>([])
const catalogOptions = ref<CourseCatalogItem[]>([])
const saving = ref(false)
const editVisible = ref(false)
const editingId = ref<number | null>(null)
const emptyForm = () => ({ courseName: '', courseCode: '', teacherId: undefined as number | undefined, catalogSourceId: undefined as string | undefined, semester: '2026-2027-1', dayOfWeek: 1, timeSlot: '', building: '', classroom: '', credit: 2, weeks: '1-16周', color: '#409EFF' })
const form = reactive(emptyForm())

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
async function load() { [courses.value, teachers.value] = await Promise.all([getTeachingCourses(), getTeachingTeachers()]) }
function resetForm() { Object.assign(form, emptyForm()); editingId.value = null }
function validate() { return form.courseName.trim() && form.teacherId && form.semester.trim() && form.timeSlot.trim() && form.classroom.trim() }
async function searchCatalog(keyword: string) { catalogOptions.value = await getReviewCatalog({ keyword: keyword || undefined, size: 50 }) }
function chooseCatalog(sourceId: string | undefined) {
  const item = catalogOptions.value.find(option => option.sourceId === sourceId)
  if (item) form.courseName = item.courseName
}
async function submit() {
  if (!validate()) return ElMessage.warning('请填写课程、教师、学期、上课时间和教室')
  saving.value = true
  try {
    if (editingId.value) {
      await updateTeachingCourse(editingId.value, form)
      ElMessage.success('教学安排已更新')
      editVisible.value = false
    } else {
      await arrangeTeachingCourse(form as never)
      ElMessage.success('教学安排已创建')
    }
    resetForm(); await load()
  } finally { saving.value = false }
}
function edit(row: AdminTeachingCourse) { Object.assign(form, emptyForm(), row); editingId.value = row.id; editVisible.value = true }
async function remove(row: AdminTeachingCourse) {
  await ElMessageBox.confirm(`确定删除「${row.courseName}」的教学安排吗？未选课的安排可以删除。`, '删除教学安排', { type: 'warning' })
  await deleteTeachingCourse(row.id); ElMessage.success('教学安排已删除'); await load()
}
onMounted(async () => { await Promise.all([load(), searchCatalog('')]) })
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>安排教师授课</template>
      <el-alert title="管理员负责安排授课教师、学期、时间和教室。系统会阻止同一学期教师或教室的时间冲突。" type="info" :closable="false" class="hint" />
      <el-form :model="form" label-width="84px" class="form" @submit.prevent="submit">
        <el-form-item label="课程名称"><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="评价目录"><el-select v-model="form.catalogSourceId" clearable filterable remote reserve-keyword placeholder="检索并关联爬虫课程评价" :remote-method="searchCatalog" @change="chooseCatalog"><el-option v-for="item in catalogOptions" :key="item.sourceId" :label="`${item.courseName} · ${item.professor}（${item.reviewCount} 条评价）`" :value="item.sourceId" /></el-select></el-form-item>
        <el-form-item label="课程代码"><el-input v-model="form.courseCode" /></el-form-item>
        <el-form-item label="授课教师"><el-select v-model="form.teacherId" placeholder="选择教师"><el-option v-for="teacher in teachers" :key="teacher.userId" :label="`${teacher.realName} ${teacher.department ? `· ${teacher.department}` : ''}`" :value="teacher.userId" /></el-select></el-form-item>
        <el-form-item label="学期"><el-input v-model="form.semester" placeholder="例如 2026-2027-1" /></el-form-item>
        <el-form-item label="上课日期"><el-select v-model="form.dayOfWeek"><el-option v-for="(day, index) in weekdays" :key="day" :label="day" :value="index + 1" /></el-select></el-form-item>
        <el-form-item label="上课时间"><el-input v-model="form.timeSlot" placeholder="例如 08:00-09:35" /></el-form-item>
        <el-form-item label="楼宇 / 教室"><div class="pair"><el-input v-model="form.building" placeholder="教学楼" /><el-input v-model="form.classroom" placeholder="教室" /></div></el-form-item>
        <el-form-item label="学分 / 周次"><div class="pair"><el-input-number v-model="form.credit" :min="0.5" :step="0.5" /><el-input v-model="form.weeks" /></div></el-form-item>
        <el-button type="primary" native-type="submit" :loading="saving">创建教学安排</el-button>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>已安排教学课程</template>
      <el-table :data="courses" empty-text="暂无教学安排">
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column label="评价关联" min-width="120"><template #default="{ row }"><el-tag v-if="row.catalogSourceId" type="success" effect="plain">{{ row.reviewCount }} 条评价</el-tag><span v-else>未关联</span></template></el-table-column>
        <el-table-column prop="teacherName" label="授课教师" min-width="110" />
        <el-table-column prop="semester" label="学期" min-width="120" />
        <el-table-column label="上课时间" min-width="160"><template #default="{ row }">{{ weekdays[row.dayOfWeek - 1] }} {{ row.timeSlot }}</template></el-table-column>
        <el-table-column label="地点" min-width="130"><template #default="{ row }">{{ row.building ? `${row.building} ` : '' }}{{ row.classroom }}</template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="edit(row as AdminTeachingCourse)">调整</el-button><el-button link type="danger" @click="remove(row as AdminTeachingCourse)">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="调整教学安排" width="min(680px, 94vw)" @closed="resetForm">
      <el-form :model="form" label-width="84px" class="form">
        <el-form-item label="课程名称"><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="评价目录"><el-select v-model="form.catalogSourceId" clearable filterable remote reserve-keyword placeholder="检索并关联爬虫课程评价" :remote-method="searchCatalog" @change="chooseCatalog"><el-option v-for="item in catalogOptions" :key="item.sourceId" :label="`${item.courseName} · ${item.professor}（${item.reviewCount} 条评价）`" :value="item.sourceId" /></el-select></el-form-item>
        <el-form-item label="授课教师"><el-select v-model="form.teacherId"><el-option v-for="teacher in teachers" :key="teacher.userId" :label="teacher.realName" :value="teacher.userId" /></el-select></el-form-item>
        <el-form-item label="学期"><el-input v-model="form.semester" /></el-form-item>
        <el-form-item label="上课日期"><el-select v-model="form.dayOfWeek"><el-option v-for="(day, index) in weekdays" :key="day" :label="day" :value="index + 1" /></el-select></el-form-item>
        <el-form-item label="上课时间"><el-input v-model="form.timeSlot" /></el-form-item>
        <el-form-item label="楼宇 / 教室"><div class="pair"><el-input v-model="form.building" /><el-input v-model="form.classroom" /></div></el-form-item>
        <el-form-item label="学分 / 周次"><div class="pair"><el-input-number v-model="form.credit" :min="0.5" :step="0.5" /><el-input v-model="form.weeks" /></div></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存调整</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { display: grid; gap: 16px; }
.form { max-width: 700px; }
.pair { display: flex; gap: 8px; width: 100%; }
.pair > * { flex: 1; }
.hint { margin-bottom: 18px; }
</style>

<script setup lang="ts">
/**
 * 个人信息中心（功能 2）。
 *
 * 四块内容做成侧边 Tab：
 *   基本资料：展示 + 编辑（姓名/手机/邮箱/性别）、上传头像；
 *   学籍/档案：学生看学籍信息，教师看个人档案（按 userType 调不同接口）；
 *   实名认证：提交姓名 + 身份证号 + 学号（演示流程 -> 审核中）；
 *   修改密码：原密码 + 新密码。
 *
 * 头像上传用 el-upload 的手动模式（http-request 交给我们自己的 upload 封装）。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { Camera, Edit, Lock, Postcard, School, User } from '@element-plus/icons-vue'
import {
  changePassword, getProfile, getStudentProfile, getTeacherProfile,
  updateProfile, uploadAvatar, verifyIdentity,
} from '@/api/user'
import type { UserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { fileUrl } from '@/utils/format'

const userStore = useUserStore()
const activeTab = ref('basic')

// ===== 基本资料 =====
const loading = ref(false)
const profile = ref<UserProfile | null>(null)
const editing = ref(false)
const savingProfile = ref(false)
const profileForm = reactive({ realName: '', phone: '', email: '', gender: '' })

/** userType -> 中文，展示用 */
const USER_TYPE_LABEL: Record<number, string> = { 1: '学生', 2: '教师', 3: '管理员' }

async function loadProfile() {
  loading.value = true
  try {
    profile.value = await getProfile()
  } finally {
    loading.value = false
  }
}

function startEdit() {
  if (!profile.value) return
  profileForm.realName = profile.value.realName ?? ''
  profileForm.phone = profile.value.phone ?? ''
  profileForm.email = profile.value.email ?? ''
  profileForm.gender = profile.value.gender ?? ''
  editing.value = true
}

async function saveProfile() {
  savingProfile.value = true
  try {
    profile.value = await updateProfile(profileForm)
    userStore.updateDisplay({ realName: profile.value.realName })
    ElMessage.success('资料已更新')
    editing.value = false
  } finally {
    savingProfile.value = false
  }
}

/** 头像上传：el-upload 的自定义上传，成功后刷新展示和顶栏 */
async function handleAvatarUpload(options: UploadRequestOptions) {
  const updated = await uploadAvatar(options.file)
  profile.value = updated
  userStore.updateDisplay({ avatar: updated.avatar ?? '' })
  ElMessage.success('头像已更新')
}

function beforeAvatarUpload(file: File) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像不能超过 5MB')
    return false
  }
  return true
}

// ===== 学籍 / 档案 =====
const detailLoading = ref(false)
const detail = ref<UserProfile | null>(null)
const detailError = ref('')

async function loadDetail() {
  if (!profile.value) return
  detailLoading.value = true
  detailError.value = ''
  try {
    detail.value = profile.value.userType === 2 ? await getTeacherProfile() : await getStudentProfile()
  } catch (e) {
    detailError.value = (e as Error).message || '暂无档案信息'
  } finally {
    detailLoading.value = false
  }
}

function onTabChange(name: string | number) {
  if (name === 'detail' && !detail.value && !detailError.value) loadDetail()
}

// ===== 实名认证 =====
const verifyFormRef = ref<FormInstance>()
const verifying = ref(false)
const verifyForm = reactive({ realName: '', idCard: '', studentId: '' })
const verifyRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' },
  ],
  studentId: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
}

async function submitVerify() {
  const valid = await verifyFormRef.value?.validate().catch(() => false)
  if (!valid) return
  verifying.value = true
  try {
    const result = await verifyIdentity(verifyForm)
    ElMessage.success(`已提交，当前状态：${result.status}`)
    await loadProfile()
  } finally {
    verifying.value = false
  }
}

// ===== 修改密码 =====
const pwdFormRef = ref<FormInstance>()
const changingPwd = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, value, cb) => (value === pwdForm.newPassword ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur',
    },
  ],
}

async function submitPwd() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  changingPwd.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    changingPwd.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <div v-loading="loading" class="profile-page">
    <section v-if="profile" class="profile-hero">
      <div class="hero-glow hero-glow--one" />
      <div class="hero-glow hero-glow--two" />
      <div class="avatar-wrap">
        <el-avatar :size="104" :src="fileUrl(profile.avatar)" class="hero-avatar">
          {{ (profile.realName || profile.username || '?').slice(0, 1) }}
        </el-avatar>
        <el-upload
          class="avatar-upload"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="handleAvatarUpload"
        >
          <button class="avatar-button" title="更换头像"><el-icon><Camera /></el-icon></button>
        </el-upload>
      </div>

      <div class="hero-info">
        <div class="hero-title-row">
          <h1>{{ profile.realName || profile.username }}</h1>
          <span class="role-badge">{{ USER_TYPE_LABEL[profile.userType] || '用户' }}</span>
          <span v-if="profile.identityStatus" class="verified-badge">{{ profile.identityStatus }}</span>
        </div>
        <p class="hero-account">@{{ profile.username }}</p>
        <div class="hero-meta">
          <span><el-icon><School /></el-icon>{{ profile.department || '院系信息待完善' }}</span>
          <span><el-icon><Postcard /></el-icon>{{ profile.studentId || '学号/工号待完善' }}</span>
        </div>
      </div>

      <el-button class="hero-edit" :icon="Edit" @click="startEdit">编辑资料</el-button>
    </section>

    <section class="profile-content">
      <el-tabs v-model="activeTab" class="profile-tabs" @tab-change="onTabChange">
        <!-- 基本资料 -->
        <el-tab-pane name="basic">
          <template #label><span class="tab-label"><el-icon><User /></el-icon>基本资料</span></template>
          <div v-if="profile" class="basic-pane">
            <div class="section-heading">
              <div><h2>个人资料</h2><p>管理你的基本信息和联系方式</p></div>
              <el-button v-if="!editing" text type="primary" :icon="Edit" @click="startEdit">编辑</el-button>
            </div>

            <el-descriptions v-if="!editing" :column="2" class="info-table">
              <el-descriptions-item label="姓名">{{ profile.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="账号">{{ profile.username }}</el-descriptions-item>
              <el-descriptions-item label="身份">{{ USER_TYPE_LABEL[profile.userType] || '-' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ profile.gender || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ profile.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ profile.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="院系">{{ profile.department || '-' }}</el-descriptions-item>
              <el-descriptions-item label="实名状态">
                <el-tag v-if="profile.identityStatus" size="small" type="warning">{{ profile.identityStatus }}</el-tag>
                <span v-else>未认证</span>
              </el-descriptions-item>
            </el-descriptions>

            <el-form v-else :model="profileForm" label-width="70px" class="edit-form">
              <el-form-item label="姓名"><el-input v-model="profileForm.realName" /></el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio value="男">男</el-radio>
                  <el-radio value="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="手机"><el-input v-model="profileForm.phone" maxlength="11" /></el-form-item>
              <el-form-item label="邮箱"><el-input v-model="profileForm.email" /></el-form-item>
            </el-form>

            <div class="actions">
              <template v-if="editing">
                <el-button :loading="savingProfile" type="primary" @click="saveProfile">保存</el-button>
                <el-button @click="editing = false">取消</el-button>
              </template>
            </div>
          </div>
        </el-tab-pane>

        <!-- 学籍 / 档案 -->
        <el-tab-pane name="detail">
          <template #label><span class="tab-label"><el-icon><School /></el-icon>{{ profile?.userType === 2 ? '个人档案' : '学籍信息' }}</span></template>
          <div v-loading="detailLoading" class="pane-body">
            <div class="section-heading"><div><h2>{{ profile?.userType === 2 ? '个人档案' : '学籍信息' }}</h2><p>由学校统一维护的身份与院系信息</p></div></div>
            <el-descriptions v-if="detail" :column="2" class="info-table">
              <el-descriptions-item label="姓名">{{ detail.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="profile?.userType === 2 ? '工号' : '学号'">{{ detail.studentId || detail.username }}</el-descriptions-item>
              <el-descriptions-item label="院系">{{ detail.department || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="profile?.userType === 2" label="职称">{{ detail.title || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="profile?.userType === 2" label="研究方向">{{ detail.researchField || '-' }}</el-descriptions-item>
              <template v-else>
                <el-descriptions-item label="专业">{{ detail.major || '-' }}</el-descriptions-item>
                <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
                <el-descriptions-item label="入学年份">{{ detail.enrollmentYear || '-' }}</el-descriptions-item>
              </template>
            </el-descriptions>
            <el-empty v-else-if="detailError" :description="detailError" />
          </div>
        </el-tab-pane>

        <!-- 实名认证 -->
        <el-tab-pane name="verify">
          <template #label><span class="tab-label"><el-icon><Postcard /></el-icon>实名认证</span></template>
          <div class="pane-body">
          <div class="section-heading"><div><h2>实名认证</h2><p>认证信息仅用于校园身份核验</p></div></div>
          <el-alert
            v-if="profile?.identityStatus"
            :title="`当前认证状态：${profile.identityStatus}`"
            type="warning"
            :closable="false"
            class="verify-alert"
          />
          <el-form ref="verifyFormRef" :model="verifyForm" :rules="verifyRules" label-width="90px" class="narrow-form">
            <el-form-item label="真实姓名" prop="realName"><el-input v-model="verifyForm.realName" /></el-form-item>
            <el-form-item label="身份证号" prop="idCard"><el-input v-model="verifyForm.idCard" maxlength="18" /></el-form-item>
            <el-form-item label="学号/工号" prop="studentId"><el-input v-model="verifyForm.studentId" /></el-form-item>
            <el-button type="primary" :loading="verifying" @click="submitVerify">提交认证</el-button>
          </el-form>
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane name="password">
          <template #label><span class="tab-label"><el-icon><Lock /></el-icon>修改密码</span></template>
          <div class="pane-body">
          <div class="section-heading"><div><h2>账号安全</h2><p>定期更换密码可以保护账号安全</p></div></div>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="108px" class="narrow-form password-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-button type="primary" :loading="changingPwd" @click="submitPwd">修改密码</el-button>
          </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<style scoped>
.profile-page {
  display: grid;
  gap: 18px;
}

.profile-hero {
  position: relative;
  min-height: 210px;
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 38px 42px;
  overflow: hidden;
  color: #fff;
  border-radius: 24px;
  background: linear-gradient(125deg, #49358e 0%, #7354c6 52%, #a47bd2 100%);
  box-shadow: 0 18px 45px #6042a333;
}

.hero-glow { position: absolute; border-radius: 50%; background: #fff; opacity: .1; filter: blur(2px); }
.hero-glow--one { width: 280px; height: 280px; right: -75px; top: -150px; }
.hero-glow--two { width: 150px; height: 150px; right: 170px; bottom: -100px; }

.avatar-wrap { position: relative; z-index: 1; flex: none; }
.hero-avatar { border: 4px solid #ffffffb8; background: #d9cafa; color: #49358e; font-size: 36px; font-weight: 700; box-shadow: 0 10px 30px #291a6655; }
.avatar-upload { position: absolute; right: -2px; bottom: 1px; }
.avatar-button { width: 34px; height: 34px; display: grid; place-items: center; padding: 0; border: 3px solid #7354c6; border-radius: 50%; color: #6247ab; background: #fff; cursor: pointer; box-shadow: 0 3px 10px #2d1d6444; }
.avatar-button:hover { color: #fff; background: #a08ae3; }

.hero-info { position: relative; z-index: 1; min-width: 0; flex: 1; }
.hero-title-row { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.hero-title-row h1 { margin: 0; font-size: 30px; line-height: 1.25; letter-spacing: .5px; }
.role-badge, .verified-badge { padding: 4px 10px; border: 1px solid #ffffff42; border-radius: 999px; background: #ffffff1f; font-size: 12px; }
.verified-badge { color: #fff1b7; }
.hero-account { margin: 7px 0 19px; color: #e7def9; font-size: 14px; }
.hero-meta { display: flex; flex-wrap: wrap; gap: 12px 24px; color: #f2edfc; font-size: 14px; }
.hero-meta span { display: inline-flex; align-items: center; gap: 7px; }
.hero-edit { position: relative; z-index: 1; border-color: #ffffff70; color: #fff; background: #ffffff18; }
.hero-edit:hover { border-color: #fff; color: #6247ab; background: #fff; }

.profile-content {
  min-height: 430px;
  padding: 8px 30px 30px;
  border: 1px solid #ece4fb;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 32px #6042a30d;
}

.profile-tabs {
  min-height: 390px;
}

.tab-label { display: inline-flex; align-items: center; gap: 7px; padding: 0 10px; }

.profile-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.profile-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background: #eee9f8; }
.profile-tabs :deep(.el-tabs__item) { height: 58px; color: #756d88; font-weight: 500; }
.profile-tabs :deep(.el-tabs__item.is-active) { color: #6247ab; font-weight: 700; }
.profile-tabs :deep(.el-tabs__active-bar) { height: 3px; border-radius: 3px 3px 0 0; }
.profile-tabs :deep(.el-tabs__content) { padding-top: 28px; }

.basic-pane,
.pane-body {
  max-width: 820px;
  margin: 0 auto;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}
.section-heading h2 { margin: 0 0 5px; color: #2c2350; font-size: 20px; }
.section-heading p { margin: 0; color: #a39bad; font-size: 13px; }

.info-table,
.edit-form {
  margin-bottom: 24px;
}
.info-table :deep(.el-descriptions__body) { background: #faf9fd; }
.info-table :deep(.el-descriptions__cell) { padding: 17px 20px !important; border-bottom: 1px solid #eeeaf6; }
.info-table :deep(.el-descriptions__label) { width: 78px; color: #9a91a8; font-weight: 400; }
.info-table :deep(.el-descriptions__content) { color: #3e3651; font-weight: 500; }

.edit-form {
  max-width: 420px;
}

.actions {
  display: flex;
  gap: 12px;
}

.narrow-form {
  max-width: 420px;
}

.verify-alert {
  max-width: 420px;
  margin-bottom: 16px;
}

.password-form :deep(.el-form-item__label) { white-space: nowrap; }

@media (max-width: 700px) {
  .profile-hero { min-height: auto; flex-direction: column; align-items: flex-start; gap: 18px; padding: 28px 24px; }
  .hero-info { width: 100%; }
  .hero-title-row h1 { font-size: 24px; }
  .hero-meta { flex-direction: column; gap: 8px; }
  .hero-edit { position: absolute; right: 20px; top: 24px; }
  .profile-content { padding: 4px 16px 24px; }
  .tab-label { padding: 0 2px; }
  .profile-tabs :deep(.el-tabs__item) { padding: 0 9px; font-size: 13px; }
  .profile-tabs :deep(.el-tabs__item .el-icon) { display: none; }
  .profile-tabs :deep(.el-tabs__content) { padding-top: 22px; }
  .info-table :deep(.el-descriptions__cell) { padding: 13px 12px !important; }
  .edit-form, .narrow-form { max-width: none; }
}
</style>

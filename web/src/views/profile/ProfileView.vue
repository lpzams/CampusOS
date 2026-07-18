<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <!-- ============================================================ -->
      <!-- 头像和基本信息 -->
      <!-- ============================================================ -->
      <div class="profile-header">
        <!-- 头像：点击打开上传弹窗（带"换头像" badge） -->
        <el-badge value="换头像" :hidden="false" type="primary" class="avatar-badge">
          <el-button
            type="text"
            @click="showAvatarDialog = true"
            style="padding: 0; border: none;"
          >
            <el-avatar :size="80" :src="userStore.avatar || defaultAvatar">
              {{ userStore.name?.[0] }}
            </el-avatar>
          </el-button>
        </el-badge>

        <div class="header-info">
          <h2>{{ userStore.profile?.realName || userStore.name }}</h2>
          <div class="badges">
            <el-tag :type="userTypeTagMap[userStore.profile?.userType || 1]" size="small">
              {{ userTypeMap[userStore.profile?.userType || 1] }}
            </el-tag>
            <el-tag :type="statusTagMap[userStore.profile?.status || 0]" size="small">
              {{ statusMap[userStore.profile?.status || 0] }}
            </el-tag>
          </div>
          <p class="username">账号：{{ userStore.profile?.username }}</p>
        </div>

        <div class="header-actions">
          <!-- 2.2 编辑资料 -->
          <el-button type="primary" size="small" @click="goToEdit">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>

          <!-- 2.6 实名认证（图标改为 CircleCheck） -->
          <el-button type="warning" plain size="small" @click="goToVerify">
            <el-icon><CircleCheck /></el-icon>
            实名认证
          </el-button>

          <!-- 2.7 修改密码 -->
          <el-button type="info" plain size="small" @click="goToChangePassword">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>

          <!-- 2.1 刷新 -->
          <el-button type="primary" plain size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- ============================================================ -->
      <!-- 基本信息 -->
      <!-- ============================================================ -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="真实姓名">
          {{ userStore.profile?.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ userStore.profile?.gender || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userStore.profile?.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ userStore.profile?.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="院系">
          {{ userStore.profile?.department || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="专业">
          {{ userStore.profile?.major || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="班级">
          {{ userStore.profile?.className || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="学号/工号">
          {{ userStore.profile?.studentId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="入学/入职年份">
          {{ userStore.profile?.enrollmentYear || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatDateTime(userStore.profile?.createdTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- ============================================================ -->
      <!-- 2.4 学生专属信息 -->
      <!-- ============================================================ -->
      <template v-if="userStore.isStudent && userStore.studentProfile">
        <el-divider content-position="left">
          <el-icon><School /></el-icon>
          学生专属信息
        </el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="宿舍">
            {{ userStore.studentProfile.dormitory || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="辅导员">
            {{ userStore.studentProfile.advisor || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- ============================================================ -->
      <!-- 2.5 教师专属信息 -->
      <!-- ============================================================ -->
      <template v-if="userStore.isTeacher && userStore.teacherProfile">
        <el-divider content-position="left">
          <el-icon><User /></el-icon>
          教师专属信息
        </el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="职称">
            {{ userStore.teacherProfile.title || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="办公室">
            {{ userStore.teacherProfile.office || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="研究方向" :span="2">
            {{ userStore.teacherProfile.researchArea || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ userStore.teacherProfile.introduction || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>

    </el-card>

    <!-- ============================================================ -->
    <!-- 头像上传弹窗（2.3） -->
    <!-- ============================================================ -->
    <AvatarUploadDialog
      v-model="showAvatarDialog"
      @success="handleAvatarSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Edit,
  School,
  User,
  CircleCheck,  // ✅ 替换 Verified
  Lock,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import AvatarUploadDialog from './AvatarUploadDialog.vue'

// ============================================================
// 路由 & Store
// ============================================================

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// ============================================================
// 状态
// ============================================================

/** 控制头像上传弹窗显示（2.3） */
const showAvatarDialog = ref(false)

// ============================================================
// 映射表
// ============================================================

/** 用户类型映射（显示文字） */
const userTypeMap: Record<number, string> = {
  1: '学生',
  2: '教师',
  3: '管理员',
}

/** 用户类型映射（el-tag 颜色） */
const userTypeTagMap = {
  1: 'primary',
  2: 'success',
  3: 'danger',
} as const

/** 状态映射（显示文字） */
const statusMap: Record<number, string> = {
  0: '正常',
  1: '冻结',
}

/** 状态映射（el-tag 颜色） */
const statusTagMap = {
  0: 'success',
  1: 'danger',
} as const

// ============================================================
// 方法
// ============================================================

/** 刷新个人信息（2.1 + 2.4 + 2.5） */
async function handleRefresh() {
  const result = await userStore.fetchProfile()
  if (result) {
    // 根据用户类型刷新对应的详细信息
    if (userStore.isStudent) {
      await userStore.fetchStudentProfile()
    } else if (userStore.isTeacher) {
      await userStore.fetchTeacherProfile()
    }
    ElMessage.success('个人信息已刷新')
  } else {
    ElMessage.warning('刷新失败，请稍后重试')
  }
}

/** 跳转编辑资料页（2.2） */
function goToEdit() {
  router.push('/profile/edit')
}

/** 跳转实名认证页（2.6） */
function goToVerify() {
  router.push('/verify')
}

/** 跳转修改密码页（2.7） */
function goToChangePassword() {
  router.push('/change-password')
}

/** 头像上传成功回调（2.3） */
function handleAvatarSuccess(newAvatar: string) {
  ElMessage.success('头像已更新')
}

// ============================================================
// 生命周期：页面加载时自动获取数据
// ============================================================

onMounted(async () => {
  if (!userStore.profile && userStore.isLoggedIn) {
    await userStore.fetchProfile()
    // 根据用户类型自动获取详细信息
    if (userStore.isStudent && !userStore.studentProfile) {
      await userStore.fetchStudentProfile()
    } else if (userStore.isTeacher && !userStore.teacherProfile) {
      await userStore.fetchTeacherProfile()
    }
  }
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.profile-card {
  max-width: 900px;
  margin: 0 auto;
}

/* ===== 头像 + 基本信息区域 ===== */
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 0;
}

.header-info {
  flex: 1;
}

.header-info h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
}

.header-info .badges {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
}

.header-info .username {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  align-self: flex-start;
  padding-top: 4px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* ===== 头像 badge ===== */
.avatar-badge :deep(.el-badge__content) {
  cursor: pointer;
  font-size: 12px;
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
  top: -8px;
  right: -10px;
}
</style>
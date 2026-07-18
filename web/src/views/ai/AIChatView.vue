<template>
  <div class="ai-chat-page">
    <el-card class="chat-container">
      <!-- 头部 -->
      <template #header>
        <div class="chat-header">
          <div class="header-left">
            <span class="title">🤖 AI智慧助手</span>
            <el-tag type="success" size="small">在线</el-tag>
          </div>
          <div class="header-right">
            <el-button type="text" size="small" @click="clearChat">
              <el-icon><Delete /></el-icon>
              清空对话
            </el-button>
          </div>
        </div>
      </template>

      <!-- 消息列表 -->
      <div ref="messageContainer" class="message-list">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome">
          <div class="welcome-icon">🤖</div>
          <h3>你好！我是AI智慧助手</h3>
          <p>我可以帮你解答关于校园生活的各种问题，如：</p>
          <div class="welcome-questions">
            <el-tag
              v-for="q in welcomeQuestions"
              :key="q"
              class="welcome-tag"
              @click="sendQuestion(q)"
            >
              {{ q }}
            </el-tag>
          </div>
          <!-- 热门问题 -->
          <div v-if="hotQuestions.length > 0" class="hot-questions">
            <el-divider content-position="left">🔥 热门问题</el-divider>
            <div
              v-for="item in hotQuestions"
              :key="item.id"
              class="hot-item"
              @click="sendQuestion(item.title)"
            >
              <span class="hot-title">{{ item.title }}</span>
              <span class="hot-views">👁️ {{ item.viewCount }}</span>
            </div>
          </div>
        </div>

        <!-- 对话消息 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message-item"
          :class="msg.role"
        >
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 正在输入 -->
        <div v-if="isTyping" class="message-item assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-bubble">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="inputQuestion"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题… (Enter发送，Shift+Enter换行)"
          resize="none"
          @keydown.enter.prevent="handleKeyDown"
        />
        <el-button
          type="primary"
          :loading="sending"
          :disabled="!inputQuestion.trim()"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { aiChat, getHotQuestions } from '@/api/ai'
import { useUserStore } from '@/stores/user'
import type { HotQuestion } from '@/api/types'

const userStore = useUserStore()

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  time: Date
}

const messages = ref<ChatMessage[]>([])
const inputQuestion = ref('')
const sending = ref(false)
const isTyping = ref(false)
const messageContainer = ref<HTMLElement>()
const hotQuestions = ref<HotQuestion[]>([])

const welcomeQuestions = [
  '如何申请奖学金？',
  '校历安排是怎样的？',
  '怎么选课？',
  '图书馆开放时间？',
]

// ===== 格式化时间 =====
function formatTime(date: Date): string {
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

// ===== 滚动到底部 =====
function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// ===== ✅ 修复：键盘事件处理（参数类型兼容） =====
function handleKeyDown(evt: KeyboardEvent | Event) {
  // 确保是 KeyboardEvent
  const keyboardEvent = evt as KeyboardEvent
  // Shift+Enter 换行，不发送
  if (keyboardEvent.shiftKey) {
    return
  }
  keyboardEvent.preventDefault()
  sendMessage()
}

// ===== 发送消息 =====
async function sendMessage() {
  const question = inputQuestion.value.trim()
  if (!question) return

  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再使用AI助手')
    return
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: question,
    time: new Date(),
  })
  inputQuestion.value = ''
  scrollToBottom()

  // 调用 AI 接口
  sending.value = true
  isTyping.value = true

  try {
    const history = messages.value
      .slice(0, -1) // 不包括刚发的
      .map((m) => ({
        role: m.role,
        content: m.content,
      }))

    const res = await aiChat({
      question,
      history: history.length > 0 ? history : undefined,
    })

    if (res.code === 200 && res.data) {
      // 添加 AI 回复
      messages.value.push({
        role: 'assistant',
        content: res.data.answer,
        time: new Date(),
      })

      // 如果有相关问题，追加显示
      if (res.data.relatedQuestions && res.data.relatedQuestions.length > 0) {
        const relatedText =
          '\n\n💡 你可能还想问：\n' +
          res.data.relatedQuestions.map((q, i) => `${i + 1}. ${q}`).join('\n')
        messages.value[messages.value.length - 1].content += relatedText
      }
    } else {
      ElMessage.error(res.msg || 'AI回答失败')
    }
  } catch {
    ElMessage.error('AI服务异常，请稍后重试')
  } finally {
    sending.value = false
    isTyping.value = false
    scrollToBottom()
  }
}

// ===== 快速发送问题（点击欢迎问题 / 热门问题） =====
function sendQuestion(question: string) {
  inputQuestion.value = question
  sendMessage()
}

// ===== 清空对话 =====
function clearChat() {
  if (messages.value.length === 0) return
  messages.value = []
  ElMessage.success('对话已清空')
}

// ===== 获取热门问题 =====
async function fetchHotQuestions() {
  try {
    const res = await getHotQuestions()
    if (res.code === 200 && res.data) {
      hotQuestions.value = res.data.slice(0, 6)
    }
  } catch {
    // 热门问题加载失败不影响主功能
  }
}

onMounted(() => {
  fetchHotQuestions()
})
</script>

<style scoped>
.ai-chat-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.chat-container {
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

/* ===== 消息列表 ===== */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 8px;
  min-height: 400px;
  max-height: calc(80vh - 200px);
}

/* ===== 欢迎区域 ===== */
.welcome {
  text-align: center;
  padding: 40px 20px;
  color: #606266;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.welcome h3 {
  margin: 0 0 8px 0;
  font-size: 22px;
  color: #303133;
}

.welcome p {
  margin: 0 0 16px 0;
  color: #909399;
}

.welcome-questions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.welcome-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.welcome-tag:hover {
  transform: scale(1.05);
  color: #409eff;
}

/* ===== 热门问题 ===== */
.hot-questions {
  text-align: left;
  max-width: 500px;
  margin: 0 auto;
}

.hot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
}

.hot-item:hover {
  background: #f0f2f5;
}

.hot-title {
  color: #303133;
  font-size: 14px;
}

.hot-views {
  color: #909399;
  font-size: 12px;
}

/* ===== 消息项 ===== */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-item.user .message-bubble {
  background: #409eff;
  color: #fff;
}

.message-item.assistant .message-bubble {
  background: #f0f2f5;
  color: #303133;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: #f0f2f5;
}

.message-item.user .message-avatar {
  background: #409eff;
}

.message-bubble {
  max-width: 75%;
  padding: 10px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-content {
  font-size: 15px;
  line-height: 1.6;
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 4px;
  text-align: right;
}

/* ===== 打字指示器 ===== */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite both;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}
.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: scale(0.4);
    opacity: 0.4;
  }
  30% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ===== 输入区 ===== */
.input-area {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  align-items: flex-end;
}

.input-area .el-textarea {
  flex: 1;
}

.input-area .el-button {
  height: 56px;
  padding: 0 28px;
  flex-shrink: 0;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .ai-chat-page {
    padding: 10px;
  }
  .chat-container {
    height: 90vh;
  }
  .message-list {
    max-height: calc(90vh - 160px);
  }
  .welcome-questions {
    flex-direction: column;
    align-items: center;
  }
  .input-area {
    flex-direction: column;
  }
  .input-area .el-button {
    width: 100%;
    height: 40px;
  }
}
</style>
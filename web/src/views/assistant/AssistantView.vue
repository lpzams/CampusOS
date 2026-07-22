<script setup lang="ts">
/**
 * AI 智慧校园助手页（功能 15，特色功能）。
 *
 * 左边聊天区（气泡对话，需登录），右边三个辅助卡片：
 * 热门问题（点了直接发问）、办事流程查询、智能推荐（需登录）。
 * 对应后端 /api/ai/chat、/hot-questions、/process、/recommend。
 * 后端目前是本地 FAQ 规则回答，聊天记录只保存在页面内存里。
 */
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Paperclip, Promotion } from '@element-plus/icons-vue'
import {
  PROCESS_TYPES, RECOMMEND_TYPES, chat, getHotQuestions, getProcess, getRecommend,
} from '@/api/assistant'
import type { HotQuestion, ProcessGuide, RecommendItem } from '@/api/assistant'
import { useUserStore } from '@/stores/user'
import { renderMarkdown } from '@/utils/markdown'

const router = useRouter()
const userStore = useUserStore()

// ===== 聊天 =====
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  related?: string[]
  image?: string
}

const messages = ref<ChatMessage[]>([
  { role: 'assistant', content: '你好，我是校园智慧助手。可以问我奖学金、校园卡、图书馆等校园事务，也可以用右侧的办事流程查询。' },
])
const input = ref('')
const image = ref<{ name: string; preview: string; mediaType: string; data: string } | null>(null)
const sending = ref(false)
const chatBodyRef = ref<HTMLElement | null>(null)

function chooseImage(file: File) {
  if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) return ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 图片'), false
  if (file.size > 15 * 1024 * 1024) return ElMessage.warning('图片不能超过 15MB'), false
  const reader = new FileReader()
  reader.onload = () => {
    const preview = String(reader.result)
    image.value = { name: file.name, preview, mediaType: file.type, data: preview.split(',', 2)[1] }
  }
  reader.readAsDataURL(file)
  return false
}

async function scrollToBottom() {
  await nextTick()
  chatBodyRef.value?.scrollTo({ top: chatBodyRef.value.scrollHeight, behavior: 'smooth' })
}

async function send(question?: string) {
  const text = (question ?? input.value).trim()
  if (!text && !image.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再使用 AI 问答')
    router.push({ path: '/login', query: { redirect: '/assistant' } })
    return
  }
  const selectedImage = image.value
  messages.value.push({ role: 'user', content: text || `请分析图片：${selectedImage?.name}`, image: selectedImage?.preview })
  input.value = ''
  image.value = null
  sending.value = true
  scrollToBottom()
  try {
    const reply = await chat(text, messages.value.slice(1, -1).map(({ role, content }) => ({ role, content })), selectedImage ? [{ mediaType: selectedImage.mediaType, data: selectedImage.data }] : [])
    messages.value.push({ role: 'assistant', content: reply.answer, related: reply.relatedQuestions })
  } catch {
    // 错误提示已由 request.ts 弹出，这里补一条兜底回复保持对话完整
    messages.value.push({ role: 'assistant', content: '抱歉，我暂时无法回答，请稍后再试。' })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

// ===== 热门问题 =====
const hotQuestions = ref<HotQuestion[]>([])

async function fetchHot() {
  try {
    hotQuestions.value = await getHotQuestions()
  } catch {
    // 热门问题失败不影响聊天
  }
}

function showHotQuestion(question: HotQuestion) {
  messages.value.push({ role: 'user', content: question.question })
  messages.value.push({ role: 'assistant', content: question.answer })
  scrollToBottom()
}

// ===== 办事流程查询 =====
const processForm = reactive({ type: 'LIBRARY', action: '' })
const processLoading = ref(false)
const processResult = ref<ProcessGuide | null>(null)

async function queryProcess() {
  if (!processForm.action.trim()) {
    ElMessage.warning('请输入要办理的事项，如：借书')
    return
  }
  processLoading.value = true
  try {
    processResult.value = await getProcess(processForm)
  } finally {
    processLoading.value = false
  }
}

// ===== 智能推荐 =====
const recommendType = ref('ACTIVITY')
const recommendLoading = ref(false)
const recommends = ref<RecommendItem[]>([])

async function fetchRecommend() {
  if (!userStore.isLoggedIn) return
  recommendLoading.value = true
  try {
    recommends.value = await getRecommend(recommendType.value)
  } finally {
    recommendLoading.value = false
  }
}

onMounted(() => {
  fetchHot()
  fetchRecommend()
})
</script>

<template>
  <div>
    <div class="ai-layout">
    <!-- 聊天区 -->
    <el-card shadow="never" class="chat-card" body-class="chat-card-body">
      <template #header>
        <div class="chat-header">
          <span class="card-title">AI 校园助手</span>
          <el-tag size="small" type="success" effect="plain">在线</el-tag>
        </div>
      </template>

      <div ref="chatBodyRef" class="chat-body">
        <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
          <div class="bubble">
            <img v-if="m.image" class="message-image" :src="m.image" alt="用户发送的图片">
            <div v-if="m.role === 'assistant'" class="bubble-text markdown" v-html="renderMarkdown(m.content)" />
            <div v-else class="bubble-text">{{ m.content }}</div>
            <div v-if="m.related?.length" class="related">
              <span class="related-label">猜你想问：</span>
              <el-button v-for="q in m.related" :key="q" link type="primary" size="small" @click="send(q)">{{ q }}</el-button>
            </div>
          </div>
        </div>
        <div v-if="sending" class="msg-row assistant">
          <div class="bubble typing">正在思考…</div>
        </div>
      </div>

      <div class="chat-input-row">
        <el-input
          v-model="input"
          placeholder="输入问题，如：如何申请奖学金？"
          :disabled="sending"
          @keyup.enter="send()"
        />
        <el-upload :show-file-list="false" :auto-upload="false" :disabled="sending" :on-change="file => chooseImage(file.raw!)">
          <el-button :disabled="sending" aria-label="发送图片"><el-icon><Paperclip /></el-icon></el-button>
        </el-upload>
        <el-button type="primary" :loading="sending" @click="send()">
          <el-icon><Promotion /></el-icon>
        </el-button>
      </div>
      <div v-if="image" class="image-pending"><img :src="image.preview" :alt="image.name"><span>{{ image.name }}</span><el-button link type="danger" @click="image = null">移除</el-button></div>
    </el-card>

    <!-- 侧栏 -->
    <div class="side-col">
      <el-card shadow="never" class="side-card">
        <template #header><span class="card-title">热门问题</span></template>
        <el-empty v-if="hotQuestions.length === 0" description="暂无热门问题" :image-size="60" />
        <div v-for="q in hotQuestions" :key="q.id" class="hot-item" @click="showHotQuestion(q)">
          {{ q.question }}
        </div>
      </el-card>

      <el-card shadow="never" class="side-card">
        <template #header><span class="card-title">办事流程查询</span></template>
        <div class="process-form">
          <el-select v-model="processForm.type" class="process-select">
            <el-option v-for="t in PROCESS_TYPES" :key="t.code" :label="t.name" :value="t.code" />
          </el-select>
          <el-input v-model="processForm.action" placeholder="事项，如：借书" @keyup.enter="queryProcess" />
          <el-button type="primary" :loading="processLoading" @click="queryProcess">查询</el-button>
        </div>
        <div v-if="processResult" class="process-result">
          <div class="process-title">{{ processResult.title }}</div>
          <el-steps direction="vertical" :active="processResult.steps.length">
            <el-step v-for="(s, i) in processResult.steps" :key="i" :title="s" />
          </el-steps>
        </div>
      </el-card>

      <el-card shadow="never" class="side-card">
        <template #header>
          <div class="recommend-header">
            <span class="card-title">智能推荐</span>
            <el-select v-model="recommendType" size="small" class="recommend-select" @change="fetchRecommend">
              <el-option v-for="t in RECOMMEND_TYPES" :key="t.code" :label="t.name" :value="t.code" />
            </el-select>
          </div>
        </template>
        <div v-loading="recommendLoading">
          <el-empty v-if="!userStore.isLoggedIn" description="登录后查看个性化推荐" :image-size="60" />
          <el-empty v-else-if="!recommendLoading && recommends.length === 0" description="暂无推荐" :image-size="60" />
          <div v-for="r in recommends" :key="r.id" class="recommend-item">
            <div class="recommend-title">{{ r.title }}</div>
            <div class="recommend-reason">{{ r.reason }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
  </div>
</template>

<style scoped>
.ai-hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
  padding: 30px 36px 26px;
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(120deg, #14555d 0%, #1d7a72 55%, #5cc1b3 100%);
  box-shadow: 0 16px 40px #1d7a7233;
}

.ai-hero::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: radial-gradient(#ffffff59 0 1.4px, transparent 2.2px), radial-gradient(#ffe9ad4d 0 1px, transparent 1.8px);
  background-size: 180px 130px, 240px 170px;
}

.hero-eyebrow {
  color: #ffd98a;
  font: 700 11px monospace;
  letter-spacing: 3px;
}

.ai-hero h1 {
  margin: 10px 0 8px;
  font: 700 28px/1.35 Georgia, "STSong", serif;
}

.ai-hero h1 em {
  color: #ffd98a;
  font-style: normal;
}

.ai-hero p {
  margin: 0;
  color: #ffffffb3;
  font-size: 13px;
}

@media (max-width: 640px) {
  .ai-hero { padding: 22px 20px; }
  .ai-hero h1 { font-size: 21px; }
}
</style>

<style scoped>
.ai-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
  max-width: 1200px;
  margin: 0 auto;
}

.chat-card { border: 1px solid #e5e5e5; border-radius: 16px; background: #fff; }
.chat-card :deep(.el-card__header) { padding: 16px 22px; border-bottom-color: #ececec; }
.chat-card :deep(.chat-card-body) { padding: 0 0 18px; }

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-weight: 600;
}

.chat-body {
  height: min(580px, calc(100vh - 280px));
  min-height: 440px;
  overflow-y: auto;
  padding: 22px clamp(18px, 6vw, 72px);
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.msg-row {
  display: flex;
}

.msg-row.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 86%;
  font-size: 15px;
  line-height: 1.75;
}

.msg-row.assistant .bubble {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #ececec;
  border-radius: 14px;
  background: #f7f7f8;
  color: #0d0d0d;
}

.msg-row.user .bubble {
  padding: 10px 16px;
  border-radius: 18px;
  background: #f4f4f4;
  color: #0d0d0d;
}

.bubble-text {
  white-space: pre-wrap;
}

.message-image { display: block; max-width: 260px; max-height: 220px; margin-bottom: 8px; border-radius: 6px; object-fit: contain; }

.typing {
  color: #909399;
  font-style: italic;
}

.related {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.related-label {
  color: #909399;
  font-size: 12px;
}

.chat-input-row {
  display: flex;
  gap: 8px;
  margin: 0 clamp(18px, 6vw, 72px);
  padding: 9px 10px 9px 16px;
  border: 1px solid #dedede;
  border-radius: 26px;
  box-shadow: 0 2px 10px #0000000d;
}
.chat-input-row :deep(.el-input__wrapper) { padding: 0; box-shadow: none !important; }
.chat-input-row :deep(.el-button) { border-radius: 50%; }
.chat-input-row > :deep(.el-button--primary) { width: 34px; height: 34px; padding: 0; border-color: #0d0d0d; background: #0d0d0d; }

.image-pending { display: flex; align-items: center; gap: 8px; margin-top: 8px; color: #606266; font-size: 12px; }
.image-pending img { width: 34px; height: 34px; object-fit: cover; border-radius: 4px; }

.side-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-card :deep(.el-card__body) {
  padding-top: 12px;
}

.hot-item {
  padding: 8px 10px;
  border-radius: 6px;
  color: #7c5cd6;
  font-size: 14px;
  cursor: pointer;
}

.hot-item:hover {
  background: #f4effd;
}

.process-form {
  display: flex;
  gap: 8px;
}

.process-select {
  width: 110px;
  flex-shrink: 0;
}

.process-result {
  margin-top: 12px;
}

.process-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.recommend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.recommend-select {
  width: 90px;
}

.recommend-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.recommend-item:last-child {
  border-bottom: none;
}

.recommend-title {
  font-weight: 600;
  color: #303133;
}

.recommend-reason {
  margin-top: 2px;
  color: #909399;
  font-size: 13px;
}

.markdown :deep(p) { margin: 0 0 12px; }
.markdown :deep(p:last-child) { margin-bottom: 0; }
.markdown :deep(h1), .markdown :deep(h2), .markdown :deep(h3), .markdown :deep(h4) { margin: 20px 0 10px; line-height: 1.35; }
.markdown :deep(ul), .markdown :deep(ol) { margin: 10px 0; padding-left: 24px; }
.markdown :deep(li) { margin: 5px 0; }
.markdown :deep(code) { padding: 2px 5px; border-radius: 5px; background: #ececec; font: 13px Consolas, monospace; }
.markdown :deep(pre) { overflow: auto; margin: 14px 0; padding: 14px; border-radius: 8px; color: #f8f8f2; background: #202123; }
.markdown :deep(pre code) { padding: 0; color: inherit; background: transparent; }
.markdown :deep(blockquote) { margin: 14px 0; padding-left: 14px; border-left: 3px solid #b4b4b4; color: #5f5f5f; }
.markdown :deep(a) { color: #0b57d0; }
.markdown :deep(hr) { margin: 20px 0; border: 0; border-top: 1px solid #dedede; }

@media (max-width: 768px) {
  .ai-layout {
    grid-template-columns: 1fr;
  }
  .side-col { display: none; }
  .chat-body { height: calc(100vh - 250px); min-height: 380px; padding-inline: 16px; }
  .chat-input-row { margin-inline: 12px; }
}
</style>

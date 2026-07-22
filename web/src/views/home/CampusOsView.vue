<script setup lang="ts">
/**
 * CampusOS 桌面首页 —— 梦幻暮光主题 + 锁屏登录。
 *
 * 流程四幕：intro（笔记本待机）-> zooming（放大进屏幕）-> lock（Windows 式锁屏登录）-> desktop。
 * 锁屏用真实的 /auth/login 接口，登录态与门户共享（stores/user.ts）。
 * 桌面窗口全部接真实后端：资讯/通知列表详情、今日课程、AI 问答、校园地点。
 * 桌面图标可拖拽/右键删除/拖进拖出文件夹，布局存 localStorage。
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import type { Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell, Calendar, Close, Cpu, CreditCard, DataAnalysis, Delete, Document, EditPen, Folder, FolderOpened,
  FullScreen, Grid, MapLocation, Monitor, Notebook, Reading, Trophy, Wallet,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login as apiLogin } from '@/api/auth'
import { getNewsDetail, pageNews } from '@/api/news'
import type { NewsItem } from '@/api/news'
import { getNoticeDetail, getUnreadCount, markNoticeRead, pageNotices } from '@/api/notice'
import type { NoticeItem } from '@/api/notice'
import { getTodayCourses } from '@/api/course'
import type { CourseSlot } from '@/api/course'
import { chat, getHotQuestions } from '@/api/assistant'
import type { HotQuestion } from '@/api/assistant'
import { getLocations } from '@/api/location'
import type { LocationItem } from '@/api/location'
import { fileUrl, formatDateTime, formatMoney } from '@/utils/format'
import { renderMarkdown } from '@/utils/markdown'
import { getExams } from '@/api/exam'
import type { ExamItem } from '@/api/exam'
import { createPaymentOrder, getPendingPayments } from '@/api/payment'
import type { PaymentItem } from '@/api/payment'
import { cancelLoss, getCardInfo, rechargeCard, reportLoss } from '@/api/card'
import type { CardInfo } from '@/api/card'
import { getScores } from '@/api/score'
import type { ScoreSummary } from '@/api/score'
import CanvasMount from '@/canvas/CanvasMount.vue'
import MinesweeperGame from '@/components/games/MinesweeperGame.vue'
import Game2048 from '@/components/games/Game2048.vue'

type Phase = 'intro' | 'team' | 'canvas' | 'waking' | 'zooming' | 'settling' | 'lock' | 'desktop'
type AppName = 'news' | 'schedule' | 'map' | 'guide' | 'notices' | 'ai' | 'score' | 'exam' | 'payment' | 'card' | 'minesweeper' | 'game2048'

interface DesktopItem {
  id?: string
  app?: AppName
  /** 无独立窗口的应用：双击直接跳门户对应页面 */
  routeTo?: string
  label: string
  suffix: string
  icon: Component
  /** 彩色磁贴背景色（与门户快捷导航一致）；用户文件/文件夹不设，保持玻璃质感 */
  tileColor?: string
  kind: 'folder' | 'file'
  x?: number
  y?: number
  parentId?: string
  content?: string
  mime?: string
  size?: number
}

interface AppWindow {
  id: number
  app?: AppName
  itemId?: string
  x: number
  y: number
  width: number
  height: number
  z: number
  maximized: boolean
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
// 从门户返回根路径时保留已登录用户的桌面会话；只有显式登录入口才显示锁屏。
const phase = ref<Phase>(route.query.login === '1' ? 'lock' : (userStore.isLoggedIn ? 'desktop' : 'intro'))
const selected = ref<string | null>(null)
const suppressedDesktopClicks = new Set<string>()
const now = ref(new Date())
const windows = ref<AppWindow[]>([])
// targetId 有值 = 在某个图标上右键（菜单显示 打开/删除），否则是空白处（新建）
const contextMenu = ref({ open: false, x: 0, y: 0, targetId: undefined as string | undefined })
const isFileOver = ref(false)

// ===== 锁屏登录 =====
const unlocking = ref(false)
const lockForm = reactive({ username: '', password: '' })
const lockLoading = ref(false)
const lockError = ref('')

// 默认桌面：应用一律是「文件」，真正的文件夹只放用户自己的东西（可拖文件进去）。
// 图标与配色对齐门户首页的快捷导航宫格（整合自 CampusOS_a 的 8 个彩色圆角图标），
// 其中成绩/考试/缴费/校园卡没有桌面小窗口，双击直接跳门户对应页面。
const initialApps: DesktopItem[] = [
  { app: 'news', label: '新闻资讯', suffix: '.html', icon: Document, tileColor: '#409EFF', kind: 'file' },
  { app: 'notices', label: '公告通知', suffix: '.md', icon: Bell, tileColor: '#E6A23C', kind: 'file' },
  { app: 'schedule', label: '课程管理', suffix: '.app', icon: Reading, tileColor: '#67C23A', kind: 'file' },
  { app: 'score', label: '成绩查询', suffix: '.app', icon: DataAnalysis, tileColor: '#909399', kind: 'file' },
  { app: 'exam', label: '考试安排', suffix: '.app', icon: Calendar, tileColor: '#F56C6C', kind: 'file' },
  { app: 'payment', label: '校园缴费', suffix: '.app', icon: Wallet, tileColor: '#9B59B6', kind: 'file' },
  { app: 'card', label: '校园卡', suffix: '.app', icon: CreditCard, tileColor: '#1ABC9C', kind: 'file' },
  { app: 'ai', label: 'AI助手', suffix: '.app', icon: Cpu, tileColor: '#3498DB', kind: 'file' },
  { app: 'map', label: '校园地图', suffix: '.app', icon: MapLocation, tileColor: '#42A987', kind: 'file' },
  { app: 'guide', label: '新生手册', suffix: '.md', icon: Notebook, tileColor: '#D4995E', kind: 'file' },
  { app: 'minesweeper', label: '扫雷', suffix: '.game', icon: Grid, tileColor: '#566573', kind: 'file' },
  { app: 'game2048', label: '2048', suffix: '.game', icon: Trophy, tileColor: '#D4A236', kind: 'file' },
  { label: '学习资料', suffix: '', icon: Folder, kind: 'folder' },
]

const apps = ref<DesktopItem[]>([])
// v5：快捷导航彩色磁贴上桌面（新增成绩/考试/缴费/校园卡）；v4 存档的用户文件会自动迁移过来
const desktopStorageKey = 'campus-os-desktop-v6'
const legacyStorageKey = 'campus-os-desktop-v5'

const titles: Record<AppName, string> = {
  news: '新闻资讯.html',
  schedule: '课程管理.app',
  map: '校园地图.app',
  guide: '新生手册.md',
  notices: '公告通知.md',
  ai: 'AI助手.app',
  score: '成绩查询.app',
  exam: '考试安排.app',
  payment: '校园缴费.app',
  card: '校园卡.app',
  minesweeper: '扫雷.game',
  game2048: '2048.game',
}

const guideMarkdown = `# CampusOS 新生手册

欢迎你来到 CampusOS 校园。大学生活的节奏和高中不同：课程安排、住宿服务、缴费通知和校园活动会同时出现。这份手册按报到后的实际顺序整理，建议在入学第一周完整看一遍，并把需要办理的事项逐项记录下来。

## 一、报到当天

1. 到指定报到点完成身份核验，领取校园卡、宿舍钥匙或门禁权限资料。
2. 检查个人信息、院系专业、班级和联系方式是否正确；发现错误请当天联系辅导员或学院教务老师。
3. 入住宿舍后核对床位、家具和水电表状态。有损坏请拍照留存，并通过“校园报修”提交工单。
4. 妥善保管录取材料、缴费凭证和证件复印件。不要把校园卡、身份证和银行卡交给陌生人代管。

## 二、先完成这几项设置

- 使用学号和初始密码登录 CampusOS，首次登录后立即修改密码并绑定常用手机号码。
- 领取或激活校园卡。它通常用于门禁、食堂消费、图书借阅和部分校内缴费，遗失后应第一时间挂失。
- 连接学校官方 Wi-Fi。请通过校园提供的认证页面登录，不要使用来历不明的“加速器”或共享账号。
- 在“个人中心”补全联系方式和紧急联系人；手机号码变更后也请及时更新。

## 三、课程与学习

打开“课程管理”可以查看当天课程；进入“查看整周课表”可按学期和教学周核对全部安排。上课前请确认教学楼、教室和上课时间，提前预留步行时间。实践课、实验课和体育课可能有服装、器材或分组要求，应以任课教师通知为准。

大学课程强调自主学习。建议每门课建立固定的资料目录：课程大纲、课堂笔记、作业、阅读材料和复习提纲分开保存。遇到选课、退课、缓考、补考或成绩复核等问题，先查看教务通知的时间窗口，再联系学院教务办公室，避免错过截止日期。

## 四、考试与成绩

“考试安排”会显示考试日期、时间、楼宇、教室和座位号。考前一周建议再次确认安排，并准备校园卡或学生证、必要文具及课程规定的材料。请至少提前二十分钟到达考场；迟到、替考和携带违规电子设备都可能造成严重后果。

成绩发布后可在“成绩查询”查看课程成绩、学分和绩点。若认为成绩记录有误，应在学校规定的复核期内按流程提交申请，不要仅通过私下消息要求修改成绩。

## 五、缴费与校园生活

“校园缴费”集中展示待缴项目和历史记录。缴费前请核对项目名称、金额、截止日期和收款主体；完成后保存电子回单。遇到陌生二维码、要求私下转账或自称“代缴费”的消息，请先向辅导员或财务部门核实。

宿舍生活中请遵守作息、消防和用电规定。不要在寝室使用违规大功率电器，不占用消防通道，也不要将门禁借给校外人员。水电余额、故障报修、快递点和校医院等服务都可以从门户页面快速进入。

## 六、安全与求助

警惕网络诈骗：陌生链接、刷单兼职、虚假助学金、冒充老师收费和“注销校园贷”都是高发套路。任何涉及验证码、密码、银行卡或转账的要求都应停止操作并核实。紧急情况优先联系学校保卫部门、辅导员和当地紧急服务。

适应新环境需要时间。感到学习压力、人际困扰或情绪持续低落时，可以预约学校心理咨询服务，也可以主动与辅导员、可信赖的老师或同学交流。寻求帮助是正常且有效的选择。

## 七、常用检查清单

- 每周查看一次课表、通知和待办缴费。
- 重要截止日期提前两天设置提醒。
- 离开宿舍时断电、锁门，贵重物品随身保管。
- 学期末备份课程资料和缴费凭证。

祝你在 CampusOS 的校园生活顺利、充实，并逐步建立属于自己的学习节奏。`

/** 按时段问候：桌面问候语「{greeting}，{名字}」 */
const greeting = computed(() => {
  const hour = now.value.getHours()
  if (hour < 5) return '夜深了'
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(() => (userStore.isLoggedIn ? userStore.name : '同学'))

let clockTimer = 0
let wakeTimer = 0
let settleTimer = 0
let zoomTimer = 0
let nextWindowId = 1
let topZ = 10

function launch() {
  if (phase.value !== 'intro') return
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    phase.value = 'lock'
    return
  }
  phase.value = 'waking'
  wakeTimer = window.setTimeout(() => phase.value = 'zooming', 420)
  settleTimer = window.setTimeout(() => phase.value = 'settling', 3420)
  zoomTimer = window.setTimeout(() => phase.value = 'lock', 3820)
}

function openIntroPanel(panel: 'team') {
  if (phase.value === 'intro') phase.value = panel
}

/** 锁屏账号密码登录（真实接口，登录态与门户共享） */
async function lockLogin() {
  if (lockLoading.value) return
  if (!lockForm.username.trim() || !lockForm.password) {
    lockError.value = '请输入账号和密码'
    return
  }
  lockLoading.value = true
  lockError.value = ''
  try {
    const result = await apiLogin({ username: lockForm.username.trim(), password: lockForm.password })
    userStore.login(result)
    loadDesktop()
    lockForm.password = ''
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (redirect.startsWith('/') && !redirect.startsWith('//')) router.replace(redirect)
    else unlock()
  } catch (e) {
    lockError.value = (e as Error).message || '登录失败，请重试'
  } finally {
    lockLoading.value = false
  }
}

/** 解锁进桌面（登录成功或已有登录态） */
function unlock() {
  if (phase.value !== 'lock' || unlocking.value) return
  unlocking.value = true
  window.setTimeout(() => {
    phase.value = 'desktop'
    unlocking.value = false
    // 已打开的窗口按需补拉数据
    windows.value.forEach(win => ensureAppData(win.app))
  }, 460)
}

/** 回到锁屏（右上角「锁定」） */
function lockNow() {
  if (phase.value !== 'desktop') return
  lockError.value = ''
  phase.value = 'lock'
}

/** 锁屏上的「切换账号」：清登录态，显示登录表单 */
function switchAccount() {
  userStore.logout()
  lockForm.username = ''
  lockForm.password = ''
  lockError.value = ''
}

function reset() {
  window.clearTimeout(wakeTimer)
  window.clearTimeout(settleTimer)
  window.clearTimeout(zoomTimer)
  windows.value = []
  selected.value = null
  unlocking.value = false
  lockError.value = ''
  phase.value = 'intro'
}

/** 退出当前账号并回到启动页（右上角「退出」） */
function exitToIntro() {
  userStore.logout()
  lockForm.username = ''
  lockForm.password = ''
  reset()
}

type SavedItem = Omit<DesktopItem, 'icon' | 'tileColor'>

/** 存档还原：图标/磁贴色不入库，按 app/routeTo 对回 initialApps；文件夹一律 Folder */
function restoreItems(saved: SavedItem[]): DesktopItem[] {
  return saved.filter(item => !item.routeTo || userStore.canAccess(item.routeTo)).map(item => {
    const migratedApp = ({ '/score': 'score', '/exam': 'exam', '/payment': 'payment', '/card': 'card' } as Record<string, AppName>)[item.routeTo || '']
    const normalized = migratedApp ? { ...item, app: migratedApp, routeTo: undefined } : item
    const proto = initialApps.find(app =>
      (normalized.app && app.app === normalized.app) || (normalized.routeTo && app.routeTo === normalized.routeTo))
    return {
      ...normalized,
      icon: normalized.kind === 'folder' ? Folder : (proto?.icon ?? Document),
      tileColor: normalized.kind === 'folder' ? undefined : proto?.tileColor,
    }
  })
}

/** 全新默认桌面：图标按列排布 */
function defaultDesktop(): DesktopItem[] {
  const rows = Math.max(1, Math.floor((window.innerHeight - 40) / 104))
  return initialApps.filter(item => !item.routeTo || userStore.canAccess(item.routeTo)).map((item, index) => ({
    ...item,
    id: `sys-${index}`,
    x: 18 + Math.floor(index / rows) * 96,
    y: 18 + (index % rows) * 104,
  }))
}

function loadDesktop() {
  try {
    const saved = JSON.parse(localStorage.getItem(desktopStorageKey) || 'null') as SavedItem[] | null
    if (saved?.length) {
      apps.value = restoreItems(saved)
      normalizeLayout()
      return
    }
    // v4 存档迁移：应用图标换成新磁贴排布，用户自己的文件/文件夹（含内容）原样保留
    const legacy = JSON.parse(localStorage.getItem(legacyStorageKey) || 'null') as SavedItem[] | null
    if (legacy?.length) {
      const carried = legacy.filter(item => !item.app)
      const hasTopFolder = carried.some(item => item.kind === 'folder' && !item.parentId)
      const defaults = defaultDesktop().filter(item => item.app || item.routeTo || !hasTopFolder)
      apps.value = [...defaults, ...restoreItems(carried)]
      normalizeLayout()
      saveDesktop()
      localStorage.removeItem(legacyStorageKey)
      return
    }
  } catch { /* use the default desktop */ }

  apps.value = defaultDesktop()
}

function saveDesktop() {
  try {
    localStorage.setItem(desktopStorageKey, JSON.stringify(apps.value.map(({ icon, tileColor, ...item }) => item)))
  } catch {
    window.alert('桌面存储空间不足（浏览器本地存储上限约 5MB），请删除部分大文件后重试。')
  }
}

function showContextMenu(event: MouseEvent) {
  if (phase.value !== 'desktop') return
  event.preventDefault()
  // 桌面图标或文件夹窗口里的子项都能右键
  const hit = (event.target as HTMLElement).closest('.desktop-icon, .folder-child') as HTMLElement | null
  const target = desktopItem(hit?.dataset.id)
  if (target) selected.value = `${target.label}-${target.suffix}`
  contextMenu.value = {
    open: true,
    x: Math.min(event.clientX, window.innerWidth - 180),
    y: Math.min(event.clientY, window.innerHeight - 150),
    targetId: target?.id,
  }
}

function createDesktopItem(kind: 'folder' | 'file') {
  const sameKind = apps.value.filter(item => !item.app && !item.routeTo && item.kind === kind).length
  const cell = nearestFreeCell(contextMenu.value.x, contextMenu.value.y, takenCells())
  apps.value.push({
    id: `user-${Date.now()}`,
    label: kind === 'folder' ? `新建文件夹${sameKind ? ` (${sameKind + 1})` : ''}` : `新建文本文档${sameKind ? ` (${sameKind + 1})` : ''}`,
    suffix: kind === 'file' ? '.txt' : '',
    icon: kind === 'folder' ? Folder : Document,
    kind,
    x: cell.x,
    y: cell.y,
    content: '',
  })
  contextMenu.value.open = false
  saveDesktop()
}

/** 删除桌面项：文件夹连同里面的东西一起删，并关掉相关窗口 */
function deleteDesktopItem(id?: string) {
  if (!id) return
  const doomed = new Set<string>()
  const collect = (targetId: string) => {
    doomed.add(targetId)
    apps.value.forEach(item => {
      if (item.parentId === targetId && item.id && !doomed.has(item.id)) collect(item.id)
    })
  }
  collect(id)
  apps.value = apps.value.filter(item => !item.id || !doomed.has(item.id))
  windows.value = windows.value.filter(item => !item.itemId || !doomed.has(item.itemId))
  contextMenu.value.open = false
  selected.value = null
  saveDesktop()
}

function renameFolder(id?: string) {
  const item = desktopItem(id)
  if (!item || item.kind !== 'folder') return
  const name = window.prompt('请输入文件夹名称', item.label)?.trim()
  if (!name || name === item.label) return
  item.label = name.slice(0, 40)
  selected.value = `${item.label}-${item.suffix}`
  contextMenu.value.open = false
  saveDesktop()
}

function desktopItem(id?: string) {
  return apps.value.find(item => item.id === id)
}

function childrenOf(id?: string) {
  return apps.value.filter(item => item.parentId === id)
}

/** 桌面网格吸附（图标基准 18px、格宽 96/高 104，与初始布局一致） */
function snapX(px: number) {
  return Math.min(Math.max(0, 18 + Math.round((px - 18) / 96) * 96), window.innerWidth - 88)
}

function snapY(px: number) {
  return Math.min(Math.max(8, 18 + Math.round((px - 18) / 104) * 104), window.innerHeight - 120)
}

/** 桌面上已被占用的格子集合（可排除拖拽中的图标自己） */
function takenCells(excludeId?: string) {
  return new Set(apps.value
    .filter(item => !item.parentId && item.id !== excludeId && item.x !== undefined)
    .map(item => `${item.x},${item.y}`))
}

/**
 * 以落点为中心找最近的空网格 —— 图标不允许重叠。
 * 从落点格子按半径一圈圈向外扫，同半径取直线距离最近的；全满时退回吸附位兜底。
 */
function nearestFreeCell(px: number, py: number, taken: Set<string>): { x: number; y: number } {
  const cols = Math.max(1, Math.floor((window.innerWidth - 100) / 96) + 1)
  const rows = Math.max(1, Math.floor((window.innerHeight - 130) / 104) + 1)
  const startCol = Math.min(Math.max(0, Math.round((px - 18) / 96)), cols - 1)
  const startRow = Math.min(Math.max(0, Math.round((py - 18) / 104)), rows - 1)

  for (let radius = 0; radius <= Math.max(cols, rows); radius++) {
    let best: { x: number; y: number; d: number } | null = null
    for (let dc = -radius; dc <= radius; dc++) {
      for (let dr = -radius; dr <= radius; dr++) {
        if (Math.max(Math.abs(dc), Math.abs(dr)) !== radius) continue
        const col = startCol + dc
        const row = startRow + dr
        if (col < 0 || row < 0 || col >= cols || row >= rows) continue
        const x = 18 + col * 96
        const y = 18 + row * 104
        if (taken.has(`${x},${y}`)) continue
        const d = dc * dc + dr * dr
        if (!best || d < best.d) best = { x, y, d }
      }
    }
    if (best) return { x: best.x, y: best.y }
  }
  return { x: snapX(px), y: snapY(py) }
}

/** 整理桌面布局：全部吸附到网格，先到先得，重叠的往最近空格挪（加载/迁移时兜底） */
function normalizeLayout() {
  const taken = new Set<string>()
  apps.value.forEach(item => {
    if (item.parentId) return
    let x = snapX(item.x ?? 18)
    let y = snapY(item.y ?? 18)
    if (taken.has(`${x},${y}`)) ({ x, y } = nearestFreeCell(x, y, taken))
    item.x = x
    item.y = y
    taken.add(`${x},${y}`)
  })
}

/** 目标文件夹是不是自己或自己的子孙（防止 A 放进 B、B 又放进 A 的循环） */
function wouldCycle(draggedId: string | undefined, targetFolderId: string) {
  let cursor: string | undefined = targetFolderId
  while (cursor) {
    if (cursor === draggedId) return true
    cursor = desktopItem(cursor)?.parentId
  }
  return false
}

/**
 * 找落点对应的文件夹：桌面上的文件夹图标、打开的文件夹窗口、窗口里的子文件夹都算。
 * 返回 undefined 表示落点不是（合法的）文件夹。
 */
function dropTargetFolder(el: Element | null, dragged: DesktopItem): DesktopItem | undefined {
  if (!el) return undefined
  const candidates = [
    (el.closest('.desktop-icon, .folder-child') as HTMLElement | null)?.dataset.id,
    (el.closest('[data-folder-target]') as HTMLElement | null)?.dataset.folderTarget,
  ]
  for (const id of candidates) {
    const target = desktopItem(id)
    if (target?.id && target.kind === 'folder' && !target.app
      && target.id !== dragged.id && !wouldCycle(dragged.id, target.id)) {
      return target
    }
  }
  return undefined
}

/** 桌面上第一个没被占用的网格位（从文件夹往外放东西时用） */
function firstFreeSlot() {
  return nearestFreeCell(18, 18, takenCells())
}

/** 右键菜单「移到桌面」：从文件夹里拿出来，放到第一个空格子 */
function moveToDesktop(id?: string) {
  const item = desktopItem(id)
  if (!item) return
  const slot = firstFreeSlot()
  item.parentId = undefined
  item.x = slot.x
  item.y = slot.y
  contextMenu.value.open = false
  saveDesktop()
}

function openDesktopItem(item: DesktopItem) {
  if (item.routeTo) void router.push(item.routeTo)
  else if (item.app) openApp(item.app)
  else openApp(undefined, item.id)
}

/** 单击打开，拖动图标结束后的 click 则只用于落位，不误触发打开。 */
function handleDesktopItemClick(item: DesktopItem) {
  if (item.id && suppressedDesktopClicks.delete(item.id)) return
  selected.value = `${item.label}-${item.suffix}`
  openDesktopItem(item)
}

function openContextTarget() {
  const item = desktopItem(contextMenu.value.targetId)
  if (item) openDesktopItem(item)
  contextMenu.value.open = false
}

function handleFileDrag(event: DragEvent) {
  if (phase.value !== 'desktop' || !event.dataTransfer?.types.includes('Files')) return
  event.preventDefault()
  event.dataTransfer.dropEffect = 'copy'
  isFileOver.value = true
}

/** 读文件为 DataURL：连内容一起存进 localStorage，刷新后图片可预览、文件可下载 */
function readAsDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

const MAX_TEXT_SIZE = 262_144 // 256KB：文本存原文
const MAX_BINARY_SIZE = 1_572_864 // 1.5MB：其它文件存 DataURL（base64 后约 2MB，给 localStorage 留余量）

async function importDesktopFiles(event: DragEvent) {
  if (phase.value !== 'desktop' || !event.dataTransfer?.files.length) return
  event.preventDefault()
  isFileOver.value = false
  const files = [...event.dataTransfer.files].slice(0, 20)
  const oversized: string[] = []

  for (const [index, file] of files.entries()) {
    const dot = file.name.lastIndexOf('.')
    const suffix = dot > 0 ? file.name.slice(dot, dot + 12) : ''
    const label = (dot > 0 ? file.name.slice(0, dot) : file.name).slice(0, 80) || '未命名文件'
    const markdownFile = /\.(md|markdown)$/i.test(file.name)
    const textFile = file.type.startsWith('text/') || markdownFile || /\.(json|csv|xml|js|ts|css|vue|java|py|log)$/i.test(file.name)

    // 内容持久化：文本存原文、其它存 DataURL；超限只保留文件信息并提示
    let content: string | undefined
    if (textFile && file.size <= MAX_TEXT_SIZE) content = await file.text().catch(() => undefined)
    else if (!textFile && file.size <= MAX_BINARY_SIZE) content = await readAsDataUrl(file).catch(() => undefined)
    if (content === undefined) oversized.push(file.name)

    // 每个文件都落在离鼠标最近的空格子上，绝不与已有图标重叠
    const cell = nearestFreeCell(event.clientX, event.clientY, takenCells())
    apps.value.push({
      id: `drop-${Date.now()}-${index}`,
      label,
      suffix,
      icon: Document,
      kind: 'file',
      x: cell.x,
      y: cell.y,
      content,
      mime: file.type || (markdownFile ? 'text/markdown' : 'application/octet-stream'),
      size: file.size,
    })
  }
  saveDesktop()
  if (oversized.length) {
    ElMessage.warning(`${oversized.length} 个文件超出保存上限（文本 256KB / 其它 1.5MB），仅保留了文件信息：${oversized.join('、')}`)
  }
}

function endFileDrag(event: DragEvent) {
  if (!event.relatedTarget) isFileOver.value = false
}

function startDesktopItemDrag(event: PointerEvent, item: DesktopItem) {
  if (event.button !== 0 || item.x === undefined || item.y === undefined) return
  const button = event.currentTarget as HTMLElement
  const startX = event.clientX
  const startY = event.clientY
  const originX = item.x
  const originY = item.y
  let moved = false

  const move = (moveEvent: PointerEvent) => {
    if (Math.hypot(moveEvent.clientX - startX, moveEvent.clientY - startY) > 4) moved = true
    item.x = Math.min(Math.max(0, originX + moveEvent.clientX - startX), window.innerWidth - 88)
    item.y = Math.min(Math.max(8, originY + moveEvent.clientY - startY), window.innerHeight - 120)
    moveEvent.preventDefault()
  }
  const stop = (upEvent: PointerEvent) => {
    if (moved && item.id) suppressedDesktopClicks.add(item.id)
    button.style.pointerEvents = 'none'
    const el = document.elementFromPoint(upEvent.clientX, upEvent.clientY)
    button.style.pointerEvents = ''
    const folder = dropTargetFolder(el, item)
    if (folder) {
      item.parentId = folder.id
    } else {
      // 吸附到最近的空格子：格子被占就往外挪，图标不允许叠在一起
      const cell = nearestFreeCell(item.x ?? originX, item.y ?? originY, takenCells(item.id))
      item.x = cell.x
      item.y = cell.y
    }
    saveDesktop()
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', stop)
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', stop, { once: true })
}

/**
 * 从文件夹窗口往外拖：超过 6px 才认定为拖拽（不影响双击打开），
 * 拖动时跟一个小幽灵标签，松手时按落点决定去向：
 * 别的文件夹 -> 挪进去；桌面空白 -> 拿出来放在落点；窗口内部 -> 不动。
 */
function startFolderChildDrag(event: PointerEvent, item: DesktopItem) {
  if (event.button !== 0) return
  const startX = event.clientX
  const startY = event.clientY
  let ghost: HTMLElement | null = null

  const move = (moveEvent: PointerEvent) => {
    if (!ghost && Math.hypot(moveEvent.clientX - startX, moveEvent.clientY - startY) > 6) {
      ghost = document.createElement('div')
      ghost.textContent = `${item.label}${item.suffix}`
      // 幽灵标签挂在 body 上，吃不到 scoped 样式，直接写行内样式
      Object.assign(ghost.style, {
        position: 'fixed', zIndex: '2000', padding: '6px 12px', borderRadius: '999px',
        color: '#fff', background: '#241a4ae6', font: '12px sans-serif', pointerEvents: 'none',
        boxShadow: '0 10px 26px #0b063080', backdropFilter: 'blur(8px)',
      })
      document.body.appendChild(ghost)
    }
    if (ghost) {
      ghost.style.left = `${moveEvent.clientX + 12}px`
      ghost.style.top = `${moveEvent.clientY + 12}px`
      moveEvent.preventDefault()
    }
  }
  const stop = (upEvent: PointerEvent) => {
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', stop)
    if (!ghost) return
    ghost.remove()
    const el = document.elementFromPoint(upEvent.clientX, upEvent.clientY)
    const folder = dropTargetFolder(el, item)
    if (folder) {
      item.parentId = folder.id
    } else if (!el?.closest('.app-window')) {
      item.parentId = undefined
      const cell = nearestFreeCell(upEvent.clientX - 44, upEvent.clientY - 48, takenCells(item.id))
      item.x = cell.x
      item.y = cell.y
    }
    saveDesktop()
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', stop, { once: true })
}

function updateDocument(id: string | undefined, event: Event) {
  const item = desktopItem(id)
  if (!item) return
  item.content = (event.target as HTMLTextAreaElement).value
  saveDesktop()
}

/** 内容是 DataURL 图片：文件窗口里直接预览 */
function isImageItem(item?: DesktopItem) {
  return !!item?.content?.startsWith('data:image/')
}

/** 内容是其它 DataURL 二进制：文件窗口里给下载入口 */
function isBinaryItem(item?: DesktopItem) {
  return !!item?.content?.startsWith('data:') && !isImageItem(item)
}

function isMarkdownItem(item?: DesktopItem) {
  if (!item || typeof item.content !== 'string' || item.content.startsWith('data:')) return false
  // 兼容旧版桌面存档：早期导入项可能没有正确写入 suffix，但仍保留了 MIME 类型或完整名称。
  return /\.md(?:own)?$/i.test(`${item.label}${item.suffix}`)
    || /markdown/i.test(item.mime || '')
}

function windowTitle(item: AppWindow) {
  return item.app ? titles[item.app] : `${desktopItem(item.itemId)?.label || '文件'}${desktopItem(item.itemId)?.suffix || ''}`
}

function openApp(app?: AppName, itemId?: string) {
  // Desktop apps and files are single-instance: reopening brings the existing window forward.
  const existing = windows.value.find(window => window.app === app && window.itemId === itemId)
  if (existing) {
    focusWindow(existing)
    return
  }
  ensureAppData(app)
  const width = Math.min(960, Math.max(420, window.innerWidth - 80))
  const height = Math.min(680, Math.max(360, window.innerHeight - 112))
  const offset = (windows.value.length % 6) * 28
  const baseX = Math.max(16, (window.innerWidth - width) / 2)
  const baseY = Math.max(24, (window.innerHeight - height) / 2)
  windows.value.push({
    id: nextWindowId++,
    app,
    itemId,
    width,
    height,
    x: Math.min(baseX + offset, window.innerWidth - width - 16),
    y: Math.min(baseY + offset, window.innerHeight - height - 16),
    z: ++topZ,
    maximized: false,
  })
}

function windowStyle(item: AppWindow) {
  return item.maximized ? { zIndex: item.z } : {
    left: `${item.x}px`,
    top: `${item.y}px`,
    width: `${item.width}px`,
    height: `${item.height}px`,
    zIndex: item.z,
  }
}

function focusWindow(item: AppWindow) {
  item.z = ++topZ
}

function closeWindow(id: number) {
  windows.value = windows.value.filter(item => item.id !== id)
}

function startDrag(event: PointerEvent, item: AppWindow) {
  if (item.maximized || (event.target as HTMLElement).closest('.window-controls')) return
  const startX = event.clientX
  const startY = event.clientY
  const originX = item.x
  const originY = item.y

  const move = (moveEvent: PointerEvent) => {
    item.x = Math.min(Math.max(0, originX + moveEvent.clientX - startX), window.innerWidth - 180)
    item.y = Math.min(Math.max(8, originY + moveEvent.clientY - startY), window.innerHeight - 80)
  }
  const stop = () => {
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', stop)
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', stop, { once: true })
}

// ============================================================
// 窗口真实数据：打开窗口时按需拉后端接口，失败静默（request.ts 会弹提示）
// ============================================================

const newsList = ref<NewsItem[]>([])
const newsLoaded = ref(false)
const newsActive = ref<NewsItem | null>(null)

const noticeList = ref<NoticeItem[]>([])
const noticeLoaded = ref(false)
const noticeActive = ref<NoticeItem | null>(null)
const noticeUnread = ref(0)

const todayCourses = ref<CourseSlot[]>([])
const todayLoaded = ref(false)

const hotQuestions = ref<HotQuestion[]>([])
interface AiMessage { role: 'user' | 'assistant'; content: string; related?: string[] }
const aiMessages = ref<AiMessage[]>([
  { role: 'assistant', content: '你好，我是校园智慧助手。可以问我奖学金、校园卡、图书馆这些校园事务。' },
])
const aiInput = ref('')
const aiSending = ref(false)
const aiBodyRef = ref<HTMLElement | null>(null)

const locations = ref<LocationItem[]>([])
const locationsLoaded = ref(false)
const scoreSummary = ref<ScoreSummary | null>(null)
const exams = ref<ExamItem[]>([])
const pendingPayments = ref<PaymentItem[]>([])
const payingPaymentId = ref<number | null>(null)
const campusCard = ref<CardInfo | null>(null)
const cardBusy = ref(false)

function ensureAppData(app?: AppName) {
  if (app === 'news' && !newsLoaded.value) fetchWindowNews()
  if (app === 'notices' && !noticeLoaded.value) fetchWindowNotices()
  if (app === 'schedule' && userStore.isLoggedIn && !todayLoaded.value) fetchWindowToday()
  if (app === 'ai' && !hotQuestions.value.length) fetchWindowHot()
  if (app === 'map' && !locationsLoaded.value) fetchWindowLocations()
  if (app === 'score' && !scoreSummary.value) fetchWindowScores()
  if (app === 'exam' && !exams.value.length) fetchWindowExams()
  if (app === 'payment' && !pendingPayments.value.length) fetchWindowPayments()
  if (app === 'card' && !campusCard.value) fetchWindowCard()
}

async function fetchWindowScores() { scoreSummary.value = await getScores() }
async function fetchWindowExams() { exams.value = await getExams() }
async function fetchWindowPayments() { pendingPayments.value = await getPendingPayments() }
async function payFromDesktop(paymentId: number) {
  payingPaymentId.value = paymentId
  try {
    await createPaymentOrder({ paymentId, payMethod: 'WECHAT' })
    ElMessage.success('缴费订单已创建')
    await fetchWindowPayments()
  } finally { payingPaymentId.value = null }
}
async function fetchWindowCard() { campusCard.value = await getCardInfo() }
async function rechargeCardFromDesktop() {
  cardBusy.value = true
  try {
    campusCard.value = await rechargeCard({ amount: 50, payMethod: 'WECHAT' })
    ElMessage.success('校园卡已充值 50 元')
  } finally { cardBusy.value = false }
}
async function toggleCardLoss() {
  if (!campusCard.value) return
  cardBusy.value = true
  try {
    campusCard.value = campusCard.value.status === '正常' ? await reportLoss() : await cancelLoss()
    ElMessage.success(campusCard.value.status === '正常' ? '校园卡已解挂' : '校园卡已挂失')
  } finally { cardBusy.value = false }
}

async function fetchWindowNews() {
  newsLoaded.value = true
  try {
    newsList.value = (await pageNews({ pageNum: 1, pageSize: 8 })).list
  } catch { newsLoaded.value = false }
}

async function openWindowNews(id: number) {
  try {
    newsActive.value = await getNewsDetail(id)
  } catch { /* 提示已由 request.ts 弹出 */ }
}

async function fetchWindowNotices() {
  noticeLoaded.value = true
  try {
    noticeList.value = (await pageNotices({ page: 1, size: 8 })).list
    if (userStore.isLoggedIn) noticeUnread.value = (await getUnreadCount()).count
  } catch { noticeLoaded.value = false }
}

async function openWindowNotice(id: number) {
  try {
    noticeActive.value = await getNoticeDetail(id)
    if (userStore.isLoggedIn) {
      markNoticeRead(id).then(async () => {
        noticeUnread.value = (await getUnreadCount()).count
      }).catch(() => {})
    }
  } catch { /* 同上 */ }
}

async function fetchWindowToday() {
  todayLoaded.value = true
  try {
    todayCourses.value = await getTodayCourses()
  } catch { todayLoaded.value = false }
}

async function fetchWindowHot() {
  try {
    hotQuestions.value = await getHotQuestions()
  } catch { /* 热门问题失败不影响聊天 */ }
}

function showWindowHot(question: HotQuestion) {
  aiMessages.value.push({ role: 'user', content: question.question })
  aiMessages.value.push({ role: 'assistant', content: question.answer })
  scrollAiToBottom()
}

async function sendAi(question?: string) {
  const text = (question ?? aiInput.value).trim()
  if (!text || aiSending.value || !userStore.isLoggedIn) return
  aiMessages.value.push({ role: 'user', content: text })
  aiInput.value = ''
  aiSending.value = true
  scrollAiToBottom()
  try {
    const reply = await chat(text)
    aiMessages.value.push({ role: 'assistant', content: reply.answer, related: reply.relatedQuestions })
  } catch {
    aiMessages.value.push({ role: 'assistant', content: '抱歉，我暂时无法回答，请稍后再试。' })
  } finally {
    aiSending.value = false
    scrollAiToBottom()
  }
}

function scrollAiToBottom() {
  window.setTimeout(() => aiBodyRef.value?.scrollTo({ top: aiBodyRef.value.scrollHeight, behavior: 'smooth' }), 30)
}

async function fetchWindowLocations() {
  locationsLoaded.value = true
  try {
    locations.value = await getLocations()
  } catch { locationsLoaded.value = false }
}

function handleKey(event: KeyboardEvent) {
  if (event.key === 'Enter' && phase.value === 'intro') launch()
  if (event.key === 'Enter' && phase.value === 'lock' && userStore.isLoggedIn) unlock()
  if (event.key === 'Escape' && phase.value === 'desktop' && windows.value.length) {
    const top = [...windows.value].sort((a, b) => b.z - a.z)[0]
    closeWindow(top.id)
  }
}

function closeDesktopMenus(event: PointerEvent) {
  if (!(event.target as Element).closest('.desktop-menu')) contextMenu.value.open = false
}

onMounted(() => {
  loadDesktop()
  window.addEventListener('keydown', handleKey)
  window.addEventListener('contextmenu', showContextMenu)
  window.addEventListener('pointerdown', closeDesktopMenus)
  window.addEventListener('dragover', handleFileDrag)
  window.addEventListener('dragleave', endFileDrag)
  window.addEventListener('drop', importDesktopFiles)
  clockTimer = window.setInterval(() => now.value = new Date(), 30_000)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKey)
  window.removeEventListener('contextmenu', showContextMenu)
  window.removeEventListener('pointerdown', closeDesktopMenus)
  window.removeEventListener('dragover', handleFileDrag)
  window.removeEventListener('dragleave', endFileDrag)
  window.removeEventListener('drop', importDesktopFiles)
  window.clearInterval(clockTimer)
  window.clearTimeout(wakeTimer)
  window.clearTimeout(settleTimer)
  window.clearTimeout(zoomTimer)
})
</script>

<template>
  <main class="os-scene" :class="`phase-${phase}`">
    <section v-if="phase !== 'canvas'" class="intro-copy">
      <span>智慧校园 · 一站式服务</span>
      <h1>CampusOS</h1>
      <p>课程、资讯与校园服务，都在这一块屏幕里。</p>
    </section>

    <div v-if="phase !== 'canvas'" class="computer">
      <div class="screen-frame">
        <div
          class="screen"
          :class="{ launchable: phase === 'intro' }"
          :role="phase === 'intro' ? 'button' : undefined"
          :tabindex="phase === 'intro' ? 0 : undefined"
          :aria-label="phase === 'intro' ? '启动 CampusOS' : undefined"
          @click="launch"
        >
          <!-- 待机/开机屏：一轮月亮慢慢醒来 -->
          <section class="boot-screen" aria-hidden="true">
            <div class="boot-inner">
              <i class="boot-moon" />
              <p class="boot-text">正在唤醒 CampusOS</p>
              <span class="boot-bar"><i /></span>
            </div>
          </section>

          <section class="desktop-surface" aria-label="CampusOS 桌面">
            <!-- 暮光天空：极光、月亮、云、流星（纯装饰） -->
            <div class="sky" aria-hidden="true">
              <i class="aurora a1" /><i class="aurora a2" /><i class="aurora a3" />
              <i class="moon" />
              <i class="cloud c1" /><i class="cloud c2" /><i class="cloud c3" />
              <i class="shooting" />
            </div>
            <div class="moving-stars layer-one" />
            <div class="moving-stars layer-two" />
            <div class="hills" aria-hidden="true"><i /><i /><i /></div>

            <div v-if="isFileOver" class="drop-overlay">释放到“我的桌面”</div>

            <template v-if="phase === 'desktop'">
              <!-- 右上角玻璃胶囊：进入门户 / 锁定 / 重启动画 -->
              <nav class="corner-actions" aria-label="快捷操作">
                <button type="button" @click="router.push('/home')">进入门户 ↗</button>
                <button type="button" title="回到锁屏" @click="lockNow">锁定</button>
                <button type="button" title="返回启动页" @click="reset">重启</button>
                <button type="button" title="退出账号并返回启动页" @click.stop="exitToIntro">退出</button>
              </nav>

              <!-- 锁屏式时钟 + 按用户名问候 -->
              <div class="desktop-greeting">
                <span class="greet-date">{{ now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }) }}</span>
                <strong class="greet-clock">{{ now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }) }}</strong>
                <h2 class="greet-line">{{ greeting }}，{{ displayName }}</h2>
                <p class="greet-sub">今天也要稳稳地向前走。</p>
              </div>

              <section class="desktop-icons" aria-label="桌面文件">
                <button
                  v-for="item in apps.filter(item => !item.parentId)"
                  :key="item.id"
                  class="desktop-icon"
                  :data-id="item.id"
                  :style="{ left: `${item.x}px`, top: `${item.y}px` }"
                  type="button"
                  :class="{ selected: selected === `${item.label}-${item.suffix}` }"
                  @pointerdown.stop="startDesktopItemDrag($event, item)"
                  @click="handleDesktopItemClick(item)"
                >
                  <!-- 应用渲染成彩色磁贴（与门户快捷导航同款配色），文件夹/用户文件保持玻璃质感 -->
                  <span
                    class="file-icon"
                    :class="[item.kind, { 'app-tile': item.tileColor }]"
                    :style="item.tileColor ? { background: item.tileColor } : undefined"
                  >
                    <component :is="item.kind === 'folder' ? Folder : item.icon" />
                    <small v-if="item.suffix">{{ item.suffix }}</small>
                  </span>
                  <span>{{ item.label }}</span>
                </button>
              </section>
            </template>

            <!-- 右键菜单：图标上 = 打开/移到桌面/删除；空白处 = 新建 -->
            <menu
              v-if="contextMenu.open"
              class="desktop-menu"
              :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
              @pointerdown.stop
            >
              <template v-if="contextMenu.targetId">
                <button type="button" @click="openContextTarget"><FolderOpened />打开</button>
                <button
                  v-if="desktopItem(contextMenu.targetId)?.kind === 'folder'"
                  type="button"
                  @click="renameFolder(contextMenu.targetId)"
                ><EditPen />重命名</button>
                <button
                  v-if="desktopItem(contextMenu.targetId)?.parentId"
                  type="button"
                  @click="moveToDesktop(contextMenu.targetId)"
                ><Monitor />移到桌面</button>
                <button type="button" class="danger" @click="deleteDesktopItem(contextMenu.targetId)"><Delete />删除</button>
              </template>
              <template v-else>
                <button type="button" @click="createDesktopItem('folder')"><Folder />新建文件夹</button>
                <button type="button" @click="createDesktopItem('file')"><Document />新建文本文档</button>
              </template>
            </menu>

            <section
              v-for="item in windows"
              :key="item.id"
              class="app-window"
              :class="{ maximized: item.maximized, 'markdown-window': !item.app && isMarkdownItem(desktopItem(item.itemId)) }"
              :style="windowStyle(item)"
              :data-folder-target="!item.app && desktopItem(item.itemId)?.kind === 'folder' ? item.itemId : undefined"
              aria-live="polite"
              @pointerdown="focusWindow(item)"
            >
              <header class="window-titlebar" @pointerdown="startDrag($event, item)" @dblclick="item.maximized = !item.maximized">
                <div class="window-controls">
                  <button type="button" title="关闭" aria-label="关闭窗口" @click="closeWindow(item.id)"><Close /></button>
                  <button type="button" title="最大化" aria-label="最大化窗口" @click="item.maximized = !item.maximized"><FullScreen /></button>
                </div>
                <strong>{{ windowTitle(item) }}</strong>
                <span />
              </header>

              <div class="window-body">
                <!-- 校园资讯：真实列表 + 窗口内详情 -->
                <div v-if="item.app === 'news'" class="win-pane">
                  <div v-if="newsActive" class="win-detail">
                    <button class="win-back" type="button" @click="newsActive = null">‹ 返回列表</button>
                    <h4>{{ newsActive.title }}</h4>
                    <p class="win-meta">
                      <span class="win-chip">{{ newsActive.category }}</span>
                      {{ newsActive.author }} · {{ formatDateTime(newsActive.publishedAt) }} · {{ newsActive.viewCount }} 次浏览
                    </p>
                    <div class="win-content">{{ newsActive.content }}</div>
                  </div>
                  <template v-else>
                    <p v-if="!newsList.length" class="win-empty">正在加载校园资讯…（后端未启动时这里会是空的）</p>
                    <button
                      v-for="news in newsList"
                      :key="news.id"
                      class="win-row"
                      type="button"
                      @click="openWindowNews(news.id)"
                    >
                      <span class="win-row-title">{{ news.title }}</span>
                      <span class="win-row-meta"><i class="win-chip">{{ news.category }}</i>{{ formatDateTime(news.publishedAt).slice(0, 10) }}</span>
                    </button>
                    <footer class="win-footer">
                      <button type="button" @click="router.push('/news')">进入资讯中心 ↗</button>
                    </footer>
                  </template>
                </div>

                <!-- 通知中心：真实公告 + 已读 -->
                <div v-else-if="item.app === 'notices'" class="win-pane">
                  <div v-if="noticeActive" class="win-detail">
                    <button class="win-back" type="button" @click="noticeActive = null">‹ 返回列表</button>
                    <h4>{{ noticeActive.title }}</h4>
                    <p class="win-meta">
                      <span class="win-chip" :class="noticeActive.type === 'SCHOOL' ? 'rose' : ''">{{ noticeActive.type === 'SCHOOL' ? '学校' : '院系' }}</span>
                      {{ noticeActive.department }} · {{ formatDateTime(noticeActive.createTime) }}
                    </p>
                    <div class="win-content">{{ noticeActive.content }}</div>
                  </div>
                  <template v-else>
                    <p v-if="userStore.isLoggedIn && noticeUnread > 0" class="win-unread">你有 {{ noticeUnread }} 条未读通知</p>
                    <p v-if="!noticeList.length" class="win-empty">暂无通知</p>
                    <button
                      v-for="notice in noticeList"
                      :key="notice.id"
                      class="win-row"
                      type="button"
                      @click="openWindowNotice(notice.id)"
                    >
                      <span class="win-row-title">{{ notice.title }}</span>
                      <span class="win-row-meta"><i class="win-chip" :class="notice.type === 'SCHOOL' ? 'rose' : ''">{{ notice.type === 'SCHOOL' ? '学校' : '院系' }}</i>{{ notice.department }}</span>
                    </button>
                    <footer class="win-footer">
                      <button type="button" @click="router.push('/notice')">查看全部通知 ↗</button>
                    </footer>
                  </template>
                </div>

                <!-- 我的课程：真实今日课程（需登录） -->
                <div v-else-if="item.app === 'schedule'" class="win-pane">
                  <div v-if="!userStore.isLoggedIn" class="win-auth">
                    <p>登录后可查看你的课表</p>
                    <button type="button" @click="lockNow">去登录</button>
                  </div>
                  <template v-else>
                    <p class="win-section">今日课程</p>
                    <p v-if="!todayCourses.length" class="win-empty">今天没有课，好好休息～</p>
                    <div v-for="(slot, i) in todayCourses" :key="i" class="win-course-slot">
                      <time>{{ slot.timeSlot }}</time>
                      <div class="win-course-list">
                        <div v-for="c in slot.courses" :key="c.id" class="win-course" :style="{ borderColor: c.color || '#7c5cd6' }">
                          <b>{{ c.name }}</b><span>{{ c.classroom }} · {{ c.teacher }}</span>
                        </div>
                      </div>
                    </div>
                    <footer class="win-footer">
                      <button type="button" @click="router.push('/schedule')">查看整周课表 ↗</button>
                    </footer>
                  </template>
                </div>

                <!-- 成绩查询：桌面内概览 -->
                <div v-else-if="item.app === 'score'" class="win-pane">
                  <div v-if="!userStore.isLoggedIn" class="win-auth"><p>登录后可查看个人成绩</p><button type="button" @click="lockNow">去登录</button></div>
                  <template v-else>
                    <div class="desktop-metrics"><div><small>GPA</small><b>{{ scoreSummary?.gpa?.toFixed(2) ?? '-' }}</b></div><div><small>平均分</small><b>{{ scoreSummary?.average?.toFixed(1) ?? '-' }}</b></div><div><small>总学分</small><b>{{ scoreSummary?.totalCredits ?? '-' }}</b></div></div>
                    <p v-if="!scoreSummary?.scores.length" class="win-empty">暂无成绩数据</p>
                    <div v-for="score in scoreSummary?.scores.slice(0, 6)" :key="score.id || score.courseName" class="win-row static"><span class="win-row-title">{{ score.courseName }}</span><span class="win-row-meta">{{ score.credit }} 学分 · {{ score.score }} 分 · {{ score.grade || '-' }}</span></div>
                  </template>
                </div>

                <!-- 考试安排：桌面内清单 -->
                <div v-else-if="item.app === 'exam'" class="win-pane">
                  <div v-if="!userStore.isLoggedIn" class="win-auth"><p>登录后可查看个人考试安排</p><button type="button" @click="lockNow">去登录</button></div>
                  <template v-else>
                    <p v-if="!exams.length" class="win-empty">暂无考试安排</p>
                    <div v-for="exam in exams" :key="exam.id" class="win-row static"><span class="win-row-title">{{ exam.courseName }}</span><span class="win-row-meta">{{ exam.examDate }} {{ exam.examTime }} · {{ exam.building }} {{ exam.classroom }} · 座位 {{ exam.seatNumber || '待定' }}</span></div>
                  </template>
                </div>

                <!-- 校园缴费：桌面内创建演示订单 -->
                <div v-else-if="item.app === 'payment'" class="win-pane">
                  <div v-if="!userStore.isLoggedIn" class="win-auth"><p>登录后可处理待缴账单</p><button type="button" @click="lockNow">去登录</button></div>
                  <template v-else>
                    <p v-if="!pendingPayments.length" class="win-empty">当前没有待缴项目</p>
                    <div v-for="payment in pendingPayments" :key="payment.id" class="win-payment"><div><b>{{ payment.type || '校园缴费' }}</b><span>{{ payment.description || payment.deadline }}</span></div><div><strong>{{ formatMoney(payment.amount) }}</strong><button type="button" :disabled="payingPaymentId === payment.id" @click="payFromDesktop(payment.id)">{{ payingPaymentId === payment.id ? '处理中' : '微信缴费' }}</button></div></div>
                    <p class="win-note">支付为系统内演示流程，不会发起真实扣款。</p>
                  </template>
                </div>

                <!-- 校园卡：桌面内余额、充值与挂失 -->
                <div v-else-if="item.app === 'card'" class="win-pane">
                  <div v-if="!userStore.isLoggedIn" class="win-auth"><p>登录后可管理校园卡</p><button type="button" @click="lockNow">去登录</button></div>
                  <template v-else-if="campusCard">
                    <div class="desktop-card"><span>校园一卡通 · {{ campusCard.cardId }}</span><b>{{ formatMoney(campusCard.balance) }}</b><small>{{ campusCard.realName }} · {{ campusCard.status }}</small></div>
                    <div class="desktop-actions"><button type="button" :disabled="cardBusy || campusCard.status !== '正常'" @click="rechargeCardFromDesktop">充值 50 元</button><button type="button" :disabled="cardBusy" @click="toggleCardLoss">{{ campusCard.status === '正常' ? '挂失卡片' : '解除挂失' }}</button></div>
                  </template>
                  <p v-else class="win-empty">正在加载校园卡信息...</p>
                </div>

                <!-- AI 助手：真实问答（需登录） -->
                <div v-else-if="item.app === 'ai'" class="win-ai">
                  <div v-if="!userStore.isLoggedIn" class="win-auth">
                    <p>登录后即可和 AI 助手对话</p>
                    <button type="button" @click="lockNow">去登录</button>
                  </div>
                  <template v-else>
                    <div ref="aiBodyRef" class="win-ai-body">
                      <div v-for="(m, i) in aiMessages" :key="i" class="win-msg" :class="m.role">
                        <div class="win-bubble">
                          <div v-if="m.role === 'assistant'" class="win-markdown" v-html="renderMarkdown(m.content)" />
                          <template v-else>{{ m.content }}</template>
                          <div v-if="m.related?.length" class="win-related">
                            <button v-for="q in m.related" :key="q" type="button" @click="sendAi(q)">{{ q }}</button>
                          </div>
                        </div>
                      </div>
                      <div v-if="aiSending" class="win-msg assistant"><div class="win-bubble typing">正在思考…</div></div>
                    </div>
                    <div v-if="hotQuestions.length && aiMessages.length <= 1" class="win-hot">
                      <button v-for="q in hotQuestions" :key="q.id" type="button" @click="showWindowHot(q)">{{ q.question }}</button>
                    </div>
                    <div class="win-ai-input">
                      <input v-model="aiInput" placeholder="问点什么，比如：如何申请奖学金？" :disabled="aiSending" @keyup.enter="sendAi()">
                      <button type="button" :disabled="aiSending" @click="sendAi()">发送</button>
                    </div>
                  </template>
                </div>

                <!-- 校园地图：真实地点列表 -->
                <div v-else-if="item.app === 'map'" class="win-pane">
                  <p v-if="!locations.length" class="win-empty">正在加载校园地点…</p>
                  <div v-for="loc in locations" :key="loc.id" class="win-row static">
                    <span class="win-row-title">{{ loc.name }}</span>
                    <span class="win-row-meta"><i class="win-chip">{{ loc.category }}</i>{{ loc.address }}</span>
                  </div>
                  <footer class="win-footer">
                    <button type="button" @click="router.push('/map')">打开地图与路径规划 ↗</button>
                  </footer>
                </div>

                <MinesweeperGame v-else-if="item.app === 'minesweeper'" />
                <Game2048 v-else-if="item.app === 'game2048'" />

                <div v-else-if="!item.app && desktopItem(item.itemId)?.kind === 'folder'" class="folder-window">
                  <button
                    v-for="child in childrenOf(item.itemId)"
                    :key="child.id"
                    class="folder-child"
                    :data-id="child.id"
                    type="button"
                    @pointerdown.stop="startFolderChildDrag($event, child)"
                    @dblclick="openDesktopItem(child)"
                  >
                    <span
                      class="file-icon"
                      :class="[child.kind, { 'app-tile': child.tileColor }]"
                      :style="child.tileColor ? { background: child.tileColor } : undefined"
                    >
                      <component :is="child.kind === 'folder' ? Folder : child.icon" />
                      <small v-if="child.suffix">{{ child.suffix }}</small>
                    </span>
                    <span>{{ child.label }}</span>
                  </button>
                  <p v-if="!childrenOf(item.itemId).length">此文件夹为空，把桌面文件拖进来试试</p>
                </div>

                <!-- 图片文件：DataURL 已持久保存，窗口内直接预览 -->
                <div v-else-if="!item.app && isImageItem(desktopItem(item.itemId))" class="image-viewer">
                  <img :src="desktopItem(item.itemId)?.content" :alt="windowTitle(item)">
                </div>

                <!-- 其它二进制文件：内容已持久保存，提供下载 -->
                <div v-else-if="!item.app && isBinaryItem(desktopItem(item.itemId))" class="file-details">
                  <Document />
                  <strong>{{ windowTitle(item) }}</strong>
                  <span>{{ desktopItem(item.itemId)?.mime }}</span>
                  <span>{{ Math.ceil((desktopItem(item.itemId)?.size || 0) / 1024) }} KB</span>
                  <p>文件内容已保存在浏览器本地，刷新页面后仍然在桌面上。</p>
                  <a class="file-download" :href="desktopItem(item.itemId)?.content" :download="windowTitle(item)">下载文件</a>
                </div>

                <!-- 超出保存上限的文件：只保留了文件信息 -->
                <div v-else-if="!item.app && desktopItem(item.itemId)?.content === undefined" class="file-details">
                  <Document />
                  <strong>{{ windowTitle(item) }}</strong>
                  <span>{{ desktopItem(item.itemId)?.mime }}</span>
                  <span>{{ Math.ceil((desktopItem(item.itemId)?.size || 0) / 1024) }} KB</span>
                  <p>该文件超过保存上限（文本 256KB / 其它 1.5MB），仅保留文件信息，未复制内容。</p>
                </div>

                <article
                  v-else-if="!item.app && isMarkdownItem(desktopItem(item.itemId))"
                  class="markdown-document markdown-preview"
                  v-html="renderMarkdown(desktopItem(item.itemId)?.content)"
                />

                <textarea
                  v-else-if="!item.app"
                  class="text-editor"
                  :value="desktopItem(item.itemId)?.content"
                  autofocus
                  spellcheck="false"
                  @input="updateDocument(item.itemId, $event)"
                />

                <article v-else class="markdown-document markdown-preview" v-html="renderMarkdown(guideMarkdown)" />
              </div>
            </section>

            <!-- Windows 式锁屏：登录后进入桌面 -->
            <section
              v-if="phase === 'zooming' || phase === 'settling' || phase === 'lock'"
              class="lock-screen"
              :class="{ leaving: unlocking }"
            >
              <div class="lock-time">
                <strong>{{ now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }) }}</strong>
                <span>{{ now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }) }}</span>
              </div>

              <div class="lock-card" @pointerdown.stop>
                <span class="lock-avatar">
                  <img v-if="userStore.avatar" :src="fileUrl(userStore.avatar)" alt="">
                  <template v-else>{{ (userStore.isLoggedIn ? userStore.name : 'C').slice(0, 1) }}</template>
                </span>

                <!-- 已登录：像 Windows 一样点头像直接进 -->
                <template v-if="userStore.isLoggedIn">
                  <strong class="lock-name">{{ userStore.name }}</strong>
                  <button class="lock-enter" type="button" @click="unlock">进入桌面</button>
                  <button class="lock-link" type="button" @click="switchAccount">切换账号</button>
                </template>

                <!-- 未登录：账号密码登录 -->
                <template v-else>
                  <strong class="lock-name">登录 CampusOS</strong>
                  <input
                    v-model="lockForm.username"
                    class="lock-input"
                    placeholder="学号 / 工号 / 用户名"
                    autocomplete="username"
                    @keyup.enter="lockLogin"
                  >
                  <div class="lock-pass">
                    <input
                      v-model="lockForm.password"
                      class="lock-input"
                      type="password"
                      placeholder="密码"
                      autocomplete="current-password"
                      @keyup.enter="lockLogin"
                    >
                    <button type="button" :disabled="lockLoading" @click="lockLogin">→</button>
                  </div>
                  <p v-if="lockError" :key="lockError" class="lock-error">{{ lockError }}</p>
                  <button class="lock-link" type="button" @click="router.push('/register')">注册账号</button>
                  <p class="lock-hint">学生 student / 123456 · 教师 teacher / 123456 · 管理员 admin / 123456</p>
                </template>
              </div>
            </section>
          </section>
        </div>
      </div>
      <div class="computer-base" />
      <div class="computer-shadow" />
    </div>

    <nav v-if="phase === 'intro'" class="intro-actions" aria-label="项目入口">
      <button class="primary" type="button" @click="launch"><span>进入 CampusOS</span><span class="enter-key">↵</span></button>
      <button type="button" @click="phase = 'canvas'">项目开发历程</button>
      <button type="button" @click="openIntroPanel('team')">小组介绍</button>
    </nav>

    <section v-if="phase === 'team'" class="showcase-page">
      <nav class="showcase-nav"><button type="button" @click="phase = 'intro'">← 返回启动页</button><span>CampusOS · THE TEAM</span></nav>
      <div class="showcase-content team-page">
        <p class="showcase-eyebrow">THE TEAM</p><h2>三位成员，一套完整的 CampusOS</h2><p class="showcase-lead">按模块分工，以全栈交付为共同标准，覆盖从数据、接口到 Web 与小程序体验的完整链路。</p>
        <div class="team-showcase"><article><span>PM</span><h3>李佩泽</h3><small>项目经理</small><p>负责认证、个人中心、资讯公告与 AI 助手，推进整体协作与验收。</p></article><article><span>FE</span><h3>王昕煜</h3><small>前端工程师</small><p>负责课程、成绩、考试、二手交易与校园地图的全栈功能。</p></article><article><span>BE</span><h3>刘永聪</h3><small>后端工程师</small><p>负责缴费、校园卡、宿舍、报修与活动等生活服务模块。</p></article></div>
      </div>
    </section>
    <CanvasMount v-if="phase === 'canvas'" @back="phase = 'intro'" />
  </main>
</template>

<style scoped>
/* ============ 场景与开场（intro） ============ */
.os-scene { position: relative; min-height: 100vh; overflow: hidden; color: #ece6ff; background: #0a0722; background-image: radial-gradient(#ffffff2e 0 1px, transparent 1.6px), radial-gradient(#ffe9ad24 0 1.2px, transparent 2px); background-size: 260px 220px, 340px 300px; transition: background-color .8s; }
.intro-copy { position: absolute; z-index: 1; top: 5vh; left: 50%; text-align: center; transform: translateX(-50%); transition: opacity .35s, transform .6s; }
.intro-copy span { color: #ffd98a; font-size: 12px; font-weight: 700; letter-spacing: 3px; }
.intro-copy h1 { margin: 7px 0 2px; color: #fff; font: 700 58px/1 Georgia, serif; text-shadow: 0 0 34px #a583ff66; }
.intro-copy p { margin: 10px 0 0; color: #b3a8dd; font-size: 14px; white-space: nowrap; }

.computer { --start-top: 55%; --start-w: min(780px, 78vw); --start-h: min(487.5px, 48.75vw); position: fixed; z-index: 2; top: var(--start-top); left: 50%; width: var(--start-w); height: var(--start-h); transform: translate(-50%, -50%); transition: transform .42s cubic-bezier(.2, .8, .2, 1); will-change: top, left, width, height, transform; }
.screen-frame { width: 100%; height: calc(100% - 17px); padding: 16px 16px 22px; border-radius: 17px 17px 7px 7px; background: #16112e; box-shadow: 0 22px 62px #0b063080; transition: box-shadow .42s; will-change: padding, border-radius; }
.screen { position: relative; width: 100%; height: 100%; overflow: hidden; border-radius: 5px; background: #120c33; }
.screen.launchable { cursor: pointer; }
.screen::after { content: ""; position: absolute; z-index: 20; inset: -20%; pointer-events: none; background: linear-gradient(105deg, transparent 35%, #ffffff59 50%, transparent 65%); opacity: 0; transform: translateX(-120%); }
.computer-base { width: calc(100% + 52px); height: 17px; margin-left: -26px; border-radius: 2px 2px 18px 18px; background: linear-gradient(#4b4370, #262043 55%, #574f80); box-shadow: inset 0 2px #8f84c477; transition: opacity .35s .2s; }
.computer-shadow { position: absolute; z-index: -1; right: 8%; bottom: -34px; left: 8%; height: 35px; border-radius: 50%; background: #00000059; filter: blur(15px); transition: opacity .25s; }

/* 待机屏：月亮 + 唤醒文案 */
.boot-screen { position: absolute; z-index: 7; inset: 0; display: grid; place-items: center; background: radial-gradient(circle at 50% 40%, #2a1a5e 0, #150f3c 46%, #090622 100%); will-change: opacity, transform, filter; }
.boot-inner { display: grid; justify-items: center; gap: 18px; }
.boot-moon { width: 92px; height: 92px; border-radius: 50%; background: radial-gradient(circle at 35% 30%, #fffef6, #ffe9c2 52%, #ecc9f5 100%); box-shadow: 0 0 34px #ffe9c288, 0 0 110px #c9a6ff55; animation: moon-breathe 4.5s ease-in-out infinite; }
.boot-text { margin: 0; color: #d9cdf6; font-size: 14px; letter-spacing: 6px; text-shadow: 0 0 12px #a583ff88; }
.boot-bar { width: 190px; height: 3px; overflow: hidden; border-radius: 2px; background: #ffffff1e; opacity: 0; }
.boot-bar i { display: block; width: 0; height: 100%; border-radius: 2px; background: linear-gradient(90deg, #ffe9ad, #c9a6ff); box-shadow: 0 0 10px #d9bfffaa; }

/* ============ 桌面：暮光天空 ============ */
.desktop-surface { position: absolute; inset: 0; overflow: hidden; color: #fff; background: linear-gradient(180deg, #0e0b2e 0%, #2b1b5e 36%, #6b3fa0 64%, #c06fae 84%, #f0a3b4 100%); font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif; opacity: 0; transform: scale(.92); filter: blur(10px); will-change: opacity, transform, filter; }

.sky { position: absolute; inset: 0; pointer-events: none; }
.aurora { position: absolute; left: -15%; width: 130%; height: 36%; background: linear-gradient(100deg, transparent 6%, #7fe3d24d 30%, #a583ff4d 55%, #f79ad342 76%, transparent 94%); filter: blur(46px); transform: skewY(-8deg); animation: aurora-flow 18s ease-in-out infinite alternate; }
.aurora.a1 { top: 2%; }
.aurora.a2 { top: 18%; opacity: .6; animation-duration: 26s; animation-delay: -8s; }
.aurora.a3 { top: -10%; opacity: .45; animation-duration: 32s; animation-delay: -15s; transform: skewY(-3deg); }
.moon { position: absolute; top: 13%; right: 11%; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle at 36% 32%, #fffdf3, #ffedc9 54%, #f2d3a8 100%); box-shadow: 0 0 44px #ffe9b877, 0 0 150px #f4c8ff3d; animation: moon-breathe 9s ease-in-out infinite; }
.cloud { position: absolute; height: 54px; border-radius: 999px; background: #ffffff21; filter: blur(17px); animation: cloud-drift 46s linear infinite; }
.cloud.c1 { top: 58%; left: -12%; width: 300px; }
.cloud.c2 { top: 68%; left: 28%; width: 220px; animation-duration: 60s; animation-delay: -22s; }
.cloud.c3 { top: 50%; left: 62%; width: 260px; animation-duration: 54s; animation-delay: -40s; }
.shooting { position: absolute; top: 10%; left: 66%; width: 120px; height: 2px; border-radius: 2px; background: linear-gradient(90deg, #fff, transparent); opacity: 0; transform: rotate(-26deg); animation: shoot 9s ease-in 3s infinite; }

.moving-stars { position: absolute; inset: -12%; pointer-events: none; background-image: radial-gradient(#fff9e8 0 1.6px, transparent 2.6px), radial-gradient(#ffe9ad99 0 1.2px, transparent 2.2px), radial-gradient(#ffffff59 0 1px, transparent 1.8px); background-position: 20px 30px, 110px 80px, 55px 130px; background-size: 230px 190px, 300px 250px, 180px 200px; mask-image: linear-gradient(#000 52%, transparent 82%); -webkit-mask-image: linear-gradient(#000 52%, transparent 82%); animation: drift 60s linear infinite; }
.moving-stars.layer-two { opacity: .6; transform: scale(.82) rotate(10deg); animation-duration: 90s; animation-direction: reverse; }

.hills { position: absolute; z-index: 1; right: 0; bottom: -2%; left: 0; height: 32%; pointer-events: none; animation: breathe 9s ease-in-out infinite; }
.hills i { position: absolute; bottom: 0; background: #241852e6; clip-path: polygon(50% 0, 100% 100%, 0 100%); }
.hills i:nth-child(1) { left: -6%; width: 46%; height: 72%; background: #1b1145f2; }
.hills i:nth-child(2) { left: 24%; width: 52%; height: 100%; }
.hills i:nth-child(3) { right: -6%; width: 48%; height: 58%; background: #2e1d63d9; }

/* ============ 锁屏（Windows 式登录） ============ */
.lock-screen { position: absolute; z-index: 900; inset: 0; display: grid; grid-template-rows: auto 1fr; justify-items: center; padding-top: 9vh; background: #150f3c59; opacity: 0; pointer-events: none; backdrop-filter: blur(22px) saturate(1.15); transition: opacity .45s ease, transform .45s ease; }
.phase-settling .lock-screen, .phase-lock .lock-screen { opacity: 1; pointer-events: auto; }
.lock-screen.leaving { opacity: 0; transform: translateY(-46px) scale(1.03); pointer-events: none; }
.lock-time { display: grid; justify-items: center; gap: 4px; text-shadow: 0 2px 20px #1c0a4899; }
.lock-time strong { font: 200 clamp(56px, 8vw, 96px)/1 "Segoe UI Light", "Segoe UI", sans-serif; color: #fff; font-variant-numeric: tabular-nums; }
.lock-time span { color: #ffe9c9d9; font-size: 14px; letter-spacing: 3px; }
.lock-card { align-self: center; display: grid; justify-items: center; gap: 12px; width: min(320px, 86vw); padding: 26px 26px 20px; border: 1px solid #ffffff2e; border-radius: 20px; background: #ffffff14; backdrop-filter: blur(18px); box-shadow: 0 30px 80px #0b063059; }
.lock-avatar { display: grid; place-items: center; width: 84px; height: 84px; overflow: hidden; border: 2px solid #ffffff59; border-radius: 50%; background: linear-gradient(135deg, #7c5cd6, #b06fd0); color: #fff; font-size: 34px; font-weight: 700; box-shadow: 0 0 34px #a583ff59; }
.lock-avatar img { width: 100%; height: 100%; object-fit: cover; }
.lock-name { color: #fff; font-size: 18px; letter-spacing: 1px; text-shadow: 0 1px 10px #1c0a4899; }
.lock-input { width: 100%; padding: 10px 16px; border: 1px solid #ffffff42; border-radius: 999px; color: #fff; background: #ffffff1f; font-size: 13px; outline: none; transition: border-color .2s, background .2s; }
.lock-input::placeholder { color: #ffffff8c; }
.lock-input:focus { border-color: #ffe9ad; background: #ffffff2e; }
.lock-pass { display: flex; gap: 8px; width: 100%; }
.lock-pass button { flex-shrink: 0; width: 42px; border: 0; border-radius: 50%; color: #fff; background: linear-gradient(135deg, #7c5cd6, #b06fd0); font-size: 16px; cursor: pointer; box-shadow: 0 6px 16px #7c5cd659; transition: transform .15s; }
.lock-pass button:hover { transform: translateX(2px); }
.lock-pass button:disabled { opacity: .6; cursor: wait; }
.lock-enter { width: 100%; padding: 10px 0; border: 0; border-radius: 999px; color: #fff; background: linear-gradient(135deg, #7c5cd6, #b06fd0); font-size: 14px; letter-spacing: 4px; cursor: pointer; box-shadow: 0 8px 22px #7c5cd659; transition: transform .15s; }
.lock-enter:hover { transform: translateY(-1px); }
.lock-link { border: 0; color: #d9cdf6; background: transparent; font-size: 12px; cursor: pointer; }
.lock-link:hover { color: #fff; text-decoration: underline; }
.lock-error { margin: -4px 0 0; color: #ffb3c1; font-size: 12px; animation: lock-shake .4s; }
.lock-hint { margin: 0; color: #ffffff73; font-size: 11px; text-align: center; }

/* 右上角胶囊按钮 */
.corner-actions { position: absolute; z-index: 6; top: 16px; right: 16px; display: flex; gap: 8px; animation: chips-enter .6s .35s both; }
.corner-actions button { padding: 7px 14px; border: 1px solid #ffffff40; border-radius: 999px; color: #fff; background: #ffffff1c; font-size: 12px; letter-spacing: 1px; cursor: pointer; backdrop-filter: blur(10px); transition: background .2s, transform .2s; }
.corner-actions button:hover { background: #ffffff33; transform: translateY(-1px); }

/* 锁屏式时钟（右侧） */
.desktop-greeting { position: absolute; z-index: 2; top: 17%; right: 8%; display: grid; justify-items: end; gap: 2px; text-align: right; text-shadow: 0 2px 18px #1c0a4899; animation: greeting-enter .8s .1s both; }
.greet-date { color: #ffe9c9d9; font-size: 13px; letter-spacing: 4px; }
.greet-clock { font: 200 clamp(64px, 9vw, 112px)/1.05 "Segoe UI Light", "Segoe UI", "PingFang SC", sans-serif; color: #fff; font-variant-numeric: tabular-nums; text-shadow: 0 0 38px #c9a6ff8c, 0 2px 18px #1c0a4899; }
.greet-line { margin: 6px 0 0; font: 500 26px/1.3 Georgia, "STSong", serif; color: #fff0e0; }
.greet-sub { margin: 2px 0 0; color: #ffffffb3; font-size: 13px; letter-spacing: 2px; }

/* ============ 桌面图标（玻璃拟态） ============ */
.desktop-icons { position: absolute; z-index: 5; inset: 0; display: block; pointer-events: none; animation: icons-enter .8s .2s both; }
.desktop-icons button.desktop-icon { position: absolute; display: grid; grid-template-rows: 52px 34px; align-items: start; justify-items: center; gap: 5px; width: 86px; height: 96px; padding: 5px; overflow: hidden; border: 1px solid transparent; border-radius: 12px; color: #fff; font-size: 12px; background: transparent; cursor: default; pointer-events: auto; touch-action: none; text-shadow: 0 1px 6px #1c0a48cc; transition: background .18s, border-color .18s; }
.desktop-icons button.desktop-icon:hover { border-color: #ffffff3d; background: #ffffff1a; }
.desktop-icons button.desktop-icon.selected { border-color: #ffe9ad80; background: #ffffff26; box-shadow: 0 0 18px #ffe9ad2e; }
.desktop-icon > span:last-child { width: 80px; max-height: 32px; overflow: hidden; line-height: 16px; overflow-wrap: anywhere; text-align: center; }
.file-icon { position: relative; display: grid; width: 48px; height: 48px; place-items: center; border: 1px solid #ffffff47; border-radius: 13px; color: #f4efff; background: #ffffff26; box-shadow: 0 8px 20px #1c0a4859, inset 0 1px #ffffff40; backdrop-filter: blur(8px); }
.file-icon svg { width: 25px; filter: drop-shadow(0 1px 2px #1c0a4866); }
.file-icon small { position: absolute; right: 3px; bottom: 3px; padding: 1px 4px; border-radius: 4px; color: #fff; background: #7c5cd6e6; font: 700 9px monospace; }
/* 文件夹：暖金色实心，一眼就是文件夹 */
.file-icon.folder { border-color: #ffe9ad8c; background: linear-gradient(#ffd873, #f5b34c); color: #7c5410; box-shadow: 0 8px 20px #1c0a4859, inset 0 1px #fff3c9; }
.file-icon.folder svg { filter: none; }

/* 彩色应用磁贴：与门户首页快捷导航宫格同款（白色图标 + 彩色圆角底） */
.file-icon.app-tile { border-color: #ffffff38; color: #fff; box-shadow: 0 8px 20px #1c0a4866, inset 0 1px #ffffff4d; }
.file-icon.app-tile svg { width: 26px; filter: drop-shadow(0 1px 2px #00000033); }
.file-icon.app-tile small { background: #00000059; }

/* 右键菜单（玻璃暗紫） */
.desktop-menu { position: fixed; z-index: 1400; width: 168px; margin: 0; padding: 6px; border: 1px solid #ffffff30; border-radius: 12px; color: #f3edff; background: #241a4af0; box-shadow: 0 18px 46px #0b063080; backdrop-filter: blur(14px); }
.desktop-menu button { display: flex; align-items: center; gap: 10px; width: 100%; min-height: 34px; padding: 5px 9px; border: 0; border-radius: 8px; color: inherit; background: transparent; font-size: 12px; text-align: left; cursor: pointer; }
.desktop-menu button:hover { background: #ffffff1f; }
.desktop-menu button.danger:hover { color: #ffc4d0; background: #ff5d7a38; }
.desktop-menu svg { width: 18px; color: #cdb8ff; }
.desktop-menu button.danger svg { color: #ff9fb3; }

.drop-overlay { position: absolute; z-index: 1500; inset: 14px; display: grid; place-items: center; border: 2px dashed #ffe9ad99; border-radius: 14px; color: #fff; background: #2a1b5e99; font-size: 20px; letter-spacing: 2px; text-shadow: 0 1px 8px #1c0a48; pointer-events: none; backdrop-filter: blur(4px); }

/* ============ 应用窗口（磨砂玻璃） ============ */
.app-window { position: absolute; z-index: 8; min-width: 420px; min-height: 360px; max-width: calc(100vw - 32px); max-height: calc(100vh - 32px); overflow: hidden; resize: both; border: 1px solid #d9ccff73; border-radius: 10px; color: #f3efff; background: #1d1641e8; box-shadow: 0 30px 90px #09051f99, inset 0 1px #ffffff1c; backdrop-filter: blur(20px) saturate(1.1); }
.app-window.maximized { inset: 0 !important; width: 100% !important; height: 100% !important; border-radius: 0; resize: none; }
.desktop-metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 14px; }
.desktop-metrics > div { display: grid; gap: 4px; padding: 12px; border: 1px solid #ffffff1f; background: #ffffff0d; }
.desktop-metrics small, .desktop-card small, .win-payment span, .win-note { color: #c9c1df; }
.desktop-metrics b { font-size: 22px; }
.win-payment { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 13px 0; border-bottom: 1px solid #ffffff18; }
.win-payment > div { display: grid; gap: 5px; }
.win-payment > div:last-child { justify-items: end; }
.win-payment strong { color: #f4ca77; }
.win-payment button, .desktop-actions button { border: 1px solid #cfc2ff66; border-radius: 5px; padding: 6px 10px; color: #fff; background: #8a6fe0; cursor: pointer; }
.win-payment button:disabled, .desktop-actions button:disabled { cursor: not-allowed; opacity: .55; }
.win-note { margin: 14px 0 0; font-size: 12px; }
.desktop-card { display: grid; gap: 12px; padding: 22px; color: #fff; background: linear-gradient(135deg, #167f81, #3d71bd); box-shadow: 0 12px 26px #00000033; }
.desktop-card b { font-size: 32px; }
.desktop-actions { display: flex; gap: 10px; margin-top: 16px; }
.window-titlebar { display: grid; grid-template-columns: auto 1fr auto; align-items: center; height: 42px; padding: 0 12px; border-bottom: 1px solid #ffffff1f; background: #ffffff12; cursor: grab; user-select: none; touch-action: none; }
.window-titlebar:active { cursor: grabbing; }
.window-titlebar strong { justify-self: center; color: #f5f0ff; font-size: 12px; font-weight: 600; text-shadow: 0 1px 8px #09051f; }
.window-controls { display: flex; gap: 8px; }
.window-controls button { display: grid; width: 14px; height: 14px; padding: 0; place-items: center; overflow: hidden; border: 0; border-radius: 50%; color: transparent; background: #ff7d9d; cursor: pointer; }
.window-controls button:nth-child(2) { background: #63d7c3; }
.window-controls button:hover { color: #fff; }
.window-controls svg { width: 9px; height: 9px; }
.window-body { height: calc(100% - 42px); overflow: auto; background: #17103370; }
.markdown-window .window-body { overflow: hidden; }

/* 窗口通用内容样式（真实数据小面板） */
.win-pane { display: flex; flex-direction: column; min-height: 100%; padding: 14px 16px; }
.win-row { display: grid; gap: 4px; margin-bottom: 7px; padding: 10px 12px; border: 1px solid #ffffff10; border-radius: 8px; color: inherit; background: #ffffff0c; text-align: left; cursor: pointer; }
.win-row:hover { border-color: #d9ccff6e; background: #ffffff1b; }
.win-row.static { cursor: default; }
.win-row-title { color: #f5f0ff; font-size: 14px; font-weight: 600; }
.win-row-meta { display: flex; align-items: center; gap: 8px; color: #c5b9dd; font-size: 12px; }
.win-chip { padding: 1px 8px; border-radius: 999px; color: #f1e8ff; background: #8062ca66; font-size: 11px; font-style: normal; }
.win-chip.rose { color: #ffe2ea; background: #d65b8066; }
.win-footer { margin-top: auto; padding-top: 10px; text-align: center; }
.win-footer button, .win-auth button { padding: 8px 18px; border: 0; border-radius: 999px; color: #fff; background: linear-gradient(120deg, #7c5cd6, #b06fd0); font-size: 13px; cursor: pointer; box-shadow: 0 6px 16px #7c5cd64d; }
.win-empty { padding: 18px 0; color: #c5b9dd; font-size: 13px; text-align: center; }
.win-unread { margin: 0 0 8px; padding: 8px 12px; border-radius: 8px; color: #ffe9ac; background: #8b67273d; font-size: 12px; }
.win-section { margin: 2px 0 10px; color: #f5f0ff; font-size: 13px; font-weight: 700; letter-spacing: 1px; }
.win-back { align-self: flex-start; margin-bottom: 10px; padding: 4px 12px; border: 1px solid #d9ccff4d; border-radius: 999px; color: #f0e9ff; background: #ffffff12; font-size: 12px; cursor: pointer; }
.win-detail { display: flex; flex-direction: column; }
.win-detail h4 { margin: 0 0 8px; color: #f5f0ff; font-size: 18px; }
.win-meta { display: flex; align-items: center; gap: 8px; margin: 0 0 12px; color: #c5b9dd; font-size: 12px; flex-wrap: wrap; }
.win-content { color: #ede8f7; font-size: 14px; line-height: 1.8; white-space: pre-wrap; }
.win-auth { display: grid; place-items: center; gap: 12px; min-height: 100%; padding: 30px; color: #d5cae8; }
.win-course-slot { display: flex; gap: 12px; align-items: center; margin-bottom: 10px; }
.win-course-slot time { flex-shrink: 0; width: 96px; color: #c5b9dd; font-size: 12px; }
.win-course-list { display: flex; flex-wrap: wrap; gap: 8px; }
.win-course { display: flex; flex-direction: column; padding: 6px 12px; border-left: 3px solid; border-radius: 6px; background: #ffffff10; font-size: 13px; }
.win-course b { color: #f5f0ff; }
.win-course span { color: #c5b9dd; font-size: 12px; }

/* AI 窗口 */
.win-ai { display: flex; flex-direction: column; min-height: 100%; max-height: 100%; padding: 12px 14px; }
.win-ai-body { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; padding: 4px; }
.win-msg { display: flex; }
.win-msg.user { justify-content: flex-end; }
.win-bubble { max-width: 80%; padding: 8px 12px; border-radius: 12px; font-size: 13px; line-height: 1.6; white-space: pre-wrap; }
.win-msg.assistant .win-bubble { background: #f3eefb; color: #2c2350; border-top-left-radius: 3px; }
.win-msg.user .win-bubble { background: linear-gradient(120deg, #7c5cd6, #b06fd0); color: #fff; border-top-right-radius: 3px; }
.win-bubble.typing { color: #9a92b8; font-style: italic; background: #f3eefb; }
.win-markdown :deep(p) { margin: 0 0 8px; }
.win-markdown :deep(p:last-child) { margin-bottom: 0; }
.win-markdown :deep(ul), .win-markdown :deep(ol) { margin: 8px 0; padding-left: 22px; }
.win-markdown :deep(code) { padding: 1px 4px; border-radius: 3px; background: #ddd5ee; font-family: Consolas, monospace; }
.win-markdown :deep(pre) { overflow: auto; padding: 10px; border-radius: 6px; color: #eee; background: #201c28; }
.win-markdown :deep(pre code) { padding: 0; background: transparent; }
.win-markdown :deep(blockquote) { margin: 8px 0; padding-left: 10px; border-left: 3px solid #8d72cf; }
.win-markdown :deep(a) { color: #684bb0; }
.win-related { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.win-related button, .win-hot button { padding: 4px 10px; border: 1px solid #e2d8f7; border-radius: 999px; color: #6247ab; background: #fff; font-size: 12px; cursor: pointer; }
.win-related button:hover, .win-hot button:hover { background: #f6f1fd; }
.win-hot { display: flex; flex-wrap: wrap; gap: 6px; padding: 8px 2px; }
.win-ai-input { display: flex; gap: 8px; padding-top: 10px; }
.win-ai-input input { flex: 1; padding: 9px 14px; border: 1px solid #e2d8f7; border-radius: 999px; outline: none; color: #2c2350; font-size: 13px; }
.win-ai-input input:focus { border-color: #7c5cd6; }
.win-ai-input button { padding: 0 18px; border: 0; border-radius: 999px; color: #fff; background: linear-gradient(120deg, #7c5cd6, #b06fd0); font-size: 13px; cursor: pointer; }
.win-ai-input button:disabled { opacity: .6; cursor: wait; }

/* 各窗口静态内容 */
.markdown-document { min-height: 100%; padding: 30px clamp(24px, 5vw, 56px) 44px; color: #eee8f8; background: #17103352; font: 15px/1.8 "Segoe UI", "PingFang SC", sans-serif; }
.markdown-window .markdown-document { height: 100%; overflow: auto; }
.markdown-preview :deep(h1), .markdown-preview :deep(h2), .markdown-preview :deep(h3), .markdown-preview :deep(h4) { margin: 28px 0 12px; color: #fff6df; line-height: 1.3; }
.markdown-preview :deep(h1) { margin-top: 0; padding-bottom: 14px; border-bottom: 1px solid #ffffff24; font-size: 28px; }
.markdown-preview :deep(h2) { padding-bottom: 8px; border-bottom: 1px solid #ffffff1c; font-size: 21px; }
.markdown-preview :deep(h3) { font-size: 18px; }
.markdown-preview :deep(p) { margin: 10px 0; }
.markdown-preview :deep(ul), .markdown-preview :deep(ol) { margin: 10px 0; padding-left: 26px; }
.markdown-preview :deep(li) { margin: 5px 0; }
.markdown-preview :deep(code) { padding: 1px 5px; border-radius: 3px; color: #ffe0a1; background: #00000032; font: 13px Consolas, monospace; }
.markdown-preview :deep(pre) { overflow: auto; padding: 14px; border-radius: 6px; color: #e6edf3; background: #1f2937; }
.markdown-preview :deep(pre code) { padding: 0; color: inherit; background: transparent; }
.markdown-preview :deep(blockquote) { margin: 14px 0; padding: 8px 14px; border-left: 4px solid #a583ff; color: #ddd2f0; background: #ffffff0d; }
.markdown-preview :deep(a) { color: #bca6ff; }
.markdown-preview :deep(hr) { border: 0; border-top: 1px solid #ffffff24; margin: 24px 0; }
.folder-window { display: flex; align-content: flex-start; flex-wrap: wrap; gap: 14px; min-height: 100%; padding: 24px; color: #6a628c; background: #fdfbff; }
.folder-window button { display: grid; justify-items: center; gap: 6px; width: 84px; padding: 6px; border: 1px solid transparent; border-radius: 10px; background: transparent; cursor: default; touch-action: none; user-select: none; }
.folder-window button:hover { border-color: #d9cdf6; background: #f3edff; }
.folder-window .file-icon { color: #7c5cd6; background: #efeafd; border-color: #ddd2f5; backdrop-filter: none; box-shadow: none; }
.folder-window .file-icon.folder { color: #7c5410; background: linear-gradient(#ffd873, #f5b34c); border-color: #ffe9ad; }
.folder-window .file-icon.app-tile { color: #fff; border-color: transparent; }
.folder-window p { width: 100%; text-align: center; }
.text-editor { box-sizing: border-box; width: 100%; height: 100%; padding: 14px 16px; resize: none; border: 0; outline: 0; color: #241a4a; background: #fdfbff; font: 14px/1.6 Consolas, monospace; }
.file-details { display: grid; justify-items: center; align-content: center; gap: 9px; min-height: 100%; color: #6a628c; background: #fdfbff; }
.file-details > svg { width: 54px; color: #7c5cd6; }
.file-details strong { color: #241a4a; }
.file-details p { max-width: 360px; text-align: center; }
.file-download { padding: 7px 20px; border-radius: 999px; background: #7c5cd6; color: #fff; text-decoration: none; font-size: 13px; transition: background .18s; }
.file-download:hover { background: #6247ab; }

/* 图片文件预览窗：深色底 + 等比缩放 */
.image-viewer { display: grid; place-items: center; min-height: 100%; padding: 14px; background: #1c1140; }
.image-viewer img { max-width: 100%; max-height: 100%; border-radius: 8px; box-shadow: 0 14px 40px #0b063080; object-fit: contain; }

/* 初始页入口 */
.intro-actions { position: fixed; z-index: 10; bottom: 3.5vh; left: 50%; display: flex; align-items: center; gap: 9px; transform: translateX(-50%); }
.intro-actions button { display: flex; align-items: center; gap: 9px; min-height: 40px; padding: 9px 14px; border: 1px solid #ffffff2e; border-radius: 7px; color: #ddd4f4; background: #1c1440e6; font-size: 13px; cursor: pointer; box-shadow: 0 12px 34px #0b063066; backdrop-filter: blur(10px); }
.intro-actions button:hover { border-color: #bca6ff; color: #fff; background: #2a2058ee; }
.intro-actions button.primary { border-color: #a583ff8c; color: #fff; background: #6d4fc4d9; }
.intro-actions button.primary:hover { background: #7c5cd6; }
.enter-key { display: grid; width: 29px; height: 25px; place-items: center; border-radius: 6px; color: #fff; background: linear-gradient(120deg, #7c5cd6, #b06fd0); }
.showcase-page { position: fixed; z-index: 2000; inset: 0; overflow: auto; color: #f3edff; background: #100a2b; background-image: radial-gradient(#ffffff18 0 1px, transparent 1.5px), linear-gradient(150deg, #100a2b, #2b195d 55%, #552f78); background-size: 180px 180px, 100% 100%; }
.showcase-nav { display: flex; justify-content: space-between; align-items: center; width: min(1180px, calc(100% - 48px)); margin: 0 auto; padding: 22px 0; color: #d8cbed; font-size: 12px; letter-spacing: 2px; }
.showcase-nav button { padding: 8px 12px; border: 1px solid #ffffff35; border-radius: 6px; color: #fff; background: #ffffff12; cursor: pointer; }
.showcase-nav button:hover { background: #ffffff24; }
.showcase-content { width: min(1040px, calc(100% - 48px)); margin: clamp(36px, 8vh, 100px) auto; }
.showcase-eyebrow { margin: 0; color: #ffd98a; font-size: 12px; font-weight: 700; letter-spacing: 4px; }
.showcase-content h2 { max-width: 760px; margin: 14px 0; color: #fff; font: 700 clamp(36px, 5vw, 62px)/1.15 Georgia, "STSong", serif; }
.showcase-lead { max-width: 660px; color: #d8cbed; font-size: 16px; line-height: 1.9; }
.journey-grid, .team-showcase { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-top: 48px; }
.journey-grid article { min-height: 230px; padding: 24px; border: 1px solid #ffffff20; border-radius: 8px; background: #ffffff0d; box-shadow: inset 0 1px #ffffff18; }
.journey-grid b { color: #ffd98a; font: 700 32px Georgia, serif; }.journey-grid h3, .team-showcase h3 { margin: 42px 0 8px; color: #fff; font-size: 18px; }.journey-grid p, .team-showcase p { color: #cfc3e3; font-size: 14px; line-height: 1.8; }
.team-showcase { grid-template-columns: repeat(3, 1fr); }.team-showcase article { min-height: 300px; padding: 28px; border: 1px solid #e4d7ff45; border-radius: 8px; background: #180e3dbd; box-shadow: 0 18px 45px #08041b45; }.team-showcase article:nth-child(2) { transform: translateY(-18px); background: #291556d9; }.team-showcase article > span { display: grid; width: 52px; height: 52px; place-items: center; border: 1px solid #ffd98a80; border-radius: 50%; color: #ffd98a; font-weight: 700; }.team-showcase h3 { margin-top: 48px; font-size: 24px; }.team-showcase small { color: #bca6ff; font-size: 13px; }

/* ============ 幕间转场 ============ */
.phase-waking .intro-copy { opacity: .2; transform: translate(-50%, -10px); }
.phase-waking .computer { transform: translate(-50%, -50%) translateY(-10px) scale(1.018); }
.phase-waking .screen-frame { box-shadow: 0 28px 85px #7c5cd652, 0 0 0 1px #a583ff55; }
.phase-waking .boot-screen { filter: brightness(1.16) saturate(1.05); }
.phase-waking .boot-bar { opacity: 1; transition: opacity .15s; }
.phase-waking .boot-bar i { animation: boot-progress 2.1s cubic-bezier(.2, .7, .2, 1) forwards; }
.phase-waking .launch-button { opacity: 0; transform: translate(-50%, 12px); pointer-events: none; }

.phase-zooming { background: #0e0b2e; }
.phase-zooming .intro-copy, .phase-settling .intro-copy, .phase-lock .intro-copy, .phase-desktop .intro-copy { opacity: 0; transform: translate(-50%, -28px); pointer-events: none; }
.phase-zooming .computer { animation: cinematic-zoom 3s cubic-bezier(.72, 0, .16, 1) forwards; }
.phase-zooming .screen-frame { animation: frame-release 3s cubic-bezier(.72, 0, .16, 1) forwards; }
.phase-zooming .screen { animation: screen-release 3s cubic-bezier(.72, 0, .16, 1) forwards; }
.phase-zooming .screen::after { animation: screen-sweep 1.5s .52s ease-in-out forwards; }
.phase-zooming .computer-base { animation: base-exit 2.25s .1s ease-in forwards; }
.phase-zooming .computer-shadow { animation: shadow-exit 1.5s ease-in forwards; }
.phase-zooming .boot-screen { animation: boot-exit 1.45s .08s cubic-bezier(.4, 0, .6, 1) forwards; pointer-events: none; }
.phase-zooming .desktop-surface { animation: desktop-enter 2.2s .48s cubic-bezier(.2, .75, .2, 1) forwards; }
.phase-zooming .lock-screen { animation: lock-enter .8s 2s both; }
.phase-zooming .launch-button, .phase-settling .launch-button, .phase-lock .launch-button, .phase-desktop .launch-button { opacity: 0; transform: translate(-50%, 18px); pointer-events: none; }

.phase-settling, .phase-lock, .phase-desktop { background: #0e0b2e; }
.phase-settling .computer, .phase-lock .computer, .phase-desktop .computer { top: 0; left: 0; width: 100vw; height: 100vh; transform: none; }
.phase-settling .computer { animation: settle-screen .4s cubic-bezier(.2, .8, .2, 1); }
.phase-settling .screen-frame, .phase-lock .screen-frame, .phase-desktop .screen-frame { height: 100%; padding: 0; border-radius: 0; background: transparent; box-shadow: none; }
.phase-settling .screen, .phase-lock .screen, .phase-desktop .screen { border-radius: 0; }
.phase-settling .computer-base, .phase-settling .computer-shadow, .phase-lock .computer-base, .phase-lock .computer-shadow, .phase-desktop .computer-base, .phase-desktop .computer-shadow { opacity: 0; }
.phase-settling .boot-screen, .phase-lock .boot-screen, .phase-desktop .boot-screen { opacity: 0; transform: scale(1.08); pointer-events: none; }
.phase-settling .desktop-surface, .phase-lock .desktop-surface, .phase-desktop .desktop-surface { opacity: 1; transform: none; filter: none; }

@keyframes cinematic-zoom { 0% { top: var(--start-top); left: 50%; width: var(--start-w); height: var(--start-h); transform: translate(-50%, -50%) translateY(-10px) scale(1.018); } 14% { top: 51%; left: 50%; width: var(--start-w); height: var(--start-h); transform: translate(-50%, -50%) scale(.955); } 46% { top: 50%; left: 50%; width: 82vw; height: 76vh; transform: translate(-50%, -50%); } 78% { top: 50%; left: 50%; width: 98vw; height: 96vh; transform: translate(-50%, -50%); } 100% { top: 0; left: 0; width: 100vw; height: 100vh; transform: none; } }
@keyframes frame-release { 0%, 20% { height: calc(100% - 17px); padding: 16px 16px 22px; border-radius: 17px 17px 7px 7px; background: #16112e; box-shadow: 0 28px 85px #7c5cd652; } 72% { height: calc(100% - 8px); padding: 8px; border-radius: 10px; background: #16112e; } 100% { height: 100%; padding: 0; border-radius: 0; background: transparent; box-shadow: none; } }
@keyframes screen-release { 0%, 72% { border-radius: 5px; } 100% { border-radius: 0; } }
@keyframes base-exit { 0%, 28% { opacity: 1; transform: scaleX(1); } 75% { opacity: .3; transform: scaleX(.82); } 100% { opacity: 0; transform: scaleX(.65); } }
@keyframes shadow-exit { to { opacity: 0; transform: scale(.7); filter: blur(24px); } }
@keyframes boot-exit { 0%, 16% { opacity: 1; transform: scale(1); filter: brightness(1.16); } 65% { opacity: .18; filter: brightness(1.4) blur(1px); } 100% { opacity: 0; transform: scale(1.08); filter: blur(5px); } }
@keyframes desktop-enter { 0%, 10% { opacity: 0; transform: scale(.92); filter: blur(10px); } 45% { opacity: .55; filter: blur(4px); } 100% { opacity: 1; transform: none; filter: none; } }
@keyframes screen-sweep { 0% { opacity: 0; transform: translateX(-120%); } 35% { opacity: .8; } 100% { opacity: 0; transform: translateX(120%); } }
@keyframes lock-enter { from { opacity: 0; transform: translateY(24px); } to { opacity: 1; transform: none; } }
@keyframes lock-shake { 0%, 100% { transform: none; } 25% { transform: translateX(-5px); } 50% { transform: translateX(4px); } 75% { transform: translateX(-3px); } }
@keyframes greeting-enter { from { opacity: 0; transform: translateX(35px); } to { opacity: 1; transform: none; } }
@keyframes icons-enter { from { opacity: 0; transform: translateX(-32px); } to { opacity: 1; transform: none; } }
@keyframes chips-enter { from { opacity: 0; transform: translateY(-14px); } to { opacity: 1; transform: none; } }
@keyframes settle-screen { 0% { transform: scale(1.012); } 100% { transform: none; } }
@keyframes boot-progress { to { width: 100%; } }
@keyframes moon-breathe { 50% { box-shadow: 0 0 54px #ffe9b8aa, 0 0 170px #f4c8ff59; transform: scale(1.03); } }
@keyframes aurora-flow { from { transform: translateX(-5%) skewY(-8deg); opacity: .75; } to { transform: translateX(5%) skewY(-5deg); opacity: 1; } }
@keyframes cloud-drift { from { transform: translateX(-8vw); } to { transform: translateX(108vw); } }
@keyframes shoot { 0% { opacity: 0; transform: translate(0, 0) rotate(-26deg); } 3% { opacity: 1; } 11% { opacity: 0; transform: translate(-260px, 140px) rotate(-26deg); } 100% { opacity: 0; } }
@keyframes drift { to { background-position: 250px 220px, -190px 330px, 235px -70px; } }
@keyframes breathe { 50% { transform: translateY(10px); } }

@media (max-width: 700px) {
  .intro-copy { top: 7vh; }
  .intro-copy h1 { font-size: 43px; }
  .intro-copy p { max-width: 88vw; white-space: normal; }
  .computer { --start-top: 53%; --start-w: 96vw; --start-h: 60vw; top: var(--start-top); width: var(--start-w); height: var(--start-h); }
  .screen-frame { padding: 9px 9px 13px; }
  .computer-base { width: 100%; margin-left: 0; }
  .lock-screen { padding-top: 6vh; }
  .desktop-greeting { top: 60px; right: 14px; }
  .greet-clock { font-size: 54px; }
  .greet-line { font-size: 19px; }
  .moon { top: 9%; right: 8%; width: 84px; height: 84px; }
  .corner-actions { top: 10px; right: 10px; }
  .corner-actions button { padding: 6px 10px; }
  .desktop-icons button.desktop-icon { width: 76px; }
  .intro-actions { width: calc(100vw - 24px); gap: 6px; justify-content: center; }
  .intro-actions button { min-height: 38px; padding: 8px 9px; font-size: 12px; }
  .intro-actions button.primary span:first-child { display: none; }
  .showcase-nav, .showcase-content { width: calc(100% - 28px); }
  .showcase-nav span { display: none; }
  .showcase-content { margin-top: 42px; }
  .showcase-content h2 { font-size: 35px; }
  .journey-grid, .team-showcase { grid-template-columns: 1fr; margin-top: 28px; }
  .journey-grid article, .team-showcase article { min-height: 0; }
  .team-showcase article:nth-child(2) { transform: none; }
  .app-window { min-width: 0; min-height: 0; resize: none; }
  .markdown-document { padding: 22px 20px 32px; font-size: 14px; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { scroll-behavior: auto !important; animation-duration: .01ms !important; animation-iteration-count: 1 !important; transition-duration: .01ms !important; }
}
</style>

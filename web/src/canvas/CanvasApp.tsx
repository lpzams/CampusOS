import { useCallback, useEffect, useMemo, useState, type CSSProperties, type MouseEvent } from 'react'
import { Background, Controls, Handle, MiniMap, Position, ReactFlow, addEdge, useEdgesState, useNodesState, type Connection, type Edge, type Node, type NodeProps } from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { Eye, EyeOff, Plus, Trash2, Undo2 } from 'lucide-react'
import './canvas.css'

type Props = { onBack: () => void }
type CardData = { title: string; content: string; color: string; tags: string[] }
type Card = Node<CardData, 'projectCard'>

function ProjectCard({ data, selected }: NodeProps<Card>) {
  return <article className={`project-card${selected ? ' selected' : ''}`} style={{ '--card-color': data.color } as CSSProperties}>
    <Handle type="target" position={Position.Left} />
    <span className="project-card__dot" />
    <h3>{data.title}</h3>
    <p>{data.content}</p>
    <footer>{data.tags.map(tag => <i key={tag}>{tag}</i>)}</footer>
    <Handle type="source" position={Position.Right} />
  </article>
}

const nodeTypes = { projectCard: ProjectCard }
const makeCard = (id: string, x: number, y: number, title: string, content: string, color: string, tags: string[]): Card => ({
  id, type: 'projectCard', position: { x, y }, data: { title, content, color, tags },
})

const seed: Card[] = [
  makeCard('kickoff', 40, 320, '项目立项', '明确 CampusOS 的智慧校园定位、目标用户、15 个服务模块与阶段验收范围。', '#f7c948', ['启动', '01']),
  makeCard('research', 320, 20, '用户与场景调研', '围绕选课、缴费、报修、查成绩、看通知等高频校园场景梳理痛点。', '#83c5be', ['调研', '02']),
  makeCard('requirements', 320, 190, '需求与流程拆解', '定义学生、教师、管理员角色，补齐每项服务的提交、处理、反馈闭环。', '#a8dadc', ['规划', '03']),
  makeCard('data', 320, 360, '数据与接口建模', '梳理用户、课程、账单、工单、公告等核心实体及前后端接口契约。', '#8ecae6', ['数据', '04']),
  makeCard('architecture', 320, 530, '系统架构设计', '确定 Vue、React Flow、Spring Boot、MySQL 与 Redis 的分层协作。', '#90caf9', ['技术', '05']),
  makeCard('design', 610, 20, '交互与视觉设计', '完成启动页、桌面、门户导航和服务页的布局、状态与响应式规范。', '#cdb4db', ['设计', '06']),
  makeCard('desktop', 610, 170, '桌面与文件体验', '实现单实例窗口、拖拽布局、文件夹、Markdown 预览及本地持久化。', '#d7c6ff', ['前端', '07']),
  makeCard('account', 610, 320, '认证与用户中心', '实现登录、注册、角色权限、个人资料、切换账号和安全退出。', '#ffafcc', ['账户', '08']),
  makeCard('content', 610, 470, '资讯与通知闭环', '支持资讯发布、通知公告、已读状态、收藏、详情浏览与后台维护。', '#ffd6a5', ['内容', '09']),
  makeCard('academic', 900, -40, '课程与成绩服务', '提供今日课表、整周课表、成绩查询、统计视图与数据发布能力。', '#bde0fe', ['教务', '10']),
  makeCard('exam', 900, 110, '考试与教室安排', '展示考试时间、地点、座位及空闲教室，并由教务端统一维护。', '#cdeac0', ['教务', '11']),
  makeCard('payment', 900, 260, '缴费与校园卡', '完成待缴账单、演示支付回写、交易记录、校园卡余额与充值挂失。', '#f9c6c9', ['生活', '12']),
  makeCard('dormitory', 900, 410, '宿舍与报修工单', '覆盖宿舍信息、水电查询、故障提交、处理状态更新和维修评价。', '#b9fbc0', ['生活', '13']),
  makeCard('campus', 900, 560, '活动、二手与地图', '实现活动报名、二手交易、校园地点查询、路线规划与状态反馈。', '#a9def9', ['校园', '14']),
  makeCard('assistant', 1190, 80, 'AI 校园助手', '结合热门问题与校园服务入口，提供政策问答和办事步骤推荐。', '#e4c1f9', ['智能', '15']),
  makeCard('admin', 1190, 280, '后台管理与发布', '管理员统一维护资讯、通知、课程、考试、活动、工单与用户数据。', '#f5d0fe', ['后台', '16']),
  makeCard('testing', 1480, 180, '联调与测试', '验证接口、权限、状态回写、错误提示、窗口交互和跨角色业务流程。', '#fdffb6', ['验证', '17']),
  makeCard('deployment', 1770, 100, '容器化部署', '通过 Docker 编排前后端、数据库与缓存，形成稳定可运行的演示环境。', '#95d5b2', ['部署', '18']),
  makeCard('delivery', 1770, 330, '文档与答辩展示', '整理演示账号、功能说明、测试记录、项目历程画布与答辩材料。', '#b7e4c7', ['交付', '19']),
]
const link = (id: string, source: string, target: string, dashed = false): Edge => ({
  id, source, target, type: 'smoothstep', style: { stroke: '#6f6291', strokeWidth: 2, ...(dashed ? { strokeDasharray: '6 5' } : {}) },
})
const seedEdges: Edge[] = [
  link('e-k-r', 'kickoff', 'research'), link('e-k-requirements', 'kickoff', 'requirements', true), link('e-k-data', 'kickoff', 'data', true),
  link('e-r-requirements', 'research', 'requirements'), link('e-requirements-data', 'requirements', 'data'), link('e-data-architecture', 'data', 'architecture'),
  link('e-requirements-design', 'requirements', 'design'), link('e-design-desktop', 'design', 'desktop'), link('e-data-account', 'data', 'account'), link('e-architecture-content', 'architecture', 'content'),
  link('e-account-academic', 'account', 'academic', true), link('e-account-exam', 'account', 'exam', true), link('e-content-payment', 'content', 'payment'), link('e-content-dormitory', 'content', 'dormitory'), link('e-desktop-campus', 'desktop', 'campus'),
  link('e-academic-assistant', 'academic', 'assistant'), link('e-campus-assistant', 'campus', 'assistant', true), link('e-exam-admin', 'exam', 'admin'), link('e-dormitory-admin', 'dormitory', 'admin'),
  link('e-assistant-testing', 'assistant', 'testing'), link('e-admin-testing', 'admin', 'testing'), link('e-testing-deployment', 'testing', 'deployment'), link('e-testing-delivery', 'testing', 'delivery'), link('e-deployment-delivery', 'deployment', 'delivery'),
]
const key = 'campus-development-canvas-v3'

export default function CanvasApp({ onBack }: Props) {
  const saved = useMemo(() => { try { return JSON.parse(localStorage.getItem(key) || 'null') } catch { return null } }, [])
  const [nodes, setNodes, onNodesChange] = useNodesState<Card>(saved?.nodes || seed)
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>(saved?.edges || seedEdges)
  const [selected, setSelected] = useState<string | undefined>()
  const [past, setPast] = useState<string[]>([])

  useEffect(() => localStorage.setItem(key, JSON.stringify({ nodes, edges })), [nodes, edges])
  const snapshot = () => setPast(history => [...history.slice(-19), JSON.stringify({ nodes, edges })])
  const current = nodes.find(node => node.id === selected)

  const add = useCallback((event: MouseEvent) => {
    if (!(event.target as HTMLElement).closest('.react-flow__pane')) return
    snapshot()
    const id = crypto.randomUUID()
    setNodes(currentNodes => [...currentNodes, makeCard(id, event.clientX - 210, event.clientY - 130, '新建节点', '双击画布后补充这个开发事项。', '#fff2a8', ['待整理'])])
    setSelected(id)
  }, [nodes, edges])

  const connect = useCallback((connection: Connection) => {
    snapshot()
    setEdges(currentEdges => addEdge({ ...connection, id: crypto.randomUUID(), type: 'smoothstep', style: { stroke: '#6f6291', strokeWidth: 2 } }, currentEdges))
  }, [nodes, edges])

  function update(field: 'title' | 'content' | 'color', value: string) {
    if (!current) return
    snapshot()
    setNodes(currentNodes => currentNodes.map(node => node.id === current.id ? { ...node, data: { ...node.data, [field]: value } } : node))
  }
  function remove() {
    if (!selected) return
    snapshot()
    setNodes(currentNodes => currentNodes.filter(node => node.id !== selected))
    setEdges(currentEdges => currentEdges.filter(edge => edge.source !== selected && edge.target !== selected))
    setSelected(undefined)
  }
  function addCard() {
    snapshot()
    const id = crypto.randomUUID()
    setNodes(currentNodes => [...currentNodes, makeCard(id, 220, 220, '新建节点', '补充一个新的开发事项。', '#fff2a8', ['待整理'])])
    setSelected(id)
  }
  function undo() {
    const last = past.at(-1)
    if (!last) return
    const state = JSON.parse(last)
    setNodes(state.nodes)
    setEdges(state.edges)
    setPast(history => history.slice(0, -1))
  }

  useEffect(() => {
    const keydown = (event: KeyboardEvent) => {
      if (event.key === 'Delete' && selected) remove()
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') { event.preventDefault(); undo() }
    }
    window.addEventListener('keydown', keydown)
    return () => window.removeEventListener('keydown', keydown)
  }, [selected, past, nodes, edges])

  return <div className="canvas-shell">
    <header>
      <button onClick={onBack}>返回启动页</button>
      <div className="brand"><b>CAMPUSOS</b><span>DEVELOPMENT MAP / 项目开发历程</span></div>
      <button onClick={addCard}><Plus size={16} />新建节点</button>
    </header>
    <aside className="layers"><h2>图层 · 开发节点</h2>
      {nodes.map(node => <div className={selected === node.id ? 'active' : ''} key={node.id} onClick={() => setSelected(node.id)}>
        <span style={{ background: node.data.color }} />{node.data.title}
        <button title={node.hidden ? '显示节点' : '隐藏节点'} onClick={event => { event.stopPropagation(); setNodes(currentNodes => currentNodes.map(item => item.id === node.id ? { ...item, hidden: !item.hidden } : item)) }}>
          {node.hidden ? <EyeOff size={14} /> : <Eye size={14} />}
        </button>
      </div>)}
    </aside>
    <main onDoubleClick={add}>
      <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} onConnect={connect} onNodeClick={(_, node) => setSelected(node.id)} fitView nodesDraggable>
        <Background gap={18} size={1} color="#ded9e5" /><MiniMap pannable zoomable /><Controls />
      </ReactFlow>
    </main>
    <aside className="props"><div className="prop-head"><h2>节点属性</h2>{current && <button title="删除节点" onClick={remove}><Trash2 size={16} /></button>}</div>
      {current ? <><label>节点名称<input value={current.data.title} onChange={event => update('title', event.target.value)} /></label><label>内容<textarea value={current.data.content} onChange={event => update('content', event.target.value)} /></label><label>颜色<input type="color" value={current.data.color} onChange={event => update('color', event.target.value)} /></label><p>{edges.filter(edge => edge.source === current.id || edge.target === current.id).length} 条连接关系</p></> : <p>选择一个开发节点以编辑内容</p>}
    </aside>
    <div className="canvas-hint">双击画布新增节点 · 拖拽端点创建连接 · Delete 删除 · Ctrl+Z 撤销</div>
    <button className="undo-button" title="撤销" onClick={undo}><Undo2 size={16} /></button>
  </div>
}

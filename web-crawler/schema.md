# 活水 (app.huoshui.org) 接口 Schema

> 本文件是对目标站点抓包/JS 分析后清洗出来的「请求说明书」。
> 生成方式：分析 `main.js` 里的 LeanCloud SDK 初始化 + 对 REST 接口做最小化验证请求。
> 后续写脚本、改脚本，都以本文件为准。

## 一、站点形态

- **前端**：Ionic + Angular 单页应用（SPA）。页面 HTML 里没有数据，老师评价全部由 JS 通过 XHR 接口异步加载。
- **后端**：**LeanCloud**（国内 BaaS）标准 REST API，SDK 版本 2.5.7。
- 所以「爬取」本质上就是**直接调用它的 LeanCloud REST 接口分页拉数据**，不需要模拟点击、不需要跑浏览器。

## 二、鉴权机制

LeanCloud 客户端用两个请求头鉴权，值直接写在网页 `main.js` 里公开下发（属于**匿名客户端 key**，只能做被 ACL 允许的读写，不是 masterKey）：

| Header | 值 |
|---|---|
| `X-LC-Id` | `zwjjm3MbxDYRKny9f31amkXq` |
| `X-LC-Key` | `PczcQb9HEBCLtLj4ohJ7ePj5` |
| `Content-Type` | `application/json` |

- **不需要登录**即可读取 `Courses` / `Reviews`（这两张表的 ACL 对匿名可读）。
- 登录后才会多一个 `X-LC-Session` 头（用于发表评价、点赞等写操作）。**只做读取爬取，用不到它。**

- **Base URL**：`https://lean.huoshui.org/1.1`

## 三、数据模型（两张核心表）

老师评价数据 = **`Reviews` 表**（一条条具体评价）+ **`Courses` 表**（每位老师每门课的聚合评分）。

### 表 1：`Reviews`（评价，约 16573 条）—— 这是"对老师的评价"主体

| 字段 | 类型 | 含义 | 示例 |
|---|---|---|---|
| `objectId` | string | 评价唯一 ID | `565b164c60b2597424f50a64` |
| `profName` | string | **老师姓名** | `何瑞文` |
| `courseName` | string | 课程名 | `线性代数A` |
| `courseId` | Pointer→Courses | 关联的课程对象 | `{__type:Pointer, objectId:...}` |
| `comment` | string | **评价正文** | `非常有趣的小老头，非常亲切…` |
| `rating` | object | 评分 `{rate1,rate2,rate3,overall}`（overall 满分 15） | `{rate1:5,rate2:5,rate3:5,overall:15}` |
| `tags` | array | 标签，元素 `{value,positive,checked}`，checked=true 才是勾选的 | `幽默 / 氛围轻松 / 富有创新` |
| `attendance` | object | 出勤 `{name,value}` | `{name:"中",value:3}` |
| `bird` | object | 水课程度 `{name,value}` | `{name:"绝非水课",value:1}` |
| `homework` | object | 作业量 `{name,value}` | `{name:"少",value:2}` |
| `exam` | object | 考试情况（划重点/开卷/原题/给分） | `{examprep:{checked:true}…}` |
| `upVote` / `downVote` | int | 有用 / 没用票数 | `32` / `0` |
| `approved` | bool | 是否审核通过 | `true` |
| `authorId` | Pointer→_User | 评价作者（匿名用户对象，**不要去反查真实身份**） | |
| `createdAt` / `updatedAt` | date | 创建 / 更新时间 | `2015-11-29T15:14:20.003Z` |

### 表 2：`Courses`（课程/老师聚合，约 6600 条）

| 字段 | 类型 | 含义 | 示例 |
|---|---|---|---|
| `objectId` | string | 课程唯一 ID | `561da590e4b0d98abf280f7c` |
| `prof` | string | **老师姓名** | `王慧` |
| `name` | string | 课程名 | `视阅口译` |
| `dept` | string | 院系 | `外语` |
| `position` | string | 职称 | `讲师` |
| `rateOverall` / `rate1..3` | number | 综合/分项平均分 | |
| `reviewCount` / `reviewGoodCount` | int | 评价数 / 好评数 | |
| `attendanceOverall` `homeworkOverall` `examOverall` `birdOverall` | number | 各维度平均 | |
| `tags` | array(22) | 各标签被勾选的累计次数 | `[0,3,0,...]` |
| `createdAt` / `updatedAt` | date | | |

> `Reviews.courseId` 指向 `Courses.objectId`，`Reviews.profName` 与 `Courses.prof` 对应同一位老师。
> 想要「某老师的全部评价」两条路：① 直接按 `Reviews.profName == 姓名` 查；② 先在 `Courses` 找该老师的 `objectId`，再按 `Reviews.courseId` 查。

## 四、请求模板（LeanCloud REST 查询）

### 4.1 分页拉取整张表

```
GET https://lean.huoshui.org/1.1/classes/Reviews?limit=1000&order=createdAt
Headers:
  X-LC-Id: zwjjm3MbxDYRKny9f31amkXq
  X-LC-Key: PczcQb9HEBCLtLj4ohJ7ePj5
```

- `limit`：单页条数，**最大 1000**。
- `skip`：跳过条数。⚠️ **skip 超过 ~10000 会变慢/不可靠**，全量抓取请改用游标（见下）。
- `count=1`：让响应里带上 `count` 总数（配合 `limit=0` 可只取总数）。

### 4.2 游标翻页（推荐，全量抓取用）

按 `createdAt` 升序，用上一页最后一条的时间做 `$gt` 游标，直到返回空：

```
GET /1.1/classes/Reviews?limit=1000&order=createdAt&where={"createdAt":{"$gt":{"__type":"Date","iso":"2016-09-01T01:15:23.008Z"}}}
```

（`scrape_reviews.py` 里 `fetch_class()` 就是这么做的，并用 `objectId` 去重。）

### 4.3 条件查询（`where` 参数，值是 URL 编码后的 JSON）

```
# 某位老师的全部评价
GET /1.1/classes/Reviews?where={"profName":"何瑞文"}&limit=1000

# 综合分 >= 12 的好评
GET /1.1/classes/Reviews?where={"rating.overall":{"$gte":12}}

# 只要某几个字段（省流量）
GET /1.1/classes/Reviews?keys=profName,courseName,comment,rating&limit=1000

# 按某门课查
GET /1.1/classes/Reviews?where={"courseId":{"__type":"Pointer","className":"Courses","objectId":"561da58be..."}}
```

## 五、时序与依赖

这个站点非常简单，**没有复杂的请求时序依赖**：

1. （可选）打开页面 → SDK 用内置 appId/appKey 初始化，无需登录即可读。
2. 直接 `GET /1.1/classes/Reviews`（或 `Courses`）分页拉取即可。
3. 后一页依赖前一页的 `createdAt` 游标 —— 这是唯一的"依赖"。

**没有** token 交换、**没有** 签名(sign)、**没有** 时间戳校验、**没有** 验证码。属于最好爬的一类。

## 六、注意事项（合规与礼貌）

- 数据是用户公开贡献的课程/老师评价。**仅供个人分析研究**，不要二次公开发布、不要用于骚扰或对老师/评价作者做人身攻击。
- `authorId` 是匿名用户指针，**不要尝试去匿名化/关联真人身份**。
- 请**限速**（脚本默认每页间隔 0.4s）。全量 Reviews 约 17 个请求就抓完了，没必要并发猛冲，别给对方服务器添乱。
- 用的是公开客户端 key，只做只读查询；不做任何写入/删除。

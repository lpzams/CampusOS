# campus-miniapp —— 微信小程序端（uni-app + Vue3）

高校智慧校园门户系统的小程序端。已接入接口文档中的认证、个人中心、新闻、公告、教务、缴费、校园卡、宿舍、报修、二手、活动、地图和 AI 模块，
与网站端调用同一批后端接口。

## 怎么跑起来（HBuilderX 方式，推荐）

1. 安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)（标准版即可，首次运行 uni-app 项目会提示装插件）；
2. HBuilderX 菜单 `文件 -> 导入 -> 从本地目录导入`，选择本 `miniapp` 目录；
3. 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)，
   并在其 `设置 -> 安全设置` 里开启「服务端口」（HBuilderX 要靠它拉起微信开发者工具）；
4. 先把接口服务跑起来（本机直接跑后端时地址为 `http://localhost:8080/api`；`utils/request.js` 的 `BASE_URL` 已按此配置）；
5. HBuilderX 菜单 `运行 -> 运行到小程序模拟器 -> 微信开发者工具`。

> 注意（都和"域名校验"有关）：
> - 开发阶段请求的是 `http://localhost:8080`，微信开发者工具需勾选
>   `详情 -> 本地设置 -> 不校验合法域名…`（`manifest.json` 的 `urlCheck:false` 一般会自动帮你勾上）；
> - **真机预览**时手机访问不到你电脑的 `localhost`，要把 `utils/request.js` 里的
>   `BASE_URL` 改成电脑的局域网 IP（手机和电脑连同一个 WiFi）；
> - 正式发布必须用 https 域名，并到微信公众平台配置 request 合法域名。
> - `manifest.json` 里的 appid 是游客体验 `touristappid`，正式开发时换成
>   自己在微信公众平台注册的小程序 appid。

## 目录结构

```
miniapp/
├── main.js               # 入口（uni-app Vue3 固定写法）
├── App.vue               # 应用生命周期 + 全局样式
├── manifest.json         # 应用配置：appid、各平台设置
├── pages.json            # 页面注册 + tabBar + 导航栏样式（加页面必改）
├── uni.scss              # 全局 SCSS 变量（主题色在这里改）
├── utils/
│   ├── request.js        # uni.request 封装：拆包 Result、统一错误提示
│   └── format.js         # 时间格式化等展示工具
├── api/
│   ├── news.js           # 新闻模块接口 ★模板
│   └── campus.js         # 其余 14 个模块的接口（认证/教务/生活/活动…）
└── pages/                # 15 个功能模块，均调后端同一批接口
    ├── index/index.vue   # 首页(tab)：新闻搜索/栏目筛选/九宫格入口/下拉刷新/上拉加载
    ├── services/index.vue# 服务(tab)：全部模块分组总目录
    ├── ai/index.vue      # 助手(tab)：AI 问答/办事流程/智能推荐
    ├── profile/index.vue # 我的(tab)：资料/学籍档案/实名/收藏，未登录显示登录引导
    ├── auth/index.vue    # 登录·短信·注册·找回
    ├── news/detail.vue   # 新闻详情（带参数跳转）★详情页模板
    ├── notice/index.vue  # 公告通知
    ├── academic/index.vue# 教务：课程/成绩/考试/空教室
    ├── life/index.vue    # 生活：缴费/校园卡/宿舍
    ├── repair/index.vue  # 校园报修
    ├── market/index.vue  # 二手交易
    ├── activity/index.vue# 校园活动
    ├── map/index.vue     # 校园地图与步行导航
    └── detail/index.vue  # 通用详情页（公告/报修/商品/活动/地点）
```

### 底部 tabBar（4 个主入口）

`pages.json` 的 `tabBar` 配了 **首页 / 服务 / 助手 / 我的** 四个标签页（纯文字，无图标依赖）。

> tabBar 页有两条 uni-app 硬规则，改动时注意：
> - 跳 tabBar 页只能用 `uni.switchTab`（`navigateTo` 会静默失败），且 **不能带 `?query` 参数**；
> - tabBar 页用 `onShow` 而非 `onLoad` 刷新数据；未登录不要 `redirectTo`，改为展示登录引导
>   （见 `pages/profile/index.vue`）。非 tab 页之间照旧用 `navigateTo`。

## 怎么加一个新页面（30 秒版）

1. `api/你的功能.js`：照抄 `api/news.js` 写接口函数；
2. `pages/你的功能/xxx.vue`：照抄 `pages/index/index.vue` 或 `pages/news/detail.vue`；
3. `pages.json`：在 `pages` 数组里注册新页面（不注册白屏！）；要进底部标签栏再加到 `tabBar.list`；
4. 从别的页面 `uni.navigateTo({ url: '/pages/你的功能/xxx?id=1' })` 跳过去
   （目标是 tabBar 页则用 `uni.switchTab`，且不带参数）。

完整说明（含后端、网站端步骤）见仓库根目录 `docs/新增功能指南.md`。

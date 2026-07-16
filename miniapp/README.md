# campus-miniapp —— 微信小程序端（uni-app + Vue3）

高校智慧校园门户系统的小程序端。当前实现 **新闻模块**（首页最新新闻 + 详情），
与网站端调用同一批后端接口。

## 怎么跑起来（HBuilderX 方式，推荐）

1. 安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)（标准版即可，首次运行 uni-app 项目会提示装插件）；
2. HBuilderX 菜单 `文件 -> 导入 -> 从本地目录导入`，选择本 `miniapp` 目录；
3. 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)，
   并在其 `设置 -> 安全设置` 里开启「服务端口」（HBuilderX 要靠它拉起微信开发者工具）；
4. 先把后端跑起来（默认 `http://localhost:8080`，数据库先执行 `docs/sql/init.sql`）；
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
├── pages.json            # 页面注册 + 导航栏样式（加页面必改）
├── uni.scss              # 全局 SCSS 变量（主题色在这里改）
├── utils/
│   ├── request.js        # uni.request 封装：拆包 Result、统一错误提示
│   └── format.js         # 时间格式化等展示工具
├── api/
│   └── news.js           # 新闻模块接口 ★模板
└── pages/
    ├── index/index.vue   # 首页：最新新闻（下拉刷新/上拉加载/栏目筛选）★列表页模板
    └── news/detail.vue   # 新闻详情（带参数跳转）★详情页模板
```

## 怎么加一个新页面（30 秒版）

1. `api/你的功能.js`：照抄 `api/news.js` 写接口函数；
2. `pages/你的功能/xxx.vue`：照抄 `pages/index/index.vue` 或 `pages/news/detail.vue`；
3. `pages.json`：在 `pages` 数组里注册新页面（不注册白屏！）；
4. 从别的页面 `uni.navigateTo({ url: '/pages/你的功能/xxx?id=1' })` 跳过去。

完整说明（含后端、网站端步骤）见仓库根目录 `docs/新增功能指南.md`。

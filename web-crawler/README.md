# web-crawler

爬取「活水」(app.huoshui.org) 老师评价的工作区。方法论：**人工探索 + HAR 记录 + 分析成 schema + 生成脚本**。

## 快速开始

```bash
cd scripts
pip install -r requirements.txt
python scrape_reviews.py --limit 20     # 先小批量验证
python scrape_reviews.py                # 抓全量 → ../output/reviews.{json,csv}
```

## 文件说明

| 文件 | 作用 |
|---|---|
| [详细的操作步骤.md](详细的操作步骤.md) | **从这里开始**，完整操作步骤（含 HAR 通用方法） |
| [schema.md](schema.md) | 目标站点接口说明书（清洗后的核心资产） |
| [scripts/scrape_reviews.py](scripts/scrape_reviews.py) | 老师评价爬虫，直接可运行 |
| [scripts/export_mysql_seed.py](scripts/export_mysql_seed.py) | 将本地评价清洗为 CampusOS 课程/评价数据库种子 |
| [scripts/analyze_har.py](scripts/analyze_har.py) | HAR 文件分析工具（提炼真接口） |
| `hars/` | 放浏览器导出的 .har（不入库） |
| `output/` | 爬取结果 json/csv（不入库） |

## 结论速览

活水后端是 **LeanCloud REST API**，评价数据在 `Reviews` 表（约 1.6 万条），**无需登录、无签名、无验证码**，`limit`+`createdAt` 游标翻页即可全量抓取。详见 [schema.md](schema.md)。

> 仅供个人分析研究，请限速、勿二次公开、勿反查评价作者身份。

## 导入 CampusOS 数据库

已有 `output/reviews.csv` 时，无需重新联网抓取：

```bash
python web-crawler/scripts/export_mysql_seed.py
```

命令会在本地生成 `docs/sql/004_course_reviews.sql`，其中包含课程-教师目录与已审核评价。
为保护评价者隐私，转换过程不会读取或导入作者账号、用户指针或登录信息。
该全量文件已加入 `.gitignore`，不会随项目二次公开；版本库只保留表结构和转换工具。

- 新数据库：`docker compose down -v && docker compose up -d`，首次启动自动执行全部 SQL。
- 已有数据库：`docker exec -i campus-mysql mysql -uroot -proot campus_os < docs/sql/004_course_reviews.sql`。
- 重新运行安全：课程和评价均使用来源 ID 唯一键与 `INSERT IGNORE` 去重。

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
活水 (app.huoshui.org) 老师评价爬虫
=====================================
后端是 LeanCloud REST API，直接分页抓取 Reviews / Courses 两张表。
接口与字段说明见同目录上一层的 schema.md。

用法：
    pip install -r requirements.txt
    python scrape_reviews.py                 # 抓全部评价 -> output/reviews.json + reviews.csv
    python scrape_reviews.py --limit 200      # 只抓前 200 条，先验证
    python scrape_reviews.py --classes Courses  # 顺便把课程聚合表也抓下来
    python scrape_reviews.py --prof 王慧        # 只抓某位老师的评价

说明：脚本用的是网页端 JS 里公开下发的客户端 key（X-LC-Id / X-LC-Key），
      这是给浏览器用的匿名只读 key，不是 masterKey。请合理限速、仅供个人分析。
"""
import argparse
import csv
import json
import os
import sys
import time

import requests

# ---- LeanCloud 客户端配置（来自 app.huoshui.org 的 main.js，公开客户端 key）----
APP_ID = "zwjjm3MbxDYRKny9f31amkXq"
APP_KEY = "PczcQb9HEBCLtLj4ohJ7ePj5"
BASE = "https://lean.huoshui.org/1.1"

HEADERS = {
    "X-LC-Id": APP_ID,
    "X-LC-Key": APP_KEY,
    "Content-Type": "application/json",
    "User-Agent": "Mozilla/5.0 (huoshui-reviews-scraper; personal analysis)",
}

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "output")


def fetch_class(cls, where=None, batch=1000, sleep=0.4, hard_limit=None):
    """
    用 createdAt 游标分页抓取一整张 LeanCloud 表。
    注意：skip 分页在 skip>10000 后不可靠，所以这里用 createdAt 递增游标 + objectId 去重。
    """
    seen = {}          # objectId -> record，天然去重
    cursor = None      # 上一页最后一条的 createdAt
    page_no = 0
    if hard_limit:
        batch = min(batch, hard_limit)  # 调试小 limit 时不必整页拉 1000
    while True:
        cond = dict(where or {})
        if cursor:
            cond["createdAt"] = {"$gt": {"__type": "Date", "iso": cursor}}
        params = {"limit": batch, "order": "createdAt"}
        if cond:
            params["where"] = json.dumps(cond, ensure_ascii=False)

        r = requests.get(f"{BASE}/classes/{cls}", headers=HEADERS, params=params, timeout=30)
        r.raise_for_status()
        page = r.json().get("results", [])
        if not page:
            break

        page_no += 1
        for rec in page:
            seen[rec["objectId"]] = rec
        cursor = page[-1]["createdAt"]
        print(f"  [{cls}] 第 {page_no} 页 +{len(page)}，累计 {len(seen)} 条 (cursor={cursor})")

        if hard_limit and len(seen) >= hard_limit:
            break
        if len(page) < batch:
            break
        time.sleep(sleep)

    records = list(seen.values())
    if hard_limit:
        records = records[:hard_limit]
    return records


def flatten_review(r):
    """把一条 Review 拍平成适合 CSV 的一维字典（只取老师评价关心的字段）。"""
    rating = r.get("rating") or {}
    def name_of(field):
        v = r.get(field)
        return v.get("name") if isinstance(v, dict) else v
    tags = r.get("tags") or []
    tag_str = " ".join(t.get("value", "") for t in tags
                       if isinstance(t, dict) and t.get("checked"))
    return {
        "objectId": r.get("objectId"),
        "profName": r.get("profName"),            # 老师
        "courseName": r.get("courseName"),        # 课程
        "courseId": (r.get("courseId") or {}).get("objectId"),
        "comment": (r.get("comment") or "").replace("\n", " ").strip(),  # 评价正文
        "tags": tag_str,                          # 勾选的标签（幽默/氛围轻松...）
        "overall": rating.get("overall"),         # 综合分
        "rate1": rating.get("rate1"),
        "rate2": rating.get("rate2"),
        "rate3": rating.get("rate3"),
        "attendance": name_of("attendance"),      # 出勤
        "bird": name_of("bird"),                  # 水课程度
        "homework": name_of("homework"),          # 作业
        "upVote": r.get("upVote"),
        "downVote": r.get("downVote"),
        "approved": r.get("approved"),
        "createdAt": r.get("createdAt"),
    }


def save_json(records, name):
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)
    print(f"  已写出 {path}（{len(records)} 条）")


def save_csv(rows, name, fieldnames):
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, name)
    with open(path, "w", encoding="utf-8-sig", newline="") as f:  # utf-8-sig 便于 Excel 直接打开
        w = csv.DictWriter(f, fieldnames=fieldnames)
        w.writeheader()
        w.writerows(rows)
    print(f"  已写出 {path}（{len(rows)} 行）")


def main():
    ap = argparse.ArgumentParser(description="活水老师评价爬虫")
    ap.add_argument("--classes", nargs="+", default=["Reviews"],
                    choices=["Reviews", "Courses"], help="要抓取的表，默认 Reviews")
    ap.add_argument("--limit", type=int, default=None, help="最多抓多少条（调试用），默认全量")
    ap.add_argument("--prof", type=str, default=None, help="只抓某位老师（按 profName 精确匹配）")
    ap.add_argument("--sleep", type=float, default=0.4, help="每页之间的间隔秒数，礼貌限速")
    args = ap.parse_args()

    for cls in args.classes:
        print(f"\n==> 抓取 {cls} ...")
        where = None
        if args.prof and cls == "Reviews":
            where = {"profName": args.prof}
        records = fetch_class(cls, where=where, sleep=args.sleep, hard_limit=args.limit)
        save_json(records, f"{cls.lower()}.json")

        if cls == "Reviews":
            rows = [flatten_review(r) for r in records]
            save_csv(rows, "reviews.csv", list(rows[0].keys()) if rows else [])

    print("\n完成。结果在 web-crawler/output/ 下。")


if __name__ == "__main__":
    try:
        main()
    except requests.HTTPError as e:
        print(f"HTTP 错误：{e}\n响应：{e.response.text[:500]}", file=sys.stderr)
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n已中断。", file=sys.stderr)
        sys.exit(130)

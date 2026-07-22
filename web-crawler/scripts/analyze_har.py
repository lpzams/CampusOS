#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
HAR 分析辅助脚本
=================
把浏览器导出的 .har 文件里的"真接口"提炼出来，过滤掉 css/图片/字体/埋点等噪音，
方便你（或 AI）快速看清一个站点到底调了哪些接口、参数长什么样。

用法：
    python analyze_har.py ../hars/01-login.har                # 列出所有接口调用
    python analyze_har.py ../hars/01-login.har --json         # 输出精简 JSON（喂给 AI）
    python analyze_har.py ../hars/01-login.har --grep review   # 只看 URL 含 review 的
    python analyze_har.py ../hars/01-login.har --detail 3      # 看第 3 条的完整请求/响应
"""
import argparse
import json
import re
import sys

# 静态资源 / 埋点，分析接口时通常直接过滤掉
NOISE = re.compile(
    r"\.(css|js|png|jpe?g|gif|webp|svg|ico|woff2?|ttf|eot|map)(\?|$)"
    r"|google-analytics|googletagmanager|gtag/js|doubleclick|sentry|hotjar",
    re.I,
)


def load_entries(path):
    with open(path, "r", encoding="utf-8") as f:
        har = json.load(f)
    return har.get("log", {}).get("entries", [])


def is_api(entry):
    url = entry["request"]["url"]
    if NOISE.search(url):
        return False
    mime = (entry.get("response", {}).get("content", {}) or {}).get("mimeType", "")
    # 保留 json / 表单 / 无类型（很多接口 OPTIONS 预检也留着看）
    return ("json" in mime) or ("form" in mime) or (entry["request"]["method"] != "GET") or ("json" in url)


def body_of(msg):
    """取请求 postData 或响应 content 文本。"""
    if "postData" in msg:
        return msg["postData"].get("text", "")
    content = msg.get("content", {}) or {}
    return content.get("text", "")


def main():
    ap = argparse.ArgumentParser(description="HAR 接口提炼")
    ap.add_argument("har", help="har 文件路径")
    ap.add_argument("--json", action="store_true", help="输出精简 JSON（适合喂 AI）")
    ap.add_argument("--grep", help="只保留 URL 匹配该关键词的条目")
    ap.add_argument("--detail", type=int, metavar="N", help="打印第 N 条的完整请求+响应")
    ap.add_argument("--all", action="store_true", help="不过滤静态资源，列出全部请求")
    args = ap.parse_args()

    entries = load_entries(args.har)
    picked = []
    for e in entries:
        if not args.all and not is_api(e):
            continue
        if args.grep and args.grep.lower() not in e["request"]["url"].lower():
            continue
        picked.append(e)

    if not picked:
        print("没有匹配的请求。试试 --all 或换个 --grep 关键词。", file=sys.stderr)
        return

    # --detail：打印某一条的全部细节
    if args.detail is not None:
        i = args.detail
        if not (0 <= i < len(picked)):
            print(f"序号越界，共 {len(picked)} 条（0..{len(picked)-1}）", file=sys.stderr)
            return
        e = picked[i]
        req, resp = e["request"], e["response"]
        print(f"# [{i}] {req['method']} {req['url']}")
        print(f"\n## 请求头")
        for h in req.get("headers", []):
            print(f"  {h['name']}: {h['value']}")
        rb = body_of(req)
        if rb:
            print(f"\n## 请求体\n{rb}")
        print(f"\n## 响应 {resp.get('status')} {resp.get('statusText','')}")
        sb = body_of(resp)
        print(f"\n## 响应体（前 2000 字）\n{(sb or '')[:2000]}")
        return

    # --json：精简结构，方便丢给 AI
    if args.json:
        out = []
        for e in picked:
            req = e["request"]
            out.append({
                "method": req["method"],
                "url": req["url"],
                "status": e["response"].get("status"),
                "reqBody": body_of(req)[:500],
                "respPreview": (body_of(e["response"]) or "")[:500],
            })
        print(json.dumps(out, ensure_ascii=False, indent=2))
        return

    # 默认：一行一条，概览
    print(f"共 {len(picked)} 条接口调用：\n")
    for i, e in enumerate(picked):
        req = e["request"]
        status = e["response"].get("status")
        print(f"[{i:>3}] {req['method']:<6} {status} {req['url']}")


if __name__ == "__main__":
    main()

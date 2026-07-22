#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""将 reviews.csv 转成 CampusOS 可重复执行的课程评价种子 SQL。

只导入已审核评价，并主动丢弃 authorId 等身份字段。默认读取本目录下的
../output/reviews.csv，输出到 ../../docs/sql/004_course_reviews.sql。
"""
import argparse
import csv
import hashlib
import os
from collections import OrderedDict
from datetime import datetime


ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
DEFAULT_INPUT = os.path.join(os.path.dirname(__file__), "..", "output", "reviews.csv")
DEFAULT_OUTPUT = os.path.join(ROOT, "docs", "sql", "004_course_reviews.sql")


def sql_string(value):
    if value is None or value == "":
        return "NULL"
    value = str(value).replace("\\", "\\\\").replace("'", "''").replace("\x00", "")
    return "'" + value + "'"


def sql_number(value, decimal=False):
    if value is None or value == "":
        return "NULL"
    try:
        number = float(value)
        return f"{number:.2f}" if decimal else str(int(number))
    except (TypeError, ValueError):
        return "NULL"


def sql_datetime(value):
    if not value:
        return "NULL"
    try:
        return sql_string(datetime.fromisoformat(value.replace("Z", "+00:00")).strftime("%Y-%m-%d %H:%M:%S"))
    except ValueError:
        return "NULL"


def source_key(row):
    raw = "|".join(str(row.get(field) or "").strip() for field in ("courseId", "courseName", "profName"))
    return hashlib.sha1((raw or repr(row)).encode("utf-8")).hexdigest()


def read_rows(path):
    with open(path, encoding="utf-8-sig", newline="") as handle:
        for row in csv.DictReader(handle):
            if str(row.get("approved", "")).lower() == "false":
                continue
            if not row.get("courseName") or not row.get("comment"):
                continue
            yield row


def build_catalog(rows):
    grouped = OrderedDict()
    for row in rows:
        key = source_key(row)
        item = grouped.setdefault(key, {
            "source": key, "name": row.get("courseName", "").strip(),
            "prof": row.get("profName", "").strip(), "count": 0,
            "overall": [], "rate1": [], "rate2": [], "rate3": [],
        })
        item["count"] += 1
        for field in ("overall", "rate1", "rate2", "rate3"):
            try:
                item[field].append(float(row[field]))
            except (TypeError, ValueError, KeyError):
                pass
    return grouped


def average(values):
    return f"{sum(values) / len(values):.2f}" if values else "NULL"


def write_seed(rows, output):
    catalog = build_catalog(rows)
    os.makedirs(os.path.dirname(output), exist_ok=True)
    with open(output, "w", encoding="utf-8") as handle:
        handle.write("""-- 004 课程目录与课程评价（由 web-crawler/scripts/export_mysql_seed.py 生成）
-- 数据来源：活水公开课程评价；仅保留已审核内容，不导入评价作者身份信息。
-- 重新生成：python web-crawler/scripts/export_mysql_seed.py
SET NAMES utf8mb4;
USE campus_os;

CREATE TABLE IF NOT EXISTS t_course_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_id VARCHAR(80) NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  professor VARCHAR(100) NOT NULL,
  review_count INT NOT NULL DEFAULT 0,
  avg_overall DECIMAL(5,2) NULL,
  avg_rate1 DECIMAL(5,2) NULL,
  avg_rate2 DECIMAL(5,2) NULL,
  avg_rate3 DECIMAL(5,2) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_course_source (source_id), KEY idx_course_name (course_name), KEY idx_professor (professor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程-教师评价聚合';

CREATE TABLE IF NOT EXISTS t_course_review (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_id VARCHAR(80) NOT NULL,
  course_source_id VARCHAR(80) NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  professor VARCHAR(100) NOT NULL,
  comment TEXT NOT NULL,
  overall TINYINT NULL,
  rate1 TINYINT NULL,
  rate2 TINYINT NULL,
  rate3 TINYINT NULL,
  tags VARCHAR(500) NULL,
  attendance VARCHAR(50) NULL,
  bird VARCHAR(50) NULL,
  homework VARCHAR(50) NULL,
  up_vote INT NOT NULL DEFAULT 0,
  down_vote INT NOT NULL DEFAULT 0,
  source_created_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_review_source (source_id), KEY idx_review_course (course_source_id), KEY idx_review_overall (overall)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匿名课程评价';

INSERT IGNORE INTO t_course_catalog (source_id,course_name,professor,review_count,avg_overall,avg_rate1,avg_rate2,avg_rate3) VALUES
""")
        catalog_values = []
        for item in catalog.values():
            catalog_values.append("(%s,%s,%s,%s,%s,%s,%s,%s)" % (
                sql_string(item["source"]), sql_string(item["name"]), sql_string(item["prof"]),
                item["count"], average(item["overall"]), average(item["rate1"]),
                average(item["rate2"]), average(item["rate3"])))
        handle.write(",\n".join(catalog_values) + ";\n\n")
        handle.write("INSERT IGNORE INTO t_course_review (source_id,course_source_id,course_name,professor,comment,overall,rate1,rate2,rate3,tags,attendance,bird,homework,up_vote,down_vote,source_created_at) VALUES\n")
        review_values = []
        for index, row in enumerate(rows):
            review_values.append("(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)" % (
                sql_string(row.get("objectId")), sql_string(source_key(row)), sql_string(row.get("courseName", "").strip()),
                sql_string(row.get("profName", "").strip()), sql_string(row.get("comment", "").strip()),
                sql_number(row.get("overall")), sql_number(row.get("rate1")), sql_number(row.get("rate2")), sql_number(row.get("rate3")),
                sql_string(row.get("tags")), sql_string(row.get("attendance")), sql_string(row.get("bird")), sql_string(row.get("homework")),
                sql_number(row.get("upVote")), sql_number(row.get("downVote")), sql_datetime(row.get("createdAt"))))
            if len(review_values) >= 500:
                handle.write(",\n".join(review_values) + ";\n")
                if index < len(rows) - 1:
                    handle.write("INSERT IGNORE INTO t_course_review (source_id,course_source_id,course_name,professor,comment,overall,rate1,rate2,rate3,tags,attendance,bird,homework,up_vote,down_vote,source_created_at) VALUES\n")
                review_values = []
        if review_values:
            handle.write(",\n".join(review_values) + ";\n")
    print(f"写入 {output}: {len(catalog)} 个课程-教师条目，{len(rows)} 条评价")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", default=DEFAULT_INPUT)
    parser.add_argument("--output", default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    rows = list(read_rows(args.input))
    if not rows:
        raise SystemExit("没有找到可导入的已审核评价")
    write_seed(rows, args.output)


if __name__ == "__main__":
    main()

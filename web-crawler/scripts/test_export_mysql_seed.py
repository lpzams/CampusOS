#!/usr/bin/env python3
import csv
import os
import tempfile
import unittest

from export_mysql_seed import read_rows, source_key, write_seed


class ExportMysqlSeedTest(unittest.TestCase):
    def test_export_filters_unapproved_and_removes_identity(self):
        fields = ["objectId", "profName", "courseName", "courseId", "comment", "overall", "rate1", "rate2", "rate3", "approved", "createdAt"]
        rows = [
            {"objectId": "r1", "profName": "张老师", "courseName": "数据结构", "courseId": "c1", "comment": "讲解清晰", "overall": "15", "rate1": "5", "rate2": "5", "rate3": "5", "approved": "True", "createdAt": "2026-01-01T00:00:00.000Z"},
            {"objectId": "r2", "profName": "张老师", "courseName": "数据结构", "courseId": "c1", "comment": "不应导入", "overall": "1", "approved": "False"},
        ]
        with tempfile.TemporaryDirectory() as directory:
            csv_path = os.path.join(directory, "reviews.csv")
            sql_path = os.path.join(directory, "seed.sql")
            with open(csv_path, "w", encoding="utf-8-sig", newline="") as handle:
                writer = csv.DictWriter(handle, fieldnames=fields)
                writer.writeheader()
                writer.writerows(rows)
            accepted = list(read_rows(csv_path))
            write_seed(accepted, sql_path)
            with open(sql_path, encoding="utf-8") as handle:
                sql = handle.read()
        self.assertEqual(1, len(accepted))
        self.assertIn("讲解清晰", sql)
        self.assertNotIn("不应导入", sql)
        self.assertNotIn("authorId", sql)

    def test_course_key_distinguishes_professors(self):
        first = source_key({"courseId": "c1", "courseName": "数据结构", "profName": "张老师"})
        second = source_key({"courseId": "c1", "courseName": "数据结构", "profName": "李老师"})
        self.assertNotEqual(first, second)

    def test_course_key_distinguishes_reused_source_ids(self):
        first = source_key({"courseId": "c1", "courseName": "形势与政策", "profName": "张老师"})
        second = source_key({"courseId": "c1", "courseName": "大学体育", "profName": "张老师"})
        self.assertNotEqual(first, second)

    def test_course_key_ignores_csv_whitespace(self):
        clean = source_key({"courseId": "c1", "courseName": "英语Ⅱ", "profName": "田田"})
        padded = source_key({"courseId": " c1 ", "courseName": "英语Ⅱ ", "profName": "田田 "})
        self.assertEqual(clean, padded)

    def test_exact_batch_has_no_dangling_insert(self):
        rows = [{
            "objectId": f"r{index}", "profName": "张老师", "courseName": "数据结构",
            "courseId": "c1", "comment": "讲解清晰", "approved": "True",
        } for index in range(500)]
        with tempfile.TemporaryDirectory() as directory:
            sql_path = os.path.join(directory, "seed.sql")
            write_seed(rows, sql_path)
            with open(sql_path, encoding="utf-8") as handle:
                sql = handle.read().rstrip()
        self.assertTrue(sql.endswith(";"))
        self.assertFalse(sql.endswith("VALUES"))


if __name__ == "__main__":
    unittest.main()

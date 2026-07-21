-- ============================================================
-- 模块五测试数据：教室管理 + 排课管理 + 选课管理
-- 前提：t_user 表中需存在对应ID的用户（1-管理员 2,3-教师 4,5,6,7,8-学生）
-- ============================================================

-- ==================== 1. 教室数据（10间） ====================

INSERT INTO t_classroom (building, name, floor, capacity, type, status) VALUES
('教学楼A', 'A101', 1, 120, 2, 0),   -- 多媒体教室，空闲
('教学楼A', 'A102', 1, 80,  2, 0),   -- 多媒体教室，空闲
('教学楼A', 'A103', 1, 60,  1, 0),   -- 普通教室，空闲
('教学楼A', 'A201', 2, 80,  3, 0),   -- 智慧教室，空闲
('教学楼A', 'A202', 2, 60,  1, 0),   -- 普通教室，空闲
('教学楼A', 'A301', 3, 40,  4, 0),   -- 实验室，空闲
('教学楼B', 'B101', 1, 120, 2, 0),   -- 多媒体教室，空闲
('教学楼B', 'B102', 1, 60,  5, 0),   -- 机房，空闲
('教学楼B', 'B201', 2, 60,  1, 1),   -- 普通教室，占用（行政状态）
('教学楼B', 'B301', 3, 100, 2, 2);   -- 多媒体教室，停用

-- ==================== 2. 课程数据（8门） ====================

-- 学期：2024-2025-1（秋季学期）
INSERT INTO t_course (name, course_code, credit, teacher_id, teacher_name, semester, max_students, color) VALUES
('高等数学A',    'MATH101', 4, 2, '王教授', '2024-2025-1', 120, '#FF6B6B'),
('线性代数',     'MATH102', 3, 2, '王教授', '2024-2025-1', 80,  '#FF8E8E'),
('Java程序设计', 'CS301',   4, 3, '张教授', '2024-2025-1', 60,  '#4ECDC4'),
('数据库原理',   'CS302',   3, 3, '张教授', '2024-2025-1', 60,  '#45B7D1'),
('软件工程',     'CS303',   3, 3, '张教授', '2024-2025-1', 45,  '#96CEB4'),
('大学英语3',    'ENG201',  2, 2, '王教授', '2024-2025-1', 100, '#FFEAA7'),
('大学物理B',    'PHY101',  3, 2, '王教授', '2024-2025-1', 80,  '#DDA0DD'),
('操作系统',     'CS304',   4, 3, '张教授', '2024-2025-1', 50,  '#98D8C8');

-- ==================== 3. 排课数据（课程→教室+时间） ====================

-- 课程1：高等数学A — 教学楼A A101 周一 08:00-09:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(1, 1, 1, '08:00-09:35', '1-16周');

-- 课程2：线性代数 — 教学楼A A102 周三 10:00-11:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(2, 2, 3, '10:00-11:35', '1-16周');

-- 课程3：Java程序设计 — 教学楼A A201 周二 08:00-09:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(3, 4, 2, '08:00-09:35', '1-16周');

-- 课程4：数据库原理 — 教学楼B B101 周二 14:00-15:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(4, 7, 2, '14:00-15:35', '1-16周');

-- 课程5：软件工程 — 教学楼A A202 周四 10:00-11:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(5, 5, 4, '10:00-11:35', '1-16周');

-- 课程6：大学英语3 — 教学楼A A101 周五 08:00-09:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(6, 1, 5, '08:00-09:35', '1-16周');

-- 课程7：大学物理B — 教学楼B B101 周一 14:00-15:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(7, 7, 1, '14:00-15:35', '1-16周');

-- 课程8：操作系统 — 教学楼A A201 周三 16:00-17:35 1-16周
INSERT INTO t_schedule (course_id, classroom_id, day_of_week, time_slot, weeks) VALUES
(8, 4, 3, '16:00-17:35', '1-16周');

-- ==================== 4. 选课数据 ====================

-- 学生4（张三）：选了5门课
INSERT INTO t_course_enrollment (course_id, student_id, semester) VALUES
(1, 4, '2024-2025-1'),   -- 高等数学A
(2, 4, '2024-2025-1'),   -- 线性代数
(3, 4, '2024-2025-1'),   -- Java程序设计
(6, 4, '2024-2025-1'),   -- 大学英语3
(7, 4, '2024-2025-1');   -- 大学物理B

-- 学生5（李四）：选了4门课
INSERT INTO t_course_enrollment (course_id, student_id, semester) VALUES
(1, 5, '2024-2025-1'),   -- 高等数学A
(3, 5, '2024-2025-1'),   -- Java程序设计
(4, 5, '2024-2025-1'),   -- 数据库原理
(8, 5, '2024-2025-1');   -- 操作系统

-- 学生6（王五）：选了3门课
INSERT INTO t_course_enrollment (course_id, student_id, semester) VALUES
(1, 6, '2024-2025-1'),   -- 高等数学A
(4, 6, '2024-2025-1'),   -- 数据库原理
(5, 6, '2024-2025-1');   -- 软件工程

-- 学生7（赵六）：选了3门课
INSERT INTO t_course_enrollment (course_id, student_id, semester) VALUES
(2, 7, '2024-2025-1'),   -- 线性代数
(3, 7, '2024-2025-1'),   -- Java程序设计
(6, 7, '2024-2025-1');   -- 大学英语3

-- 学生8（孙七）：选了2门课
INSERT INTO t_course_enrollment (course_id, student_id, semester) VALUES
(5, 8, '2024-2025-1'),   -- 软件工程
(8, 8, '2024-2025-1');   -- 操作系统

-- ============================================================
-- 003 号脚本：课表演示数据加密度（配合 002 的单条课程，凑出一整周课表）
-- 无 userId 字段 = 所有登录用户可见（演示用）。
-- 已有环境补执行：docker exec -i campus-mysql mysql -uroot -proot < docs/sql/003_course_demo.sql
-- ============================================================
SET NAMES utf8mb4;
USE campus_os;

INSERT INTO t_campus_record(record_type, record_data) VALUES
('course', JSON_OBJECT('dayOfWeek',1,'timeSlot','10:10-11:45','courses',JSON_ARRAY(JSON_OBJECT('id',2,'name','高等数学','teacher','李教授','classroom','B203','building','教学楼B','weeks','1-16周','color','#7C5CD6')))),
('course', JSON_OBJECT('dayOfWeek',2,'timeSlot','08:00-09:35','courses',JSON_ARRAY(JSON_OBJECT('id',3,'name','数据结构','teacher','陈教授','classroom','A305','building','教学楼A','weeks','1-16周','color','#5AA6A0')))),
('course', JSON_OBJECT('dayOfWeek',2,'timeSlot','14:30-16:05','courses',JSON_ARRAY(JSON_OBJECT('id',4,'name','大学英语','teacher','Sarah','classroom','C102','building','外语楼','weeks','1-16周','color','#D4995E')))),
('course', JSON_OBJECT('dayOfWeek',3,'timeSlot','08:00-09:35','courses',JSON_ARRAY(JSON_OBJECT('id',5,'name','计算机网络','teacher','赵教授','classroom','B305','building','教学楼B','weeks','1-16周','color','#E36F9A')))),
('course', JSON_OBJECT('dayOfWeek',3,'timeSlot','10:10-11:45','courses',JSON_ARRAY(JSON_OBJECT('id',6,'name','体育（羽毛球）','teacher','刘教练','classroom','体育馆','building','体育馆','weeks','1-16周','color','#63B75D')))),
('course', JSON_OBJECT('dayOfWeek',4,'timeSlot','10:10-11:45','courses',JSON_ARRAY(JSON_OBJECT('id',7,'name','操作系统','teacher','孙教授','classroom','A402','building','教学楼A','weeks','1-16周','color','#B06FD0')))),
('course', JSON_OBJECT('dayOfWeek',4,'timeSlot','14:30-16:05','courses',JSON_ARRAY(JSON_OBJECT('id',8,'name','数据库原理','teacher','王教授','classroom','A301','building','教学楼A','weeks','1-16周','color','#4E8FD9')))),
('course', JSON_OBJECT('dayOfWeek',5,'timeSlot','08:00-09:35','courses',JSON_ARRAY(JSON_OBJECT('id',9,'name','软件工程','teacher','周教授','classroom','信息楼402','building','信息楼','weeks','1-16周','color','#D9645E')))),
('course', JSON_OBJECT('dayOfWeek',5,'timeSlot','16:20-17:55','courses',JSON_ARRAY(JSON_OBJECT('id',10,'name','形势与政策','teacher','辅导员','classroom','A101','building','教学楼A','weeks','1-8周','color','#8A8FA3'))));

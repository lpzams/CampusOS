-- 演示学生/教师持续使用数据；可重复执行，只刷新 demoSeed 标记的数据。
SET NAMES utf8mb4;
USE campus_os;

SET @student_id = (SELECT id FROM t_campus_record WHERE record_type='user' AND JSON_UNQUOTE(JSON_EXTRACT(record_data,'$.username'))='student' LIMIT 1);
SET @teacher_id = (SELECT id FROM t_campus_record WHERE record_type='user' AND JSON_UNQUOTE(JSON_EXTRACT(record_data,'$.username'))='teacher' LIMIT 1);

DELETE FROM t_campus_record WHERE JSON_UNQUOTE(JSON_EXTRACT(record_data,'$.demoSeed'))='rich-demo-v1';

UPDATE t_campus_record SET record_data=JSON_SET(record_data,'$.realName','演示学生','$.balance',186.35,'$.status','正常','$.expireTime','2030-09-01')
WHERE record_type='card' AND JSON_EXTRACT(record_data,'$.userId')=@student_id;

INSERT INTO t_campus_record(record_type,record_data) VALUES
('dormitory',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'building','北区学生宿舍8栋','room','412室','type','四人间','bedNumber','2号床','checkInDate','2025-09-01','counselor','陈老师','electricityBalance',68.40,'waterBalance',32.60,'members',JSON_ARRAY(JSON_OBJECT('userId',@student_id,'realName','演示学生','phone','13800138001','isRoomLeader',true),JSON_OBJECT('realName','李明','phone','13800138112','isRoomLeader',false),JSON_OBJECT('realName','王浩','phone','13800138223','isRoomLeader',false),JSON_OBJECT('realName','周宇','phone','13800138334','isRoomLeader',false)),'facilities',JSON_ARRAY('空调','独立卫生间','热水器','书桌','校园网','阳台'))),
('payment',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','教材费','amount',368.00,'deadline','2026-08-20','status','待缴费','description','2026秋季学期教材预订费')),
('payment',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','住宿费','amount',1200.00,'deadline','2026-09-10','status','待缴费','description','2026-2027学年住宿费')),
('paymentOrder',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','学费','amount',5200.00,'payMethod','WECHAT','status','支付成功','createTime','2026-07-05 09:18:26','orderNo','PAY20260705091826')),
('paymentOrder',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','电费充值','dormitoryId','8-412','amount',100.00,'payMethod','ALIPAY','status','支付成功','createTime','2026-07-18 20:06:15','orderNo','ELE20260718200615')),
('paymentOrder',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','水费缴纳','dormitoryId','8-412','amount',50.00,'payMethod','WECHAT','status','支付成功','createTime','2026-06-28 18:42:09','orderNo','WAT20260628184209')),
('paymentOrder',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','电费充值','dormitoryId','8-412','amount',80.00,'payMethod','WECHAT','status','支付成功','createTime','2026-05-23 21:11:36','orderNo','ELE20260523211136')),
('paymentOrder',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','考试报名费','amount',30.00,'payMethod','ALIPAY','status','支付成功','createTime','2026-04-12 14:35:40','orderNo','EXM20260412143540')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','余额充值','amount',200.00,'merchant','微信充值','status','成功','createTime','2026-07-21 19:32:08')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','食堂消费','amount',-15.80,'merchant','北区第二食堂','createTime','2026-07-22 12:16:35')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','早餐消费','amount',-6.50,'merchant','北区第一食堂','createTime','2026-07-22 07:43:12')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','超市消费','amount',-23.60,'merchant','校园红旗超市','createTime','2026-07-21 20:05:44')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','食堂消费','amount',-18.20,'merchant','南区美食广场','createTime','2026-07-21 12:08:19')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','图书馆打印','amount',-4.80,'merchant','图书馆文印中心','createTime','2026-07-20 16:48:03')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','校车消费','amount',-2.00,'merchant','犀浦校区摆渡车','createTime','2026-07-20 08:02:51')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','洗浴消费','amount',-3.45,'merchant','北区公共浴室','createTime','2026-07-19 22:14:27')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','食堂消费','amount',-12.60,'merchant','北区第一食堂','createTime','2026-07-19 12:20:16')),
('cardTransaction',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','余额充值','amount',100.00,'merchant','支付宝充值','status','成功','createTime','2026-07-15 18:10:22')),
('repair',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','水电','title','卫生间水龙头漏水','description','洗手台水龙头关闭后仍持续滴水','building','北区学生宿舍8栋','room','412室','contactPhone','13800138001','status','处理中','statusDesc','维修师傅已接单，预计7月23日上午上门','createTime','2026-07-21 21:08:30')),
('repair',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','网络','title','宿舍网口连接不稳定','description','2号床网口晚间频繁断连','building','北区学生宿舍8栋','room','412室','contactPhone','13800138001','status','已完成','statusDesc','已更换墙面网络模块并测试通过','createTime','2026-06-15 19:22:10','completeTime','2026-06-16 15:40:00')),
('repair',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','家具','title','书桌抽屉滑轨损坏','description','抽屉无法正常拉出','building','北区学生宿舍8栋','room','412室','contactPhone','13800138001','status','已评价','statusDesc','滑轨已更换','score',5,'evaluation','响应很快，维修师傅很认真。','createTime','2026-04-08 10:12:00','completeTime','2026-04-09 09:35:00')),
('repair',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'type','电器','title','宿舍空调制冷效果差','description','空调运行一小时室温仍较高','building','北区学生宿舍8栋','room','412室','contactPhone','13800138001','status','已评价','statusDesc','完成滤网清洁并补充制冷剂','score',4,'evaluation','处理及时，制冷恢复正常。','createTime','2026-05-26 13:30:00','completeTime','2026-05-27 11:20:00'));

-- 演示教师再增加五门正式课程，随后让演示学生选入，课表与教师端共用同一批数据。
INSERT INTO t_campus_record(record_type,record_data) VALUES
('teachingCourse',JSON_OBJECT('demoSeed','rich-demo-v1','teacherId',@teacher_id,'teacherName','演示教师','courseName','Web前端开发','courseCode','CS310','credit',3,'semester','2026-2027-1','dayOfWeek',2,'timeSlot','10:10-11:45','classroom','机房301','building','信息楼','weeks','1-16周','color','#7C5CD6'));
SET @course_web=LAST_INSERT_ID();
INSERT INTO t_campus_record(record_type,record_data) VALUES
('teachingCourse',JSON_OBJECT('demoSeed','rich-demo-v1','teacherId',@teacher_id,'teacherName','演示教师','courseName','数据库系统原理','courseCode','CS302','credit',4,'semester','2026-2027-1','dayOfWeek',2,'timeSlot','14:30-16:05','classroom','A301','building','教学楼A','weeks','1-16周','color','#E6A23C'));
SET @course_db=LAST_INSERT_ID();
INSERT INTO t_campus_record(record_type,record_data) VALUES
('teachingCourse',JSON_OBJECT('demoSeed','rich-demo-v1','teacherId',@teacher_id,'teacherName','演示教师','courseName','计算机网络','courseCode','CS305','credit',3.5,'semester','2026-2027-1','dayOfWeek',4,'timeSlot','08:00-09:35','classroom','B305','building','教学楼B','weeks','1-16周','color','#F56C6C'));
SET @course_net=LAST_INSERT_ID();
INSERT INTO t_campus_record(record_type,record_data) VALUES
('teachingCourse',JSON_OBJECT('demoSeed','rich-demo-v1','teacherId',@teacher_id,'teacherName','演示教师','courseName','软件工程实践','courseCode','CS401','credit',3,'semester','2026-2027-1','dayOfWeek',4,'timeSlot','16:20-17:55','classroom','研讨室2','building','图书馆','weeks','1-12周','color','#909399'));
SET @course_se=LAST_INSERT_ID();
INSERT INTO t_campus_record(record_type,record_data) VALUES
('teachingCourse',JSON_OBJECT('demoSeed','rich-demo-v1','teacherId',@teacher_id,'teacherName','演示教师','courseName','人工智能导论','courseCode','AI201','credit',3,'semester','2026-2027-1','dayOfWeek',5,'timeSlot','10:10-11:45','classroom','信息楼402','building','信息楼','weeks','1-16周','color','#67C23A'));
SET @course_ai=LAST_INSERT_ID();

INSERT INTO t_campus_record(record_type,record_data) VALUES
('courseEnrollment',JSON_OBJECT('demoSeed','rich-demo-v1','studentId',@student_id,'courseId',@course_web,'selectedAt','2026-07-10T09:05:20')),
('courseEnrollment',JSON_OBJECT('demoSeed','rich-demo-v1','studentId',@student_id,'courseId',@course_db,'selectedAt','2026-07-10T09:06:12')),
('courseEnrollment',JSON_OBJECT('demoSeed','rich-demo-v1','studentId',@student_id,'courseId',@course_net,'selectedAt','2026-07-10T09:07:43')),
('courseEnrollment',JSON_OBJECT('demoSeed','rich-demo-v1','studentId',@student_id,'courseId',@course_se,'selectedAt','2026-07-10T09:08:31')),
('courseEnrollment',JSON_OBJECT('demoSeed','rich-demo-v1','studentId',@student_id,'courseId',@course_ai,'selectedAt','2026-07-10T09:09:18')),
('score',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseId',@course_web,'teacherId',@teacher_id,'courseName','Web前端开发','courseCode','CS310','semester','2025-2026-2','credit',3,'score',94,'grade','优秀','type','课程成绩','examTime','2026-06-20')),
('score',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseId',@course_db,'teacherId',@teacher_id,'courseName','数据库系统原理','courseCode','CS302','semester','2025-2026-2','credit',4,'score',88,'grade','良好','type','课程成绩','examTime','2026-06-23')),
('score',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseId',@course_net,'teacherId',@teacher_id,'courseName','计算机网络','courseCode','CS305','semester','2025-2026-2','credit',3.5,'score',91,'grade','优秀','type','课程成绩','examTime','2026-06-26')),
('score',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseId',@course_se,'teacherId',@teacher_id,'courseName','软件工程实践','courseCode','CS401','semester','2025-2026-2','credit',3,'score',96,'grade','优秀','type','课程成绩','examTime','2026-06-28')),
('score',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseId',@course_ai,'teacherId',@teacher_id,'courseName','人工智能导论','courseCode','AI201','semester','2025-2026-2','credit',3,'score',90,'grade','优秀','type','课程成绩','examTime','2026-06-30')),
('exam',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseName','Web前端开发','courseCode','CS310','examDate','2026-07-24','examTime','09:00-11:00','building','信息楼','classroom','机房301','seatNumber','22','status','待考试')),
('exam',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseName','数据库系统原理','courseCode','CS302','examDate','2026-07-27','examTime','14:00-16:00','building','教学楼A','classroom','A301','seatNumber','17','status','待考试')),
('exam',JSON_OBJECT('demoSeed','rich-demo-v1','userId',@student_id,'courseName','人工智能导论','courseCode','AI201','examDate','2026-07-30','examTime','09:00-11:00','building','信息楼','classroom','402','seatNumber','08','status','待考试')),
('courseFeedback',JSON_OBJECT('demoSeed','rich-demo-v1','courseId',@course_web,'studentId',@student_id,'overall',5,'comment','案例丰富，课堂代码演示清晰。','createdTime','2026-07-01T16:30:00')),
('courseFeedback',JSON_OBJECT('demoSeed','rich-demo-v1','courseId',@course_db,'studentId',@student_id,'overall',4,'comment','实验安排紧凑，对理解事务和索引很有帮助。','createdTime','2026-07-02T18:20:00'));

-- 005 扩充业务演示数据：教务、校园生活、市场、活动、地图与通知
SET NAMES utf8mb4;
USE campus_os;

INSERT INTO t_campus_record(record_type, record_data) VALUES
('classroom', JSON_OBJECT('name','A201','building','教学楼A','capacity',80,'timeSlot','1-2节','available',true)),
('classroom', JSON_OBJECT('name','A305','building','教学楼A','capacity',60,'timeSlot','3-4节','available',true)),
('classroom', JSON_OBJECT('name','A402','building','教学楼A','capacity',45,'timeSlot','7-8节','available',true)),
('classroom', JSON_OBJECT('name','B103','building','教学楼B','capacity',120,'timeSlot','1-2节','available',true)),
('classroom', JSON_OBJECT('name','B205','building','教学楼B','capacity',60,'timeSlot','5-6节','available',true)),
('classroom', JSON_OBJECT('name','B307','building','教学楼B','capacity',40,'timeSlot','9-10节','available',true)),
('classroom', JSON_OBJECT('name','C102','building','外语楼','capacity',36,'timeSlot','3-4节','available',true)),
('classroom', JSON_OBJECT('name','C208','building','外语楼','capacity',48,'timeSlot','7-8节','available',true)),
('classroom', JSON_OBJECT('name','机房301','building','信息楼','capacity',72,'timeSlot','5-6节','available',true)),
('classroom', JSON_OBJECT('name','研讨室2','building','图书馆','capacity',16,'timeSlot','9-10节','available',true)),

('score', JSON_OBJECT('courseName','高等数学','courseCode','MATH101','semester','2026-2027-1','credit',5,'score',88,'grade','良好','type','考试','examTime','2026-06-18')),
('score', JSON_OBJECT('courseName','数据结构','courseCode','CS202','semester','2026-2027-1','credit',4,'score',95,'grade','优秀','type','考试','examTime','2026-06-21')),
('score', JSON_OBJECT('courseName','大学英语','courseCode','ENG102','semester','2026-2027-1','credit',3,'score',86,'grade','良好','type','考试','examTime','2026-06-23')),
('score', JSON_OBJECT('courseName','计算机网络','courseCode','CS305','semester','2026-2027-1','credit',3.5,'score',91,'grade','优秀','type','考试','examTime','2026-06-25')),
('score', JSON_OBJECT('courseName','体育（羽毛球）','courseCode','PE108','semester','2026-2027-1','credit',1,'score',93,'grade','优秀','type','平时','examTime','2026-06-12')),
('score', JSON_OBJECT('courseName','操作系统','courseCode','CS303','semester','2026-2027-1','credit',4,'score',84,'grade','良好','type','考试','examTime','2026-06-27')),
('score', JSON_OBJECT('courseName','数据库原理','courseCode','CS302','semester','2026-2027-1','credit',4,'score',89,'grade','良好','type','考试','examTime','2026-06-29')),
('score', JSON_OBJECT('courseName','形势与政策','courseCode','GE201','semester','2026-2027-1','credit',1,'score',96,'grade','优秀','type','平时','examTime','2026-06-15')),

('exam', JSON_OBJECT('courseName','高等数学','courseCode','MATH101','examDate','2026-07-22','examTime','09:00-11:00','building','教学楼B','classroom','B203','seatNumber','18','status','待考试')),
('exam', JSON_OBJECT('courseName','数据结构','courseCode','CS202','examDate','2026-07-24','examTime','14:00-16:00','building','教学楼A','classroom','A305','seatNumber','26','status','待考试')),
('exam', JSON_OBJECT('courseName','大学英语','courseCode','ENG102','examDate','2026-07-26','examTime','09:00-11:00','building','外语楼','classroom','C102','seatNumber','09','status','待考试')),
('exam', JSON_OBJECT('courseName','计算机网络','courseCode','CS305','examDate','2026-07-28','examTime','14:00-16:00','building','教学楼B','classroom','B305','seatNumber','31','status','待考试')),
('exam', JSON_OBJECT('courseName','操作系统','courseCode','CS303','examDate','2026-07-30','examTime','09:00-11:00','building','教学楼A','classroom','A402','seatNumber','15','status','待考试')),

('notice', JSON_OBJECT('title','秋季学期选课开放通知','content','第一轮选课将于7月25日9:00开放，请提前查看培养方案。','type','ACADEMIC','department','教务处','isRead',false,'createTime','2026-07-19 09:00:00')),
('notice', JSON_OBJECT('title','图书馆研讨室预约规则调整','content','研讨室最长预约时长调整为两小时。','type','SCHOOL','department','图书馆','isRead',false,'createTime','2026-07-18 14:30:00')),
('notice', JSON_OBJECT('title','暑期留校学生登记提醒','content','请留校同学于7月22日前完成线上登记。','type','DORMITORY','department','学生处','isRead',false,'createTime','2026-07-17 16:00:00')),
('notice', JSON_OBJECT('title','校园网维护公告','content','7月23日凌晨校园网将进行短时维护。','type','SCHOOL','department','信息化办公室','isRead',false,'createTime','2026-07-16 18:00:00')),

('cardTransaction', JSON_OBJECT('userId',1,'type','食堂消费','amount',-16.80,'merchant','第二食堂','createTime','2026-07-19 12:20:00')),
('cardTransaction', JSON_OBJECT('userId',1,'type','超市消费','amount',-8.50,'merchant','校园超市','createTime','2026-07-19 10:05:00')),
('cardTransaction', JSON_OBJECT('userId',1,'type','余额充值','amount',100.00,'merchant','线上充值','createTime','2026-07-18 20:10:00')),
('cardTransaction', JSON_OBJECT('userId',1,'type','食堂消费','amount',-11.00,'merchant','第一食堂','createTime','2026-07-18 07:48:00')),
('cardTransaction', JSON_OBJECT('userId',1,'type','图书馆打印','amount',-2.40,'merchant','图书馆打印中心','createTime','2026-07-17 16:35:00')),
('cardTransaction', JSON_OBJECT('userId',1,'type','食堂消费','amount',-13.60,'merchant','清真食堂','createTime','2026-07-17 12:12:00')),

('product', JSON_OBJECT('sellerId',1,'title','高等数学教材与习题册','price',28.00,'description','教材有少量笔记，习题册基本全新','coverImage','','category','书籍资料','categoryId',2,'status','在售','viewCount',86,'contactPhone','13800138000','createTime','2026-07-19 09:30:00')),
('product', JSON_OBJECT('sellerId',2,'title','机械键盘 87键','price',120.00,'description','青轴，功能正常，支持当面验货','coverImage','','category','电子产品','categoryId',1,'status','在售','viewCount',143,'contactPhone','13900139000','createTime','2026-07-18 20:00:00')),
('product', JSON_OBJECT('sellerId',3,'title','宿舍折叠桌','price',35.00,'description','九成新，尺寸60x40cm','coverImage','','category','生活用品','categoryId',3,'status','在售','viewCount',42,'contactPhone','13700137000','createTime','2026-07-18 15:20:00')),
('product', JSON_OBJECT('sellerId',4,'title','英语四六级真题套装','price',18.00,'description','包含近五年真题和听力资料','coverImage','','category','书籍资料','categoryId',2,'status','在售','viewCount',67,'contactPhone','13600136000','createTime','2026-07-17 18:45:00')),
('product', JSON_OBJECT('sellerId',5,'title','山地自行车','price',460.00,'description','校内通勤使用，车况良好','coverImage','','category','运动出行','categoryId',4,'status','在售','viewCount',215,'contactPhone','13500135000','createTime','2026-07-17 11:00:00')),
('product', JSON_OBJECT('sellerId',6,'title','羽毛球拍一对','price',75.00,'description','含拍套和三只羽毛球','coverImage','','category','运动出行','categoryId',4,'status','在售','viewCount',58,'contactPhone','13400134000','createTime','2026-07-16 19:10:00')),

('activity', JSON_OBJECT('title','迎新志愿者招募','category','志愿服务','categoryCode','VOLUNTEER','coverImage','','startTime','2026-08-28 08:00:00','endTime','2026-08-30 18:00:00','location','学校南门','maxParticipants',200,'currentParticipants',126,'status','报名中')),
('activity', JSON_OBJECT('title','校园歌手大赛','category','文艺','categoryCode','ART','coverImage','','startTime','2026-09-12 19:00:00','endTime','2026-09-12 21:30:00','location','大学生活动中心','maxParticipants',500,'currentParticipants',238,'status','报名中')),
('activity', JSON_OBJECT('title','人工智能创新工作坊','category','学术','categoryCode','ACADEMIC','coverImage','','startTime','2026-08-05 14:00:00','endTime','2026-08-05 17:30:00','location','信息楼报告厅','maxParticipants',80,'currentParticipants',63,'status','报名中')),
('activity', JSON_OBJECT('title','夜跑打卡计划','category','体育','categoryCode','SPORTS','coverImage','','startTime','2026-07-25 19:30:00','endTime','2026-08-25 21:00:00','location','东区操场','maxParticipants',300,'currentParticipants',194,'status','报名中')),
('activity', JSON_OBJECT('title','旧书交换市集','category','社团','categoryCode','CLUB','coverImage','','startTime','2026-08-10 10:00:00','endTime','2026-08-10 17:00:00','location','图书馆广场','maxParticipants',150,'currentParticipants',72,'status','报名中')),

('location', JSON_OBJECT('name','教学楼B','category','教学楼','categoryCode','BUILDING','longitude',113.3253,'latitude',23.1007,'address','大学城校区B区','building','B栋','image','')),
('location', JSON_OBJECT('name','图书馆','category','学习空间','categoryCode','LIBRARY','longitude',113.3238,'latitude',23.1014,'address','校园中轴线北侧','building','图书馆','image','')),
('location', JSON_OBJECT('name','第一食堂','category','餐饮','categoryCode','CANTEEN','longitude',113.3229,'latitude',23.0996,'address','生活区1号楼旁','building','第一食堂','image','')),
('location', JSON_OBJECT('name','校医院','category','医疗','categoryCode','HOSPITAL','longitude',113.3261,'latitude',23.0992,'address','校园东门内侧','building','校医院','image','')),
('location', JSON_OBJECT('name','大学生活动中心','category','活动场地','categoryCode','VENUE','longitude',113.3241,'latitude',23.0988,'address','南区广场','building','活动中心','image','')),
('location', JSON_OBJECT('name','东区操场','category','体育场地','categoryCode','SPORT','longitude',113.3270,'latitude',23.1018,'address','校园东区','building','东区操场','image','')),

('hotQuestion', JSON_OBJECT('question','在哪里查看本周课表？','category','教务学习')),
('hotQuestion', JSON_OBJECT('question','如何查询空闲教室？','category','教务学习')),
('hotQuestion', JSON_OBJECT('question','校园卡丢失后怎么挂失？','category','校园生活')),
('hotQuestion', JSON_OBJECT('question','宿舍报修需要准备什么？','category','校园生活')),
('hotQuestion', JSON_OBJECT('question','活动报名后如何签到？','category','校园活动')),
('hotQuestion', JSON_OBJECT('question','如何申请奖学金？','category','学生事务'));

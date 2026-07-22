SET NAMES utf8mb4;
USE campus_os;

CREATE TABLE IF NOT EXISTS t_campus_record (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  record_type VARCHAR(50) NOT NULL,
  record_data JSON        NOT NULL,
  PRIMARY KEY (id),
  KEY idx_record_type (record_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园业务通用资源表';

INSERT INTO t_campus_record(record_type, record_data) VALUES
('notice', JSON_OBJECT('title','暑期校园安全提示','content','离校前请关闭宿舍水电，妥善保管个人物品。','type','SCHOOL','department','学校办公室','isRead',false,'createTime','2026-07-16 09:00:00')),
('course', JSON_OBJECT('dayOfWeek',1,'timeSlot','08:00-09:35','teacherId',2,'courses',JSON_ARRAY(JSON_OBJECT('id',1,'name','Java程序设计','teacher','王教授','classroom','A101','building','教学楼A','weeks','1-16周','color','#2F7D66')))),
('classroom', JSON_OBJECT('name','A102','building','教学楼A','capacity',60,'timeSlot','1-2节','available',true)),
('score', JSON_OBJECT('courseName','Java程序设计','courseCode','CS301','credit',4,'score',92,'grade','优秀','type','考试','examTime','2026-06-20')),
('exam', JSON_OBJECT('courseName','数据库原理','courseCode','CS302','examDate','2026-07-20','examTime','14:00-16:00','building','教学楼A','classroom','A301','seatNumber','12','status','待考试')),
('payment', JSON_OBJECT('type','学费','amount',5200.00,'deadline','2026-09-30','status','待缴费','description','2026-2027学年学费')),
('card', JSON_OBJECT('cardId','C20260001','userId',1,'realName','管理员','balance',56.50,'status','正常','expireTime','2029-09-01')),
('cardTransaction', JSON_OBJECT('userId',1,'type','食堂消费','amount',-12.50,'merchant','第一食堂','createTime','2026-07-16 12:10:00')),
('dormitory', JSON_OBJECT('building','6栋','room','302室','type','四人间','members',JSON_ARRAY(JSON_OBJECT('userId',1,'realName','张三','phone','13800138000','isRoomLeader',true)),'facilities',JSON_ARRAY('空调','热水器','独立卫生间'),'electricityBalance',23.5,'waterBalance',12.0)),
('dormitoryNotice', JSON_OBJECT('title','暑期宿舍开放安排','content','暑期留校学生请在宿管处登记。','createTime','2026-07-10 09:00:00')),
('repair', JSON_OBJECT('userId',1,'title','宿舍灯具报修','type','水电','description','顶灯不亮','building','6栋','room','302室','contactPhone','13800138000','status','已完成','statusDesc','维修已完成，可评价','createTime','2026-07-16 10:00:00')),
('product', JSON_OBJECT('sellerId',1,'title','二手显示器','price',300.00,'description','九成新，使用半年','coverImage','','category','电子产品','categoryId',1,'status','在售','viewCount',56,'contactPhone','13800138000','createTime','2026-07-15 10:00:00')),
('activity', JSON_OBJECT('title','校园篮球赛','category','体育','categoryCode','SPORTS','coverImage','','startTime','2026-07-20 14:00:00','endTime','2026-07-20 17:00:00','location','体育馆','maxParticipants',120,'currentParticipants',80,'status','报名中')),
('location', JSON_OBJECT('name','教学楼A','category','教学楼','categoryCode','BUILDING','longitude',113.3245,'latitude',23.1000,'address','大学城校区A栋','building','A栋','image','')),
('hotQuestion', JSON_OBJECT('question','如何申请奖学金？','category','学生事务')),
('recommendation', JSON_OBJECT('type','ACTIVITY','title','校园篮球赛','reason','近期热门活动'));

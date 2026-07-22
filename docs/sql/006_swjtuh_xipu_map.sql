-- Replace the old Guangzhou demo map data with Southwest Jiaotong University,
-- Xipu Campus. This migration is safe to run more than once.
SET NAMES utf8mb4;
USE campus_os;

DELETE FROM t_campus_record WHERE record_type = 'location';

INSERT INTO t_campus_record (record_type, record_data) VALUES
('location', JSON_OBJECT('name','南门','category','校门','categoryCode','GATE','longitude',103.9897905,'latitude',30.7629679,'address','西南交通大学犀浦校区南门','building','南门','image','')),
('location', JSON_OBJECT('name','图书馆','category','图书馆','categoryCode','LIBRARY','longitude',103.9830288,'latitude',30.7688033,'address','精勤路','building','西南交通大学犀浦校区图书馆','image','')),
('location', JSON_OBJECT('name','一食堂（黍园）','category','食堂','categoryCode','CANTEEN','longitude',103.9822864,'latitude',30.7617028,'address','西南交通大学犀浦校区南区','building','一食堂（黍园）','image','')),
('location', JSON_OBJECT('name','二食堂（梁园）','category','食堂','categoryCode','CANTEEN','longitude',103.9815023,'latitude',30.7627961,'address','西南交通大学犀浦校区南区','building','二食堂（梁园）','image','')),
('location', JSON_OBJECT('name','三食堂（粟园）','category','食堂','categoryCode','CANTEEN','longitude',103.9807092,'latitude',30.7637305,'address','西南交通大学犀浦校区南区','building','三食堂（粟园）','image','')),
('location', JSON_OBJECT('name','四食堂（馨园）','category','食堂','categoryCode','CANTEEN','longitude',103.9766213,'latitude',30.7687810,'address','西南交通大学犀浦校区北区','building','四食堂（馨园）','image','')),
('location', JSON_OBJECT('name','五食堂','category','食堂','categoryCode','CANTEEN','longitude',103.9755337,'latitude',30.7703602,'address','西南交通大学犀浦校区北区','building','五食堂','image','')),
('location', JSON_OBJECT('name','西南交大犀浦8号教学楼','category','教学楼','categoryCode','BUILDING','longitude',103.9812081,'latitude',30.7705730,'address','西南交通大学犀浦校区北区','building','8号教学楼','image','')),
('location', JSON_OBJECT('name','东门','category','校门','categoryCode','GATE','longitude',103.9912939,'latitude',30.7716985,'address','西南交通大学犀浦校区东门','building','东门','image','')),
('location', JSON_OBJECT('name','北区体育场','category','体育馆','categoryCode','GYM','longitude',103.9791775,'latitude',30.7670576,'address','西南交通大学犀浦校区北区','building','北区体育场','image','')),
('location', JSON_OBJECT('name','南区体育场','category','体育馆','categoryCode','GYM','longitude',103.9843163,'latitude',30.7599748,'address','西南交通大学犀浦校区南区','building','南区体育场','image','')),
('location', JSON_OBJECT('name','体育馆','category','体育馆','categoryCode','GYM','longitude',103.9811701,'latitude',30.7671058,'address','西南交通大学犀浦校区北区','building','体育馆','image','')),
('location', JSON_OBJECT('name','天佑斋3号楼','category','宿舍','categoryCode','DORMITORY','longitude',103.9830761,'latitude',30.7628866,'address','西南交通大学犀浦校区南区','building','天佑斋3号楼','image','')),
('location', JSON_OBJECT('name','天佑斋19号楼','category','宿舍','categoryCode','DORMITORY','longitude',103.9771088,'latitude',30.7708948,'address','西南交通大学犀浦校区北区','building','天佑斋19号楼','image',''));

-- ==================== 1. 校园地点表 ====================
CREATE TABLE t_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地点ID',
    name VARCHAR(100) NOT NULL COMMENT '地点名称',
    category VARCHAR(30) NOT NULL COMMENT '分类：BUILDING/LIBRARY/CANTEEN/DORMITORY/GYM',
    longitude DOUBLE NOT NULL COMMENT '经度',
    latitude DOUBLE NOT NULL COMMENT '纬度',
    address VARCHAR(300) COMMENT '地址',
    building VARCHAR(100) COMMENT '楼栋名称',
    image VARCHAR(500) COMMENT '图片URL',
    description VARCHAR(1000) COMMENT '描述',
    facilities JSON COMMENT '设施列表',
    open_time VARCHAR(50) COMMENT '开放时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园地点表';

-- ==================== 2. 楼层/房间表 ====================
CREATE TABLE t_location_floor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    location_id BIGINT NOT NULL COMMENT '地点ID',
    floor INT NOT NULL COMMENT '楼层',
    rooms JSON COMMENT '房间号列表',
    INDEX idx_location (location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点楼层表';

-- ==================== 预置数据 ====================
INSERT INTO t_location (name, category, longitude, latitude, address, building, image, description, facilities, open_time) VALUES
('教学楼A', 'BUILDING', 113.3245, 23.1000, '广州市大学城校区A栋', 'A栋', 'https://cdn.example.com/location/building_a.jpg', '主要教学楼，共6层，设有电梯', '["电梯","空调","饮水机","无障碍通道"]', '07:00-22:00'),
('图书馆', 'LIBRARY', 113.3250, 23.1010, '广州市大学城校区图书馆', '图书馆', 'https://cdn.example.com/location/library.jpg', '藏书200万册，24小时自习室', '["WiFi","空调","自习室","电子阅览室"]', '06:00-23:00'),
('一食堂', 'CANTEEN', 113.3240, 23.0995, '广州市大学城校区一食堂', '一食堂', 'https://cdn.example.com/location/canteen_1.jpg', '三层，提供各类餐饮', '["空调","校园卡支付"]', '06:30-21:00'),
('二食堂', 'CANTEEN', 113.3260, 23.0985, '广州市大学城校区二食堂', '二食堂', 'https://cdn.example.com/location/canteen_2.jpg', '两层，主营面食和快餐', '["空调","校园卡支付","微信支付"]', '06:30-20:30'),
('1号宿舍楼', 'DORMITORY', 113.3220, 23.1020, '广州市大学城校区1号宿舍楼', '1号宿舍', 'https://cdn.example.com/location/dorm_1.jpg', '男生宿舍，6人间', '["空调","热水器","洗衣机","饮水机"]', '06:00-23:00'),
('2号宿舍楼', 'DORMITORY', 113.3225, 23.1025, '广州市大学城校区2号宿舍楼', '2号宿舍', 'https://cdn.example.com/location/dorm_2.jpg', '女生宿舍，4人间', '["空调","热水器","洗衣机","饮水机"]', '06:00-23:00'),
('体育馆', 'GYM', 113.3230, 23.0990, '广州市大学城校区体育馆', '体育馆', 'https://cdn.example.com/location/gym.jpg', '室内体育馆，含篮球场、羽毛球场、游泳池', '["空调","更衣室","淋浴间"]', '08:00-22:00'),
('教学楼B', 'BUILDING', 113.3255, 23.1015, '广州市大学城校区B栋', 'B栋', 'https://cdn.example.com/location/building_b.jpg', '实验教学楼，共5层', '["电梯","空调","实验室"]', '07:00-22:00'),
('教学楼C', 'BUILDING', 113.3265, 23.1005, '广州市大学城校区C栋', 'C栋', 'https://cdn.example.com/location/building_c.jpg', '综合教学楼，共8层', '["电梯","空调","饮水机","无障碍通道","多媒体教室"]', '07:00-22:00'),
('行政楼', 'BUILDING', 113.3235, 23.1030, '广州市大学城校区行政楼', '行政楼', 'https://cdn.example.com/location/admin.jpg', '学校行政办公大楼，共4层', '["电梯","空调"]', '08:00-17:30');

-- 插入楼层数据（教学楼A）
INSERT INTO t_location_floor (location_id, floor, rooms) VALUES
(1, 1, '["A101","A102","A103","A104","A105","A106"]'),
(1, 2, '["A201","A202","A203","A204","A205","A206"]'),
(1, 3, '["A301","A302","A303","A304","A305","A306"]'),
(1, 4, '["A401","A402","A403","A404","A405","A406"]'),
(1, 5, '["A501","A502","A503","A504"]'),
(1, 6, '["A601","A602","A603"]');

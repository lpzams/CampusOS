-- ==================== 个人信息中心：扩展 t_user_profile 表 ====================

ALTER TABLE t_user_profile
    ADD COLUMN dormitory VARCHAR(50) COMMENT '宿舍号（学生）' AFTER research_direction,
    ADD COLUMN advisor VARCHAR(50) COMMENT '辅导员/导师（学生）' AFTER dormitory,
    ADD COLUMN office VARCHAR(100) COMMENT '办公室（教师）' AFTER advisor,
    ADD COLUMN introduction VARCHAR(500) COMMENT '个人简介（教师）' AFTER office;

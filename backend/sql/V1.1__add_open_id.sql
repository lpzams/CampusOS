-- ==================== 微信登录支持：新增 open_id 字段 ====================
-- 如果表已存在，执行此迁移添加微信 openId 列

ALTER TABLE t_user
    ADD COLUMN open_id VARCHAR(64) COMMENT '微信openId' AFTER lock_time;

-- 为 openId 查询加索引
CREATE INDEX idx_open_id ON t_user(open_id);

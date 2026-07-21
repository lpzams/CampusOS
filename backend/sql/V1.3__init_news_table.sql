-- ==================== 1. 新闻分类表 ====================
CREATE TABLE t_news_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻分类表';

-- 预置默认分类
INSERT INTO t_news_category (name, description, sort_order) VALUES
    ('校园新闻', '学校重大事件、活动报道', 1),
    ('学术动态', '学术讲座、科研成果', 2),
    ('通知公告', '学校各类通知', 3),
    ('校园生活', '校园文化活动、社团动态', 4);

-- ==================== 2. 新闻表 ====================
CREATE TABLE t_news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '新闻ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary VARCHAR(500) COMMENT '摘要',
    content MEDIUMTEXT COMMENT '内容（富文本HTML）',
    cover_image VARCHAR(500) COMMENT '封面图URL',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    author VARCHAR(50) COMMENT '作者/来源',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    favorite_count INT DEFAULT 0 COMMENT '收藏次数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    is_published TINYINT DEFAULT 0 COMMENT '是否发布：0-草稿 1-已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_category (category_id),
    INDEX idx_published_time (is_published, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻表';

-- ==================== 3. 新闻收藏表 ====================
CREATE TABLE t_news_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    news_id BIGINT NOT NULL COMMENT '新闻ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_news (user_id, news_id),
    INDEX idx_user (user_id),
    INDEX idx_news (news_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻收藏表';

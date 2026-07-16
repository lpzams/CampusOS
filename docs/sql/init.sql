-- ============================================================
-- CampusOS 数据库初始化脚本
--
-- 用法（任选其一）：
--   命令行： mysql -uroot -p < docs/sql/init.sql
--   图形化： Navicat / DataGrip 里直接整个文件运行
--
-- 账号密码与后端 backend/campus-api/src/main/resources/application.yml
-- 里的 spring.datasource 保持一致（默认 root/root，按需修改）。
--
-- 【新增功能时】不要改别人的表；在本目录新建 002_你的功能.sql，
-- 写你自己的建表语句，并在 docs/新增功能指南.md 的约定下走评审。
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_os
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE campus_os;

-- ------------------------------------------------------------
-- 新闻表 t_news（news 示例模块）
-- 列名与 backend NewsPO.java 的字段一一对应（驼峰 <-> 下划线自动映射）。
-- 注意：create_time / update_time 由数据库默认值维护，Java 代码不填。
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_news;
CREATE TABLE t_news (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  title        VARCHAR(200) NOT NULL                COMMENT '标题',
  content      TEXT         NOT NULL                COMMENT '正文内容',
  category     VARCHAR(50)  NOT NULL                COMMENT '栏目：校园新闻/学院动态/通知公告/政策文件',
  cover_image  VARCHAR(500) NULL                    COMMENT '封面图 URL，可空（预留）',
  author       VARCHAR(50)  NOT NULL                COMMENT '作者/发布人',
  status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿/PUBLISHED已发布/OFFLINE已下线',
  view_count   INT          NOT NULL DEFAULT 0      COMMENT '浏览量',
  publish_time DATETIME     NULL                    COMMENT '发布时间',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删 1已删（对应 @TableLogic）',
  PRIMARY KEY (id),
  KEY idx_status_publish_time (status, publish_time),
  KEY idx_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '校园新闻表';

-- ------------------------------------------------------------
-- 示例数据：10 条已发布（覆盖 4 个栏目）+ 1 条草稿 + 1 条已下线。
-- 前台列表只会查出 PUBLISHED 的 10 条，草稿/下线两条用来验证状态过滤。
-- ------------------------------------------------------------
INSERT INTO t_news (title, content, category, author, status, view_count, publish_time) VALUES
('我校在全国大学生程序设计竞赛中获金奖',
 '近日，第 49 届全国大学生程序设计竞赛落下帷幕。\n我校计算机学院代表队经过五小时鏖战，以解出 9 题的成绩夺得金奖，创历史最好成绩。\n\n指导教师表示，团队从春季学期开始每周组织三次集训，付出终有回报。',
 '校园新闻', '校新闻中心', 'PUBLISHED', 358, '2026-07-15 09:30:00'),

('图书馆暑期开放时间调整公告',
 '各位读者：\n暑假期间（7 月 20 日至 8 月 31 日），图书馆开放时间调整为每日 8:30-17:30，周一闭馆整理。\n借阅到期日顺延至开学后一周，请合理安排借还时间。',
 '通知公告', '图书馆', 'PUBLISHED', 512, '2026-07-14 16:00:00'),

('计算机学院举办人工智能前沿讲座',
 '7 月 12 日下午，计算机学院邀请业界专家开展"大模型时代的软件工程"主题讲座。\n讲座围绕代码生成、智能体协作等热点展开，300 余名师生到场聆听。',
 '学院动态', '计算机学院', 'PUBLISHED', 264, '2026-07-13 10:00:00'),

('关于评选 2026 年度国家奖学金的通知',
 '各学院：\n现启动 2026 年度国家奖学金评选工作，请符合条件的同学于 9 月 10 日前向所在学院提交申请材料。\n评选细则详见附件（学生处网站可下载）。',
 '通知公告', '学生处', 'PUBLISHED', 941, '2026-07-12 09:00:00'),

('《本科生学分制管理办法（2026 修订版）》正式发布',
 '经校长办公会审议通过，《本科生学分制管理办法（2026 修订版）》自 2026-2027 学年起施行。\n新版办法优化了跨专业选课与学分互认流程，请各位同学认真阅读。',
 '政策文件', '教务处', 'PUBLISHED', 205, '2026-07-11 14:30:00'),

('机械学院学子在省机器人大赛中包揽前三',
 '在刚刚结束的省大学生机器人大赛中，我校机械学院三支队伍表现出色，包揽冠亚季军。',
 '学院动态', '机械学院', 'PUBLISHED', 178, '2026-07-10 11:00:00'),

('校园一卡通系统升级维护通知',
 '为提升服务质量，一卡通系统将于 7 月 18 日 0:00-6:00 停机升级。\n升级期间刷卡消费、充值暂停，请提前做好准备。',
 '通知公告', '信息化办公室', 'PUBLISHED', 866, '2026-07-09 17:20:00'),

('我校与本市三甲医院共建临床教学基地',
 '7 月 8 日上午，我校与市第一人民医院举行临床教学基地签约揭牌仪式，双方将在人才培养、科研合作等方面深入协作。',
 '校园新闻', '校新闻中心', 'PUBLISHED', 132, '2026-07-08 15:00:00'),

('外国语学院暑期国际交流项目启动',
 '外国语学院 2026 年暑期国际交流项目正式启动，首批 45 名学生将赴海外合作院校开展为期四周的语言研修。',
 '学院动态', '外国语学院', 'PUBLISHED', 97, '2026-07-07 10:30:00'),

('关于加强实验室安全管理的规定',
 '为进一步加强实验室安全管理，保障师生人身与财产安全，现发布《实验室安全管理规定（试行）》，请各单位组织学习并严格执行。',
 '政策文件', '实验室与设备管理处', 'PUBLISHED', 301, '2026-07-06 09:00:00'),

('（草稿）秋季运动会筹备方案',
 '本条为草稿状态示例数据：前台列表接口只查已发布，草稿不会出现在网站/小程序上。',
 '校园新闻', '体育部', 'DRAFT', 0, NULL),

('（已下线）旧版选课系统使用指南',
 '本条为已下线状态示例数据：曾经发布过，后被管理员下线，前台不再展示。',
 '通知公告', '教务处', 'OFFLINE', 66, '2026-06-01 08:00:00');

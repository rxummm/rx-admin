-- =============================================
-- 技术博客 - 文章表 & 菜单
-- 数据来源: https://www.nicklitten.com/blog/
-- =============================================

-- 文章表
CREATE TABLE IF NOT EXISTS tech_blog_article (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID',
    title           VARCHAR(500) NOT NULL COMMENT '文章标题',
    slug            VARCHAR(500) COMMENT 'URL slug',
    source_url      VARCHAR(1000) COMMENT '原始链接',
    author          VARCHAR(100) DEFAULT 'Nick Litten' COMMENT '作者',
    publish_date    VARCHAR(20) COMMENT '发布日期(yyyy-MM-dd)',
    categories      VARCHAR(500) COMMENT '分类标签(英文逗号分隔)',
    excerpt_text    TEXT COMMENT '摘要',
    content_html    MEDIUMTEXT COMMENT 'HTML正文内容',
    content_text    TEXT COMMENT '纯文本正文(用于搜索)',
    cover_image     VARCHAR(1000) COMMENT '封面图URL',
    sort            INT DEFAULT 0 COMMENT '排序',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_publish_date (publish_date),
    INDEX idx_source_url (source_url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术博客文章表(NickLitten)';

-- 菜单: AS400管理 > 技术博客 (使用较大ID避免冲突)
-- 注意: 文章详情通过卡片点击进入，不在菜单中显示
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
(380, 107, '技术博客', 1, '/as400/techblog', '', '', 'Notebook', 3),
(381, 380, '文章列表', 2, '/as400/techblog/list', 'as400/techblog/index', 'techblog:list', 'Reading', 1);

-- 给超级管理员(role_id=1)分配菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 380), (1, 381);

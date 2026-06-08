-- ====================================================
-- sys_notice 表扩展：支持待办事项分类
-- ====================================================

-- 1. 新增 category 字段（notice/announcement/todo）
ALTER TABLE sys_notice ADD COLUMN category VARCHAR(20) DEFAULT 'notice' COMMENT '分类：notice=通知, announcement=公告, todo=待办事项';

-- 2. 新增 link_path 字段（待办项点击后跳转路径，非待办可为空）
ALTER TABLE sys_notice ADD COLUMN link_path VARCHAR(200) DEFAULT NULL COMMENT '跳转路径（待办项专用，如 /system/user）';

-- 3. 更新现有数据：已有 noticeType='1' → category='notice', noticeType='2' → category='announcement'
UPDATE sys_notice SET category = 'notice' WHERE notice_type = '1';
UPDATE sys_notice SET category = 'announcement' WHERE notice_type = '2';

-- 4. 插入待办数据示例：admin 登录后可看到未处理的权限审批
INSERT INTO sys_notice (title, content, notice_type, category, link_path, status, deleted, create_time, update_time)
SELECT CONCAT('权限审批待办：用户 ', username, ' 申请了 ', menu_names, ' 权限'),
       CONCAT('用户 ', username, ' 申请了以下菜单权限：', menu_names, '，请及时处理。'),
       '1', 'todo', '/system/user', 1, 0, create_time, update_time
FROM sys_permission_request
WHERE status = 0;

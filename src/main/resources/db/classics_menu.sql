-- ============================================
-- 四大名著 - 菜单注册 SQL
-- 在 sys_menu 表中新增菜单项（rx_admin 库）
-- 结构：四大名著（一级目录）> 红楼/西游/三国/水浒（二级目录）> 具体页面（三级菜单）> 按钮权限
-- ============================================

-- ============ 0. 先删除旧数据（如果存在） ============
-- 删除范围覆盖所有四大名著相关菜单 ID 230-330
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 230 AND 330;
DELETE FROM sys_menu WHERE id BETWEEN 230 AND 330;

-- ============ 一级菜单：四大名著（parent_id=0，menu_type=1 目录） ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (229, 0, '四大名著', 1, '/classics', '', '', 'Reading', 355, 1, 1, 0, NOW(), NOW());

-- ========== 红楼梦（parent_id=229 四大名著） ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (230, 229, '红楼梦', 1, '/classics/honglou', '', '', 'Collection', 10, 1, 1, 0, NOW(), NOW());

-- 三级菜单：红楼诗词（parent_id=230 红楼梦）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (231, 230, '红楼诗词', 2, '/classics/honglou/poems', 'classics/honglou/poems/index', 'classics:honglou:poem:list', 'EditPen', 1, 1, 1, 0, NOW(), NOW());

-- 三级菜单：红楼人物
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (232, 230, '红楼人物', 2, '/classics/honglou/characters', 'classics/honglou/characters/index', 'classics:honglou:character:list', 'User', 2, 1, 1, 0, NOW(), NOW());

-- 三级菜单：人物关系
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (233, 230, '人物关系', 2, '/classics/honglou/relations', 'classics/honglou/relations/index', 'classics:honglou:relation:list', 'Connection', 3, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：红楼诗词（parent_id=231）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (234, 231, '诗词查询', 3, '', '', 'classics:honglou:poem:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (301, 231, '诗词新增', 3, '', '', 'classics:honglou:poem:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (302, 231, '诗词编辑', 3, '', '', 'classics:honglou:poem:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (303, 231, '诗词删除', 3, '', '', 'classics:honglou:poem:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：红楼人物（parent_id=232）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (235, 232, '人物查询', 3, '', '', 'classics:honglou:character:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (304, 232, '人物新增', 3, '', '', 'classics:honglou:character:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (305, 232, '人物编辑', 3, '', '', 'classics:honglou:character:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (306, 232, '人物删除', 3, '', '', 'classics:honglou:character:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：人物关系（parent_id=233）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (307, 233, '关系新增', 3, '', '', 'classics:honglou:relation:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (308, 233, '关系编辑', 3, '', '', 'classics:honglou:relation:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (309, 233, '关系删除', 3, '', '', 'classics:honglou:relation:delete', '', 4, 1, 1, 0, NOW(), NOW());


-- ========== 西游记（parent_id=229 四大名著） ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (236, 229, '西游记', 1, '/classics/xiyou', '', '', 'Collection', 20, 1, 1, 0, NOW(), NOW());

-- 三级菜单：西游诗词（parent_id=236 西游记）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (237, 236, '西游诗词', 2, '/classics/xiyou/poems', 'classics/xiyou/poems/index', 'classics:xiyou:poem:list', 'EditPen', 1, 1, 1, 0, NOW(), NOW());

-- 三级菜单：西游人物
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (238, 236, '西游人物', 2, '/classics/xiyou/characters', 'classics/xiyou/characters/index', 'classics:xiyou:character:list', 'User', 2, 1, 1, 0, NOW(), NOW());

-- 三级菜单：八十一难
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (239, 236, '八十一难', 2, '/classics/xiyou/events', 'classics/xiyou/events/index', 'classics:xiyou:event:list', 'Timer', 3, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：西游诗词（parent_id=237）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (240, 237, '诗词查询', 3, '', '', 'classics:xiyou:poem:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (310, 237, '诗词新增', 3, '', '', 'classics:xiyou:poem:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (311, 237, '诗词编辑', 3, '', '', 'classics:xiyou:poem:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (312, 237, '诗词删除', 3, '', '', 'classics:xiyou:poem:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：西游人物（parent_id=238）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (241, 238, '人物查询', 3, '', '', 'classics:xiyou:character:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (313, 238, '人物新增', 3, '', '', 'classics:xiyou:character:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (314, 238, '人物编辑', 3, '', '', 'classics:xiyou:character:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (315, 238, '人物删除', 3, '', '', 'classics:xiyou:character:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：八十一难（parent_id=239）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (316, 239, '事件新增', 3, '', '', 'classics:xiyou:event:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (317, 239, '事件编辑', 3, '', '', 'classics:xiyou:event:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (318, 239, '事件删除', 3, '', '', 'classics:xiyou:event:delete', '', 4, 1, 1, 0, NOW(), NOW());


-- ========== 三国演义（parent_id=229 四大名著） ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (242, 229, '三国演义', 1, '/classics/sanguo', '', '', 'Collection', 30, 1, 1, 0, NOW(), NOW());

-- 三级菜单：三国诗词（parent_id=242 三国演义）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (243, 242, '三国诗词', 2, '/classics/sanguo/poems', 'classics/sanguo/poems/index', 'classics:sanguo:poem:list', 'EditPen', 1, 1, 1, 0, NOW(), NOW());

-- 三级菜单：三国人物
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (244, 242, '三国人物', 2, '/classics/sanguo/characters', 'classics/sanguo/characters/index', 'classics:sanguo:character:list', 'User', 2, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：三国诗词（parent_id=243）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (245, 243, '诗词查询', 3, '', '', 'classics:sanguo:poem:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (319, 243, '诗词新增', 3, '', '', 'classics:sanguo:poem:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (320, 243, '诗词编辑', 3, '', '', 'classics:sanguo:poem:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (321, 243, '诗词删除', 3, '', '', 'classics:sanguo:poem:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：三国人物（parent_id=244）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (246, 244, '人物查询', 3, '', '', 'classics:sanguo:character:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (322, 244, '人物新增', 3, '', '', 'classics:sanguo:character:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (323, 244, '人物编辑', 3, '', '', 'classics:sanguo:character:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (324, 244, '人物删除', 3, '', '', 'classics:sanguo:character:delete', '', 4, 1, 1, 0, NOW(), NOW());


-- ========== 水浒传（parent_id=229 四大名著） ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (247, 229, '水浒传', 1, '/classics/shuihu', '', '', 'Collection', 40, 1, 1, 0, NOW(), NOW());

-- 三级菜单：水浒诗词（parent_id=247 水浒传）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (248, 247, '水浒诗词', 2, '/classics/shuihu/poems', 'classics/shuihu/poems/index', 'classics:shuihu:poem:list', 'EditPen', 1, 1, 1, 0, NOW(), NOW());

-- 三级菜单：水浒章节
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (249, 247, '水浒章节', 2, '/classics/shuihu/chapters', 'classics/shuihu/chapters/index', 'classics:shuihu:chapter:list', 'Document', 2, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：水浒诗词（parent_id=248）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (250, 248, '诗词查询', 3, '', '', 'classics:shuihu:poem:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (325, 248, '诗词新增', 3, '', '', 'classics:shuihu:poem:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (326, 248, '诗词编辑', 3, '', '', 'classics:shuihu:poem:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (327, 248, '诗词删除', 3, '', '', 'classics:shuihu:poem:delete', '', 4, 1, 1, 0, NOW(), NOW());

-- 按钮级权限：水浒章节（parent_id=249）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (251, 249, '章节查询', 3, '', '', 'classics:shuihu:chapter:query', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (328, 249, '章节新增', 3, '', '', 'classics:shuihu:chapter:add', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (329, 249, '章节编辑', 3, '', '', 'classics:shuihu:chapter:edit', '', 3, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (330, 249, '章节删除', 3, '', '', 'classics:shuihu:chapter:delete', '', 4, 1, 1, 0, NOW(), NOW());


-- ============================================
-- 将四大名著所有菜单分配给超级管理员角色（role_id=1）
-- ============================================
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 229 AND 330;

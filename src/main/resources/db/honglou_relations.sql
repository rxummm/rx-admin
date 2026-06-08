-- =============================================
-- 红楼梦人物关系数据
-- from_character_id 和 to_character_id 对应 honglou_characters 表中的id
-- =============================================

-- ========== 贾府核心家庭关系 ==========

-- 贾母与子孙
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(5, 6, '母子', '贾母为贾政之母', NOW(), NOW()),
(5, 17, '母子', '贾母为贾赦之母', NOW(), NOW()),
(5, 38, '母女', '贾母为贾敏之母', NOW(), NOW()),
(5, 1, '祖孙', '贾母为贾宝玉之祖母', NOW(), NOW()),
(5, 8, '祖孙', '贾母为贾元春之祖母', NOW(), NOW()),
(5, 9, '祖孙', '贾母为贾迎春之祖母', NOW(), NOW()),
(5, 10, '祖孙', '贾母为贾探春之祖母', NOW(), NOW()),
(5, 11, '祖孙', '贾母为贾惜春之祖母', NOW(), NOW()),
(5, 2, '外祖孙', '贾母为林黛玉之外祖母', NOW(), NOW()),
(5, 12, '侄祖孙', '贾母为史湘云之姑祖母', NOW(), NOW());

-- 贾政一支
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(6, 1, '父子', '贾政为贾宝玉之父', NOW(), NOW()),
(6, 8, '父女', '贾政为贾元春之父', NOW(), NOW()),
(6, 10, '父女', '贾政为贾探春之父', NOW(), NOW()),
(6, 20, '父子', '贾政为贾环之父', NOW(), NOW()),
(6, 7, '夫妻', '贾政之正妻为王夫人', NOW(), NOW()),
(6, 21, '夫妾', '贾政之妾为赵姨娘', NOW(), NOW()),
(7, 1, '母子', '王夫人为贾宝玉之母', NOW(), NOW()),
(7, 8, '母女', '王夫人为贾元春之母', NOW(), NOW()),
(21, 10, '母女', '赵姨娘为贾探春之母', NOW(), NOW()),
(21, 20, '母子', '赵姨娘为贾环之母', NOW(), NOW());

-- 贾珠（已故）与李纨
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(14, 25, '母子', '李纨为贾兰之母', NOW(), NOW()),
(6, 25, '祖孙', '贾政为贾兰之祖父', NOW(), NOW());

-- 贾赦一支
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(17, 18, '夫妻', '贾赦之妻为邢夫人', NOW(), NOW()),
(17, 19, '父子', '贾赦为贾琏之父', NOW(), NOW()),
(17, 9, '父女', '贾赦为贾迎春之父', NOW(), NOW());

-- 贾琏、王熙凤、巧姐
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(19, 4, '夫妻', '贾琏之妻为王熙凤', NOW(), NOW()),
(4, 16, '母女', '王熙凤为巧姐之母', NOW(), NOW()),
(19, 16, '父女', '贾琏为巧姐之父', NOW(), NOW()),
(4, 41, '主仆', '王熙凤的贴身丫鬟为平儿', NOW(), NOW()),
(19, 41, '夫妾', '贾琏纳平儿为通房丫鬟', NOW(), NOW());

-- 贾敏与林黛玉
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(38, 37, '夫妻', '贾敏之夫为林如海', NOW(), NOW()),
(37, 2, '父女', '林如海为林黛玉之父', NOW(), NOW()),
(38, 2, '母女', '贾敏为林黛玉之母', NOW(), NOW());

-- ========== 宁国府关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(22, 23, '夫妻', '贾珍之妻为尤氏', NOW(), NOW()),
(22, 24, '父子', '贾珍为贾蓉之父', NOW(), NOW()),
(24, 15, '夫妻', '贾蓉之妻为秦可卿', NOW(), NOW()),
(22, 11, '兄妹', '贾珍为贾惜春之兄', NOW(), NOW()),
(22, 28, '养父子', '贾珍为贾蔷之养父', NOW(), NOW());

-- 尤氏家族
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(23, 29, '姐妹', '尤氏与尤二姐为名义上的姐妹', NOW(), NOW()),
(23, 30, '姐妹', '尤氏与尤三姐为名义上的姐妹', NOW(), NOW()),
(29, 30, '姐妹', '尤二姐与尤三姐为亲姐妹', NOW(), NOW());

-- ========== 王家关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(7, 31, '姐妹', '王夫人与薛姨妈为亲姐妹', NOW(), NOW()),
(7, 4, '姑侄', '王夫人为王熙凤之姑母', NOW(), NOW()),
(31, 4, '姨甥', '薛姨妈为王熙凤之姨母', NOW(), NOW());

-- ========== 薛家关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(31, 3, '母女', '薛姨妈为薛宝钗之母', NOW(), NOW()),
(31, 32, '母子', '薛姨妈为薛蟠之母', NOW(), NOW()),
(3, 32, '兄妹', '薛宝钗与薛蟠为兄妹', NOW(), NOW()),
(32, 34, '夫妻', '薛蟠之妻为夏金桂', NOW(), NOW()),
(32, 35, '夫妾', '薛蟠之妾为香菱', NOW(), NOW()),
(3, 33, '堂姐妹', '薛宝钗与薛宝琴为堂姐妹', NOW(), NOW());

-- ========== 宝黛钗核心情感关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(1, 2, '姑表兄妹', '贾宝玉与林黛玉为姑表兄妹，心意相通', NOW(), NOW()),
(1, 3, '姨表兄妹', '贾宝玉与薛宝钗为姨表兄妹', NOW(), NOW()),
(2, 3, '情敌/知己', '林黛玉与薛宝钗由情敌转为知己', NOW(), NOW()),
(1, 3, '夫妻', '贾宝玉与薛宝钗最终成婚', NOW(), NOW());

-- ========== 宝玉与丫鬟、友人 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(1, 39, '主仆', '贾宝玉贴身大丫鬟为袭人', NOW(), NOW()),
(1, 40, '主仆', '贾宝玉丫鬟晴雯', NOW(), NOW()),
(1, 47, '主仆', '贾宝玉丫鬟麝月', NOW(), NOW()),
(1, 48, '主仆', '贾宝玉贴身小厮茗烟', NOW(), NOW()),
(1, 50, '挚友', '贾宝玉与秦钟为至交好友', NOW(), NOW()),
(1, 51, '朋友', '贾宝玉与柳湘莲相交', NOW(), NOW()),
(1, 52, '朋友', '贾宝玉与蒋玉菡互赠汗巾结交', NOW(), NOW()),
(1, 20, '兄弟', '贾宝玉与贾环为同父异母兄弟', NOW(), NOW()),
(1, 19, '堂兄弟', '贾宝玉与贾琏为堂兄弟', NOW(), NOW()),
(1, 22, '族兄弟', '贾宝玉与贾珍为族兄弟', NOW(), NOW()),
(1, 27, '义父子', '贾宝玉认贾芸为义子', NOW(), NOW());

-- 宝玉与姐妹们
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(1, 8, '姐弟', '贾元春为贾宝玉之胞姐', NOW(), NOW()),
(1, 9, '堂兄妹', '贾迎春为贾宝玉之堂姐', NOW(), NOW()),
(1, 10, '兄妹', '贾探春为贾宝玉之同父异母妹', NOW(), NOW()),
(1, 11, '族兄妹', '贾惜春为贾宝玉之族妹', NOW(), NOW()),
(1, 12, '表兄妹', '贾宝玉与史湘云为表兄妹', NOW(), NOW()),
(1, 14, '叔嫂', '李纨为贾宝玉之寡嫂', NOW(), NOW());

-- ========== 林黛玉关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(2, 43, '主仆', '林黛玉贴身丫鬟紫鹃', NOW(), NOW()),
(2, 44, '主仆', '林黛玉丫鬟雪雁', NOW(), NOW()),
(2, 12, '朋友', '林黛玉与史湘云为诗友兼闺蜜', NOW(), NOW()),
(2, 13, '朋友', '林黛玉与妙玉为诗友', NOW(), NOW());

-- ========== 薛宝钗关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(3, 45, '主仆', '薛宝钗贴身丫鬟莺儿', NOW(), NOW()),
(3, 10, '朋友', '薛宝钗与贾探春志趣相投', NOW(), NOW()),
(3, 12, '朋友', '薛宝钗与史湘云关系亲密', NOW(), NOW()),
(3, 2, '朋友', '薛宝钗与林黛玉冰释前嫌后为知己', NOW(), NOW());

-- ========== 王熙凤关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(4, 36, '恩人', '刘姥姥搭救王熙凤之女巧姐', NOW(), NOW()),
(4, 29, '情敌/加害', '王熙凤设计逼死尤二姐', NOW(), NOW()),
(4, 26, '戏弄', '王熙凤毒设相思局害死贾瑞', NOW(), NOW());

-- ========== 史湘云关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(12, 5, '姑侄', '史湘云为贾母之侄孙女', NOW(), NOW());

-- ========== 丫鬟、仆人之间关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(39, 52, '夫妻', '袭人最终嫁蒋玉菡为妻', NOW(), NOW()),
(39, 40, '同僚', '袭人与晴雯同为宝玉大丫鬟', NOW(), NOW()),
(5, 42, '主仆', '鸳鸯为贾母首席大丫鬟', NOW(), NOW()),
(9, 46, '主仆', '司棋为贾迎春大丫鬟', NOW(), NOW()),
(10, 41, '主仆', '平儿协助贾探春理家', NOW(), NOW());

-- ========== 柳湘莲与尤三姐 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(51, 30, '未婚夫妻', '柳湘莲与尤三姐订婚后又退婚', NOW(), NOW());

-- ========== 秦可卿关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(15, 50, '姐弟', '秦可卿为秦钟之姐（名义上）', NOW(), NOW());

-- ========== 香菱（甄英莲）关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(53, 35, '父女', '甄士隐为香菱（甄英莲）之父', NOW(), NOW());

-- ========== 刘姥姥关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(36, 16, '恩人', '刘姥姥搭救巧姐脱离火坑', NOW(), NOW());

-- ========== 贾雨村关系 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(54, 2, '师生', '贾雨村曾为林黛玉的家庭教师', NOW(), NOW()),
(54, 53, '恩人', '甄士隐资助贾雨村赴京赶考', NOW(), NOW());

-- ========== 贾府兄弟/姐妹关系补充 ==========
INSERT INTO honglou_character_relations (from_character_id, to_character_id, relation_type, relation_desc, created_time, updated_time) VALUES
(8, 9, '堂姐妹', '贾元春与贾迎春为堂姐妹', NOW(), NOW()),
(8, 10, '姐妹', '贾元春与贾探春为同父异母姐妹', NOW(), NOW()),
(9, 10, '堂姐妹', '贾迎春与贾探春为堂姐妹', NOW(), NOW()),
(9, 11, '堂姐妹', '贾迎春与贾惜春为堂姐妹', NOW(), NOW()),
(10, 11, '族姐妹', '贾探春与贾惜春为族姐妹', NOW(), NOW());

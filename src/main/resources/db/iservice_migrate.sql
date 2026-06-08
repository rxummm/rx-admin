-- =============================================
-- i-Service 增量迁移脚本（仅运行一次）
-- 用法：mysql -u root -p your_db < iservice_migrate.sql
-- 
-- 注意：如果某些列已存在，个别 ALTER TABLE 可能报错，
-- 忽略已存在的列错误即可继续执行后续语句。
-- =============================================

-- =============================================
-- 1. 升级 i_service_category 表
-- =============================================
ALTER TABLE i_service_category
    ADD COLUMN parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0=顶层' AFTER id;

ALTER TABLE i_service_category
    ADD COLUMN doc_url VARCHAR(500) COMMENT 'IBM 官方文档链接' AFTER description;

-- 填充已有分类的 doc_url
UPDATE i_service_category SET doc_url = 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-application' WHERE code = 'application' AND (doc_url IS NULL OR doc_url = '');
UPDATE i_service_category SET doc_url = 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs' WHERE code = 'ifs' AND (doc_url IS NULL OR doc_url = '');


-- =============================================
-- 2. 升级 i_service_item 表
-- =============================================
-- 第一步：把旧 description 的数据保留为 brief_description
-- 如果表已有 brief_description 列，此句会报错，跳过即可
ALTER TABLE i_service_item
    ADD COLUMN brief_description VARCHAR(500) COMMENT '简短描述（一句话概述）' AFTER service_type;

-- 从旧 description 列迁移数据（如果 brief_description 为空）
UPDATE i_service_item SET brief_description = description WHERE brief_description IS NULL OR brief_description = '';

-- 第二步：新增系统对象名
ALTER TABLE i_service_item
    ADD COLUMN system_object_name VARCHAR(10) COMMENT 'IBM i 系统对象名' AFTER service_name;

-- 第三步：新增完整描述
ALTER TABLE i_service_item
    ADD COLUMN full_description TEXT COMMENT '完整描述（多段落详细说明）' AFTER brief_description;

-- 第四步：新增 doc_url
ALTER TABLE i_service_item
    ADD COLUMN doc_url VARCHAR(500) COMMENT 'IBM 官方文档详情页链接' AFTER full_description;

-- 第五步：新增发布信息字段
ALTER TABLE i_service_item
    ADD COLUMN earliest_possible_release VARCHAR(6) COMMENT '最早可用版本（VxRxMx 格式）' AFTER doc_url;

ALTER TABLE i_service_item
    ADD COLUMN initial_db2_group_level INT COMMENT '首次引入的 Db2 PTF Group Level' AFTER earliest_possible_release;

ALTER TABLE i_service_item
    ADD COLUMN latest_db2_group_level INT COMMENT '最近更新的 Db2 PTF Group Level' AFTER initial_db2_group_level;

-- 追加索引（先创建忽略重复错误，MySQL 不支持 IF NOT EXISTS）

-- 填充 SERVICES_INFO 的详细字段
UPDATE i_service_item SET
    system_object_name = 'SERV_INFO',
    doc_url = 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-info-table'
WHERE service_name = 'QSYS2.SERVICES_INFO';


-- =============================================
-- 3. 创建新表：参数定义表
-- =============================================
CREATE TABLE IF NOT EXISTS i_service_parameter (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    service_id      BIGINT NOT NULL COMMENT '关联服务ID',
    param_name      VARCHAR(200) NOT NULL COMMENT '参数名',
    param_type      VARCHAR(100) COMMENT '数据类型',
    param_direction VARCHAR(20) DEFAULT 'IN' COMMENT '参数方向: IN / OUT / INOUT',
    is_required     TINYINT DEFAULT 1 COMMENT '是否必填 1=必填 0=可选',
    default_value   VARCHAR(500) COMMENT '默认值',
    description     TEXT COMMENT '参数说明（含枚举值说明）',
    sort            INT DEFAULT 0 COMMENT '参数排序',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sp_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 参数定义表';


-- =============================================
-- 4. 创建新表：结果列定义表
-- =============================================
CREATE TABLE IF NOT EXISTS i_service_column (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    service_id          BIGINT NOT NULL COMMENT '关联服务ID',
    column_name         VARCHAR(200) NOT NULL COMMENT '列名（SQL 长名）',
    system_column_name  VARCHAR(20) COMMENT '系统短名',
    data_type           VARCHAR(100) NOT NULL COMMENT '数据类型',
    is_nullable         TINYINT DEFAULT 1 COMMENT '是否可空',
    description         TEXT COMMENT '列说明（含枚举值列表）',
    sort                INT DEFAULT 0 COMMENT '列排序',
    deleted             TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sc_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 结果列定义表';


-- =============================================
-- 5. 创建新表：示例代码表
-- =============================================
CREATE TABLE IF NOT EXISTS i_service_example (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    service_id      BIGINT NOT NULL COMMENT '关联服务ID',
    title           VARCHAR(300) COMMENT '示例标题',
    description     VARCHAR(500) COMMENT '示例场景描述',
    sql_code        TEXT NOT NULL COMMENT 'SQL 示例代码',
    sort            INT DEFAULT 0 COMMENT '排序',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_se_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 示例代码表';


-- =============================================
-- 6. 创建新表：权限要求表
-- =============================================
CREATE TABLE IF NOT EXISTS i_service_authority (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    service_id      BIGINT NOT NULL COMMENT '关联服务ID',
    authority       VARCHAR(500) NOT NULL COMMENT '权限描述',
    context         VARCHAR(200) COMMENT '适用场景/文件系统',
    sort            INT DEFAULT 0 COMMENT '排序',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sa_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 权限要求表';


-- =============================================
-- 7. SERVICES_INFO 详情数据（id=37，幂等：先删后插）
-- =============================================

-- 权限
DELETE FROM i_service_authority WHERE service_id = 37;
INSERT INTO i_service_authority (service_id, authority, sort) VALUES
(37, '*EXECUTE authority to QSYS2', 1),
(37, '*OBJOPR and *READ authority to the QSYS2/SERV_INFO file', 2);

-- 结果列定义
DELETE FROM i_service_column WHERE service_id = 37;
INSERT INTO i_service_column (service_id, column_name, system_column_name, data_type, is_nullable, description, sort) VALUES
(37, 'SERVICE_CATEGORY', 'CATEGORY', 'VARCHAR(40)', 0,
 '服务分类\n枚举值：APPLICATION, BACKUP AND RECOVERY, COMMUNICATION, CONFIGURATION, DATABASE-APPLICATION, DATABASE-PERFORMANCE, DATABASE-PLAN CACHE, DATABASE-UTILITY, IFS, JAVA, JOURNAL, LIBRARIAN, MESSAGE HANDLING, MIGRATE WHILE ACTIVE, MIRROR-COMMUNICATION, MIRROR-PRODUCT, MIRROR-RECLONE, MIRROR-REPLICATION, MIRROR-RESYNCHRONIZATION, MIRROR-SERVICEABILITY, PERFORMANCE, PRODUCT, PTF, SECURITY, SPOOL, STORAGE, SYSTEM HEALTH, WORK MANAGEMENT', 1),
(37, 'SERVICE_SCHEMA_NAME', 'SYS_NAME', 'VARCHAR(128)', 0, '包含该服务的 schema 名称', 2),
(37, 'SERVICE_NAME', 'SERVNAME', 'VARCHAR(128)', 0, '服务名称', 3),
(37, 'SQL_OBJECT_TYPE', 'SQLTYPE', 'VARCHAR(15)', 0,
 'SQL 对象类型\n枚举值：GLOBAL VARIABLE, PROCEDURE, SCALAR FUNCTION, TABLE, TABLE FUNCTION, VIEW', 4),
(37, 'OBJECT_TYPE', 'OBJTYPE', 'VARCHAR(7)', 1, '系统对象类型（*FILE 等）。过程、函数和全局变量为 null', 5),
(37, 'SYSTEM_OBJECT_NAME', 'SYS_ONAME', 'VARCHAR(10)', 1, '系统对象名。过程、函数和全局变量为 null', 6),
(37, 'LATEST_DB2_GROUP_LEVEL', 'GROUPLVL', 'INTEGER', 1,
 '最近更新该服务的 Db2 PTF Group Level。如果该服务在此版本中未被 PTF 增强则为 null', 7),
(37, 'INITIAL_DB2_GROUP_LEVEL', 'INITIALLVL', 'INTEGER', 1,
 '引入该服务的 Db2 PTF Group Level。如果该版本基础版本中已包含则为 null', 8),
(37, 'EARLIEST_POSSIBLE_RELEASE', 'MINRLS', 'VARCHAR(6)', 0, '该服务某个版本可用的最早版本（VxRxMx 格式）', 9),
(37, 'EXAMPLE', 'EXAMPLE', 'VARCHAR(5000)', 0, '使用该服务的示例 SQL 脚本', 10);

-- 示例代码
DELETE FROM i_service_example WHERE service_id = 37;
INSERT INTO i_service_example (service_id, title, description, sql_code, sort) VALUES
(37, '查询所有 Application Services', '返回所有分类为 APPLICATION 的系统服务',
 'SELECT SERVICE_NAME, SQL_OBJECT_TYPE, SYSTEM_OBJECT_NAME
  FROM QSYS2.SERVICES_INFO
  WHERE SERVICE_CATEGORY = ''APPLICATION''
  ORDER BY SERVICE_NAME;', 1),
(37, '按类型统计服务数量', '按 SQL 对象类型分组统计服务数量',
 'SELECT SQL_OBJECT_TYPE, COUNT(*) AS COUNT
  FROM QSYS2.SERVICES_INFO
  GROUP BY SQL_OBJECT_TYPE
  ORDER BY COUNT DESC;', 2);


-- =============================================
-- 8. IFS_OBJECT_STATISTICS 详情数据（id=63，幂等：先删后插）
-- =============================================

-- 完整描述（按 service_name 定位，兼容 id 可能不同）
UPDATE i_service_item SET full_description =
  '返回起始路径名包含或可访问的对象表。\n\n'
  '此信息类似于 RTVDIRINF（检索目录信息）命令或 Qp0lGetAttr() API 返回的信息。\n\n'
  '注意：\n'
  '• 不返回远程文件系统对象的行。对于 QNTC 文件系统，仅返回 /QNTC 一行。对于 NFS 和 QFileSvr.400 文件系统，不返回任何行。\n'
  '• 某些文件系统（包括 QDLS）不是线程安全的。访问这些文件系统中的信息可能不会返回某些行。此时将根据 IGNORE_ERRORS 参数设置返回错误或警告。'
WHERE service_name = 'QSYS2.IFS_OBJECT_STATISTICS()';

-- 权限
DELETE FROM i_service_authority WHERE service_id = 63;
INSERT INTO i_service_authority (service_id, authority, context, sort) VALUES
(63, '路径名中包含的每个目录需要 *X 权限', '非 QDLS / QSYS 文件系统', 1),
(63, '递归处理的每个目录需要 *RX 权限', '非 QDLS / QSYS 文件系统', 2),
(63, '路径名中除 QDLS 外的每个目录需要 *X 权限', 'QDLS 文件系统', 3),
(63, '返回或递归处理的每个对象需要 *RWX 和 *OBJEXIST *OBJALTER *OBJREF', 'QDLS 文件系统', 4),
(63, '路径中包含的库或对象需要 *USE 权限', 'QSYS 文件系统', 5),
(63, '递归处理的每个库或对象需要 *USE 权限', 'QSYS 文件系统', 6),
(63, '返回 OBJECT_AUDIT 和 OBJECT_AUDIT_CREATE 值需要 *ALLOBJ 或 *AUDIT 特殊权限', NULL, 7);

-- 参数定义
DELETE FROM i_service_parameter WHERE service_id = 63;
INSERT INTO i_service_parameter (service_id, param_name, param_type, param_direction, is_required, default_value, description, sort) VALUES
(63, 'START_PATH_NAME', 'VARCHAR(512)', 'IN', 1, NULL,
 '指示搜索起始位置的路径名。可以传入单个对象或目录的路径。', 1),
(63, 'SUBTREE_DIRECTORIES', 'VARCHAR(3)', 'IN', 0, '''YES''',
 '是否包含子目录中的对象。\n枚举值：YES - 返回起始路径名及所有子目录中的对象；NO - 仅返回起始路径名中的对象。', 2),
(63, 'OMIT_LIST', 'VARCHAR(2048)', 'IN', 0, NULL,
 '从结果中排除的属性列表（逗号分隔的属性名）。如果指定了任何属性，则忽略 INCLUDE_LIST。', 3),
(63, 'INCLUDE_LIST', 'VARCHAR(2048)', 'IN', 0, NULL,
 '要包含在结果中的属性列表（逗号分隔的属性名）。如果指定，结果中仅返回这些属性。如果未指定 OMIT_LIST 或 INCLUDE_LIST，则返回所有属性。', 4),
(63, 'IGNORE_ERRORS', 'VARCHAR(4)', 'IN', 0, '''NO''',
 '遇到权限错误或对象不再存在时是否忽略。\n枚举值：YES - 跳过错误并继续，同时返回 SQL warning；NO - 返回错误。', 5);

-- 结果列定义
DELETE FROM i_service_column WHERE service_id = 63;
INSERT INTO i_service_column (service_id, column_name, data_type, is_nullable, description, sort) VALUES
(63, 'PATH_NAME', 'VARCHAR(512)', 0, '对象在集成文件系统中的完整路径名', 1),
(63, 'OBJECT_TYPE', 'VARCHAR(10)', 0,
 '对象类型\n枚举值：*ALLDIR, *AUTL, *BLKSF, *DDIR, *DIR, *DSTMF, *EXIT, *FIFO, *FLR, *LIB, *MBR, *NO, *OBJ, *PDIR, *SOCKET, *STMF, *STS, *SYMLNK, *UNKNOWN', 2),
(63, 'DATA_SIZE', 'BIGINT', 0, '对象的数据大小（字节）', 3),
(63, 'ALLOCATED_SIZE', 'BIGINT', 0, '对象已分配的空间（字节）', 4),
(63, 'OBJECT_OWNER', 'VARCHAR(10)', 0, '对象的所有者', 5),
(63, 'OBJECT_GROUP', 'VARCHAR(10)', 0, '对象的组', 6),
(63, 'OWNER_AUTHORITY', 'VARCHAR(10)', 0,
 '所有者对对象的权限\n枚举值：*EXCLUDE, *ALL, *CHANGE, *USE, *RW, *RWX, *RX, *R, *WX, *W, *X', 7),
(63, 'GROUP_AUTHORITY', 'VARCHAR(10)', 0, '组对对象的权限。枚举值同上', 8),
(63, 'PUBLIC_AUTHORITY', 'VARCHAR(10)', 0, '公共权限。枚举值同上', 9),
(63, 'AUTHORIZATION_LIST_NAME', 'VARCHAR(10)', 1, '与对象关联的授权列表名称。没有授权列表时为 null', 10),
(63, 'CREATE_TIMESTAMP', 'TIMESTAMP', 0, '对象创建的时间戳', 11),
(63, 'LAST_ACCESS_TIMESTAMP', 'TIMESTAMP', 0, '对象上次访问的时间戳', 12),
(63, 'LAST_CHANGE_TIMESTAMP', 'TIMESTAMP', 0, '对象上次更改的时间戳', 13),
(63, 'LAST_ATTRIBUTE_CHANGE_TIMESTAMP', 'TIMESTAMP', 0, '对象属性上次更改的时间戳', 14),
(63, 'LAST_DATA_CHANGE_TIMESTAMP', 'TIMESTAMP', 0, '对象数据上次更改的时间戳', 15),
(63, 'OBJECT_AUDIT', 'VARCHAR(10)', 0,
 '对象审计值。需要 *ALLOBJ 或 *AUDIT 权限。\n枚举值：*NONE, *USRPRF, *SYS, *CHANGE, *ALL, *AUDIT', 16),
(63, 'OBJECT_AUDIT_CREATE', 'VARCHAR(10)', 0,
 '创建时的对象审计值。需要 *ALLOBJ 或 *AUDIT 权限。枚举值同上', 17),
(63, 'CHECKED_OUT', 'VARCHAR(3)', 0, '对象是否已签出。枚举值：NO - 未签出；YES - 已签出', 18),
(63, 'CHECKED_OUT_TIMESTAMP', 'TIMESTAMP(0)', 1, '对象签出的时间。未签出时为 null', 19),
(63, 'CHECKED_OUT_USER', 'VARCHAR(10)', 1, '签出对象的用户。未签出时为 null', 20);

-- 示例代码
DELETE FROM i_service_example WHERE service_id = 63;
INSERT INTO i_service_example (service_id, title, description, sql_code, sort) VALUES
(63, '列出 /usr 目录中的文件', '列出 /usr 目录中（不含子目录）所有对象的基本信息',
'SELECT PATH_NAME, OBJECT_TYPE, DATA_SIZE, OBJECT_OWNER
  FROM TABLE (QSYS2.IFS_OBJECT_STATISTICS(
    START_PATH_NAME => ''/usr'',
    SUBTREE_DIRECTORIES => ''NO''));', 1),
(63, '列出 /usr 及其子目录中的所有文件', '递归列出 /usr 及其所有子目录中的对象基本信息',
'SELECT PATH_NAME, OBJECT_TYPE, DATA_SIZE, OBJECT_OWNER
  FROM TABLE (QSYS2.IFS_OBJECT_STATISTICS(
    START_PATH_NAME => ''/usr'',
    SUBTREE_DIRECTORIES => ''NO''));', 2);


-- =============================================
-- 9. 补充子表的 update_time 列（修复 Entity 字段与数据库列不匹配导致的 500 错误）
--    如果列已存在会报错，忽略即可继续执行
-- =============================================
ALTER TABLE i_service_parameter
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

ALTER TABLE i_service_column
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

ALTER TABLE i_service_example
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

ALTER TABLE i_service_authority
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

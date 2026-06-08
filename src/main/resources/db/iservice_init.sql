-- =============================================
-- i-Service 数据表 + 初始化数据
-- IBM i Services (Application Services & IFS Services)
-- 支持：分类 → 服务 → 参数表 → 列定义表 → 示例代码 → 权限说明
-- =============================================

-- =============================================
-- 1. 服务分类表
-- =============================================
DROP TABLE IF EXISTS i_service_category;
CREATE TABLE i_service_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    parent_id   BIGINT DEFAULT 0 COMMENT '父分类ID，0=顶层',
    name        VARCHAR(200) NOT NULL COMMENT '分类显示名',
    code        VARCHAR(100) NOT NULL COMMENT '分类编码（唯一）',
    description TEXT COMMENT '分类描述',
    doc_url     VARCHAR(500) COMMENT 'IBM 官方文档链接',
    sort        INT DEFAULT 0 COMMENT '排序',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 分类表';

-- =============================================
-- 2. 服务主表（每个 Service 一条记录）
-- =============================================
DROP TABLE IF EXISTS i_service_item;
CREATE TABLE i_service_item (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    category_id                 BIGINT NOT NULL COMMENT '分类ID',
    -- 服务标识
    service_name                VARCHAR(200) NOT NULL COMMENT '服务名称，如 QSYS2.SERVICES_INFO',
    system_object_name          VARCHAR(10) COMMENT 'IBM i 系统对象名，如 SERV_INFO',
    service_type                VARCHAR(50) NOT NULL COMMENT '服务类型: TABLE / VIEW / TABLE FUNCTION / PROCEDURE / SCALAR FUNCTION / GLOBAL VARIABLE',
    -- 描述
    brief_description           VARCHAR(500) NOT NULL COMMENT '简短描述（一句话概述）',
    full_description            TEXT COMMENT '完整描述（多段落详细说明，含 HTML）',
    -- IBM 官方链接
    doc_url                     VARCHAR(500) COMMENT 'IBM 官方文档详情页链接',
    -- 发布信息
    earliest_possible_release   VARCHAR(6) COMMENT '最早可用版本（VxRxMx 格式）',
    initial_db2_group_level     INT COMMENT '首次引入的 Db2 PTF Group Level',
    latest_db2_group_level      INT COMMENT '最近更新的 Db2 PTF Group Level',
    -- 排序 & 通用
    sort                        INT DEFAULT 0 COMMENT '排序',
    deleted                     TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time                 DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time                 DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_service_name (service_name),
    INDEX idx_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 服务主表';

-- =============================================
-- 3. 参数定义表（Table Function / Procedure）
-- =============================================
DROP TABLE IF EXISTS i_service_parameter;
CREATE TABLE i_service_parameter (
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
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 参数定义表';

-- =============================================
-- 4. 结果列定义表（Table / View / Table Function 的返回列）
-- =============================================
DROP TABLE IF EXISTS i_service_column;
CREATE TABLE i_service_column (
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
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 结果列定义表';

-- =============================================
-- 5. 示例代码表
-- =============================================
DROP TABLE IF EXISTS i_service_example;
CREATE TABLE i_service_example (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    service_id      BIGINT NOT NULL COMMENT '关联服务ID',
    title           VARCHAR(300) COMMENT '示例标题',
    description     VARCHAR(500) COMMENT '示例场景描述',
    sql_code        TEXT NOT NULL COMMENT 'SQL 示例代码',
    sort            INT DEFAULT 0 COMMENT '排序',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 示例代码表';

-- =============================================
-- 6. 权限要求表
-- =============================================
DROP TABLE IF EXISTS i_service_authority;
CREATE TABLE i_service_authority (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    service_id      BIGINT NOT NULL COMMENT '关联服务ID',
    authority       VARCHAR(500) NOT NULL COMMENT '权限描述',
    context         VARCHAR(200) COMMENT '适用场景/文件系统',
    sort            INT DEFAULT 0 COMMENT '排序',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='i-Service 权限要求表';


-- ============================================================
--                           初始化数据
-- ============================================================

-- =============================================
-- 分类数据
-- =============================================
INSERT INTO i_service_category (id, parent_id, name, code, description, doc_url, sort) VALUES
(1, 0, 'Application Services', 'application',
 '提供可被应用程序使用的接口信息（过程、函数、视图）',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-application', 1),
(2, 0, 'IFS Services', 'ifs',
 '提供集成文件系统(Integrated File System)相关信息',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs', 2);


-- =============================================
-- Application Services 服务主表
-- =============================================
INSERT INTO i_service_item (id, category_id, service_name, system_object_name, service_type, brief_description, doc_url, sort) VALUES
(1, 1, 'QSYS2.ACTIVATION_GROUP_INFO()', NULL, 'TABLE FUNCTION', '返回作业中激活组的信息', NULL, 1),
(2, 1, 'QSYS2.ADD_USER_INDEX_ENTRY()', NULL, 'PROCEDURE', '向用户索引中添加条目', NULL, 2),
(3, 1, 'QSYS2.ADD_USER_INDEX_ENTRY_BINARY()', NULL, 'PROCEDURE', '向用户索引中添加二进制条目', NULL, 3),
(4, 1, 'QSYS2.BINDING_DIRECTORY_INFO', NULL, 'VIEW', '返回绑定目录的信息', NULL, 4),
(5, 1, 'QSYS2.BOUND_MODULE_INFO', NULL, 'VIEW', '返回绑定模块的信息', NULL, 5),
(6, 1, 'QSYS2.BOUND_SRVPGM_INFO', NULL, 'VIEW', '返回绑定服务程序的信息', NULL, 6),
(7, 1, 'QSYS2.CHANGE_USER_SPACE()', NULL, 'PROCEDURE', '修改用户空间的内容', NULL, 7),
(8, 1, 'QSYS2.CHANGE_USER_SPACE_BINARY()', NULL, 'PROCEDURE', '以二进制方式修改用户空间的内容', NULL, 8),
(9, 1, 'QSYS2.CHANGE_USER_SPACE_ATTRIBUTES()', NULL, 'PROCEDURE', '修改用户空间的属性', NULL, 9),
(10, 1, 'QSYS2.CLEAR_DATA_QUEUE()', NULL, 'PROCEDURE', '清除数据队列中的所有消息', NULL, 10),
(11, 1, 'QSYS2.COMMAND_INFO', NULL, 'VIEW', '返回 CL 命令的相关信息', NULL, 11),
(12, 1, 'QSYS2.CREATE_USER_INDEX()', NULL, 'PROCEDURE', '创建用户索引', NULL, 12),
(13, 1, 'QSYS2.CREATE_USER_SPACE()', NULL, 'PROCEDURE', '创建用户空间', NULL, 13),
(14, 1, 'QSYS2.DATA_AREA_INFO()', NULL, 'TABLE FUNCTION', '返回数据区的信息', NULL, 14),
(15, 1, 'QSYS2.DATA_AREA_INFO', NULL, 'VIEW', '返回数据区信息的视图', NULL, 15),
(16, 1, 'QSYS2.DATA_QUEUE_ENTRIES()', NULL, 'TABLE FUNCTION', '返回数据队列中的条目', NULL, 16),
(17, 1, 'QSYS2.DATA_QUEUE_INFO', NULL, 'VIEW', '返回数据队列的信息', NULL, 17),
(18, 1, 'QSYS2.DB_TRANSACTION_INFO', NULL, 'VIEW', '返回数据库事务的信息', NULL, 18),
(19, 1, 'QSYS2.DB_TRANSACTION_JOURNAL_INFO', NULL, 'TABLE FUNCTION', '返回数据库事务日志信息', NULL, 19),
(20, 1, 'QSYS2.DB_TRANSACTION_OBJECT_INFO', NULL, 'TABLE FUNCTION', '返回数据库事务涉及的对象信息', NULL, 20),
(21, 1, 'QSYS2.DB_TRANSACTION_RECORD_INFO', NULL, 'TABLE FUNCTION', '返回数据库事务记录信息', NULL, 21),
(22, 1, 'QSYS2.ENVIRONMENT_VARIABLE_INFO', NULL, 'VIEW', '返回环境变量的信息', NULL, 22),
(23, 1, 'QSYS2.EXIT_POINT_INFO', NULL, 'VIEW', '返回系统出口点的信息', NULL, 23),
(24, 1, 'QSYS2.EXIT_PROGRAM_INFO', NULL, 'VIEW', '返回注册到出口点的程序信息', NULL, 24),
(25, 1, 'QSYS2.PROGRAM_EXPORT_IMPORT_INFO', NULL, 'VIEW', '返回程序的导出/导入符号信息', NULL, 25),
(26, 1, 'QSYS2.PROGRAM_RESOLVED_ACTIVATIONS()', NULL, 'TABLE FUNCTION', '返回程序解析到的激活信息', NULL, 26),
(27, 1, 'QSYS2.PROGRAM_RESOLVED_IMPORTS', NULL, 'TABLE FUNCTION', '返回程序解析到的导入信息', NULL, 27),
(28, 1, 'QSYS2.PROGRAM_INFO', NULL, 'VIEW', '返回程序对象的信息', NULL, 28),
(29, 1, 'QSYS2.QCMDEXC()', NULL, 'PROCEDURE', '执行 CL 命令（存储过程形式）', NULL, 29),
(30, 1, 'QSYS2.QCMDEXC()', NULL, 'SCALAR FUNCTION', '执行 CL 命令（标量函数形式）', NULL, 30),
(31, 1, 'QSYS2.RECEIVE_DATA_QUEUE()', NULL, 'TABLE FUNCTION', '从数据队列中接收消息', NULL, 31),
(32, 1, 'QSYS2.REMOVE_USER_INDEX_ENTRY()', NULL, 'TABLE FUNCTION', '从用户索引中移除条目', NULL, 32),
(33, 1, 'QSYS2.REMOVE_USER_INDEX_ENTRY_BINARY()', NULL, 'TABLE FUNCTION', '从用户索引中移除二进制条目', NULL, 33),
(34, 1, 'QSYS2.SEND_DATA_QUEUE()', NULL, 'PROCEDURE', '向数据队列发送消息', NULL, 34),
(35, 1, 'QSYS2.SEND_DATA_QUEUE_BINARY()', NULL, 'PROCEDURE', '向数据队列发送二进制消息', NULL, 35),
(36, 1, 'QSYS2.SEND_DATA_QUEUE_UTF8()', NULL, 'PROCEDURE', '向数据队列发送 UTF-8 消息', NULL, 36),
(37, 1, 'QSYS2.SERVICES_INFO', 'SERV_INFO', 'TABLE',
 '返回所有 IBM i Services 的元数据信息',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-info-table', 37),
(38, 1, 'QSYS2.SET_PASE_SHELL_INFO()', NULL, 'PROCEDURE', '设置 PASE shell 的环境信息', NULL, 38),
(39, 1, 'QSYS2.STACK_INFO()', NULL, 'TABLE FUNCTION', '返回程序的调用堆栈信息', NULL, 39),
(40, 1, 'QSYS2.USER_INDEX_ENTRIES()', NULL, 'TABLE FUNCTION', '返回用户索引中的条目', NULL, 40),
(41, 1, 'QSYS2.USER_INDEX_INFO', NULL, 'VIEW', '返回用户索引的信息', NULL, 41),
(42, 1, 'QSYS2.USER_SPACE()', NULL, 'TABLE FUNCTION', '返回用户空间的内容', NULL, 42),
(43, 1, 'QSYS2.USER_SPACE_INFO', NULL, 'VIEW', '返回用户空间的信息', NULL, 43),
(44, 1, 'QSYS2.VERIFY_NAME()', NULL, 'SCALAR FUNCTION', '验证 IBM i 对象名称的有效性', NULL, 44),
(45, 1, 'QSYS2.WATCH_DETAIL()', NULL, 'TABLE FUNCTION', '返回监视(Watch)的详细信息', NULL, 45),
(46, 1, 'QSYS2.WATCH_INFO', NULL, 'VIEW', '返回监视(Watch)的配置信息', NULL, 46),
(47, 1, 'SYSTOOLS.ERRNO_INFO()', NULL, 'SCALAR FUNCTION', '返回 errno 值对应的错误信息', NULL, 47),
(48, 1, 'SYSTOOLS.EVEN()', NULL, 'SCALAR FUNCTION', '判断数字是否为偶数', NULL, 48),
(49, 1, 'SYSTOOLS.GENERATE_SPREADSHEET()', NULL, 'SCALAR FUNCTION', '从查询结果生成电子表格文件', NULL, 49),
(50, 1, 'SYSTOOLS.GETENV()', NULL, 'SCALAR FUNCTION', '获取 PASE 环境变量的值', NULL, 50),
(51, 1, 'SYSTOOLS.LPRINTF()', NULL, 'PROCEDURE', '在作业日志中打印格式化消息', NULL, 51),
(52, 1, 'SYSTOOLS.ODD()', NULL, 'SCALAR FUNCTION', '判断数字是否为奇数', NULL, 52),
(53, 1, 'SYSTOOLS.OVERRIDE_INFO', NULL, 'VIEW', '返回文件覆盖(Override)的信息', NULL, 53),
(54, 1, 'SYSTOOLS.PUTENV()', NULL, 'SCALAR FUNCTION', '设置 PASE 环境变量的值', NULL, 54),
(55, 1, 'SYSTOOLS.SEND_EMAIL()', NULL, 'SCALAR FUNCTION', '通过 SMTP 发送电子邮件', NULL, 55),
(56, 1, 'SYSTOOLS.SPLIT()', NULL, 'TABLE FUNCTION', '按分隔符拆分字符串', NULL, 56);

-- =============================================
-- IFS Services 服务主表
-- =============================================
INSERT INTO i_service_item (id, category_id, service_name, system_object_name, service_type, brief_description, doc_url, sort) VALUES
(57, 2, 'QSYS2.IFS_ACCESS()', NULL, 'SCALAR FUNCTION',
 '确定文件是否可通过特定方式访问（类似 access()）',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-access-scalar-function', 1),
(58, 2, 'QSYS2.COMPARE_IFS()', NULL, 'TABLE FUNCTION',
 '比较两个 IFS 对象（文件或目录）的差异',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-compare-ifs-table-function', 2),
(59, 2, 'QSYS2.IFS_JOB_INFO()', NULL, 'TABLE FUNCTION',
 '返回包含作业对集成文件系统引用信息的表',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-job-info-table-function', 3),
(60, 2, 'QSYS2.IFS_OBJECT_LOCK_INFO()', NULL, 'TABLE FUNCTION',
 '返回已知持有对象引用或锁的每个作业的行',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-object-lock-info-table-function', 4),
(61, 2, 'QSYS2.IFS_OBJECT_PRIVILEGES()', NULL, 'TABLE FUNCTION',
 '返回路径名标识的对象的每个授权用户及其对象和数据权限',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-object-privileges-table-function', 5),
(62, 2, 'QSYS2.IFS_OBJECT_REFERENCES_INFO()', NULL, 'TABLE FUNCTION',
 '返回包含对象上集成文件系统引用信息的单行结果表',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-object-references-info-table-function', 6),
(63, 2, 'QSYS2.IFS_OBJECT_STATISTICS()', NULL, 'TABLE FUNCTION',
 '返回起始路径名包含或可访问的对象表（类似 RTVDIRINF 命令或 Qp0lGetAttr() API）',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-object-statistics-table-function', 7),
(64, 2, 'SYSTOOLS.IFS_PATH()', NULL, 'SCALAR FUNCTION',
 '返回输入路径字符串的指定部分',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-path-scalar-function', 8),
(65, 2, 'QSYS2.IFS_READ()', NULL, 'TABLE FUNCTION',
 '以字符方式读取 IFS 流文件内容',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-read-binary-read-utf8-table-functions', 9),
(66, 2, 'QSYS2.IFS_READ_BINARY()', NULL, 'TABLE FUNCTION',
 '以二进制方式读取 IFS 流文件内容',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-read-binary-read-utf8-table-functions', 10),
(67, 2, 'QSYS2.IFS_READ_UTF8()', NULL, 'TABLE FUNCTION',
 '以 UTF-8 方式读取 IFS 流文件内容',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-read-binary-read-utf8-table-functions', 11),
(68, 2, 'SYSTOOLS.IFS_RENAME()', NULL, 'SCALAR FUNCTION',
 '重命名 IFS 文件或目录',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-rename-scalar-function', 12),
(69, 2, 'SYSTOOLS.IFS_UNLINK()', NULL, 'SCALAR FUNCTION',
 '删除 IFS 流文件',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-unlink-scalar-function', 13),
(70, 2, 'QSYS2.IFS_WRITE()', NULL, 'PROCEDURE',
 '以字符方式写入 IFS 流文件',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-write-binary-write-utf8-procedures', 14),
(71, 2, 'QSYS2.IFS_WRITE_BINARY()', NULL, 'PROCEDURE',
 '以二进制方式写入 IFS 流文件',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-write-binary-write-utf8-procedures', 15),
(72, 2, 'QSYS2.IFS_WRITE_UTF8()', NULL, 'PROCEDURE',
 '以 UTF-8 方式写入 IFS 流文件',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-ifs-write-binary-write-utf8-procedures', 16),
(73, 2, 'QSYS2.SERVER_SHARE_INFO', NULL, 'VIEW',
 '返回 IBM i NetServer 共享信息',
 'https://www.ibm.com/docs/en/i/7.5.0?topic=services-server-share-info-view', 17);


-- =============================================
-- SERVICES_INFO 完整数据（id=37）
-- =============================================

-- 权限
INSERT INTO i_service_authority (service_id, authority, sort) VALUES
(37, '*EXECUTE authority to QSYS2', 1),
(37, '*OBJOPR and *READ authority to the QSYS2/SERV_INFO file', 2);

-- 结果列定义
INSERT INTO i_service_column (service_id, column_name, system_column_name, data_type, is_nullable, description, sort) VALUES
(37, 'SERVICE_CATEGORY', 'CATEGORY', 'VARCHAR(40)', 0,
 '服务分类\n枚举值：APPLICATION, BACKUP AND RECOVERY, COMMUNICATION, CONFIGURATION, DATABASE-APPLICATION, DATABASE-PERFORMANCE, DATABASE-PLAN CACHE, DATABASE-UTILITY, IFS, JAVA, JOURNAL, LIBRARIAN, MESSAGE HANDLING, MIGRATE WHILE ACTIVE, MIRROR-COMMUNICATION, MIRROR-PRODUCT, MIRROR-RECLONE, MIRROR-REPLICATION, MIRROR-RESYNCHRONIZATION, MIRROR-SERVICEABILITY, PERFORMANCE, PRODUCT, PTF, SECURITY, SPOOL, STORAGE, SYSTEM HEALTH, WORK MANAGEMENT', 1),
(37, 'SERVICE_SCHEMA_NAME', 'SYS_NAME', 'VARCHAR(128)', 0,
 '包含该服务的 schema 名称', 2),
(37, 'SERVICE_NAME', 'SERVNAME', 'VARCHAR(128)', 0,
 '服务名称', 3),
(37, 'SQL_OBJECT_TYPE', 'SQLTYPE', 'VARCHAR(15)', 0,
 'SQL 对象类型\n枚举值：GLOBAL VARIABLE, PROCEDURE, SCALAR FUNCTION, TABLE, TABLE FUNCTION, VIEW', 4),
(37, 'OBJECT_TYPE', 'OBJTYPE', 'VARCHAR(7)', 1,
 '系统对象类型（*FILE 等）。过程、函数和全局变量为 null', 5),
(37, 'SYSTEM_OBJECT_NAME', 'SYS_ONAME', 'VARCHAR(10)', 1,
 '系统对象名。过程、函数和全局变量为 null', 6),
(37, 'LATEST_DB2_GROUP_LEVEL', 'GROUPLVL', 'INTEGER', 1,
 '最近更新该服务的 Db2 PTF Group Level。如果该服务在此版本中未被 PTF 增强则为 null', 7),
(37, 'INITIAL_DB2_GROUP_LEVEL', 'INITIALLVL', 'INTEGER', 1,
 '引入该服务的 Db2 PTF Group Level。如果该版本基础版本中已包含则为 null', 8),
(37, 'EARLIEST_POSSIBLE_RELEASE', 'MINRLS', 'VARCHAR(6)', 0,
 '该服务某个版本可用的最早版本（VxRxMx 格式）', 9),
(37, 'EXAMPLE', 'EXAMPLE', 'VARCHAR(5000)', 0,
 '使用该服务的示例 SQL 脚本', 10);

-- 示例代码
INSERT INTO i_service_example (service_id, title, description, sql_code, sort) VALUES
(37, '查询所有 Application Services',
 '返回所有分类为 APPLICATION 的系统服务',
 'SELECT SERVICE_NAME, SQL_OBJECT_TYPE, SYSTEM_OBJECT_NAME
  FROM QSYS2.SERVICES_INFO
  WHERE SERVICE_CATEGORY = ''APPLICATION''
  ORDER BY SERVICE_NAME;', 1),
(37, '按类型统计服务数量',
 '按 SQL 对象类型分组统计服务数量',
 'SELECT SQL_OBJECT_TYPE, COUNT(*) AS COUNT
  FROM QSYS2.SERVICES_INFO
  GROUP BY SQL_OBJECT_TYPE
  ORDER BY COUNT DESC;', 2);


-- =============================================
-- IFS_OBJECT_STATISTICS 完整数据（id=63）
-- =============================================

-- 完整描述
UPDATE i_service_item SET full_description =
  '返回起始路径名包含或可访问的对象表。\n\n'
 || '此信息类似于 RTVDIRINF（检索目录信息）命令或 Qp0lGetAttr() API 返回的信息。\n\n'
 || '注意：\n'
 || '• 不返回远程文件系统对象的行。对于 QNTC 文件系统，仅返回 /QNTC 一行。对于 NFS 和 QFileSvr.400 文件系统，不返回任何行。\n'
 || '• 某些文件系统（包括 QDLS）不是线程安全的。访问这些文件系统中的信息可能不会返回某些行。此时将根据 IGNORE_ERRORS 参数设置返回错误或警告。'
 WHERE id = 63;

-- 权限
INSERT INTO i_service_authority (service_id, authority, context, sort) VALUES
(63, '路径名中包含的每个目录需要 *X 权限', '非 QDLS / QSYS 文件系统', 1),
(63, '递归处理的每个目录需要 *RX 权限', '非 QDLS / QSYS 文件系统', 2),
(63, '路径名中除 QDLS 外的每个目录需要 *X 权限', 'QDLS 文件系统', 3),
(63, '返回或递归处理的每个对象需要 *RWX 和 *OBJEXIST *OBJALTER *OBJREF', 'QDLS 文件系统', 4),
(63, '路径中包含的库或对象需要 *USE 权限', 'QSYS 文件系统', 5),
(63, '递归处理的每个库或对象需要 *USE 权限', 'QSYS 文件系统', 6),
(63, '返回 OBJECT_AUDIT 和 OBJECT_AUDIT_CREATE 值需要 *ALLOBJ 或 *AUDIT 特殊权限', NULL, 7);

-- 参数定义
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
(63, 'GROUP_AUTHORITY', 'VARCHAR(10)', 0,
 '组对对象的权限。枚举值同上', 8),
(63, 'PUBLIC_AUTHORITY', 'VARCHAR(10)', 0,
 '公共权限。枚举值同上', 9),
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
(63, 'CHECKED_OUT', 'VARCHAR(3)', 0,
 '对象是否已签出。枚举值：NO - 未签出；YES - 已签出', 18),
(63, 'CHECKED_OUT_TIMESTAMP', 'TIMESTAMP(0)', 1, '对象签出的时间。未签出时为 null', 19),
(63, 'CHECKED_OUT_USER', 'VARCHAR(10)', 1, '签出对象的用户。未签出时为 null', 20);

-- 示例代码
INSERT INTO i_service_example (service_id, title, description, sql_code, sort) VALUES
(63, '列出 /usr 目录中的文件',
 '列出 /usr 目录中（不含子目录）所有对象的基本信息',
 'SELECT PATH_NAME, OBJECT_TYPE, DATA_SIZE, OBJECT_OWNER
  FROM TABLE (QSYS2.IFS_OBJECT_STATISTICS(
    START_PATH_NAME => ''/usr'',
    SUBTREE_DIRECTORIES => ''NO''));', 1),
(63, '列出 /usr 及其子目录中的所有文件',
 '递归列出 /usr 及其所有子目录中的对象基本信息',
 'SELECT PATH_NAME, OBJECT_TYPE, DATA_SIZE, OBJECT_OWNER
  FROM TABLE (QSYS2.IFS_OBJECT_STATISTICS(
    START_PATH_NAME => ''/usr'',
    SUBTREE_DIRECTORIES => ''YES''));', 2);


-- =============================================
-- 菜单数据
-- =============================================
-- AS400管理 parent_id=107，新增 i-Service 目录和子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
(367, 107, 'i-Service', 1, '/as400/iservice', '', '', 'Cpu', 2),
(368, 367, '服务列表', 2, '/as400/iservice/list', 'as400/iservice/index', 'iservice:list', 'List', 1);

-- 超级管理员角色分配新菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 367), (1, 368);
